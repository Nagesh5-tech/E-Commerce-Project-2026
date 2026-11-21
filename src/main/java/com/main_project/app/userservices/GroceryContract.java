package com.main_project.app.userservices;

import java.util.List;
import java.util.Optional;

import com.main_project.app.entities.Grocery;

public interface GroceryContract {
	 	public List<Grocery> getAllGroceries();
	 	
	    public List<Grocery> getVegetables();

	    public List<Grocery> getFruits();

	    public Optional<Grocery> getGroceryById(Integer groceryId);

	    public Optional<Grocery> getGroceryByProductId(Integer productId);


}
