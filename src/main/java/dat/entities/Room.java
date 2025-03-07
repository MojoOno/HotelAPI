package dat.entities;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "room_number")
    private String roomNumber;
    private double price;

    @ManyToOne
    @ToString.Exclude
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    public Room(RoomDTO roomDTO, Hotel hotel)
    {
        this.roomNumber = roomDTO.getRoomNumber();
        this.price = roomDTO.getPrice();
        this.hotel = hotel;
    }
}
