package models

// Required for the Play db functionality
import play.api.db._
import play.api.Play.current

case class Todo(id: Long, label: String)

object Todo {
  import anorm._
  import anorm.SqlParser._
  val parser: RowParser[Todo] ={
    long("id") ~
    str("label") map {
      case id ~ label => Todo(id, label)
    }
  }

  def all(): List[Todo] = DB.withConnection { implicit c =>
    SQL("select * from todo_list").as(Todo.parser.*)
  }

  def all(username: String): List[Todo] = ???

  def create(label: String, user: User) {
    DB.withConnection { implicit c =>
      SQL("insert into todo_list (label) values ({label})").on(
        'label -> label
      ).executeUpdate()
    }
  }

  def delete(id: Long, user: User) {
    DB.withConnection { implicit c =>
      SQL("delete from todo_list where id = {id}").on(
        'id -> id
      ).executeUpdate()
    }
  }

  def owner(list_id: Long): User ={
    ???
  }
}
