package com.soufyan.userservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.soufyan.userservice.dto.DeleteUserDto;
import com.soufyan.userservice.dto.UpdateUserDto;
import com.soufyan.userservice.dto.UserDto;
import com.soufyan.userservice.mapper.UserMapper;
import com.soufyan.userservice.model.User;
import com.soufyan.userservice.service.UserService;
import com.soufyan.userservice.config.JwtUtil;
import com.soufyan.userservice.config.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@RestController
@RequestMapping("/user")
@Validated
public class UserController {
    @Autowired
    UserService userService;

    @Autowired 
    JwtUtil jwtUtil;

    @PutMapping("/update")
    public ResponseEntity<User> update(@Valid @RequestBody UpdateUserDto updateDto){
        long userId = SecurityUtils.getCurrentUserId();
        User updatedUser = userService.updateUser(userId, updateDto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/")
    public ResponseEntity<UserDto> get(){
        long userId = SecurityUtils.getCurrentUserId();
        User user = userService.loadUserById(userId);
        return ResponseEntity.ok(UserMapper.toDto(user));
    }

    @GetMapping("/all")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserDto> users = userService.getAllUsers(pageable).map(UserMapper::toDto);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@Valid @RequestBody DeleteUserDto deleteDto){
        long userId = SecurityUtils.getCurrentUserId();
        userService.deleteUser(userId,deleteDto);
        return ResponseEntity.ok("User deleted");
    }
    
}
