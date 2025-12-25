package winylka.infra;

import winylka.model.OrderItemRequest;
import winylka.model.OrderRequest;
import winylka.service.ProductService;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrderStorage {

    private final List<OrderRequest> orders = new ArrayList<>();
    private final ProductService products;

    public OrderStorage(ProductService products) {
        this.products = products;
    }

    public synchronized void add(OrderRequest order) {
        orders.add(order);
        printAll();
    }

    private void printAll() {
        System.out.println();
        System.out.println("========= ALL ORDERS =========");

        int i = 1;
        for (OrderRequest order : orders) {
            System.out.println("Order #" + i++);
            printOrder(order);
            System.out.println("------------------------------");
        }

        System.out.println("========= END =========");
        System.out.println();
    }

    private void printOrder(OrderRequest order) {
        System.out.println("Customer:");
        System.out.println("  Name : " + order.getCustomer().fullName);
        System.out.println("  Email: " + order.getCustomer().email);
        System.out.println("  Phone: " + order.getCustomer().phone);

        System.out.println("Shipping:");
        System.out.println("  City   : " + order.getShipping().city);
        System.out.println("  Address: " + order.getShipping().address);
        System.out.println("  Postal : " + order.getShipping().postalCode);

        System.out.println("Items:");
        for (OrderItemRequest item : order.getItems()) {
            var product = products.findById(item.getProductId());
            String title = product != null
                    ? product.getArtist() + " — " + product.getName()
                    : "UNKNOWN PRODUCT (id=" + item.getProductId() + ")";

            System.out.println(
                    "  - " + title +
                            " | qty=" + item.getAmount()
            );
        }

        if (order.getComment() != null && !order.getComment().isBlank()) {
            System.out.println("Comment: " + order.getComment());
        }
    }
}
