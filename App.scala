package base

import cats.effect.*
import cats.effect.std.Dispatcher
import sttp.tapir.server.interceptor.cors.CORSConfig
import sttp.tapir.server.interceptor.cors.CORSInterceptor
import sttp.tapir.server.netty.cats.NettyCatsServer
import sttp.tapir.server.netty.cats.NettyCatsServerOptions
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import cats.Applicative
import cats.syntax.all.*
import org.typelevel.otel4s.oteljava.OtelJava
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender
import org.typelevel.otel4s.oteljava.context.LocalContextProvider
import org.typelevel.otel4s.trace.Tracer
import sttp.tapir.server.tracing.otel4s.Otel4sTracing
import sttp.tapir.server.metrics.opentelemetry.OpenTelemetryMetrics
import io.opentelemetry.api.metrics.Meter
import com.zaxxer.hikari.HikariConfig
import doobie.otel4s.hikari.TelemetryHikariTransactor
import io.opentelemetry.api.OpenTelemetry

object App extends ResourceApp.Forever:

  private def serverOptions[F[_]: Async](
      dispatcher: Dispatcher[F],
      tracer: Tracer[F],
      meter: Meter
  ) =
    NettyCatsServerOptions
      .default[F](dispatcher)
      .appendInterceptor(
        CORSInterceptor.customOrThrow(
          CORSConfig.default.allowAllHeaders.allowAllMethods.allowAllOrigins
        )
      )
      .prependInterceptor(Otel4sTracing(tracer))
      .prependInterceptor(
        OpenTelemetryMetrics.default(meter).metricsInterceptor()
      )

  private def nettyServer[F[_]: Async](options: NettyCatsServerOptions[F]) =
    NettyCatsServer[F](options)

  private def startServer[F[_]: Logger: Applicative](
      server: NettyCatsServer[F],
      httpConfig: AppConfig.HttpConfig,
      endpoints: List[sttp.tapir.server.ServerEndpoint[Any, F]]
  ) =
    Resource.make(
      Logger[F].info(
        s"starting http server at ${httpConfig.host}:${httpConfig.port}"
      ) *> server
        .host(httpConfig.host)
        .port(httpConfig.port)
        .addEndpoints(endpoints)
        .start() <* Logger[F].info("http server started")
    )(_.stop())

  def hikariTransactor[F[_]: Async](
      otel: OpenTelemetry,
      postgresConfig: AppConfig.PostgresConfig
  ) =
    val config = new HikariConfig()
    config.setJdbcUrl(postgresConfig.jdbcUrl)
    config.setUsername(postgresConfig.user)
    config.setPassword(postgresConfig.password)
    config.setDriverClassName("org.postgresql.Driver")
    config.setMaximumPoolSize(10)

    TelemetryHikariTransactor.fromHikariConfig[F](otel, config)

  /** Create a logger using Slf4jLogger. It should be instantiated before
    * logging any messages for open telemetry to work correctly.
    */
  private def createLogger[F[_]: Async] = Resource.eval(Slf4jLogger.create[F])

  /** Initialize OpenTelemetry with Otel4s and install the Logback appender.
    * This will automatically configure OpenTelemetry based on the provided
    * system properties.
    */
  private def otel4s[F[_]: Async: LocalContextProvider] =
    OtelJava.autoConfigured[F]().evalTap { otel4s =>
      Async[F].pure(OpenTelemetryAppender.install(otel4s.underlying))
    }

  override def run(args: List[String]): Resource[IO, Unit] =
    Resource.both(otel4s[IO], createLogger[IO]).flatMap {
      case (otel4s, logger) =>
        (for
          tracer <- Resource.eval(otel4s.tracerProvider.get("base-app"))
          config <- Resource.eval(AppConfig.load[IO])
          transactor <- hikariTransactor[IO](otel4s.underlying, config.postgres)
          workspaces = Workspaces[IO](transactor)
          tasks = Tasks[IO](transactor)
          _ <- Resource.eval(workspaces.createTables)
          _ <- Resource.eval(tasks.createTables)
          javaMeter = otel4s.underlying.getMeter("http")
          dispatcher <- Dispatcher.parallel[IO]
          options = serverOptions[IO](dispatcher, tracer, javaMeter)
          server = nettyServer[IO](options)
          _ <- startServer(
            server,
            config.http,
            HealthEndpoints.endpoints[IO] ++
              TasksEndpoints.endpoints[IO](tasks) ++
              WorkspacesEndpoints.endpoints[IO](workspaces)
          )(using logger)
        yield ()).onFinalize { logger.info("Server stopped.") }
    }
