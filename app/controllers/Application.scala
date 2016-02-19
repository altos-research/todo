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

  def tasks(list_id: Long) = Action {
    Ok(views.html.tasks(Task.all(list_id), taskForm, list_id))
  }

  def newTask(list_id: Long) = Action { implicit request =>
    taskForm.bindFromRequest.fold(
      errors => BadRequest(views.html.tasks(Task.all(list_id), errors, list_id)),
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
