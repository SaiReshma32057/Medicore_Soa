package com.medicore.user.controller;
import com.medicore.user.dto.UserResponse; import com.medicore.user.service.UserService; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/users") public class UserController {private final UserService service; public UserController(UserService service){this.service=service;} @GetMapping("/me") public UserResponse me(Authentication authentication){return service.profile(authentication.getName());}}
