package dat;

import dat.config.HibernateConfig;
import dat.controllers.HotelController;
import dat.config.ApplicationConfig;
import dat.controllers.RoomController;
import dat.controllers.security.SecurityController;
import jakarta.persistence.EntityManagerFactory;
import dat.rest.Routes;

public class Main {
    final static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    final static HotelController hotelController = new HotelController(emf);
    final static RoomController roomController = new RoomController(emf);
    final static SecurityController securityController = new SecurityController(emf);

    public static void main(String[] args) {
        Routes.setHotelController(hotelController);
        Routes.setRoomController(roomController);
        Routes.setSecurityController(securityController);

        ApplicationConfig
                .getInstance()
                .initiateServer()
                .checkSecurityRoles()
                .setRoute(Routes.getRoutes())
                .handleException()
                .startServer(7070);

    }
}