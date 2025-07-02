package base

import io.circe.Codec
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import cats.effect.kernel.Async
import base.Workspaces.Workspace
import sttp.model.StatusCode
import cats.syntax.all.*

class Workspaces[F[_]: Async](xa: doobie.Transactor[F]):

  import doobie.*
  import doobie.implicits.*

  def createTables: F[Int] =
    sql"""
      CREATE TABLE IF NOT EXISTS workspaces (
        id SERIAL PRIMARY KEY,
        name TEXT NOT NULL,
        description TEXT,
        owner TEXT NOT NULL
      )
    """.update.run.transact(xa)

  def insert(name: String, description: Option[String], owner: String): F[Int] =
    sql"INSERT INTO workspaces (name, description, owner) VALUES ($name, $description, $owner)".update.run
      .transact(xa)

  def list: F[List[Workspace]] =
    sql"SELECT id, name, description, owner FROM workspaces"
      .query[Workspace]
      .to[List]
      .transact(xa)

  def byId(id: Long): F[Option[Workspace]] =
    sql"SELECT id, name, description, owner FROM workspaces WHERE id = $id"
      .query[Workspace]
      .option
      .transact(xa)

  def delete(id: Long): F[Int] =
    sql"DELETE FROM workspaces WHERE id = $id".update.run.transact(xa)

  def update(id: Long, name: String, description: Option[String]): F[Int] =
    sql"UPDATE workspaces SET name = $name, description = $description WHERE id = $id".update.run
      .transact(xa)

object Workspaces:

  case class CreateWorkspace(
      name: String,
      description: Option[String],
      owner: String
  ) derives Codec.AsObject,
        Schema
  case class UpdateWorkspace(name: String, description: Option[String])
      derives Codec.AsObject,
        Schema
  case class Workspace(
      id: Long,
      name: String,
      description: Option[String],
      owner: String
  ) derives Codec.AsObject,
        Schema

class WorkspacesEndpoints[F[_]: Async](workspaces: Workspaces[F]):

  import sttp.tapir.*
  import sttp.tapir.json.circe.*
  import sttp.tapir.server.ServerEndpoint

  val create: ServerEndpoint[Any, F] =
    endpoint.post
      .in("workspaces")
      .in(jsonBody[Workspaces.CreateWorkspace])
      .out(statusCode(StatusCode.Created))
      .serverLogicSuccess { create =>
        workspaces.insert(create.name, create.description, create.owner).void
      }

  val list: ServerEndpoint[Any, F] =
    endpoint.get
      .in("workspaces")
      .out(jsonBody[List[Workspaces.Workspace]])
      .serverLogicSuccess(_ => workspaces.list)

  val byId: ServerEndpoint[Any, F] = endpoint.get
    .in("workspaces" / path[Long]("id"))
    .out(jsonBody[Option[Workspaces.Workspace]])
    .serverLogicSuccess(id => workspaces.byId(id))

  val delete: ServerEndpoint[Any, F] = endpoint.delete
    .in("workspaces" / path[Long]("id"))
    .out(statusCode(StatusCode.NoContent))
    .serverLogicSuccess(id => workspaces.delete(id).void)

  val update: ServerEndpoint[Any, F] = endpoint.put
    .in("workspaces" / path[Long]("id"))
    .in(jsonBody[Workspaces.UpdateWorkspace])
    .out(statusCode(StatusCode.NoContent))
    .serverLogicSuccess { case (id, update) =>
      workspaces.update(id, update.name, update.description).void
    }

  val endpoints: List[ServerEndpoint[Any, F]] =
    List(create, list, byId, delete, update)

object WorkspacesEndpoints:
  def endpoints[F[_]: Async](
      workspaces: Workspaces[F]
  ): List[ServerEndpoint[Any, F]] =
    WorkspacesEndpoints[F](workspaces).endpoints
