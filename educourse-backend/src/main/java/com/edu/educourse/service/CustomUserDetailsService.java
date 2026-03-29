package com.edu.educourse.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.edu.educourse.entity.User;
import com.edu.educourse.repository.UserRepository;

/**
 * CustomUserDetailsService — Loads User by EMAIL for Spring Security.
 *
 * Spring Security calls loadUserByUsername(identifier) passing whatever
 * value was used as the "username" in the authentication token.
 * Since we now login by email, we pass email as the identifier
 * everywhere, and this service looks up by email.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Load user by email (used as the Spring Security "username").
     * Called during:
     *   1. Login — AuthenticationManager.authenticate()
     *   2. JWT filter — on every protected request
     *
     * @param email the user's email address
     * @throws UsernameNotFoundException if no user has this email
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No account found with email: " + email));
        return new CustomUserDetails(user);
    }
}
