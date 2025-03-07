package dat.entities;

import dat.dtos.HotelDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Data
public class Hotel
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String address;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "hotel")
    private List<Room> rooms = new ArrayList<>();

    public Hotel(HotelDTO hotelDTO)
    {
        this.name = hotelDTO.getName();
        this.address = hotelDTO.getAddress();
        this.rooms = hotelDTO.getRooms();
    }
}
