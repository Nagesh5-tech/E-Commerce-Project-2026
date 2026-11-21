package com.main_project.app.usercontrollers;

import com.main_project.app.entities.Grocery;
import com.main_project.app.userserviceimplementations.GroceryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5174", allowCredentials = "true")
@RequestMapping("/api/groceries")
public class GroceryController {

    @Autowired
    private GroceryService groceryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllGroceries() {
        List<Grocery> list = groceryService.getAllGroceries();
        Map<String, Object> response = new HashMap<>();
        response.put("category", "Groceries");
        response.put("count", list.size());
        response.put("groceries", list);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/vegetables")
    public ResponseEntity<Map<String, Object>> getVegetables() {
        List<Grocery> list = groceryService.getVegetables();
        Map<String, Object> response = new HashMap<>();
        response.put("category", "Vegetables");
        response.put("count", list.size());
        response.put("vegetables", list);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fruits")
    public ResponseEntity<Map<String, Object>> getFruits() {
        List<Grocery> list = groceryService.getFruits();
        Map<String, Object> response = new HashMap<>();
        response.put("category", "Fruits");
        response.put("count", list.size());
        response.put("fruits", list);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getGroceryById(@PathVariable("id") Integer id) {
        Optional<Grocery> item = groceryService.getGroceryById(id);
        if (item.isPresent()) {
            return ResponseEntity.ok(item.get());
        }
        return ResponseEntity.status(404).body(Map.of("error", "Grocery item not found"));
    }
}
