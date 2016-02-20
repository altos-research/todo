package controllers

import play.api._
import play.api.mvc._

import play.api.data._
import play.api.data.Forms._

import models._

object Application extends Controller {

  def getUsername(request: RequestHeader): Option[String] = request.cookies.get("user").map(_.value)

  def withUser(f: User => Request[AnyContent] => Result): EssentialAction ={
    Security.Authenticated(getUsername, _ => Redirect(routes.Application.users)) { name : String =>
      Action { request => User.byName(name)
                              .map(f(_)(request))
                              .getOrElse(Redirect(routes.Application.users))
      }
    }
  }

  // TODO the open question here is what is the correct action to take on failure.
  // Note: In AJAX the question is much less important.
  def withTodo(id: Long)(f: User => Todo => Request[AnyContent] => Result) = withUser { user => implicit request =>
    Todo.byId(id, user)
        .fold(Redirect(routes.Application.todos): Result)(f(user)(_)(request))
  }



  def index = Action {
    Redirect(routes.Application.todos)
  }

  val form = Form("label" -> nonEmptyText)



  def tasks(list_id: Long) = withTodo(list_id) { user => todo => implicit request =>
    Ok(views.html.tasks(Task.all(todo), todo, Some(user)))
  }

  def newTask(list_id: Long) = withTodo(list_id) { user => todo => implicit request =>
    form.bindFromRequest.fold(
      errors => BadRequest(views.html.tasks(Task.all(todo), todo)),
      label => {
        Task.create(label, todo)
        Redirect(routes.Application.tasks(list_id))
      }
    )
  }

  def deleteTask(id: Long, list_id: Long) = withTodo(list_id) { user => todo => implicit request =>
    Task.delete(id, todo)
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
