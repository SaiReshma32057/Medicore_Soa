package com.medicore.user.service;

import com.medicore.user.dto.*;
import com.medicore.user.entity.User;
import com.medicore.user.repository.UserRepository;
import com.medicore.user.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
 private final UserRepository repository; private final PasswordEncoder encoder; private final JwtService jwt;
 public UserService(UserRepository repository,PasswordEncoder encoder,JwtService jwt){this.repository=repository;this.encoder=encoder;this.jwt=jwt;}
 public UserResponse register(RegisterRequest r){if(repository.existsByEmail(r.email()))throw new IllegalArgumentException("Email already registered");User u=repository.save(new User(r.email(),encoder.encode(r.password()),r.name()));return response(u,jwt.generate(u.getEmail(),u.getRole()));}
 public UserResponse login(LoginRequest r){User u=repository.findByEmail(r.email()).orElseThrow(()->new IllegalArgumentException("Invalid credentials"));if(!encoder.matches(r.password(),u.getPasswordHash()))throw new IllegalArgumentException("Invalid credentials");return response(u,jwt.generate(u.getEmail(),u.getRole()));}
 public UserResponse profile(String email){return response(repository.findByEmail(email).orElseThrow(()->new IllegalArgumentException("User not found")),null);}
 private UserResponse response(User u,String token){return new UserResponse(u.getId(),u.getEmail(),u.getName(),token);}
}
