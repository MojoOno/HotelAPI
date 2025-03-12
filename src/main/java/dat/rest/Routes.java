package dat.rest;

import dat.controllers.HotelController;
import dat.controllers.RoomController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;


public class Routes
{
    private static HotelController hotelController;
    private static RoomController roomController;

    public Routes(HotelController hotelController, RoomController roomController)
    {
        Routes.hotelController = hotelController;
        Routes.roomController = roomController;
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
}