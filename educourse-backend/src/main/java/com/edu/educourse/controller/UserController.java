package com.edu.educourse.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.edu.educourse.dto.UserDto;
import com.edu.educourse.entity.User;
import com.edu.educourse.repository.UserRepository;

/**
 * UserController — returns info about the currently logged-in user.
 *
 * GET /api/user/me → returns UserDto with fullName, email, roles
 *   Used by frontend after login to decide routing and show nav links.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired private UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getMe(
            @AuthenticationPrincipal UserDetails userDetails) {

        // userDetails.getUsername() returns email (our principal)
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());

        return ResponseEntity.ok(
            new UserDto(user.getId(), user.getFullName(), user.getEmail(), user.getUsername(), roles)
        );
    }
}
