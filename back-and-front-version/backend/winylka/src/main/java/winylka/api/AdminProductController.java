package winylka.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import winylka.dto.ProductForm;
import winylka.dto.StockAdditionRequest;
import winylka.model.Product;
import winylka.service.ProductService;

@RestController
@RequestMapping("/api/admin/products")
@CrossOrigin
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Product> create(
            @ModelAttribute ProductForm form
    ) {
        Product created = productService.create(form);

        return ResponseEntity.ok(created);
    }

    @PutMapping(
            value = "/{id}",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<Product> update(
            @PathVariable int id,
            @ModelAttribute ProductForm form
    ) {
        Product updated = productService.update(id, form);

        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Product> addStock(
            @PathVariable int id,
            @RequestBody StockAdditionRequest request
    ) {
        if (request.getQuantity() == null) {
            throw new IllegalArgumentException("Не указано количество");
        }

        Product updated = productService.addStock(
                id,
                request.getQuantity()
        );

        return ResponseEntity.ok(updated);
    }
}