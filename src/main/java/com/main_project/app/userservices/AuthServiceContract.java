package com.main_project.app.userservices;

import com.main_project.app.entities.User;

public interface AuthServiceContract {
	public User authenticate(String username, String password);
	public String generateToken(User user);

}
