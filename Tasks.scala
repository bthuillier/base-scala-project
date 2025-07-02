package base

import io.circe.Codec
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import cats.effect.kernel.Async
import base.Tasks.Task
import sttp.model.StatusCode
import cats.syntax.all.*

class Tasks[F[_]: Async](xa: doobie.Transactor[F]):

  import doobie.*
  import doobie.implicits.*

  def createTables: F[Int] =
    sql"""
      CREATE TABLE IF NOT EXISTS tasks (
        id SERIAL PRIMARY KEY,
        name TEXT NOT NULL,
        workspace_id INTEGER NOT NULL,
        CONSTRAINT fk_workspace
          FOREIGN KEY (workspace_id)
          REFERENCES workspaces(id)
      )
    """.update.run.transact(xa)

  def insert(name: String, workspaceId: Long): F[Int] =
    sql"INSERT INTO tasks (name, workspace_id) VALUES ($name, $workspaceId)".update.run
      .transact(xa)

  def list: F[List[Task]] =
    sql"SELECT id, name, workspace_id FROM tasks"
      .query[Task]
      .to[List]
      .transact(xa)

  def byId(id: Long): F[Option[Task]] =
    sql"SELECT id, name, workspace_id FROM tasks WHERE id = $id"
      .query[Task]
      .option
      .transact(xa)

object Tasks:

  case class CreateTask(name: String, workspaceId: Long)
      derives Codec.AsObject,
        Schema
  case class Task(id: Long, name: String, workspaceId: Long)
      derives Codec.AsObject,
        Schema

class TasksEndpoints[F[_]: Async](tasks: Tasks[F]):

  import sttp.tapir.*
  import sttp.tapir.json.circe.*
  import sttp.tapir.server.ServerEndpoint

  val create: ServerEndpoint[Any, F] =
    endpoint.post
      .in("tasks")
      .in(jsonBody[Tasks.CreateTask])
      .out(statusCode(StatusCode.Created))
      .serverLogicSuccess { create =>
        tasks.insert(create.name, create.workspaceId).void
      }

  val list: ServerEndpoint[Any, F] =
    endpoint.get
      .in("tasks")
      .out(jsonBody[List[Tasks.Task]])
      .serverLogicSuccess(_ => tasks.list)

  val byId: ServerEndpoint[Any, F] = endpoint.get
    .in("tasks" / path[Long]("id"))
    .out(jsonBody[Option[Tasks.Task]])
    .serverLogicSuccess(id => tasks.byId(id))

  val endpoints: List[ServerEndpoint[Any, F]] = List(create, list, byId)

object TasksEndpoints:
  def endpoints[F[_]: Async](tasks: Tasks[F]): List[ServerEndpoint[Any, F]] =
    TasksEndpoints[F](tasks).endpoints
