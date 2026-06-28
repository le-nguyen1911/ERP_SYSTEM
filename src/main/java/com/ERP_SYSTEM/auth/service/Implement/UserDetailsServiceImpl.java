package com.ERP_SYSTEM.auth.service.Implement;

import com.ERP_SYSTEM.auth.entity.User;
import com.ERP_SYSTEM.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();

        user.getRoles().forEach(role -> {

            authorities.add(new SimpleGrantedAuthority(
                    "ROLE_" + role.getName()
            ));

            role.getPermissions().forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(
                            permission.getName()
                    ))
            );
        });
        System.out.println("===== AUTHORITIES =====");
        authorities.forEach(System.out::println);
        System.out.println("=======================");
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .disabled(!Boolean.TRUE.equals(user.getEnabled()))
                .build();
    }
}
