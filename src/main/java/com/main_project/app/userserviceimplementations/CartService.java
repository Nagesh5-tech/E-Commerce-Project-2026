package com.main_project.app.userserviceimplementations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.main_project.app.entities.cart_items;
import com.main_project.app.entities.Product;
import com.main_project.app.entities.ProductImage;
import com.main_project.app.entities.User;
import com.main_project.app.userrepositories.CartRepository;
import com.main_project.app.userrepositories.ProductImageRepository;
import com.main_project.app.userrepositories.ProductRepository;
import com.main_project.app.userrepositories.UserRepositories;
import com.main_project.app.userservices.CartContract;
@Service
public class CartService implements CartContract {
	@Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepositories userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

	public CartService(ProductRepository productRepository, CartRepository cartRepository,
			ProductImageRepository productImageRepository, UserRepositories userRepository) {
		super();
		this.productRepository = productRepository;
		this.cartRepository = cartRepository;
		this.productImageRepository = productImageRepository;
		this.userRepository = userRepository;
	}

	public void addToCart(User user, int productId, int quantity) {

	    Product product = productRepository.findById(productId)
	            .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

	    // Fetch cart item for this userId and productId
	    Optional<cart_items> existingItem = cartRepository.findByUserAndProduct(user.getUserId(), productId);

	    if (existingItem.isPresent()) {
	        cart_items cartItem = existingItem.get();
	        cartItem.setQuantity(cartItem.getQuantity() + quantity);
	        cartRepository.save(cartItem);
	    } else {
	        cart_items newItem = new cart_items(user, product, quantity);
	        cartRepository.save(newItem);
	    }
	}
	
	// Get Cart Items for a User
    public Map<String, Object> getCartItems(User authenticatedUser) {
        // Fetch the cart items for the user with product details
        List<cart_items> cartItems = cartRepository.findCartItemsWithProductDetails(authenticatedUser.getUserId());

        // Create a response map to hold the cart details
        Map<String, Object> response = new HashMap<>();


        response.put("username", authenticatedUser.getUsername());
        response.put("role", authenticatedUser.getRole().toString());

        // List to hold the product details
        List<Map<String, Object>> products = new ArrayList<>();
        int overallTotalPrice = 0;

        for (cart_items cartItem : cartItems) {
            Map<String, Object> productDetails = new HashMap<>();

            // Get product details
            Product product = cartItem.getProduct();

            // Fetch product images
            List<ProductImage> productImages = productImageRepository.findByProduct_ProductId(product.getProductId());
            String imageUrl = (productImages != null && !productImages.isEmpty())
                    ? productImages.get(0).getImageUrl()
                    : "default-image-url";

            // Populate product details
            productDetails.put("product_id", product.getProductId());
            productDetails.put("image_url", imageUrl);
            productDetails.put("name", product.getName());
            productDetails.put("description", product.getDescription());
            productDetails.put("price_per_unit", product.getPrice());
            productDetails.put("quantity", cartItem.getQuantity());
            productDetails.put("total_price", cartItem.getQuantity() * product.getPrice().doubleValue());

            // Add to products list
            products.add(productDetails);

            // Update overall total price
            overallTotalPrice += cartItem.getQuantity() * product.getPrice().doubleValue();
        }

        // Prepare the final cart response
        Map<String, Object> cart = new HashMap<>();
        cart.put("products", products);
        cart.put("overall_total_price", overallTotalPrice);

        response.put("cart", cart);

        return response;
    }
    
    public void updateCartItemQuantity(User authenticatedUser, int productId, int quantity) {
    	userRepository.findById(authenticatedUser.getUserId()).orElseThrow(()-> new RuntimeException("user not found"));
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Fetch cart item for this userId and productId
        Optional<cart_items> existingItem = cartRepository.findByUserAndProduct(authenticatedUser.getUserId(), productId);

        if (existingItem.isPresent()) {
        	cart_items cartItem = existingItem.get();
            if (quantity == 0) {
            	deleteCartItem(authenticatedUser.getUserId(), productId);
            } else {
                cartItem.setQuantity(quantity);
                cartRepository.save(cartItem);
            }
        }
    }
    
    
    public void deleteCartItem(int userid, int productId) {
        

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        cartRepository.deleteCartItem(userid, productId);
    }

	@Override
	public int getCartItemsCount(int userid) {
		// TODO Auto-generated method stub
		return cartRepository.countTotalItems(userid);
	}
    

    
   
	

}
