package com.genealogy.service;

import com.genealogy.entity.User;
import com.genealogy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        String username = principal.getName();
        if (username == null || username.isEmpty()) {
            return null;
        }
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElse(null);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
