package winylka.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import winylka.dto.AdminOrderDetailsResponse;
import winylka.dto.AdminOrderListItemResponse;
import winylka.dto.ShipOrderRequest;
import winylka.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<AdminOrderListItemResponse>> findAll() {
        return ResponseEntity.ok(
                orderService.findAllForAdmin()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminOrderDetailsResponse> findById(
            @PathVariable long id
    ) {
        return ResponseEntity.ok(
                orderService.findAdminOrderById(id)
        );
    }

    @PatchMapping("/{id}/ship")
    public ResponseEntity<?> ship(
            @PathVariable long id,
            @RequestBody(required = false) ShipOrderRequest request
    ) {
        try {
            String trackingNumber = request == null
                    ? null
                    : request.getTrackingNumber();

            return ResponseEntity.ok(
                    orderService.ship(id, trackingNumber)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new OrderController.ApiError(
                            "ORDER_ALREADY_SHIPPED",
                            e.getMessage()
                    ));
        }
    }
}