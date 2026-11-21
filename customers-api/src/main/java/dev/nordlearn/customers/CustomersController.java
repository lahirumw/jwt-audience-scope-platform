package dev.nordlearn.customers;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomersController {

    @GetMapping("/api/customers")
    public List<Map<String, Object>> customers() {
        return List.of(
                Map.of("id", 1, "name", "Alex Morgan"),
                Map.of("id", 2, "name", "Sam Taylor"));
    }

    @PostMapping("/api/customers")
    public Map<String, String> createCustomer() {
        return Map.of("message", "Customer created (demo endpoint)");
    }
}
