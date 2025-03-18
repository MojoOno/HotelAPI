package dat.controllers;

import dat.daos.GenericDAO;
import dat.dtos.ErrorMessage;
import dat.dtos.RoomDTO;
import dat.entities.Hotel;
import dat.entities.Room;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RoomController implements IRoomController
{
    private final GenericDAO genericDAO;

    private static Logger logger = LoggerFactory.getLogger(RoomController.class);

    public RoomController(EntityManagerFactory emf)
    {
        genericDAO = GenericDAO.getInstance(emf);
    }

    public void addRoomToHotel(Context ctx) {
        try {
            logger.info("Starting addRoomToHotel method");
            long hotelId = Long.parseLong(ctx.pathParam("id"));
            RoomDTO incomingRoomDTO = ctx.bodyAsClass(RoomDTO.class);

            // Find the existing hotel
            Hotel hotel = genericDAO.read(Hotel.class, hotelId);
            if (hotel == null) {
                logger.error("Hotel with id {} not found", hotelId);
                ctx.status(404).json(new ErrorMessage("Hotel not found"));
                return;
            }

            // Convert DTO to Room entity and set hotel reference
            Room room = new Room(incomingRoomDTO, hotel);

            // Save the room using the generic method
            genericDAO.create(room);
            logger.info("Created Room entity with id {}", room.getId());

            // Optional: Update hotel's room list and merge
            hotel.getRooms().add(room);
            genericDAO.update(hotel);
            logger.info("Updated Hotel entity with id {}", hotelId);

            ctx.status(201).json(new RoomDTO(room));
        } catch (Exception e) {
            logger.error("Error adding room to hotel", e);
            ctx.status(400).json(new ErrorMessage("Error adding room to hotel"));
        }
    }


    public void deleteRoom(Context ctx) {
        try {
            logger.info("Starting deleteRoom method");

            long roomId = Long.parseLong(ctx.pathParam("id"));
            logger.info("Room ID: {}", roomId);

            // Find the hotel and room
            Room room = genericDAO.read(Room.class, roomId);
            if (room == null) {
                logger.error("Room with id {} not found", roomId);
                ctx.status(404).json(new ErrorMessage("Room not found"));
                return;
            }
            long hotelId = room.getHotel().getId();
            logger.info("Hotel ID: {}", hotelId);
            Hotel hotel = genericDAO.read(Hotel.class, hotelId);

            if (hotel == null) {
                logger.error("Hotel with id {} not found", hotelId);
                ctx.status(404).json(new ErrorMessage("Hotel not found"));
                return;
            }
            if (!room.getHotel().getId().equals(hotelId)) {
                logger.error("Room with id {} not found in hotel with id {}", roomId, hotelId);
                ctx.status(404).json(new ErrorMessage("Room not found in this hotel"));
                return;
            }

            // Remove the room from the hotel and delete
            hotel.getRooms().remove(room);
            genericDAO.update(hotel);
            genericDAO.delete(Room.class, roomId);
            logger.info("Deleted room with id {} from hotel with id {}", roomId, hotelId);

            ctx.status(204); // No content
        } catch (Exception e) {
            logger.error("Error deleting room", e);
            ctx.status(400).json(new ErrorMessage("Error deleting room"));
        }
    }


    public void getRoomsForHotel(Context ctx) {
        try {
            long hotelId = Long.parseLong(ctx.pathParam("id"));

            // Find the hotel
            Hotel hotel = genericDAO.read(Hotel.class, hotelId);
            if (hotel == null) {
                ctx.status(404).json(new ErrorMessage("Hotel not found"));
                return;
            }

            // Convert Rooms to DTOs and return
            List<RoomDTO> roomDTOs = hotel.getRooms().stream()
                    .map(RoomDTO::new)
                    .toList();
            ctx.json(roomDTOs);
        } catch (Exception e) {
            ctx.status(400).json(new ErrorMessage("Error fetching rooms for hotel"));
        }
    }

}
