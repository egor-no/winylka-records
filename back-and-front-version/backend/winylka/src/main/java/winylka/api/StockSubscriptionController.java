package winylka.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import winylka.dto.StockSubscriptionRequest;
import winylka.dto.StockSubscriptionResponse;
import winylka.service.StockSubscriptionService;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class StockSubscriptionController {

    private final StockSubscriptionService subscriptionService;

    public StockSubscriptionController(StockSubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/{productId}/stock-subscriptions")
    public ResponseEntity<StockSubscriptionResponse> subscribe(
            @PathVariable int productId,
            @RequestBody StockSubscriptionRequest request
    ) {
        StockSubscriptionResponse response =
                subscriptionService.subscribe(
                        productId,
                        request.getEmail()
                );

        return ResponseEntity.ok(response);
    }
}