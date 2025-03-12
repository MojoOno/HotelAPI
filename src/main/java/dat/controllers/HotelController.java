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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class HotelController implements IHotelController
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
            List<Hotel> hotels = genericDAO.findAll(Hotel.class);
            List<HotelDTO> hotelDTOs = hotels.stream()
                    .map(HotelDTO::new)
                    .collect(Collectors.toList());
            ctx.json(hotelDTOs);
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
            HotelDTO hotelDTO = ctx.bodyAsClass(HotelDTO.class);
            Hotel hotel = new Hotel(hotelDTO);
            genericDAO.create(hotel);

            for (RoomDTO roomDTO : hotelDTO.getRooms()) {
                Room room = new Room(null, roomDTO.getRoomNumber(), roomDTO.getPrice(), hotel);
                genericDAO.create(room);
                roomDTO.setId(room.getId());
                roomDTO.setHotelId(hotel.getId());
            }

            hotelDTO.setId(hotel.getId());
            ctx.json(hotelDTO);
        } catch (Exception e) {
            ctx.status(400).json(Collections.singletonMap("message", "Error creating hotel"));
            e.printStackTrace();
        }
    }

    public void updateHotel(Context ctx) {
        try {
            logger.info("Starting updateHotel method");
            long id = Long.parseLong(ctx.pathParam("id"));
            Hotel existingHotel = genericDAO.read(Hotel.class, id);
            if (existingHotel == null) {
                logger.error("Hotel with id {} not found", id);
                ctx.status(404).json(new ErrorMessage("Hotel not found"));
                return;
            }

            HotelDTO hotelDTO = ctx.bodyAsClass(HotelDTO.class);
            logger.info("Parsed HotelDTO: {}", hotelDTO);

            existingHotel.setName(hotelDTO.getName());
            existingHotel.setAddress(hotelDTO.getAddress());
            logger.info("Updated Hotel entity: {}", existingHotel);

            genericDAO.update(existingHotel);
            logger.info("Updated Hotel entity in database");

            for (RoomDTO roomDTO : hotelDTO.getRooms()) {
                Room room = genericDAO.read(Room.class, roomDTO.getId());
                if (room == null) {
                    room = new Room(null, roomDTO.getRoomNumber(), roomDTO.getPrice(), existingHotel);
                    genericDAO.create(room);
                    logger.info("Created new Room entity: {}", room);
                } else {
                    room.setRoomNumber(roomDTO.getRoomNumber());
                    room.setPrice(roomDTO.getPrice());
                    room.setHotel(existingHotel);
                    genericDAO.update(room);
                    logger.info("Updated Room entity: {}", room);
                }
            }

            hotelDTO.setId(existingHotel.getId());
            ctx.json(hotelDTO);
            logger.info("Successfully updated hotel and rooms, returning response");
        } catch (Exception e) {
            logger.error("Error updating hotel", e);
            ErrorMessage error = new ErrorMessage("Error updating hotel");
            ctx.status(400).json(error);
        }
    }

    public void deleteHotel(Context ctx) {
        try {
            logger.info("Starting deleteHotel method");
            long id = Long.parseLong(ctx.pathParam("id"));
            Hotel existingHotel = genericDAO.read(Hotel.class, id);
            if (existingHotel == null) {
                logger.error("Hotel with id {} not found", id);
                ctx.status(404).json(new ErrorMessage("Hotel not found"));
                return;
            }

            // Delete all rooms associated with the hotel
            if (existingHotel.getRooms() != null)
            {
                for (Room room : existingHotel.getRooms())
                {
                    genericDAO.delete(room);
                    logger.info("Deleted Room entity with id {}", room.getId());
                }
            }

            // Delete the hotel
            genericDAO.delete(existingHotel);
            logger.info("Deleted Hotel entity with id {}", id);
            ctx.status(204).json(Collections.singletonMap("message", "Successfully deleted Hotel"));
        } catch (Exception e) {
            logger.error("Error deleting hotel", e);
            ErrorMessage error = new ErrorMessage("Error deleting hotel");
            ctx.status(400).json(error);
        }
    }

}
