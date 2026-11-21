package com.main_project.AdminServiceContracts;



import com.main_project.app.entities.User;

import jakarta.transaction.Transactional;

public interface AdminUserServiceContract {
	
	@Transactional
	public User modifyUser(Integer userId, String username, String email, String role);
	public User getUserById(Integer userId);

}
