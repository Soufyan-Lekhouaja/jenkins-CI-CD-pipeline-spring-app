package com.soufyan.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AccountUserDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String country;
    private String city;
    private String street;
    private String phone;

    
}
