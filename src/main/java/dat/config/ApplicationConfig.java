package dat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.config.JavalinConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.javalin.apibuilder.ApiBuilder.path;

public class ApplicationConfig
{
    private static ApplicationConfig applicationConfig;
    private static Javalin app;
    private static JavalinConfig javalinConfig;
    private static ObjectMapper objectMapper = new ObjectMapper();

    private Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);
    private ApplicationConfig()
    {
    }

    public static ApplicationConfig getInstance(){
        if(applicationConfig == null){
            applicationConfig = new ApplicationConfig();
        }
        return applicationConfig;
    }


    public ApplicationConfig initiateServer()
    {
        app = Javalin.create(config -> {
            javalinConfig = config;
            config.http.defaultContentType = "application/json";
            config.router.contextPath = "/api";
            config.bundledPlugins.enableRouteOverview("/routes");
            config.bundledPlugins.enableDevLogging();
        });
        return applicationConfig;
    }

    public ApplicationConfig setRoute(EndpointGroup route)
    {
        javalinConfig.router.apiBuilder(()->{
            path("/", route);
        });
        return applicationConfig;
    }

    public ApplicationConfig startServer(int port)
    {
        logger.info("Starting server on port {}", port);
        app.start(port);
        return applicationConfig;
    }

    public ApplicationConfig handleException()
    {
        app.exception(Exception.class, (exception, ctx) ->{
            ObjectNode node = objectMapper.createObjectNode();
            node.put("msg", exception.getMessage());
            ctx.status(500);
            ctx.json(node);
        });
        return applicationConfig;
    }

    public static void stopServer()
    {
        app.stop();
        app = null;
    }
}
