package com.main_project.app.userserviceimplementations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.main_project.app.entities.Order;
import com.main_project.app.entities.OrderItem;
import com.main_project.app.entities.OrderStatus;
import com.main_project.app.entities.Product;
import com.main_project.app.entities.ProductImage;
import com.main_project.app.entities.User;
import com.main_project.app.entities.cart_items;
import com.main_project.app.userrepositories.CartRepository;
import com.main_project.app.userrepositories.OrderItemRepository;
import com.main_project.app.userrepositories.OrderRepository;
import com.main_project.app.userrepositories.ProductImageRepository;
import com.main_project.app.userrepositories.ProductRepository;
import com.main_project.app.userservices.OrderServiceContract;

@Service
public class OrderService implements OrderServiceContract {

	private final OrderItemRepository orderItemRepository;
	private final ProductRepository productRepository;
	private final ProductImageRepository productImageRepository;
	private final OrderRepository orderRepository;
	private final CartRepository cartRepository;

	public OrderService(OrderItemRepository orderItemRepository, ProductRepository productRepository,
			ProductImageRepository productImageRepository, OrderRepository orderRepository,
			CartRepository cartRepository) {
		super();
		this.orderItemRepository = orderItemRepository;
		this.productRepository = productRepository;
		this.productImageRepository = productImageRepository;
		this.orderRepository = orderRepository;
		this.cartRepository = cartRepository;
	}

	/**
	 * Fetches all successful orders for a given user and returns the required response format.
	 *
	 * @param user The authenticated user object.
	 * @return A map containing the user's role, username, and ordered products.
	 */
	@Override
	public Map<String, Object> getOrdersForUser(User user) {

		// Fetch all successful order items for the user
		List<OrderItem> orderItems =
				orderItemRepository.findSuccessfulOrderItemsByUserId(user.getUserId());

		// Prepare response
		Map<String, Object> response = new HashMap<>();
		response.put("username", user.getUsername());
		response.put("role", user.getRole());

		List<Map<String, Object>> products = new ArrayList<>();

		for (OrderItem item : orderItems) {

			Product product = productRepository
					.findById(item.getProductId())
					.orElse(null);

			if (product == null) {
				continue;
			}

			// Fetch product images
			List<ProductImage> images =
					productImageRepository.findByProduct_ProductId(product.getProductId());

			String imageUrl = images.isEmpty()
					? null
					: images.get(0).getImageUrl();

			// Build product details
			Map<String, Object> productDetails = new HashMap<>();
			productDetails.put("order_id", item.getOrder().getOrderId());
			productDetails.put("quantity", item.getQuantity());
			productDetails.put("total_price", item.getTotalPrice());
			productDetails.put("image_url", imageUrl);
			productDetails.put("product_id", product.getProductId());
			productDetails.put("name", product.getName());
			productDetails.put("description", product.getDescription());
			productDetails.put("price_per_unit", item.getPricePerUnit());

			products.add(productDetails);
		}

		// Add products list to response
		response.put("products", products);

		return response;
	}

	/**
	 * Directly records an order and its order items into the database upon successful checkout.
	 * Updates the 'orders' table to SUCCESS and saves every purchased item to 'order_items'.
	 *
	 * @param user The authenticated customer
	 * @param requestBody Details including orderId, totalAmount, and items list
	 * @return Success response confirmation
	 */
	@Override
	@Transactional
	public Map<String, Object> placeOrder(User user, Map<String, Object> requestBody) {
		String orderId = (String) requestBody.get("orderId");
		if (orderId == null || orderId.trim().isEmpty()) {
			orderId = "ORD_" + System.currentTimeMillis();
		}

		BigDecimal totalAmount = BigDecimal.ZERO;
		if (requestBody.get("totalAmount") != null) {
			try {
				totalAmount = new BigDecimal(requestBody.get("totalAmount").toString());
			} catch (Exception ignored) {}
		}

		String finalOrderId = orderId;
		Order order = orderRepository.findById(finalOrderId).orElseGet(() -> {
			Order newOrd = new Order();
			newOrd.setOrderId(finalOrderId);
			newOrd.setUserId(user.getUserId());
			newOrd.setCreatedAt(LocalDateTime.now());
			return newOrd;
		});

		order.setTotalAmount(totalAmount);
		order.setStatus(OrderStatus.SUCCESS);
		order.setUpdatedAt(LocalDateTime.now());
		orderRepository.save(order);

		// Items list from request
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> itemsRaw = (List<Map<String, Object>>) requestBody.get("items");
		if (itemsRaw == null || itemsRaw.isEmpty()) {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> fallback = (List<Map<String, Object>>) requestBody.get("cartItems");
			itemsRaw = fallback;
		}

		int itemsSaved = 0;
		if (itemsRaw != null && !itemsRaw.isEmpty()) {
			for (Map<String, Object> it : itemsRaw) {
				try {
					Object pidObj = it.get("productId") != null ? it.get("productId") : it.get("product_id");
					if (pidObj == null) continue;
					int productId = Integer.parseInt(pidObj.toString());

					int quantity = 1;
					if (it.get("quantity") != null) {
						quantity = Integer.parseInt(it.get("quantity").toString());
					}

					BigDecimal pricePerUnit = BigDecimal.ZERO;
					Object priceObj = it.get("price") != null ? it.get("price") : it.get("price_per_unit");
					if (priceObj != null) {
						pricePerUnit = new BigDecimal(priceObj.toString());
					}

					BigDecimal totalPrice = pricePerUnit.multiply(BigDecimal.valueOf(quantity));

					OrderItem orderItem = new OrderItem();
					orderItem.setOrder(order);
					orderItem.setProductId(productId);
					orderItem.setQuantity(quantity);
					orderItem.setPricePerUnit(pricePerUnit);
					orderItem.setTotalPrice(totalPrice);

					orderItemRepository.save(orderItem);
					itemsSaved++;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		// If no items were explicitly provided in payload, save from user's database cart
		if (itemsSaved == 0) {
			List<cart_items> dbCart = cartRepository.findCartItemsWithProductDetails(user.getUserId());
			for (cart_items ci : dbCart) {
				try {
					OrderItem orderItem = new OrderItem();
					orderItem.setOrder(order);
					orderItem.setProductId(ci.getProduct().getProductId());
					orderItem.setQuantity(ci.getQuantity());
					orderItem.setPricePerUnit(ci.getProduct().getPrice());
					BigDecimal totalPrice = ci.getProduct().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
					orderItem.setTotalPrice(totalPrice);
					orderItemRepository.save(orderItem);
					itemsSaved++;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		// Clear user's cart in database
		try {
			cartRepository.deleteAllCartItemsByUserId(user.getUserId());
		} catch (Exception ignored) {}

		Map<String, Object> response = new HashMap<>();
		response.put("success", true);
		response.put("orderId", order.getOrderId());
		response.put("status", "SUCCESS");
		response.put("itemsSaved", itemsSaved);
		response.put("message", "Order and order items successfully recorded in database");
		return response;
	}
}
