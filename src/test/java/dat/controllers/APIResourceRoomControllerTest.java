package dat.controllers;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import dat.entities.Hotel;
import dat.entities.Room;
import dat.rest.Routes;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class APIResourceRoomControllerTest
{
    private static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private Hotel hotel1;
    private Hotel hotel2;
    private Room room1;
    private Room room2;


    @BeforeAll
    static void setUpAll()
    {
        Routes.setHotelController(new HotelController(emf));
        Routes.setRoomController(new RoomController(emf));

        ApplicationConfig
                .getInstance()
                .initiateServer()
                .setRoute(Routes.getRoutes())
                .handleException()
                .startServer(7777);

        RestAssured.baseURI = "http://localhost:7777/api";
    }

    @BeforeEach
    void setUp()
    {
        try(EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Room").executeUpdate();
            em.createQuery("DELETE FROM Hotel").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE room_id_seq RESTART WITH 1").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE hotel_id_seq RESTART WITH 1").executeUpdate();


            hotel1 = new Hotel();
            hotel1.setName("TestHotel1");
            em.persist(hotel1);

            hotel2 = new Hotel();
            hotel2.setName("TestHotel2");
            em.persist(hotel2);

            room1 = new Room();
            room1.setHotel(hotel1);
            em.persist(room1);

            room2 = new Room();
            room2.setHotel(hotel2);
            em.persist(room2);

            em.getTransaction().commit();

            // Ensure the hotels are correctly persisted with IDs
            System.out.println("Hotel 1 ID: " + hotel1.getId());  // Debugging line
            System.out.println("Hotel 2 ID: " + hotel2.getId());  // Debugging line

            em.clear();
        }
    }

    @AfterAll
    static void tearDown()
    {
        if (emf != null && emf.isOpen())
        {
            emf.close();
            System.out.println("EntityManagerFactory closed");
        }
        ApplicationConfig.getInstance().stopServer();
    }

    @Test
    @DisplayName("Test adding room to hotel")
    void addRoomToHotel()
    {
        RestAssured
                .given()
                .when()
                .contentType("application/json")
                .accept("application/json")
                .body(room1)
                .post("hotel/" +hotel1.getId() + "/room")
                .then()
                .statusCode(201)
                .body("name", equalTo(room1.getRoomNumber()));
    }

    @Test
    void deleteRoom()
    {
        RestAssured
                .given()
                .when()
                .delete("room/" + room1.getId())
                .then()
                .statusCode(204);
    }

    @Test
    void getRoomsForHotel()
    {
        RestAssured
                .given()
                .when()
                .get("hotel/" + hotel1.getId() + "/rooms")
                .then()
                .statusCode(200)
                .body("size()", equalTo(1));
    }
}