package com.soufyan.userservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soufyan.userservice.dto.DeleteUserDto;
import com.soufyan.userservice.dto.LoginUserDto;
import com.soufyan.userservice.dto.RegisterUserDto;
import com.soufyan.userservice.dto.UpdateUserDto;
import com.soufyan.userservice.exception.UnauthorizedException;
import com.soufyan.userservice.exception.UserNotFoundException;
import com.soufyan.userservice.exception.ValidationException;
import com.soufyan.userservice.mapper.UserMapper;
import com.soufyan.userservice.model.User;
import com.soufyan.userservice.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user;
    }

    public User authenticateUser(LoginUserDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        return user;
    }
    @Transactional
    public User registerUser(RegisterUserDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ValidationException("Email already exists");
        }
        
        User user = UserMapper.registerDtoToEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(long userId, UpdateUserDto updateUserDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        // Check email uniqueness if being updated
        if (updateUserDto.getEmail() != null && !updateUserDto.getEmail().equals(user.getEmail())) {
            userRepository.findByEmail(updateUserDto.getEmail()).ifPresent(existingUser -> {
                if (existingUser.getId() != userId) {
                    throw new ValidationException("Email already exists");
                }
            });
        }
        
        UserMapper.updateUserFromDto(updateUserDto, user);
        return userRepository.save(user);
    }
    
    public User loadUserById(long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public User deleteUser(long userId, DeleteUserDto deleteUserDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));
        
        if (!passwordEncoder.matches(deleteUserDto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid password");
        }
        
        userRepository.delete(user);
        return user;
    }
}
