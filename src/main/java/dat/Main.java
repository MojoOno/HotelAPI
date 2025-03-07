package dat;

import dat.config.HibernateConfig;
import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;

public class Main
{
    final static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();

    //final static SomeController cotrollerName = new SomeController(emf);

    public static void main(String[] args)
    {
//        Javalin.create(config ->{
//            config.router.contextPath = "/api";
//            config.router.apiBuilder(() ->{
//                // path("hotel", () ->{
//                //     get("/", controllerName::getAll);
//                //     get("/{id}", controllerName::getById);
//                //     post("/", controllerName::create);
//                //     put("/{id}", controllerName::update);
//                //     delete("/{id}", controllerName::delete);
//                // });
//
//                // path("room", () ->{
//                //     get("/", controllerName::getAll);
//                //     get("/{id}", controllerName::getById);
//                //     post("/", controllerName::create);
//                //     put("/{id}", controllerName::update);
//                //     delete("/{id}", controllerName::delete);
//                // });
//            });
//        }).start(7779);

    }
}