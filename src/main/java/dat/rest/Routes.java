package dat.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import dat.controllers.HotelController;
import dat.controllers.RoomController;
import dat.controllers.security.SecurityController;
import dat.enums.Roles;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.security.RouteRole;

import static io.javalin.apibuilder.ApiBuilder.*;


public class Routes
{
    private static HotelController hotelController;
    private static RoomController roomController;
    private static SecurityController securityController;
    static ObjectMapper objectMapper = new ObjectMapper();


    public Routes(HotelController hotelController, RoomController roomController, SecurityController securityController)
    {
        Routes.hotelController = hotelController;
        Routes.roomController = roomController;
        Routes.securityController = securityController;
    }

    public static EndpointGroup getRoutes()
    {
        return () ->
        {
            path("hotel", () ->
            {
                get("/", hotelController::getAllHotels);
                get("/{id}", hotelController::getHotelById);
                post("/", hotelController::createHotel);
                put("/{id}", hotelController::updateHotel);
                delete("/{id}", hotelController::deleteHotel);

                path("/{id}/room", () ->
                {
                    post(roomController::addRoomToHotel);
                });

                path("/{id}/rooms", () ->
                {
                    get(roomController::getRoomsForHotel);
                });
            });

            path("room", () ->
            {
                delete("/{id}", roomController::deleteRoom);
            });
            path("auth", () ->
            {
                post("register", securityController.register());
                post("login", securityController.login());
            });

            path("secured", () ->
            {
//                get("demo", ctx ->
//                        ctx.json(objectMapper.createObjectNode().put("demo", "Hello: "))); //add the following Roles.USER
            });
        };
    }

    public static void setHotelController(HotelController hotelController)
    {
        Routes.hotelController = hotelController;
    }

    public static void setRoomController(RoomController roomController)
    {
        Routes.roomController = roomController;
    }

    public static void setSecurityController(SecurityController securityController)
    {
        Routes.securityController = securityController;
    }
}