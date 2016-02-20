package models

import play.api.db._
import play.api.Play.current

case class Todo(id: Long, label: String)

object Todo {
  import anorm._
  import anorm.SqlParser._
  val parser: RowParser[Todo] ={
    long("id") ~ str("label") map {
      case id ~ label => Todo(id, label)
    }
  }

  def all(): List[Todo] = DB.withConnection { implicit c =>
    SQL("select * from todo_list").as(Todo.parser.*)
  }

  def all(user: User): List[Todo] = DB.withConnection { implicit c =>
    SQL("select * from todo_list where user_id = {userId}")
      .on('userId -> user.id)
      .as(Todo.parser.*)
  }

  def create(label: String, user: User) = DB.withConnection { implicit c =>
    SQL("""insert into todo_list (label, user_id)
           values ({label}, {userId})""")
      .on('label -> label, 'userId -> user.id)
      .executeUpdate()
  }

  def delete(id: Long, user: User) = DB.withConnection { implicit c =>
    SQL("delete from todo_list where id = {id} and user_id = {userId}")
      .on('id -> id, 'userId -> user.id)
      .executeUpdate()
  }

  // TODO if used this should really be moved to the case class.
  def owner(list_id: Long): User = DB.withConnection { implicit c =>
    SQL("select u.* from todo_list t inner join users u ON u.id = t.user_id where t.id = {listId}")
      .on('listId -> list_id)
      .as(User.parser.single)
  }

  // This method is used to check whether the user should have access to the list.
  // Currently it checks nothing: YOLO.
  def byId(list_id: Long, user: User): Option[Todo] = DB.withConnection { implicit c =>
    SQL("select * from todo_list where id = {listId}")
      .on('listId -> list_id)
      .as(Todo.parser.singleOpt)
  }
}
