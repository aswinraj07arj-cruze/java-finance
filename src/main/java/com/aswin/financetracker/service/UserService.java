package com.aswin.financetracker.service;

import com.aswin.financetracker.model.User;
import com.aswin.financetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User getDefaultUser() {
        return userRepository.findByUsername("default_user")
            .orElseGet(() -> userRepository.save(new User("default_user", "aswin@example.com")));
    }
}
