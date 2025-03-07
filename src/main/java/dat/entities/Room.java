package dat.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import dat.dtos.RoomDTO;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@Data
public class Room
{

    @Id
    private Long id;
    @Column(name = "room_number")
    private String roomNumber;
    private double price;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    @JsonBackReference
    private Hotel hotel;

    public Room(RoomDTO roomDTO, Hotel hotel)
    {
        this.id = roomDTO.getId();
        this.roomNumber = roomDTO.getRoomNumber();
        this.price = roomDTO.getPrice();
        this.hotel = hotel;
    }
}
