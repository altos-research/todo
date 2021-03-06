package models

import play.api.db._
import play.api.Play.current

case class Task(id: Long, label: String)

object Task {

  import anorm._
  import anorm.SqlParser._
  val parser: RowParser[Task] ={
    long("id") ~ str("label") map {
      case id ~ label => Task(id, label)
    }
  }

  def all(todo: Todo): List[Task] = DB.withConnection { implicit c =>
    SQL("select * from task where list_id = {list_id}")
      .on('list_id -> todo.id)
      .as(Task.parser.*)
  }

  def create(label: String, todo: Todo) = DB.withConnection { implicit c =>
    SQL("""insert into task (label, list_id)
           values ({label}, {list_id})""")
      .on('label -> label, 'list_id -> todo.id)
      .executeUpdate()
  }

  def delete(id: Long, todo: Todo) = DB.withConnection { implicit c =>
    SQL("delete from task where id = {id} and list_id = list_id")
      .on('id -> id, 'list_id -> todo.id)
      .executeUpdate()
  }
}
