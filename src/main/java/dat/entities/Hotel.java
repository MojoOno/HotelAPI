package dat.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import dat.dtos.HotelDTO;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "hotel")
    @JsonManagedReference
    private List<Room> rooms = new ArrayList<>();

    public Hotel(HotelDTO hotelDTO)
    {

        this.name = hotelDTO.getName();
        this.address = hotelDTO.getAddress();
        this.rooms = hotelDTO.getRooms().stream()
                .map(roomDTO -> new Room(roomDTO, this))
                .collect(Collectors.toList());
    }

    public void addRoom(Room room)
    {
        rooms.add(room);
    }
}
