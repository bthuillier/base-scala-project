//> using scala 3.7.1
//> using options --deprecation --explain -Wunused:all -source 3.0

// otel configuration
//> using javaOptions -Dotel.java.global-autoconfigure.enabled=true
//> using javaOptions -Dotel.service.name=base-app
//> using javaOptions -Dotel.service.version=1.0.0
//> using javaOptions -Dotel.exporter.otlp.endpoint=http://localhost:4317
//> using javaOptions -Dotel.logs.exporter=otlp
//> using javaOptions -Dotel.traces.exporter=otlp
//> using javaOptions -Dotel.metrics.exporter=otlp
//> using javaOptions -Dcats.effect.trackFiberContext=true

//> using resourceDirs ./resources
//> using mainClass base.App

// cats
//> using dependency org.typelevel::cats-core:2.13.0
//> using dependency org.typelevel::cats-effect:3.6.1

// pureconfig
//> using dependency com.github.pureconfig::pureconfig-cats-effect:0.17.9
//> using dependency com.github.pureconfig::pureconfig-core:0.17.9

// doobie
//> using dependency org.tpolecat::doobie-core:1.0.0-RC8
//> using dependency org.tpolecat::doobie-hikari:1.0.0-RC8
//> using dependency org.tpolecat::doobie-postgres:1.0.0-RC8

// logging
//> using dependency org.typelevel::log4cats-core:2.7.1
//> using dependency org.typelevel::log4cats-slf4j:2.7.1
//> using dependency ch.qos.logback:logback-classic:1.5.18

// json
//> using dependency io.circe::circe-core:0.14.14

// tapir
//> using dependency com.softwaremill.sttp.apispec::openapi-circe-yaml:0.11.10
//> using dependency com.softwaremill.sttp.tapir::tapir-cats:1.11.35
//> using dependency com.softwaremill.sttp.tapir::tapir-otel4s-tracing:1.11.35
//> using dependency com.softwaremill.sttp.tapir::tapir-opentelemetry-metrics:1.11.35
//> using dependency com.softwaremill.sttp.tapir::tapir-core:1.11.35
//> using dependency com.softwaremill.sttp.tapir::tapir-files:1.11.35
//> using dependency com.softwaremill.sttp.tapir::tapir-json-circe:1.11.35
//> using dependency com.softwaremill.sttp.tapir::tapir-netty-server-cats:1.11.35
//> using dependency com.softwaremill.sttp.tapir::tapir-openapi-docs:1.11.35
//> using dependency com.softwaremill.sttp.tapir::tapir-redoc-bundle:1.11.35

// opentelemetry
//> using dependency org.typelevel::otel4s-oteljava:0.12.0
//> using dependency io.opentelemetry:opentelemetry-exporter-otlp:1.51.0
//> using dependency io.opentelemetry:opentelemetry-sdk:1.51.0
//> using dependency io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:1.51.0
//> using dependency io.opentelemetry.instrumentation:opentelemetry-logback-appender-1.0:2.16.0-alpha
//> using dependency io.github.arturaz::otel4s-doobie:0.7.1
//> using dependency io.opentelemetry.instrumentation:opentelemetry-hikaricp-3.0:2.16.0-alpha

// biscuit
//> using dependency org.biscuitsec:biscuit:4.0.1
