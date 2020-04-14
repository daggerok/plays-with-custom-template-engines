package daggerok;

import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.parboiled.Parboiled;
import org.pegdown.Extensions;
import org.pegdown.Parser;
import org.pegdown.PegDownProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;

@FunctionalInterface
interface MarkdownToHtmlConverter {
  String convert(String markdown);
}

@FunctionalInterface
interface MarkdownRenderer {
  String render(String markdownLocation);
}

@Configuration
class MarkdownConfig {

  @Bean
  Parser parser() {
    return Parboiled.createParser(Parser.class, Extensions.NONE);
  }

  @Bean
  PegDownProcessor pegDownProcessor(Parser parser) {
    return new PegDownProcessor(parser);
  }

  @Bean
  MarkdownToHtmlConverter markdownToHtml(PegDownProcessor pegDownProcessor) {
    return pegDownProcessor::markdownToHtml;
  }

  @Bean
  MarkdownRenderer markdownRenderer(ResourceLoader resourceLoader, MarkdownToHtmlConverter markdownToHtmlConverter) {
    Function<Resource, Try<String>> resourceReader = resource -> Try.of(() -> {
      try (var is = resource.getInputStream();
           var isr = new InputStreamReader(is);
           var br = new BufferedReader(isr)) {
        return br.lines().collect(Collectors.joining());
      }
    });
    Function<Throwable, String> errorMapper = err -> String.format("<html><body><h1>Rendering error</h1><p>%s</p></body></html>",
                                                                   err.getLocalizedMessage());
    return path -> Try.of(() -> resourceLoader.getResource("classpath:content/" + path))
                      .flatMap(resourceReader)
                      .map(markdownToHtmlConverter::convert)
                      .getOrElseGet(errorMapper);
  }
}

@SpringBootApplication
@RequiredArgsConstructor
public class PegDownApp {

  private final MarkdownRenderer markdownRenderer;

  @Bean
  RouterFunction<ServerResponse> routes() {
    return RouterFunctions.route()
                          .nest(path("/api**"), builder -> builder.GET("/**", this::api)
                                                                  .build())
                          .build()
                          .andRoute(path("/**"), this::index);
  }

  @NotNull
  private Mono<ServerResponse> api(ServerRequest serverRequest) {
    var uri = serverRequest.uri();
    var baseUrl = String.format("%s://%s", uri.getScheme(), uri.getAuthority());
    var response = Map.of("_self", uri,
                          "index GET", baseUrl + "/");
    return ServerResponse.ok()
                         .body(Mono.just(response), Map.class);
  }

  @NotNull
  private Mono<ServerResponse> index(ServerRequest serverRequest) {
    return ServerResponse.ok()
                         .contentType(MediaType.TEXT_HTML)
                         .bodyValue(markdownRenderer.render("index.md"));
  }

  public static void main(String[] args) {
    SpringApplication.run(PegDownApp.class, args);
  }
}
