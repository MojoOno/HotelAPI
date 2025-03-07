package dat.services;

import dat.daos.GenericDAO;
import jakarta.persistence.EntityManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import dat.config.HibernateConfig;
import dat.entities.Poem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.FileReader;
import java.util.List;

public class ReadPoemsFromJson
{
    public static void main(String[] args)
    {
        try (FileReader fileReader = new FileReader("src/main/resources/poems.json"))
        {
            // Read poems from json file
            ObjectMapper objectMapper = new ObjectMapper();
            PoemJsonWrapper poemJsonWrapper = objectMapper.readValue(fileReader, PoemJsonWrapper.class);
            List<Poem> poems = poemJsonWrapper.getPoems();
            EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
            GenericDAO genericDao = GenericDAO.getInstance(emf);
            genericDao.create(poems);
            emf.close();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class PoemJsonWrapper
    {
        private List<Poem> poems;
    }
}
