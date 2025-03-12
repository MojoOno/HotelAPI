package dat;

import dat.config.HibernateConfig;
import dat.controllers.HotelController;
import dat.config.ApplicationConfig;
import jakarta.persistence.EntityManagerFactory;
import dat.rest.Routes;

public class Main {
    final static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    final static HotelController controller = new HotelController(emf);

    public static void main(String[] args) {
        Routes.setController(controller);

        ApplicationConfig
                .getInstance()
                .initiateServer()
                .setRoute(Routes.getRoutes())
                .handleException()
                .startServer(7070);

    }
}