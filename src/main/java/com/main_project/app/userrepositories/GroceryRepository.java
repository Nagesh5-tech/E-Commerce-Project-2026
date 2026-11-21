package com.main_project.app.userrepositories;

import com.main_project.app.entities.Grocery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroceryRepository extends JpaRepository<Grocery, Integer> {
    List<Grocery> findByCategoryType(String categoryType);
    Optional<Grocery> findByProductId(Integer productId);
}
