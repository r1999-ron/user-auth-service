package controllers

import javax.inject._
import play.api.mvc._
import services.UserService
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json.Json

@Singleton
class AuthController @Inject()(cc: ControllerComponents, userService: UserService)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def showSignUp = Action {
    Ok(views.html.signup(None))
  }

  def signUp = Action.async(parse.formUrlEncoded) { implicit request =>
    val username = request.body.get("username").flatMap(_.headOption).getOrElse("")
    val password = request.body.get("password").flatMap(_.headOption).getOrElse("")
    userService.register(username, password).map { user =>
      Redirect(routes.AuthController.showLogin).flashing("success" -> "User registered successfully")
    }
  }

  def showLogin = Action {
    Ok(views.html.login(None))
  }

  /*def login = Action.async(parse.formUrlEncoded) { implicit request =>
    val username = request.body.get("username").flatMap(_.headOption).getOrElse("")
    val password = request.body.get("password").flatMap(_.headOption).getOrElse("")
    userService.authenticate(username, password).map {
      case Some(_) =>
        println(s"User logged in: $username")
        val redirectUrl = "http://localhost:9199/chat"
        println(s"Redirecting to: $redirectUrl")
        val sessionDurationSeconds = 3600 // Example session duration of 1 hour
        val currentTimeMillis = System.currentTimeMillis()
        val expiryTimeMillis = currentTimeMillis + (sessionDurationSeconds * 1000) // Convert seconds to milliseconds
        val session = request.session + ("username" -> username) + ("expiry" -> expiryTimeMillis.toString)
        println(s"Session data before redirection: ${session}")
        //  Redirect(redirectUrl).withSession(request.session - "username" + ("username" -> username), "expiry" -> (System.currentTimeMillis() + 3600000).toString)
        Redirect("http://localhost:9199/chat").withSession(session)
      case None =>
        println(s"Failed login attempt for user: $username")
        Unauthorized("Invalid credentials")
    }
  }*/

  def login = Action.async(parse.formUrlEncoded) { implicit request =>
    val username = request.body.get("username").flatMap(_.headOption).getOrElse("")
    val password = request.body.get("password").flatMap(_.headOption).getOrElse("")
    userService.authenticate(username, password).map {
      case Some(_) =>
        Redirect(s"http://localhost:9199/chat?username=$username")
      case None =>
        BadRequest("Invalid credentials")
    }
  }
  def logout = Action {
    Redirect(routes.AuthController.showLogin).withNewSession
  }
}