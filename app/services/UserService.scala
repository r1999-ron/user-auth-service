package services

import javax.inject._
import models.User
import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Success, Failure}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

@Singleton
class UserService @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private class UsersTable(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username", O.Unique)
    def password = column[String]("password")
    def * = (id, username, password) <> ((User.apply _).tupled, User.unapply)
  }

  private val users = TableQuery[UsersTable]

  def register(username: String, password: String): Future[User] = db.run {
    (users.map(u => (u.username, u.password))
      returning users.map(_.id)
      into ((data, id) => User(id, data._1, data._2))
      ) += (username, password)
  }

  def authenticate(username: String, password: String): Future[Option[User]] = db.run {
    users.filter(u => u.username === username && u.password === password).result.headOption
  }
}