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

Then create a repository by extending the `MacchiatoRepository<T>` class using the entity

```java
import core.entities.Todo;
import org.gabrielgavrilov.macchiato.MacchiatoRepository;

public class TodoRepository extends MacchiatoRepository<Todo> {
}
```
