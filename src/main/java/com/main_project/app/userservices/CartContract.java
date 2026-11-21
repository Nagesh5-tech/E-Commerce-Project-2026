package com.main_project.app.userservices;

import java.util.Map;

import com.main_project.app.entities.User;

public interface CartContract {
	
	public void addToCart(User user,int productid,int quantity);
	
	public Map<String, Object> getCartItems(User authenticatedUser);
	
	public void updateCartItemQuantity(User authenticatedUser, int productId, int quantity);
	
	public void deleteCartItem(int userid, int productId);
	
	public int getCartItemsCount(int userid);

}
