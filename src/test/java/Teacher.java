import org.gabrielgavrilov.macchiato.annotations.*;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @Column(name = "teacher_id")
    public int teacherId;

    @Column(name = "first_name")
    public String firstName;

    @Column(name = "last_name")
    public String lastName;

    public static Teacher newInstance(int id, String firstName, String lastName) {
        Teacher teacher = new Teacher();
        teacher.teacherId = id;
        teacher.firstName = firstName;
        teacher.lastName = lastName;
        return teacher;
    }

}
