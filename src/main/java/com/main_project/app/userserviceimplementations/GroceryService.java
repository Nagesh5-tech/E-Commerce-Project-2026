package com.main_project.app.userserviceimplementations;

import com.main_project.app.entities.Grocery;

import com.main_project.app.userrepositories.GroceryRepository;
import com.main_project.app.userservices.GroceryContract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GroceryService implements GroceryContract{

    @Autowired
    private GroceryRepository groceryRepository;

    public List<Grocery> getAllGroceries() {
        return groceryRepository.findAll();
    }

    public List<Grocery> getVegetables() {
        return groceryRepository.findByCategoryType("Fresh Vegetables");
    }

    public List<Grocery> getFruits() {
        return groceryRepository.findByCategoryType("Fresh Fruits");
    }

    public Optional<Grocery> getGroceryById(Integer groceryId) {
        return groceryRepository.findById(groceryId);
    }

    public Optional<Grocery> getGroceryByProductId(Integer productId) {
        return groceryRepository.findByProductId(productId);
    }
}
