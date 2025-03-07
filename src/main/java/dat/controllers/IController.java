package dat.controllers;

import io.javalin.http.Context;

public interface IController
{
    void getAllHotels(Context ctx);

    void getHotelById(Context ctx);

    void createHotel(Context ctx);

    void updateHotel(Context ctx);

    void deleteHotel(Context ctx);

    void addRoomToHotel(Context ctx);

    void deleteRoom(Context ctx);

    void getRoomsForHotel(Context ctx);
}