package winylka.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import winylka.model.CartLineRequest;
import winylka.model.CartResponse;
import winylka.model.Product;

import java.util.*;

@Service
public class CartService {

    private static final String CART_KEY = "CART_MAP"; // Map<Integer, Integer>
    private final ProductService products;

    public CartService(ProductService products) {
        this.products = products;
    }

    public CartResponse getCart(HttpSession session) {
        Map<Integer, Integer> map = getOrCreate(session);
        return buildResponse(map);
    }

    public CartResponse add(HttpSession session, int productId, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be > 0");
        Product p = products.findById(productId);
        if (p == null) throw new IllegalArgumentException("Unknown productId=" + productId);

        Map<Integer, Integer> map = getOrCreate(session);
        map.put(productId, map.getOrDefault(productId, 0) + amount);
        return buildResponse(map);
    }

    public CartResponse setAmount(HttpSession session, int productId, int amount) {
        Product p = products.findById(productId);
        if (p == null) throw new IllegalArgumentException("Unknown productId=" + productId);

        Map<Integer, Integer> map = getOrCreate(session);
        if (amount <= 0) map.remove(productId);
        else map.put(productId, amount);

        return buildResponse(map);
    }

    public CartResponse replace(HttpSession session, List<CartLineRequest> lines) {
        Map<Integer, Integer> map = getOrCreate(session);
        map.clear();

        if (lines != null) {
            for (CartLineRequest l : lines) {
                if (l.getAmount() <= 0) continue;
                Product p = products.findById(l.getProductId());
                if (p == null) throw new IllegalArgumentException("Unknown productId=" + l.getProductId());
                map.put(l.getProductId(), l.getAmount());
            }
        }

        return buildResponse(map);
    }

    public CartResponse remove(HttpSession session, int productId) {
        Map<Integer, Integer> map = getOrCreate(session);
        map.remove(productId);
        return buildResponse(map);
    }

    public void clear(HttpSession session) {
        Map<Integer, Integer> map = getOrCreate(session);
        map.clear();
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Integer> getOrCreate(HttpSession session) {
        Object obj = session.getAttribute(CART_KEY);
        if (obj instanceof Map<?, ?>) {
            return (Map<Integer, Integer>) obj;
        }
        Map<Integer, Integer> map = new LinkedHashMap<>();
        session.setAttribute(CART_KEY, map);
        return map;
    }

    private CartResponse buildResponse(Map<Integer, Integer> map) {
        CartResponse resp = new CartResponse();

        int total = 0;
        for (Map.Entry<Integer, Integer> e : map.entrySet()) {
            Product p = products.findById(e.getKey());
            if (p == null) continue; // на всякий случай

            int amount = e.getValue();
            int lineTotal = p.getPrice() * amount;

            CartResponse.Line line = new CartResponse.Line();
            line.setItem(p);
            line.setAmount(amount);
            line.setLineTotal(lineTotal);

            resp.getLines().add(line);
            total += lineTotal;
        }

        resp.setItemsTotal(total);
        return resp;
    }
}
