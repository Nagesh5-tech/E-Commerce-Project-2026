package com.main_project.app.userservices;

import java.util.Map;


import com.main_project.app.entities.User;

public interface OrderServiceContract {
	public Map<String, Object> getOrdersForUser(User user);		// Fetch all successful order items for the user
	public Map<String, Object> placeOrder(User user, Map<String, Object> requestBody); // Save order & order items to DB
}
