package com.mhconsultingbe.auth.service;

import com.mhconsultingbe.auth.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {
    private final AdminRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var admin = repository.findByEmailIgnoreCase(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
        return User.withUsername(admin.getEmail())
                .password(admin.getPasswordHash())
                .roles(admin.getRole().name())
                .disabled(!admin.isActive())
                .build();
    }
}
