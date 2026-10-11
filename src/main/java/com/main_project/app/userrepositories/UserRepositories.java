package com.main_project.app.userrepositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.main_project.app.entities.User;

public interface UserRepositories extends JpaRepository<User, Integer>{
	Optional<User> findByUsername(String username);
	Optional<User> findByEmail(String email);

}
