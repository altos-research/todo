package models

// Required for Anorm mapping capability
import anorm._
import anorm.SqlParser._

// Required for the Play db functionality
import play.api.db._
import play.api.Play.current

case class Todo(id: Long, label: String)

object Todo {
	// Parser for mapping JDBC ResultSet to a single entity of Todo model
	val todo = {
		get[Long]("id") ~
		get[String]("label") map {
			case id~label => Todo(id, label)
		}
	}

	def all(): List[Todo] = DB.withConnection { implicit c =>
		SQL("select * from todo_list").as(todo *)
	}

	def create(label: String) {
		DB.withConnection { implicit c =>
			SQL("insert into todo_list (label) values ({label})").on(
				'label -> label
			).executeUpdate()
		}
	}

	def delete(id: Long) {
		DB.withConnection { implicit c =>
			SQL("delete from todo_list where id = {id}").on(
				'id -> id
			).executeUpdate()
		}
	}
}
