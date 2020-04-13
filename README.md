# Custom Template Engines [![CI](https://github.com/daggerok/plays-with-custom-template-engines/workflows/CI/badge.svg)](https://github.com/daggerok/plays-with-custom-template-engines/actions?query=workflow%3ACI)
Plays with templates

## prerequisites

```bash
jdk11
jenv local 11.0
./mvnw -f basic-plain-html
bash
```

## basic-plain-html

```bash
./mvnw clean ; ./mvnw -f basic-plain-html ; bash ./basic-plain-html/target/*jar &
http :8080/api
http :8080
```

## markdown-commonmark

```bash
./mvnw clean ; ./mvnw -f markdown-commonmark ; bash ./markdown-commonmark/target/*jar &
http :8080/api
http :8080
```


## resources
* https://github.com/atlassian/commonmark-java
