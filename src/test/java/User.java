import org.gabrielgavrilov.macchiato.annotations.*;

import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id")
    public int userId;

    @Column(name = "first_name")
    public String firstName;

    @Column(name = "last_name")
    public String lastName;

    @Column(name = "class_id")
    public int classId;

    public static User newInstance(int id, String firstName, String lastName) {
        User user = new User();
        user.userId = id;
        user.firstName = firstName;
        user.lastName = lastName;
        return user;
    }

}
