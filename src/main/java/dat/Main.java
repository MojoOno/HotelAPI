package dat;

import dat.config.HibernateConfig;
import dat.controllers.HotelController;
import dat.config.ApplicationConfig;
import dat.controllers.RoomController;
import dat.controllers.security.SecurityController;
import dat.daos.security.SecurityDAO;
import dat.enums.Roles;
import dat.services.ReadHotelsFromJson;
import jakarta.persistence.EntityManagerFactory;
import dat.rest.Routes;

public class Main {
    final static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    final static HotelController hotelController = new HotelController(emf);
    final static RoomController roomController = new RoomController(emf);
    final static SecurityController securityController = new SecurityController(emf);

    static SecurityDAO securityDAO = SecurityDAO.getInstance(emf);

    public static void main(String[] args) {
        Routes.setHotelController(hotelController);
        Routes.setRoomController(roomController);
        Routes.setSecurityController(securityController);

        //------- Create roles in the database ------- //
        for (Roles role : Roles.values()) {
            securityDAO.createRole(role.name());
        }

        //------- Create a user in the database ------- //
        ReadHotelsFromJson.main(args);


        ApplicationConfig
                .getInstance()
                .initiateServer()
                .checkSecurityRoles()
                .setRoute(Routes.getRoutes())
                .handleException()
                .startServer(7070);

    }
}