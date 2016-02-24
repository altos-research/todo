This is surely old ground/ideas:

- [Don’t Do Role-Based Authorization Checks; Do Activity-Based Checks](https://lostechies.com/derickbailey/2011/05/24/dont-do-role-based-authorization-checks-do-activity-based-checks)
- [A Pattern System for Access Control](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.3.2415&rep=rep1&type=pdf)
- [A survey of access control models](http://csrc.nist.gov/news_events/privilege-management-workshop/PvM-Model-Survey-Aug26-2009.pdf)

but I haven't seen this particular pattern.

*TODO: More links and discussion.*

---

# Object-oriented Authorization

*How to organize a Play app.*

The following ideas describe a authorization hierarchy for database access.
Although we use a Scala Play application to demonstrate this, we don't think
that the ideas are limited to just Scala or Play.

When you start a simple play application you might have the following model
and method to get a `User` by id:

```scala
object User {
  val parser: RowParser[User] = ... // omitted for brevity

  def byId(id: Long): Option[User] = DB.withConnection {
    SQL("SELECT * FROM users WHERE id = {id}")
      .on('id -> id)
      .as(User.parser.singleOpt)
  }
}
```

As your application becomes larger, having SQL inside methods becomes somewhat
unwieldy...

One approach is to extract DB accesses into a separate trait:

```scala
object User {
  val parser:RowParser[User] = ... // omitted for brevity

  def byId(db: UserDAO, id: Long): Option[User] = db.userById(id)
}

object UserDAO extends UserDAO
trait UserDAO {
  def userById(id: Long) = DB.withConnection {
    SQL("SELECT * FROM users WHERE id = {id}")
      .on('id -> id)
      .as(User.parser.singleOpt)
  }
}
```
*Note: The ["selfless trait" pattern](http://www.artima.com/scalazine/articles/selfless_trait_pattern.html).*

One benefit is that we can mock the UserDatabase in testing:

```scala
val db = new UserDAO {
  override def userById(id: Long) = None
}
// test that User.byId(db, id) is None.
```

However, you can come into issues with this approach, not least of which is
ambiguity in what the "best thing to do" is.

For example, suppose we have another model `Report`. We want to get all reports
associated with a user.

The question is:

```scala
user.reports: List[Report]
// or
Report.byUser(user: User): List[Report]
```
*Note: in this case the "obvious" choice is `user.reports`. As the business
logic gets more involved the "right thing to do" can become less obvious.*

and therefore the corresponding `UserDAO` vs `ReportDAO`.

This is actually a fairly standard concern about Object-oriented programming:

- [OOP or POO](http://blog.codinghorror.com/your-code-oop-or-poo/)
- TODO more references to this issue (I've seen it in julia mailing lists)

You have to attach your method to one object, when the other may be an
equally valid choice.

As such it can be *very* easy to end up with definitions in both (which
happen to have duplicate SQL).

---

Another, somewhat more difficult issue is who has access to what. For example,
should a User be able to look up another User by id? Can we **use the compiler**
to catch suspicious activity (seemingly above a Users' authorization level).

With the UserDatabase, the User has access to every possible User method. We
offer an alternative.

# withWhat vs withWho

In the [play documentation](https://www.playframework.com/documentation/2.0.4/ScalaSecurity)
(no longer in the latest version) it provided a withUser implementation,
essentially as follows:

```scala
def withUser(f: User => Request[AnyContent] => Result): EssentialAction ={
  Security.Authenticated(getUsername, _ => Unauthorized) { username: String =>
    Action { request => UserDAO.byName(username)
                               .map(f(_)(request))
                               .getOrElse(Unauthorized)
    }
  }
}
```
Note: DAO stands for "Data Access Object".

*See also the [Play angular demo](https://github.com/krispo/play-angular-demos/blob/master/app/controllers/Auth.scala).*

The insight, is that often the function `f` above will be doing more database
access, so why not pass it a DAO that matches its authorization, an Object
which only has methods deemed appropriate for [users at] this level.

---

Suppose we have the following three authorization tiers:

1. ReadonlyDAO
2. WriteableDAO
3. AdminDAO

One might ask where each of the following SQL queries belong:

```sql
SELECT * FROM todos WHERE user_id = {user.id};

SELECT * FROM todos;

UPDATE todos SET label = 'new label' WHERE id = {t.id} AND user_id = {user.id};

DELETE FROM todos WHERE id = {t.id};
```


- If the action which they are doing only requires Readonly access, then
pass them a ReadonlyDatabase.
- If the User has administration privilege, then pass them a AdminDatabase.




---

This solves the question above: Where should we put the `user.reports`?

Previously the answer was either `UserDAO` or `ReportDAO`. However,
since this is a "readonly" method we can put it in a `ReadonlyDAO`.


---

# todo (meta)

(actually stuff we need to do)

- read/blue teams
- admin user (that can see both) :/
