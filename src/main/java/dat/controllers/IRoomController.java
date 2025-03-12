package dat.controllers;

import io.javalin.http.Context;

public interface IRoomController
{
    void addRoomToHotel(Context ctx);

    void deleteRoom(Context ctx);

    void getRoomsForHotel(Context ctx);
}
