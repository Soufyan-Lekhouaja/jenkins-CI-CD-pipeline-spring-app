package com.soufyan.userservice.mapper;

import org.springframework.stereotype.Component;

import com.soufyan.userservice.dto.RegisterUserDto;
import com.soufyan.userservice.dto.UpdateUserDto;
import com.soufyan.userservice.dto.UserDto;
import com.soufyan.userservice.model.User;

@Component
public class UserMapper {
    
    public static void updateUserFromDto(UpdateUserDto dto, User user) {
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
        if (dto.getCountry() != null) user.setCountry(dto.getCountry());
        if (dto.getCity() != null) user.setCity(dto.getCity());
        if (dto.getStreet() != null) user.setStreet(dto.getStreet());
    }

    public static User registerDtoToEntity(RegisterUserDto dto) {
        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setCountry(dto.getCountry());
        user.setCity(dto.getCity());
        user.setStreet(dto.getStreet());
        user.setRole(dto.getRole());
        return user;
    }

    public static UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setCountry(user.getCountry());
        dto.setCity(user.getCity());
        dto.setStreet(user.getStreet());
        dto.setRole(user.getRole());
        return dto;
    }
}
