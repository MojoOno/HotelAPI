package dat.daos.security;

import dat.config.HibernateConfig;
import dat.daos.GenericDAO;
import dat.exceptions.ValidationException;
import dat.entities.security.Role;
import dat.entities.security.User;
import dk.bugelhartmann.UserDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class SecurityDAO
{
    private static final Logger logger = LoggerFactory.getLogger(SecurityDAO.class);
    private static SecurityDAO instance;
    private static EntityManagerFactory emf;

    public SecurityDAO(EntityManagerFactory emf)
    {
        this.emf = emf;
    }

    public static SecurityDAO getInstance(EntityManagerFactory emf)
    {
        if (instance == null)
        {
            instance = new SecurityDAO(emf);
        }
        return instance;
    }

    public User create(User user)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            Set<Role> newRoleSet = new HashSet<>();
            if (user.getRoles().isEmpty())
            {
                Role userRole = em.find(Role.class, "user");
                if (userRole == null)
                {
                    userRole = new Role("user");
                    em.persist(userRole);
                }
                user.addRole(userRole);
            }
            user.getRoles().forEach(role ->
            {
                Role foundRole = em.find(Role.class, role.getName());
                if (foundRole == null)
                {
                    throw new EntityNotFoundException("Role not found");
                }
                else
                {
                    newRoleSet.add(foundRole);
                }
            });

            user.setRoles(newRoleSet);
            em.getTransaction().begin();
            em.persist(user);
            em.getTransaction().commit();
            return user;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public Role createRole(Role role)
    {
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.persist(role);
            em.getTransaction().commit();
            return role;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public UserDTO getVerifiedUser(String username, String password) throws ValidationException
    {
        try (EntityManager em = emf.createEntityManager()) {
            User user = em.find(User.class, username);
            if (user == null)
                throw new EntityNotFoundException("No user found with username: " + username); //RuntimeException
            user.getRoles().size(); // force roles to be fetched from db
            if (!user.verifyPassword(password))
                throw new ValidationException("Wrong password");
            return new UserDTO(user.getUsername(), user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()));
        }
    }

}
