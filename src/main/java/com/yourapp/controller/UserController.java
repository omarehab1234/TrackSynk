package com.yourapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yourapp.dto.auth.LoginRequest;
import com.yourapp.dto.auth.LoginResponse;
import com.yourapp.dto.auth.RegisterRequest;
import com.yourapp.model.entity.User;
import com.yourapp.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(
            @Valid @RequestBody RegisterRequest request) {

        User user = userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        System.out.println("LOGIN ENDPOINT REACHED");
        System.out.println("Email received: " + request.getEmail());

        LoginResponse response =
                userService.login(request, httpRequest, httpResponse);

        System.out.println("LOGIN SUCCESSFUL");

        return ResponseEntity.ok(response);
    }
}