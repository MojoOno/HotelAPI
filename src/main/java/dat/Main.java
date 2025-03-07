package dat;

import dat.config.HibernateConfig;
import dat.controllers.HotelController;
import dat.rest.ApplicationConfig;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;
import dat.rest.Routes;
import okhttp3.Route;

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