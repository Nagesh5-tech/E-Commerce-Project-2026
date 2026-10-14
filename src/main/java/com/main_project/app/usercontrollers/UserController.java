package com.main_project.app.usercontrollers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.main_project.app.entities.User;
import com.main_project.app.userserviceimplementations.UserServices;

@RestController
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500", "http://localhost:5174", "http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
@RequestMapping("/api")
public class UserController {
	
	public UserServices userSerivces;
	
	
	public UserController(UserServices userSerivces) {
		super();
		this.userSerivces = userSerivces;
	}

	@PostMapping("/users")
	public ResponseEntity<?> registerUser(@RequestBody User user){
		try {
			User registeredUser=userSerivces.register(user);
			return ResponseEntity.ok(Map.of("message","User registered Successfully","user",registeredUser));
		}catch (RuntimeException e) {
			// TODO: handle exception
			return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));
		}
	}

}
