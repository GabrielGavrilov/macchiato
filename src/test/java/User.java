import org.gabrielgavrilov.macchiato.annotations.Column;
import org.gabrielgavrilov.macchiato.annotations.Entity;
import org.gabrielgavrilov.macchiato.annotations.Table;

@Entity
@Table
public class User {

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    public static User newInstance(String firstName, String lastName) {
        User user = new User();
        user.firstName = firstName;
        user.lastName = lastName;
        return user;
    }

}
