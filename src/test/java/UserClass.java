import org.gabrielgavrilov.macchiato.annotations.*;

@Entity
@Table(name = "user_classes")
public class UserClass {

    @Id
    @Column(name = "user_class_id")
    public int userClassId;

    @Column(name = "user_id")
    public int userId;

    @Column(name = "class_id")
    public int classId;

    @JoinTable(tableName = "users", columnName = "user_id")
    public User user;

    public static UserClass newInstance(int userClassId, int userId, int classId) {
        UserClass userClass = new UserClass();
        userClass.userClassId = userClassId;
        userClass.userId = userId;
        userClass.classId = classId;
        return userClass;
    }

}
