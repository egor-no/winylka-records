package winylka.infra;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import winylka.model.Product;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class JsonProductRepository {

    private final ObjectMapper mapper = new ObjectMapper();

    public List<Product> loadAll() {
        try {
            ClassPathResource res = new ClassPathResource("data/products.json");
            try (InputStream is = res.getInputStream()) {
                return mapper.readValue(is, new TypeReference<List<Product>>() {});
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load data/products.json", e);
        }
    }
}
