package com.main_project.app.userserviceimplementations;
import java.util.*;
import java.lang.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.main_project.app.entities.Role;
import com.main_project.app.entities.User;
import com.main_project.app.userrepositories.UserRepositories;
import com.main_project.app.userservices.UserServiesContract;
@Service
public class UserServices implements UserServiesContract{
	
	private final UserRepositories userRepositories;
	private final BCryptPasswordEncoder passwordEncoder;

	
	@Autowired
	public UserServices(UserRepositories userRepositories) {
		super();
		this.userRepositories = userRepositories;
		this.passwordEncoder =new  BCryptPasswordEncoder();
	}



	@Override
	public User register(User user) {
	    if(userRepositories.findByUsername(user.getUsername()).isPresent()) {
	        throw new RuntimeException("Username is already taken");	
	    }
	    
	    if(userRepositories.findByEmail(user.getEmail()).isPresent()) {
	        throw new RuntimeException("Email is already taken");	
	    }
	    
	    if (user.getRole() == null) {
	        user.setRole(Role.CUSTOMER);
	    }
	    user.setPassword(passwordEncoder.encode(user.getPassword()));
	    return userRepositories.save(user);
	}

}
