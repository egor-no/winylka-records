package winylka.service;

import winylka.model.*;
import winylka.infra.OrderStorage;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderService {

    private final ProductService products;
    private final AtomicLong seq = new AtomicLong(1000);
    private final OrderStorage storage;

    public OrderService(ProductService products, OrderStorage storage) {
        this.products = products;
        this.storage = storage;
    }

    public OrderResponse create(OrderRequest req) {
        if (req == null || req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain items");
        }
        if (req.getCustomer() == null || blank(req.getCustomer().fullName) || blank(req.getCustomer().email)) {
            throw new IllegalArgumentException("Customer fullName and email are required");
        }
        if (req.getShipping() == null || blank(req.getShipping().city) || blank(req.getShipping().address)) {
            throw new IllegalArgumentException("Shipping city and address are required");
        }

        int total = 0;
        for (OrderItemRequest it : req.getItems()) {
            if (it.getAmount() <= 0) throw new IllegalArgumentException("Invalid amount for productId=" + it.getProductId());
            var p = products.findById(it.getProductId());
            if (p == null) throw new IllegalArgumentException("Unknown productId=" + it.getProductId());
            total += p.getPrice() * it.getAmount();
        }

        storage.add(req);

        OrderResponse resp = new OrderResponse();
        resp.setOrderId(seq.incrementAndGet());
        resp.setItemsTotal(total);
        resp.setCreatedAt(Instant.now());

        return resp;
    }

    private boolean blank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
