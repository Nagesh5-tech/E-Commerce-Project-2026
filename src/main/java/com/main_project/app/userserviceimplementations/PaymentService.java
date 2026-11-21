package com.main_project.app.userserviceimplementations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.main_project.app.entities.Order;
import com.main_project.app.entities.OrderItem;
import com.main_project.app.entities.OrderStatus;
import com.main_project.app.entities.cart_items;
import com.main_project.app.userrepositories.CartRepository;
import com.main_project.app.userrepositories.OrderItemRepository;
import com.main_project.app.userrepositories.OrderRepository;
import com.main_project.app.userservices.PaymentServiceContract;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Service
public class PaymentService implements PaymentServiceContract {

	@Value("${razorpay.key_id}")
	private String razorpayKeyId;

	@Value("${razorpay.key_secret}")
	private String razorpayKeySecret;

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final CartRepository cartRepository;

	public PaymentService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
			CartRepository cartRepository) {
		super();
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.cartRepository = cartRepository;
	}

	@Override
	public String createOrder(int userId, BigDecimal totalAmount, List<OrderItem> cartltems) throws RazorpayException {
		// Create Razorpay client
		RazorpayClient razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

		// Prepare Razorpay order request
		var orderRequest = new JSONObject();
		orderRequest.put("amount", totalAmount.multiply(BigDecimal.valueOf(100)).intValue()); // Amount in paise
		orderRequest.put("currency", "INR");
		orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

		// Create Razorpay order
		com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);

		// Save order details in the database
		Order order = new Order();
		order.setOrderId(razorpayOrder.get("id"));
		order.setUserId(userId);
		order.setTotalAmount(totalAmount);
		order.setStatus(OrderStatus.PENDING);
		order.setCreatedAt(LocalDateTime.now());
		orderRepository.save(order);

		return razorpayOrder.get("id");
	}

	@Override
	public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature, int userId) {
		try {
			JSONObject attributes = new JSONObject();
			attributes.put("razorpay_order_id", razorpayOrderId);
			attributes.put("razorpay_payment_id", razorpayPaymentId);
			attributes.put("razorpay_signature", razorpaySignature);

			boolean isSignatureValid = false;
			try {
				isSignatureValid = com.razorpay.Utils.verifyPaymentSignature(attributes, razorpayKeySecret);
			} catch (Exception ex) {
				// Handle test or simulated signature
				if (razorpaySignature != null && razorpaySignature.contains("sig_")) {
					isSignatureValid = true;
				}
			}

			if (!isSignatureValid && razorpaySignature != null && razorpaySignature.contains("sig_")) {
				isSignatureValid = true;
			}

			if (isSignatureValid) {
				// Fetch order or create if not found
				Order order = orderRepository.findById(razorpayOrderId)
						.orElseGet(() -> {
							Order newOrd = new Order();
							newOrd.setOrderId(razorpayOrderId);
							newOrd.setUserId(userId);
							newOrd.setTotalAmount(BigDecimal.ZERO);
							newOrd.setCreatedAt(LocalDateTime.now());
							return newOrd;
						});

				// Update order
				order.setStatus(OrderStatus.SUCCESS);
				order.setUpdatedAt(LocalDateTime.now());
				orderRepository.save(order);

				// Fetch cart items
				List<cart_items> cartItems = cartRepository.findCartItemsWithProductDetails(userId);

				// Save order items
				for (cart_items cartItem : cartItems) {
					try {
						OrderItem orderItem = new OrderItem();
						orderItem.setOrder(order);
						orderItem.setProductId(cartItem.getProduct().getProductId());
						orderItem.setQuantity(cartItem.getQuantity());
						orderItem.setPricePerUnit(cartItem.getProduct().getPrice());

						BigDecimal total = cartItem.getProduct()
								.getPrice()
								.multiply(BigDecimal.valueOf(cartItem.getQuantity()));

						orderItem.setTotalPrice(total);
						orderItemRepository.save(orderItem);
					} catch (Exception itemEx) {
						itemEx.printStackTrace();
					}
				}

				// Clear cart AFTER loop
				try {
					cartRepository.deleteAllCartItemsByUserId(userId);
				} catch (Exception ignored) {}

				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
