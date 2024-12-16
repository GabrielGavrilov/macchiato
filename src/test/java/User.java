import org.gabrielgavrilov.macchiato.annotations.Column;
import org.gabrielgavrilov.macchiato.annotations.Entity;
import org.gabrielgavrilov.macchiato.annotations.Id;
import org.gabrielgavrilov.macchiato.annotations.Table;

@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "user_id")
    public int id;

    @Column(name = "first_name")
    public String firstName;

    @Column(name = "last_name")
    public String lastName;

    public static User newInstance(int id, String firstName, String lastName) {
        User user = new User();
        user.id = id;
        user.firstName = firstName;
        user.lastName = lastName;
        return user;
    }

}
