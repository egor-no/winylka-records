package winylka.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import winylka.dto.CartLineRequest;
import winylka.dto.CartResponse;
import winylka.model.Product;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {

    private static final String CART_KEY = "CART_MAP";

    private final ProductService products;

    public CartService(ProductService products) {
        this.products = products;
    }

    public CartResponse getCart(HttpSession session) {
        Map<Integer, Integer> map = getOrCreate(session);

        normalizeCart(map);

        return buildResponse(map);
    }

    public CartResponse add(
            HttpSession session,
            int productId,
            int amount
    ) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        Product product = requireProduct(productId);

        Map<Integer, Integer> map = getOrCreate(session);

        int currentAmount = map.getOrDefault(productId, 0);
        int newAmount = currentAmount + amount;

        validateStock(product, newAmount);

        map.put(productId, newAmount);

        return buildResponse(map);
    }

    public CartResponse setAmount(
            HttpSession session,
            int productId,
            int amount
    ) {
        Product product = requireProduct(productId);

        Map<Integer, Integer> map = getOrCreate(session);

        if (amount <= 0) {
            map.remove(productId);
        } else {
            validateStock(product, amount);
            map.put(productId, amount);
        }

        return buildResponse(map);
    }

    public CartResponse replace(
            HttpSession session,
            List<CartLineRequest> lines
    ) {
        /*
         * Сначала собираем и проверяем новую корзину отдельно.
         * Старую корзину не трогаем, пока вся проверка не прошла.
         */
        Map<Integer, Integer> replacement = new LinkedHashMap<>();

        if (lines != null) {
            for (CartLineRequest line : lines) {
                if (line == null || line.getAmount() <= 0) {
                    continue;
                }

                Product product = requireProduct(line.getProductId());

                int combinedAmount = replacement.getOrDefault(
                        line.getProductId(),
                        0
                ) + line.getAmount();

                validateStock(product, combinedAmount);

                replacement.put(
                        line.getProductId(),
                        combinedAmount
                );
            }
        }

        Map<Integer, Integer> map = getOrCreate(session);
        map.clear();
        map.putAll(replacement);

        return buildResponse(map);
    }

    public CartResponse remove(
            HttpSession session,
            int productId
    ) {
        Map<Integer, Integer> map = getOrCreate(session);
        map.remove(productId);

        return buildResponse(map);
    }

    public void clear(HttpSession session) {
        Map<Integer, Integer> map = getOrCreate(session);
        map.clear();
    }

    private Product requireProduct(int productId) {
        Product product = products.findById(productId);

        if (product == null) {
            throw new IllegalArgumentException(
                    "Unknown productId=" + productId
            );
        }

        return product;
    }

    private void validateStock(
            Product product,
            int requestedAmount
    ) {
        int availableStock = stockOf(product);

        if (availableStock <= 0) {
            throw new IllegalArgumentException(
                    "\"" + product.getArtist()
                            + " — " + product.getName()
                            + "\" is out of stock"
            );
        }

        if (requestedAmount > availableStock) {
            throw new IllegalArgumentException(
                    "Only " + availableStock
                            + " copies of \""
                            + product.getArtist()
                            + " — " + product.getName()
                            + "\" are available"
            );
        }
    }

    private int stockOf(Product product) {
        Integer stockQuantity = product.getStockQuantity();

        return stockQuantity == null
                ? 0
                : Math.max(stockQuantity, 0);
    }

    private void normalizeCart(Map<Integer, Integer> map) {
        Iterator<Map.Entry<Integer, Integer>> iterator =
                map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Integer, Integer> entry = iterator.next();

            Product product = products.findById(entry.getKey());

            if (product == null) {
                iterator.remove();
                continue;
            }

            int stock = stockOf(product);

            if (stock <= 0) {
                iterator.remove();
                continue;
            }

            int amount = entry.getValue() == null
                    ? 0
                    : entry.getValue();

            if (amount <= 0) {
                iterator.remove();
                continue;
            }

            if (amount > stock) {
                entry.setValue(stock);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, Integer> getOrCreate(HttpSession session) {
        Object object = session.getAttribute(CART_KEY);

        if (object instanceof Map<?, ?>) {
            return (Map<Integer, Integer>) object;
        }

        Map<Integer, Integer> map = new LinkedHashMap<>();

        session.setAttribute(CART_KEY, map);

        return map;
    }

    private CartResponse buildResponse(
            Map<Integer, Integer> map
    ) {
        CartResponse response = new CartResponse();

        BigDecimal total = BigDecimal.ZERO;

        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            Product product = products.findById(entry.getKey());

            if (product == null) {
                continue;
            }

            int amount = entry.getValue();

            BigDecimal lineTotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(amount));

            CartResponse.Line line = new CartResponse.Line();
            line.setItem(product);
            line.setAmount(amount);
            line.setLineTotal(lineTotal);

            response.getLines().add(line);

            total = total.add(lineTotal);
        }

        response.setItemsTotal(total);

        return response;
    }
}