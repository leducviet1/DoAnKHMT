package com.example.librarymanage_be.service.Impl;

import com.example.librarymanage_be.dto.request.AdminUpdateUserRequest;
import com.example.librarymanage_be.dto.request.SelfUpdateUserRequest;
import com.example.librarymanage_be.dto.response.SelfUpdateResponse;
import com.example.librarymanage_be.dto.response.UserResponse;
import com.example.librarymanage_be.entity.Role;
import com.example.librarymanage_be.entity.User;
import com.example.librarymanage_be.repo.RoleRepository;
import com.example.librarymanage_be.repo.UserRepository;
import com.example.librarymanage_be.service.JWTService;
import com.example.librarymanage_be.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    @Override
    public User create(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Integer id) {
        return getUserEntityById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserResponseById(Integer id) {
        return toResponse(getUserEntityById(id));
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Not found user with email:" + email));
    }

    @Override
    public User update(User user) {
        return null;
    }

    @Override
    public void delete(Integer id) {

    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        return toResponse(getUserEntityByEmail(email));
    }

    @Override
    public SelfUpdateResponse updateCurrentUser(String currentEmail, SelfUpdateUserRequest request) {
        User user = getUserEntityByEmail(currentEmail);

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String normalizedEmail = normalizeEmail(request.getEmail());
            ensureEmailAvailable(normalizedEmail, user.getUserId());
            user.setEmail(normalizedEmail);
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser.getEmail());
        return new SelfUpdateResponse(toResponse(savedUser), token, "Bearer");
    }

    @Override
    public UserResponse updateByAdmin(Integer id, AdminUpdateUserRequest request) {
        User user = getUserEntityById(id);

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String normalizedEmail = normalizeEmail(request.getEmail());
            ensureEmailAvailable(normalizedEmail, user.getUserId());
            user.setEmail(normalizedEmail);
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(resolveRoles(request.getRoles()));
        }

        return toResponse(userRepository.save(user));
    }

    private User getUserEntityById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found user with id:" + id));
    }

    private User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Not found user with email:" + email));
    }

    private void ensureEmailAvailable(String email, Integer currentUserId) {
        userRepository.findByEmail(email)
                .filter(existingUser -> !existingUser.getUserId().equals(currentUserId))
                .ifPresent(existingUser -> {
                    throw new RuntimeException("Email already exists");
                });
    }

    private Set<Role> resolveRoles(Set<String> roleNames) {
        return roleNames.stream()
                .filter(roleName -> roleName != null && !roleName.isBlank())
                .map(this::normalizeRoleName)
                .map(this::findOrCreateRole)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Role findOrCreateRole(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName(roleName);
                    return roleRepository.save(role);
                });
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRoleName(String roleName) {
        return roleName.trim().toUpperCase(Locale.ROOT);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAddress(),
                user.getCreatedAt(),
                user.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toCollection(LinkedHashSet::new))
        );
    }

}
