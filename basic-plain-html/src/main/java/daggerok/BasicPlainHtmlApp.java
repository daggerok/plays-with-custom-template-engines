package daggerok;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;

@SpringBootApplication
public class BasicPlainHtmlApp {

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
                         .bodyValue("<html><body>Hi</body></html>");
  }

  public static void main(String[] args) {
    SpringApplication.run(BasicPlainHtmlApp.class, args);
  }
}
