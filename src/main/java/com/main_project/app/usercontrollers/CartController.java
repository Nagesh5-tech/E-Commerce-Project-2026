package com.main_project.app.usercontrollers;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.main_project.app.entities.User;
import com.main_project.app.userserviceimplementations.CartService;

import jakarta.servlet.http.HttpServletRequest;
@RestController
@CrossOrigin(origins = "http://localhost:5174", allowCredentials = "true")
@RequestMapping("/api/cart")
public class CartController {
	public CartService cartService;
	
	
	public CartController(CartService cartService) {
		super();
		this.cartService = cartService;
	}


	@PostMapping("/add")
	@CrossOrigin(origins = "http://localhost:5174", allowCredentials = "true")
	public ResponseEntity<Void> addToCart(@RequestBody Map<String, Object> request,HttpServletRequest req) {
		
		User user=(User) req.getAttribute("authenticatedUser");
		
		String username=(String) request.get("username");
	    int productId = (int) request.get("productId");

	    // Handle quantity: Default to 1 if not provided
	    int quantity = request.containsKey("quantity") ? (int) request.get("quantity") : 1;

	    
	    // Add the product to the cart
	    cartService.addToCart(user, productId, quantity);
	    return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	// Fetch all cart items for the user (based on username)
	@GetMapping("/items")
	public ResponseEntity<Map<String, Object>> getCartItems(HttpServletRequest request) {
	    // Fetch user by username to get the userId
	    User user = (User) request.getAttribute("authenticatedUser");

	    // Call the service to get cart items for the user
	    Map<String, Object> cartItems = cartService.getCartItems(user);
	    return ResponseEntity.ok(cartItems);
	}
	
	@PutMapping("/update")
	public ResponseEntity<Void> updateCartItemQuantity(@RequestBody Map<String, Object> request,HttpServletRequest req) {
	    String username = (String) request.get("username");
	    int productId = ((Number) request.get("productId")).intValue();
	    int quantity = ((Number) request.get("quantity")).intValue();
	 // Fetch user by username to get the userId
	    User user = (User) req.getAttribute("authenticatedUser");

	    cartService.updateCartItemQuantity(user, productId, quantity);
	    return ResponseEntity.ok().build();
	}
	
	
	@DeleteMapping("/delete")
	public ResponseEntity<Void> deleteCartItem(@RequestBody Map<String, Object> request,HttpServletRequest req) {
	    String username = (String) request.get("username");
	    int productId = (int) request.get("productId");
	    
	    User user = (User) req.getAttribute("authenticatedUser");

	    // Delete the cart item
	    cartService.deleteCartItem(user.getUserId(), productId);
	    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
	@GetMapping("/items/count")
	public ResponseEntity<Integer> cartCount(@RequestParam String username,HttpServletRequest request) {
		
		User user=(User) request.getAttribute("authenticatedUser");
		
		int count=cartService.getCartItemsCount(user.getUserId());
		return ResponseEntity.ok(count);
	}
	
	

}
