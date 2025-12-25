package winylka.api;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import winylka.model.CartLineRequest;
import winylka.model.CartResponse;
import winylka.service.CartService;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin
public class CartController {

    private final CartService cart;

    public CartController(CartService cart) {
        this.cart = cart;
    }

    @GetMapping
    public CartResponse get(HttpSession session) {
        return cart.getCart(session);
    }

    @PostMapping("/items")
    public ResponseEntity<?> add(@RequestBody CartLineRequest req, HttpSession session) {
        try {
            CartResponse resp = cart.add(session, req.getProductId(), req.getAmount());
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiError("BAD_REQUEST", e.getMessage()));
        }
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<?> setAmount(@PathVariable int productId,
                                       @RequestBody CartLineRequest req,
                                       HttpSession session) {
        try {
            CartResponse resp = cart.setAmount(session, productId, req.getAmount());
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiError("BAD_REQUEST", e.getMessage()));
        }
    }

    @PutMapping
    public ResponseEntity<?> replace(@RequestBody List<CartLineRequest> lines, HttpSession session) {
        try {
            CartResponse resp = cart.replace(session, lines);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ApiError("BAD_REQUEST", e.getMessage()));
        }
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse remove(@PathVariable int productId, HttpSession session) {
        return cart.remove(session, productId);
    }

    @DeleteMapping
    public ResponseEntity<Void> clear(HttpSession session) {
        cart.clear(session);
        return ResponseEntity.noContent().build();
    }

    public record ApiError(String code, String message) {}
}
