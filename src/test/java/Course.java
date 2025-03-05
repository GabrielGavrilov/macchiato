import org.gabrielgavrilov.macchiato.annotations.*;

import java.util.List;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @Column(name = "course_id")
    public int courseId;

    @Column(name = "course_name")
    public String courseName;

    @Column(name = "teacher_id")
    public int teacherId;

    @OneToOne
    @JoinColumn(table = "teachers", column = "teacher_id", referencedClass = Teacher.class)
    public Teacher teacher;

    public static Course newInstance(int id, String courseName, int teacherId) {
        Course course = new Course();
        course.courseId = id;
        course.courseName = courseName;
        course.teacherId = teacherId;
        return course;
    }

}
