import org.gabrielgavrilov.macchiato.annotations.Column;
import org.gabrielgavrilov.macchiato.annotations.Entity;
import org.gabrielgavrilov.macchiato.annotations.Id;
import org.gabrielgavrilov.macchiato.annotations.Table;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @Column(name = "role_id")
    public int roleId;

    @Column(name = "role_name")
    public String roleName;

    public static Role newInstance(int roleId, String roleName) {
        Role role = new Role();
        role.roleId = roleId;
        role.roleName = roleName;
        return role;
    }

}
