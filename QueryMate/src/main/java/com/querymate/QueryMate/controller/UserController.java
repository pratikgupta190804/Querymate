package com.querymate.QueryMate.controller;

import com.querymate.QueryMate.auth.JwtService;
import com.querymate.QueryMate.dto.UserDto;
import com.querymate.QueryMate.payload.ApiResponse;
import com.querymate.QueryMate.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User APIs", description = "CRUD operations for User entity")
public class UserController {

    private final JwtService jwtService;
    private final UserService userService;

    @Autowired
    public UserController(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getLoggedInUser(HttpServletRequest request) {
        // Extract token from header
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7); // Remove "Bearer "
        String email = jwtService.extractUsername(token); // You must have this method

        UserDto userDto = userService.getUserByEmail(email); // Create this method
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieve a user’s profile information by their user ID."
    )
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Fetch a list of all users. This is usually used by admins."
    )
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{userId}")
    @Operation(
            summary = "Update user",
            description = "Update an existing user's details using their ID."
    )
    public ResponseEntity<UserDto> updateUser(@PathVariable Long userId, @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(userId, userDto);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{userId}")
    @Operation(
            summary = "Delete user",
            description = "Remove a user account using their user ID."
    )
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponse("User deleted successfully", true));
    }
}
