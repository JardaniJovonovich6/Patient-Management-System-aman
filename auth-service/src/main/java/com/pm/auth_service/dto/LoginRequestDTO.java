package com.pm.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequestDTO{
    @NotBlank(message="Email is Mandatory")
    @Email(message="Enter a valid Email")
    private String email;

    @NotBlank(message="Password is mandatory")
    @Size(min=8,message="Password must be atleast 8 characters Long...")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


}