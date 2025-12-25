package winylka.api;

import winylka.model.OrderRequest;
import winylka.model.OrderResponse;
import winylka.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody OrderRequest req) {
        try {
            OrderResponse resp = orders.create(req);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiError("BAD_REQUEST", e.getMessage()));
        }
    }

    public record ApiError(String code, String message) {}
}

