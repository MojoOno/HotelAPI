package dat.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dat.entities.Hotel;
import dat.entities.Room;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelDTO
{
    private Long id;
    private String name;
    private String address;
    private List<Room> rooms;

    public HotelDTO(Hotel hotel)
    {
        this.id = hotel.getId();
        this.name = hotel.getName();
        this.address = hotel.getAddress();
        this.rooms = hotel.getRooms();
    }
}
