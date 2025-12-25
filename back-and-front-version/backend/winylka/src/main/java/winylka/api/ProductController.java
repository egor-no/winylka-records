package winylka.api;

import winylka.model.Product;
import winylka.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin // потом можно заменить на нормальный CORS config
public class ProductController {

    private final ProductService products;

    public ProductController(ProductService products) {
        this.products = products;
    }

    @GetMapping
    public List<Product> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset
    ) {
        return products.findAll(q, maxPrice, format, sort, limit, offset);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> one(@PathVariable int id) {
        Product p = products.findById(id);
        return (p == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(p);
    }
}

