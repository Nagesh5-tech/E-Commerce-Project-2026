package com.main_project.app.userrepositories;

import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.main_project.app.entities.cart_items;

@Repository
public interface CartRepository extends JpaRepository<cart_items, Integer>{

	@Query("SELECT c FROM cart_items c WHERE c.user.userId = :userId AND c.product.productId = :productId")
    Optional<cart_items> findByUserAndProduct(@Param("userId") int userId, @Param("productId") int productId);
    
    @Query("SELECT c FROM cart_items c JOIN FETCH c.product p LEFT JOIN FETCH ProductImage pi ON p.productId = pi.product.productId WHERE c.user.userId = :userId")
    List<cart_items> findCartItemsWithProductDetails(@Param("userId") int userId);
    
    @Modifying
    @Transactional
    @Query("UPDATE cart_items c SET c.quantity = :quantity WHERE c.id = :cartItemId")
    void updateCartItemQuantity(@Param("cartItemId") int cartItemId, @Param("quantity") int quantity);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM cart_items c WHERE c.user.userId = :userId AND c.product.productId = :productId")
    void deleteCartItem(@Param("userId") int userId, @Param("productId") int productId);
    
    
 // Count the total quantity of items in the cart
 	@Query("SELECT COALESCE(SUM(c.quantity), 0) FROM cart_items c WHERE c.user. userId = :userId")
 	int countTotalItems(int userId);
 	
 	@Modifying
    @Transactional
    @Query("DELETE FROM cart_items c WHERE c.user.userId = :userId")
    void deleteAllCartItemsByUserId(@Param("userId") int userId);
    
    
}
