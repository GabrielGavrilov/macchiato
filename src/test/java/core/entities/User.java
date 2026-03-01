package core.entities;

import org.gabrielgavrilov.macchiato.annotations.Column;
import org.gabrielgavrilov.macchiato.annotations.Entity;
import org.gabrielgavrilov.macchiato.annotations.Id;
import org.gabrielgavrilov.macchiato.annotations.Table;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id")
    public UUID userId;

    @Column(name = "email")
    public String email;

    public static User newInstance(String email) {
        User user = new User();
        user.userId = UUID.randomUUID();
        user.email = email;
        return user;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

}
