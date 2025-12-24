package com.soufyan.userservice.controller;

import org.springframework.web.bind.annotation.RestController;

import com.soufyan.userservice.dto.LoginResponseDto;
import com.soufyan.userservice.dto.LoginUserDto;
import com.soufyan.userservice.dto.RegisterUserDto;
import com.soufyan.userservice.dto.UserDto;
import com.soufyan.userservice.mapper.UserMapper;
import com.soufyan.userservice.model.User;
import com.soufyan.userservice.service.UserService;
import com.soufyan.userservice.config.JwtUtil;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
    private  final UserService userService;
    private  final JwtUtil jwtUtil;
    @Value("${jwt.expiration}")
    private long expiration;

    public AuthenticationController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginUserDto loginDto){
        User user = userService.authenticateUser(loginDto);
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole().toString());
        return ResponseEntity.ok(new LoginResponseDto(token,expiration));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterUserDto registerDto){
        User user = userService.registerUser(registerDto);
        return ResponseEntity.ok(UserMapper.toDto(user));
    }
    
}
