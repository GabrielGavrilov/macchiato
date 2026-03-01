package core.entities;

import org.gabrielgavrilov.macchiato.annotations.*;

import java.util.UUID;

@Entity
@Table(name = "todos")
public class Todo {

    @Id
    @Column(name = "todo_id")
    public UUID todoId;

    @Column(name = "user_id")
    public UUID userId;

    @Column(name = "title")
    public String title;

    @OneToOne
    @JoinColumn(table = "users", column = "user_id", referencedClass = User.class)
    public User user;

    public static Todo newInstance(UUID userId, String title) {
        Todo todo = new Todo();
        todo.todoId = UUID.randomUUID();
        todo.userId = userId;
        todo.title = title;
        return todo;
    }

    public Todo setTitle(String title) {
        this.title = title;
        return this;
    }

}
