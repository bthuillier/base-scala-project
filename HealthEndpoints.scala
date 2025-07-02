package base

import io.circe.Codec
import sttp.tapir.*
import sttp.tapir.json.circe.*
import sttp.tapir.server.ServerEndpoint

class HealthEndpoints[F[_]]:

  val health: ServerEndpoint[Any, F] =
    HealthEndpoints.healthEndpoint.serverLogicSuccessPure(_ =>
      HealthEndpoints.HealthStatus("ok")
    )

object HealthEndpoints:

  def endpoints[F[_]]: List[ServerEndpoint[Any, F]] = List(
    HealthEndpoints[F]().health
  )
  final case class HealthStatus(status: String) derives Codec.AsObject, Schema

  val healthEndpoint: PublicEndpoint[Unit, Unit, HealthStatus, Any] =
    endpoint
      .in("_health")
      .get
      .out(jsonBody[HealthStatus])
      .description("Health check endpoint")
