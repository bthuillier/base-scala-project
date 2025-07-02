package base

import pureconfig.*
import pureconfig.module.catseffect.syntax.*
import cats.effect.kernel.Sync
import base.AppConfig.HttpConfig
import base.AppConfig.PostgresConfig
import base.AppConfig.BiscuitConfig
import com.clevercloud.biscuit.crypto.KeyPair

final case class AppConfig(
    http: HttpConfig,
    postgres: PostgresConfig,
    biscuit: BiscuitConfig
) derives ConfigReader

object AppConfig {

  def load[F[_]: Sync] = ConfigSource.default.loadF[F, AppConfig]()

  case class HttpConfig(host: String, port: Int) derives ConfigReader
  case class PostgresConfig(
      host: String,
      port: Int,
      dbname: String,
      user: String,
      password: String
  ) derives ConfigReader:
    def jdbcUrl: String = s"jdbc:postgresql://$host:$port/$dbname"

  case class BiscuitConfig(
      privateKey: String
  ) derives ConfigReader:
    val keypair = KeyPair(privateKey)

}
