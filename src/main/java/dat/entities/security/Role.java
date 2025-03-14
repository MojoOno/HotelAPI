package dat.entities.security;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter

public class Role
{
    @Id
    String name;
    @ManyToMany(mappedBy = "roles")
    Set<User> users = new HashSet<>();


    //       CONSTRUCTORS          //
    public Role(String name)
    {
        this.name = name;
    }
}
