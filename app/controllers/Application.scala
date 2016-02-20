package controllers

// Required by default
import play.api._
import play.api.mvc._

// Required for form helpers
import play.api.data._
import play.api.data.Forms._

// Required to be able to import model definitions
import models._

object Application extends Controller {

  def getUsername(request: RequestHeader): Option[String] = request.cookies.get("user").map(_.value)

  def withUser(f: User => Request[AnyContent] => Result): EssentialAction =
    Security.Authenticated(getUsername, _ => Redirect(routes.Application.users)) { name : String =>
      Action { request =>
        User.byName(name)
          .map(f(_)(request))
          .getOrElse(Redirect(routes.Application.users))
      }
  }


  // Form helper with validation to require non-empty field
  val form = Form(
    "label" -> nonEmptyText
  )

  def index = Action {
    Redirect(routes.Application.todos)
  }



  def tasks(list_id: Long) = withUser { user => implicit request =>
    Ok(views.html.tasks(Task.all(list_id), list_id, Some(user)))
  }

  def newTask(list_id: Long) = withUser { user => implicit request =>
    form.bindFromRequest.fold(
      errors => BadRequest(views.html.tasks(Task.all(list_id), list_id)),
      label => {
        Task.create(label, list_id)
        Redirect(routes.Application.tasks(list_id))
      }
    )
  }

  def deleteTask(id: Long, list_id: Long) = withUser { user => implicit request =>
    Task.delete(id, list_id)
    Redirect(routes.Application.tasks(list_id))
  }



  def todos = withUser { user => implicit request =>
    Ok(views.html.todos(Todo.all(), Some(user)))
  }

  def newTodo = withUser { user => implicit request =>
    form.bindFromRequest.fold(
      errors => BadRequest(views.html.todos(Todo.all())),
      label => {
        Todo.create(label, user)
        Redirect(routes.Application.todos)
      }
    )
  }

  def deleteTodo(id: Long) = withUser { user => implicit request =>
    Todo.delete(id, user)
    Redirect(routes.Application.todos)
  }




  def users = Action { implicit request =>
    val user = request.cookies.get("user").map(_.value).flatMap(User.byName(_))
    Ok(views.html.users(User.all(), user))
  }

  def maybeNewUser = Action { implicit request =>
    form.bindFromRequest.fold(
      errors => BadRequest(views.html.users(User.all())),
      name => {
        User.maybeCreate(name)
        Redirect(routes.Application.todos)
      }
    )
  }

  def setCookie(name: String) = Action { implicit request =>
    Redirect(routes.Application.todos).withCookies(Cookie("user", name))
  }
}
