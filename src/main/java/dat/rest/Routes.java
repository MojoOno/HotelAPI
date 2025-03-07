package dat.rest;

import dat.controllers.HotelController;
import io.javalin.apibuilder.EndpointGroup;
import lombok.Data;

import static io.javalin.apibuilder.ApiBuilder.*;


public class Routes {
    private static HotelController controller;

    public Routes(HotelController controller) {
        Routes.controller = controller;
    }

    public static EndpointGroup getRoutes() {
        return () -> {
            path("hotel", () -> {
                get("/", controller::getAllHotels);
                get("/{id}", controller::getHotelById);
                post("/", controller::createHotel);
                put("/{id}", controller::updateHotel);
                delete("/{id}", controller::deleteHotel);
            });
            path("room", () -> {
                post("/", controller::addRoomToHotel);
                delete("/{id}", controller::deleteRoom);
            });
            path("hotel/{id}/rooms", () -> {
                get(controller::getRoomsForHotel);
            });
        };
    }

    public static void setController(HotelController controller)
    {
        Routes.controller = controller;
    }
}