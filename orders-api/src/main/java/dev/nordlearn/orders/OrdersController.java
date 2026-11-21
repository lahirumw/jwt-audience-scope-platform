package dev.nordlearn.orders;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrdersController {

    @GetMapping("/api/orders")
    public List<Map<String, Object>> orders() {
        return List.of(
                Map.of("id", 101, "item", "Laptop", "status", "PROCESSING"),
                Map.of("id", 102, "item", "Keyboard", "status", "SHIPPED"));
    }

    @PostMapping("/api/orders")
    public Map<String, String> createOrder() {
        return Map.of("message", "Order created (demo endpoint)");
    }
}
