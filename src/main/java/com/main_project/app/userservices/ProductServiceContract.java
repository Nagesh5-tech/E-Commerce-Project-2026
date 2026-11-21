package com.main_project.app.userservices;

import java.util.List;

import com.main_project.app.entities.Product;

public interface ProductServiceContract {

    public List<Product> getProductsByCategory(String categoryName);

    public List<String> getProductImages(Integer productId);
}
