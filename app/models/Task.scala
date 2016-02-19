package models

// Required for Anorm mapping capability
import anorm._
import anorm.SqlParser._

// Required for the Play db functionality
import play.api.db._
import play.api.Play.current

case class Task(id: Long, label: String)

object Task {
	// Parser for mapping JDBC ResultSet to a single entity of Task model
	val task = {
		get[Long]("id") ~
		get[String]("label") map {
			case id~label => Task(id, label)
		}
	}

	def all(list_id: Long): List[Task] = DB.withConnection { implicit c =>
		SQL("select * from task WHERE list_id = {list_id}")
      .on('list_id -> list_id)
      .as(task *)
	}

	def create(label: String, list_id: Long) {
		DB.withConnection { implicit c =>
			SQL("""insert into task (label, list_id)
             values ({label}, {list_id})""")
        .on('label -> label,
            'list_id -> list_id)
        .executeUpdate()
		}
	}

	def delete(id: Long, list_id: Long) {
		DB.withConnection { implicit c =>
			SQL("""delete from task
             where id = {id}
             and list_id = {list_id}""")
        .on('id -> id,
            'list_id -> list_id)
        .executeUpdate()
		}
	}
}
