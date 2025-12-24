package com.soufyan.userservice.service;

import com.soufyan.userservice.exception.UnauthorizedException;
import com.soufyan.userservice.exception.UserNotFoundException;
import com.soufyan.userservice.exception.ValidationException;
import com.soufyan.userservice.dto.DeleteUserDto;
import com.soufyan.userservice.dto.LoginUserDto;
import com.soufyan.userservice.dto.RegisterUserDto;
import com.soufyan.userservice.dto.UpdateUserDto;
import com.soufyan.userservice.enums.Role;
import com.soufyan.userservice.model.User;
import com.soufyan.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private RegisterUserDto registerDto;
    private LoginUserDto loginDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john@example.com");
        testUser.setPhone("+1234567890");
        testUser.setPassword("encodedPassword");
        testUser.setCountry("USA");
        testUser.setCity("New York");
        testUser.setStreet("123 Main St");
        testUser.setRole(Role.USER);

        registerDto = new RegisterUserDto("Jane", "Smith", "jane@example.com", "+9876543210", "password123", "Canada", "Toronto", "456 Oak Ave", Role.USER);

        loginDto = new LoginUserDto("john@example.com", "password123");
    }

    @Test
    void loadUserByUsername_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        UserDetails result = userService.loadUserByUsername("john@example.com");

        assertNotNull(result);
        assertEquals("john@example.com", result.getUsername());
    }

    @Test
    void loadUserByUsername_NotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, 
            () -> userService.loadUserByUsername("invalid@example.com"));
    }

    @Test
    void authenticateUser_Success() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        User result = userService.authenticateUser(loginDto);

        assertNotNull(result);
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void authenticateUser_InvalidEmail_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, 
            () -> userService.authenticateUser(loginDto));
    }

    @Test
    void authenticateUser_InvalidPassword_ThrowsException() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, 
            () -> userService.authenticateUser(loginDto));
    }

    @Test
    void registerUser_Success() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.registerUser(registerDto);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_EmailExists_ThrowsException() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(ValidationException.class, 
            () -> userService.registerUser(registerDto));
    }

    @Test
    void updateUser_Success() {
        UpdateUserDto updateDto = new UpdateUserDto("UpdatedName", "Doe", "john@example.com", "+1111111111", "USA", "New York", "123 Main St");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser(1L, updateDto);

        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_NotFound_ThrowsException() {
        UpdateUserDto updateDto = new UpdateUserDto("Test", "User", "test@example.com", "+1234567890", "USA", "NYC", "Street");
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, 
            () -> userService.updateUser(999L, updateDto));
    }

    @Test
    void updateUser_EmailExists_ThrowsException() {
        UpdateUserDto updateDto = new UpdateUserDto("Test", "User", "existing@example.com", "+1234567890", "USA", "NYC", "Street");

        User existingUser = new User();
        existingUser.setId(2L);
        existingUser.setEmail("existing@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        assertThrows(ValidationException.class, 
            () -> userService.updateUser(1L, updateDto));
    }

    @Test
    void loadUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.loadUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void loadUserById_NotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, 
            () -> userService.loadUserById(999L));
    }

    @Test
    void getAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(Arrays.asList(testUser));
        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<User> result = userService.getAllUsers(pageable);

        assertEquals(1, result.getTotalElements());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void deleteUser_Success() {
        DeleteUserDto deleteDto = new DeleteUserDto("password123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        User result = userService.deleteUser(1L, deleteDto);

        assertNotNull(result);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_NotFound_ThrowsException() {
        DeleteUserDto deleteDto = new DeleteUserDto("password123");

        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, 
            () -> userService.deleteUser(999L, deleteDto));
    }

    @Test
    void deleteUser_InvalidPassword_ThrowsException() {
        DeleteUserDto deleteDto = new DeleteUserDto("wrongPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, 
            () -> userService.deleteUser(1L, deleteDto));
    }
}
