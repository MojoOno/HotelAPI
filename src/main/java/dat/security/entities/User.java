package dat.security.entities;


import jakarta.persistence.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class User implements ISecurityUser
{
    @Id
    String username;
    String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "username"),
            inverseJoinColumns = @JoinColumn(name = "role_name"))
    Set<Role> roles = new HashSet<>();

    //        CONSTRUCTORS          //
    public User(String username, String password)
    {
        this.username = username;
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public User()
    {
    }

    //        METHODS          //

    @Override
    public Set<String> getRolesAsStrings()
    {
        return Set.of();
    }

    @Override
    public boolean verifyPassword(String pw)
    {
        return BCrypt.checkpw(pw, this.password);
    }

    @Override
    public void addRole(Role role)
    {
        roles.add(role);
        role.users.add(this);
    }

    @Override
    public void removeRole(String role)
    {
//        roles.removeIf(roleEntity -> roleEntity.name.equals(role));
        for(Role roleEntity : roles)
        {
            if(roleEntity.name.equals(role))
            {
                roles.remove(roleEntity);
                roleEntity.users.remove(this);
            }
        }
    }


    //        GETTERS & SETTERS          //

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }
}
