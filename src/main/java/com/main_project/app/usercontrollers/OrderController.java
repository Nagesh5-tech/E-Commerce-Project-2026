package com.main_project.app.usercontrollers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.main_project.app.entities.User;
import com.main_project.app.userservices.OrderServiceContract;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:5174", allowCredentials = "true") // Allow cross-origin requests
@RequestMapping("/api/orders")
public class OrderController {
	
	private final OrderServiceContract orderService;

	public OrderController(OrderServiceContract orderService) {
		super();
		this.orderService = orderService;
	}
	
	/**
	 * Fetches all successful orders for the authenticated user.
	 *
	 * @param request HttpServletRequest containing the authenticated user details.
	 * @return A ResponseEntity containing the user's role, username, and their orders.
	 */
	@GetMapping
	public ResponseEntity<Map<String, Object>> getOrdersForUser(HttpServletRequest request) {
	    try {
	        // Retrieve the authenticated user from the request
	        User authenticatedUser = (User) request.getAttribute("authenticatedUser");

	        // Handle unauthenticated requests
	        if (authenticatedUser == null) {
	            return ResponseEntity
	                    .status(401)
	                    .body(Map.of("error", "User not authenticated"));
	        }

	        // Fetch orders for the user via the service layer
	        Map<String, Object> response =
	                orderService.getOrdersForUser(authenticatedUser);

	        // Return success response
	        return ResponseEntity.ok(response);

	    } catch (IllegalArgumentException e) {
	        return ResponseEntity
	                .status(400)
	                .body(Map.of("error", e.getMessage()));

	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity
	                .status(500)
	                .body(Map.of("error", "An unexpected error occurred"));
	    }
	}

	/**
	 * Records an order and its items into the database upon successful payment / checkout.
	 *
	 * @param requestBody JSON map with orderId, totalAmount, and items list
	 * @param request HttpServletRequest containing the authenticated user details.
	 * @return ResponseEntity confirmation with orderId and SUCCESS status
	 */
	@PostMapping
	public ResponseEntity<Map<String, Object>> placeOrder(
			@RequestBody Map<String, Object> requestBody,
			HttpServletRequest request) {
	    try {
	        User authenticatedUser = (User) request.getAttribute("authenticatedUser");

	        if (authenticatedUser == null) {
	            return ResponseEntity
	                    .status(401)
	                    .body(Map.of("error", "User not authenticated"));
	        }

	        Map<String, Object> response =
	                orderService.placeOrder(authenticatedUser, requestBody);

	        return ResponseEntity.ok(response);

	    } catch (Exception e) {
	        e.printStackTrace();
	        return ResponseEntity
	                .status(500)
	                .body(Map.of("error", "Failed to save order: " + e.getMessage()));
	    }
	}
}
