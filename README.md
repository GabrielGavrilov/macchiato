# Macchiato

You can create an entity object by using the `@Entity` and `@Table` annotations.

```Java
import org.gabrielgavrilov.macchiato.annotations.*;

import java.util.UUID;

@Entity
@Table(name = "todos")
public class Todo {

    @Id
    @Column(name = "todo_id")
    public UUID todoId;

    @Column(name = "title")
    public String title;

    public static Todo newInstance(UUID userId, String title) {
        Todo todo = new Todo();
        todo.todoId = UUID.randomUUID();
        todo.userId = userId;
        todo.title = title;
        return todo;
    }

}
```

Create a repository by extending the `MacchiatoRepository<T>` class using the entity

```java
import core.entities.Todo;
import org.gabrielgavrilov.macchiato.MacchiatoRepository;

public class TodoRepository extends MacchiatoRepository<Todo> {
}
```

Then initialize Macchiato with your database URI:
```java
Macchiato.initialize( "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
```

**Macchiato does not create new tables. You must create tables and establish relationships before using Macchiato**

You can check out the <a href="https://github.com/GabrielGavrilov/macchiato/tree/master/src/test/java/core">example</a> in the source code as a reference.

### Repository

The repository has the following methods:

```java
List<T> findAll()

Optional<T> findById(String id)

Optional<T> save(T entity)

Optional<T> update(Object entity)

void deleteById(String id)

void delete(Object entity)
```
> Notice that Macchiato uses a string for all id operations

### Relationships

You can use the `@JoinColumn` annotation to join two tables into an entity using foreign keys:

```java
    @Column(name = "user_id")
    public UUID userId;

    @OneToOne
    @JoinColumn(table = "users", column = "user_id", referencedClass = User.class)
    public User user;
```
