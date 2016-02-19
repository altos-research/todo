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

  // Form helper with validation to require non-empty field
  val taskForm = Form(
    "label" -> nonEmptyText
  )

  def index = Action {
    Redirect(routes.Application.todos)
  }

  def tasks(list_id: Long) = Action { implicit request =>
    val user = User(request.cookies.get("user").map(_.value).getOrElse("andy"))
    Ok(views.html.tasks(Task.all(list_id), list_id, Some(user)))
  }

  def newTask(list_id: Long) = Action { implicit request =>
    taskForm.bindFromRequest.fold(
      errors => BadRequest(views.html.tasks(Task.all(list_id), list_id)),
      label => {
        Task.create(label, list_id)
        Redirect(routes.Application.tasks(list_id))
      }
    )
  }

  def deleteTask(id: Long, list_id: Long) = Action { implicit request =>
    Task.delete(id, list_id)
    Redirect(routes.Application.tasks(list_id))
  }

  def todos = Action { implicit request =>
    val user = User(request.cookies.get("user").map(_.value).getOrElse("andy"))
    Ok(views.html.todos(Todo.all(), Some(user)))
  }

  def newTodo = Action { implicit request =>
    taskForm.bindFromRequest.fold(
      errors => BadRequest(views.html.todos(Todo.all())),
      label => {
        Todo.create(label)
        Redirect(routes.Application.todos)
      }
    )
  }

  def deleteTodo(id: Long) = Action { implicit request =>
    Todo.delete(id)
    Redirect(routes.Application.todos)
  }

  def users = Action { implicit request =>
    val user = User(request.cookies.get("user").map(_.value).getOrElse("andy"))
    Ok(views.html.users(User.all(), Some(user)))
  }

  def maybeNewUser = Action { implicit request =>
    taskForm.bindFromRequest.fold(
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
