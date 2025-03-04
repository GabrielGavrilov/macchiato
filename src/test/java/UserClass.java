import org.gabrielgavrilov.macchiato.annotations.*;

import java.util.List;

@Entity
@Table(name = "classes")
public class UserClass {

    @Id
    @Column(name = "class_id")
    public int classId;

    @Column(name = "class_name")
    public String className;

    @OneToMany
    @JoinColumn(table = "users", column = "class_id")
    List<User> users;

    public static UserClass newInstance(int id, String className) {
        UserClass userClass = new UserClass();
        userClass.classId = id;
        userClass.className = className;
        return userClass;
    }

}
