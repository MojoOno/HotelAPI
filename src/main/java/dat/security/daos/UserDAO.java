package dat.security.daos;

import dat.config.HibernateConfig;
import dat.security.entities.Role;
import dat.security.entities.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class UserDAO
{
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);
    EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();


    public User create(User user)
    {
        try (EntityManager em = emf.createEntityManager())
        {
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



    public static void main(String[] args)
    {
        UserDAO dao = new UserDAO();
        User user = new User("Holger", "Hej123");
        Role admin = new Role("admin");
        Role userRole = new Role("user");
        dao.createRole(admin);
        dao.createRole(userRole);
        user.addRole(admin);
        User createdUser = dao.create(user);
        boolean result = createdUser.verifyPassword("Hej123");
        System.out.println(result);
        logger.info("User created: " + createdUser.getUsername());
    }

}
