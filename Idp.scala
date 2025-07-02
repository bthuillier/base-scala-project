package base

import com.clevercloud.biscuit.token.Biscuit
import com.clevercloud.biscuit.crypto.KeyPair
import scala.jdk.CollectionConverters.*
import com.clevercloud.biscuit.token.builder.Term
import com.clevercloud.biscuit.token.Authorizer

object Idp:

  extension (authorizer: Authorizer)
    def addResource(resource: String): Authorizer =
      authorizer
        .add_fact(
          s"""resource("$resource")"""
        )

    def addOperation(operation: String): Authorizer =
      authorizer
        .add_fact(
          s"""operation("$operation")"""
        )

    def addWorkspaceOwnership(workspaceId: Long, owner: String): Authorizer =
      authorizer
        .add_fact(
          s"""workspace_owner("workspace_$workspaceId", "$owner")"""
        )

    def addTaskToWorkspace(
        taskId: Long,
        workspaceId: Long
    ): Authorizer =
      authorizer
        .add_fact(
          s"""task_workspace("task_$taskId", "workspace_$workspaceId")"""
        )

    def addTasksToWorkspaces(tasks: List[(Long, Long)]): Authorizer =
      tasks.foldLeft(authorizer) { case (auth, (taskId, workspaceId)) =>
        auth.add_fact(
          s"""task_workspace("task_$taskId", "workspace_$workspaceId")"""
        )
      }

  def parseToken(token: String, root: KeyPair) =
    Biscuit
      .from_b64url(token, root.public_key())
      .authorizer()
      .add_policy(
        """allow if resource($workspace_id), operation($op), user($user_id), workspace_owner($workspace_id, $user_id)"""
      )
      .add_policy(
        """allow if resource($workspace_id),  operation("read"), user($user_id)"""
      )
      .add_policy(
        """allow if resource($task_id), operation($op), user($user_id), task_workspace($task_id, $workspace_id), workspace_owner($workspace_id, $user_id)"""
      )
      .add_policy(
        """allow if resource($task_id), operation("read"), user($user_id), task_workspace($task_id, $workspace_id)"""
      )

  def getUserFromToken(token: String, root: KeyPair): Option[String] =
    val biscuit = parseToken(token, root)
    for
      firstResult <- biscuit
        .query("userfact($name) <- user($name)")
        .asScala
        .headOption
      firstTerm <- firstResult.terms().asScala.headOption
    yield firstTerm.asInstanceOf[Term.Str].getValue()
