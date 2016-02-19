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

  def tasks = Action {
    Ok(views.html.tasks(Task.all(), taskForm))
  }

  def newTask = Action { implicit request =>
    taskForm.bindFromRequest.fold(
      errors => BadRequest(views.html.tasks(Task.all(), errors)),
      label => {
        Task.create(label)
        Redirect(routes.Application.tasks)
      }
    )
  }

  def deleteTask(id: Long) = Action { implicit request =>
    Task.delete(id)
    Redirect(routes.Application.tasks)
  }

  def todos = Action {
    Ok(views.html.todos(Todo.all(), taskForm))
  }

  def newTodo = Action { implicit request =>
    taskForm.bindFromRequest.fold(
      errors => BadRequest(views.html.todos(Todo.all(), errors)),
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

}
