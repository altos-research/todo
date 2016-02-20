package models

import play.api.db._
import play.api.Play.current

case class User(id: Long, name: String)

object User {
  import anorm._
  import anorm.SqlParser._
  val parser: RowParser[User] ={
    long("id") ~ str("name") map {
      case id ~ name => User(id, name)
    }
  }

  def all(): List[User] = DB.withConnection { implicit c =>
    SQL("select * from users")
      .as(User.parser.*)
  }

  def maybeCreate(name: String): User ={
    byName(name).getOrElse(create(name))
  }

  private def create(name: String): User = DB.withConnection { implicit c =>
    SQL("insert into users (name) values ({name})")
      .on('name -> name)
      .executeInsert(User.parser.single)
  }

  def byName(name: String): Option[User] = DB.withConnection { implicit c =>
    SQL("select * from users where name = {name}")
      .on('name -> name)
      .as(User.parser.singleOpt)
  }
}
