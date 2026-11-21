package com.main_project.AdminServiceImplementations;


import org.springframework.stereotype.Service;

import com.main_project.AdminServiceContracts.AdminProductServiceContract;
import com.main_project.AdminServiceContracts.AdminUserServiceContract;
import com.main_project.app.entities.Product;
import com.main_project.app.entities.Role;
import com.main_project.app.entities.User;
import com.main_project.app.userrepositories.JWTTokenRepository;
import com.main_project.app.userrepositories.UserRepositories;

import jakarta.transaction.Transactional;
@Service
public class AdminUserService implements AdminUserServiceContract{
	
	private final UserRepositories userRepository;
	private final JWTTokenRepository jwtTokenRepository;
	
	
	public AdminUserService(UserRepositories userRepository, JWTTokenRepository jwtTokenRepository) {
		super();
		this.userRepository = userRepository;
		this.jwtTokenRepository = jwtTokenRepository;
	}


	@Transactional
	public User modifyUser(Integer userId, String username, String email, String role) {

	    // Check if the user exists
	    User existingUser = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

	    // Update username
	    if (username != null && !username.isEmpty()) {
	        existingUser.setUsername(username);
	    }

	    // Update email
	    if (email != null && !email.isEmpty()) {
	        existingUser.setEmail(email);
	    }

	    // Update role
	    if (role != null && !role.isEmpty()) {
	        try {
	            existingUser.setRole(Role.valueOf(role.toUpperCase()));
	        } catch (IllegalArgumentException e) {
	            throw new IllegalArgumentException("Invalid role: " + role);
	        }
	    }

	    // Delete associated JWT tokens (force re-login after role change)
	    jwtTokenRepository.deleteByUserId(userId);
	    // OR (better if you have relation):
	    // jwtTokenRepository.deleteByUser(existingUser);

	    // Save updated user
	    return userRepository.save(existingUser);
	}
	
	public User getUserById(Integer userId) {
	    return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
	}


	

}
