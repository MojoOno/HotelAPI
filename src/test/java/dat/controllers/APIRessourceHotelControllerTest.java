package dat.controllers;

import dat.config.ApplicationConfig;
import dat.config.HibernateConfig;
import dat.controllers.security.SecurityController;
import dat.entities.Hotel;
import dat.rest.Routes;
import io.restassured.RestAssured;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.*;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.*;


class APIRessourceHotelControllerTest
{
    private static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private Hotel hotel1;
    private Hotel hotel2;

    @BeforeAll
    static void setUpAll()
    {
        Routes.setHotelController(new HotelController(emf));
        Routes.setRoomController(new RoomController(emf));
        Routes.setSecurityController(new SecurityController(emf));

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
        try (EntityManager em = emf.createEntityManager())
        {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Room").executeUpdate();
            em.createQuery("DELETE FROM Hotel").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE room_id_seq RESTART WITH 1").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE hotel_id_seq RESTART WITH 1").executeUpdate();
            hotel1 = new Hotel();
            hotel2 = new Hotel();
            hotel1.setName("TestHotel1");
            hotel2.setName("TestHotel2");
            em.persist(hotel1);
            em.persist(hotel2);
            em.getTransaction().commit();

            // Ensure the hotels are correctly persisted with IDs
            System.out.println("Hotel 1 ID: " + hotel1.getId());  // Debugging line
            System.out.println("Hotel 2 ID: " + hotel2.getId());  // Debugging line

        }
    }

//    @AfterAll
//    static void tearDownAll()
//    {
//        if (emf != null && emf.isOpen())
//        {
//            emf.close();
//            System.out.println("EntityManagerFactory closed");
//        }
//        ApplicationConfig.getInstance().stopServer();
//    }


    @Test
    @DisplayName("Test getting all hotels")
    void getAllHotels()
    {
        RestAssured
                .given()
                .when()
                .get("/hotel")
                .then()
                .statusCode(200)
                .body("size()", is(2));
    }

    @Test
    @DisplayName("Test getting hotel by id")
    void getHotelById()
    {
        RestAssured
                .given()
                .when()
                .get("/hotel/{id}", hotel1.getId())
                .then()
                .statusCode(200)
                .body("name", equalTo(hotel1.getName()));
    }

    @Test
    @DisplayName("Test creating a new hotel")
    void createHotel()
    {
        Hotel hotel3 = new Hotel();
        hotel3.setName("TestHotel3");
        RestAssured
                .given()
                .when()
                .contentType("application/json")
                .accept("application/json")
                .body(hotel3)
                .post("/hotel")
                .then()
                .statusCode(200)
                .body("name", equalTo(hotel3.getName()));
    }

    @Test
    @DisplayName("Test updating an existing hotel")
    void updateHotel()
    {
        hotel1.setName("UpdatedHotel1");
        RestAssured
                .given()
                .when()
                .contentType("application/json")
                .accept("application/json")
                .body(hotel1)
                .put("/hotel/{id}", hotel1.getId())
                .then()
                .statusCode(200)
                .body("name", equalTo(hotel1.getName()));
    }

    @Test
    void deleteHotel()
    {
        RestAssured
                .given()
                .when()
                .delete("/hotel/{id}", hotel1.getId())
                .then()
                .statusCode(204);
    }
}