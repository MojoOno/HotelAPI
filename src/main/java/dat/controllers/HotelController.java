package dat.controllers;

import dat.daos.GenericDAO;
import dat.dtos.ErrorMessage;
import dat.dtos.HotelDTO;
import dat.dtos.RoomDTO;
import dat.entities.Hotel;
import dat.entities.Room;
import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class HotelController implements IController
{
    private final GenericDAO genericDAO;

    private static Logger logger = LoggerFactory.getLogger(HotelController.class);
    public HotelController(EntityManagerFactory emf)
    {
        genericDAO = GenericDAO.getInstance(emf);
    }

    public void getAllHotels(Context ctx)
    {
        try
        {
            ctx.json(genericDAO.findAll(Hotel.class));
        }
        catch (Exception e)
        {
            logger.error("Error getting hotels", e);
            ErrorMessage error = new ErrorMessage("Error getting hotels");
            ctx.status(404).json(error);
        }
    }


    public void getHotelById(Context ctx)
    {
        try
        {
            long id = Long.parseLong(ctx.pathParam("id"));
            HotelDTO foundHotel = new HotelDTO(genericDAO.read(Hotel.class, id));
            ctx.json(foundHotel);
        }
        catch (Exception e)
        {
            ErrorMessage error = new ErrorMessage("No hotel with that id");
            ctx.status(404).json(error);
        }
    }

    public void createHotel(Context ctx) {
        try {
            logger.info("Received request to create hotel");

            HotelDTO incomingHotel = ctx.bodyAsClass(HotelDTO.class);
            logger.info("Parsed HotelDTO: {}", incomingHotel);

            Hotel hotel = new Hotel(incomingHotel);
            logger.info("Converted to Hotel entity: {}", hotel);

            Hotel createdHotel = genericDAO.create(hotel);
            logger.info("Created Hotel entity: {}", createdHotel);

            ctx.json(new HotelDTO(createdHotel));
            logger.info("Response sent with created HotelDTO");
        } catch (Exception e) {
            logger.error("Error creating hotel", e);
            ErrorMessage error = new ErrorMessage("Error creating hotel");
            ctx.status(400).json(error);
        }
    }

    public void updateHotel(Context ctx)
    {
        try
        {
            int id = Integer.parseInt(ctx.pathParam("id"));
            HotelDTO incomingHotel = ctx.bodyAsClass(HotelDTO.class);
            Hotel hotel = new Hotel(incomingHotel);
            hotel.setId((long) id);
            Hotel updatedHotel = genericDAO.update(hotel);
            HotelDTO returnedHotel = new HotelDTO(updatedHotel);
            ctx.json(returnedHotel);
        }
        catch (Exception e)
        {
            ErrorMessage error = new ErrorMessage("Error updating hotel");
            ctx.status(400).json(error);
        }
    }

    public void deleteHotel(Context ctx)
    {
        try
        {
            long id = Long.parseLong(ctx.pathParam("id"));
            genericDAO.delete(Hotel.class, id);
            ctx.status(204);
        }
        catch (Exception e)
        {
            ErrorMessage error = new ErrorMessage("Error deleting hotel");
            ctx.status(400).json(error);
        }
    }

    public void addRoomToHotel(Context ctx) {
        try {
            long hotelId = Long.parseLong(ctx.pathParam("id"));
            RoomDTO incomingRoomDTO = ctx.bodyAsClass(RoomDTO.class);

            // Find the existing hotel
            Hotel hotel = genericDAO.read(Hotel.class, hotelId);
            if (hotel == null) {
                ctx.status(404).json(new ErrorMessage("Hotel not found"));
                return;
            }

            // Convert DTO to Room entity and set hotel reference
            Room room = new Room(incomingRoomDTO, hotel);

            // Save the room using the generic method
            genericDAO.create(room);

            // Optional: Update hotel's room list and merge
            hotel.getRooms().add(room);
            genericDAO.update(hotel);

            ctx.status(201).json(new RoomDTO(room));
        } catch (Exception e) {
            ctx.status(400).json(new ErrorMessage("Error adding room to hotel"));
        }
    }


    public void deleteRoom(Context ctx) {
        try {
            long hotelId = Long.parseLong(ctx.pathParam("id"));
            long roomId = Long.parseLong(ctx.pathParam("id"));

            // Find the hotel and room
            Hotel hotel = genericDAO.read(Hotel.class, hotelId);
            Room room = genericDAO.read(Room.class, roomId);

            if (hotel == null) {
                ctx.status(404).json(new ErrorMessage("Hotel not found"));
                return;
            }
            if (room == null || !room.getHotel().getId().equals(hotelId)) {
                ctx.status(404).json(new ErrorMessage("Room not found in this hotel"));
                return;
            }

            // Remove the room from the hotel and delete
            hotel.getRooms().remove(room);
            genericDAO.update(hotel);
            genericDAO.delete(Room.class, roomId);

            ctx.status(204); // No content
        } catch (Exception e) {
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
