package com.greetingsapp.imagesapi.services;

import com.greetingsapp.imagesapi.domain.users.User;
import com.greetingsapp.imagesapi.domain.users.UserMapper;
import com.greetingsapp.imagesapi.dto.users.CreateUserDTO;
import com.greetingsapp.imagesapi.dto.users.UserResponseDTO;
import com.greetingsapp.imagesapi.infra.errors.DuplicateResourceException;
import com.greetingsapp.imagesapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public UserResponseDTO createUser(CreateUserDTO createUserDTO) {

        // Lógica de negocio: Verificar si el usuario ya existe
        if (userRepository.findByUsername(createUserDTO.username()).isPresent()) {
            throw new DuplicateResourceException("Username " + createUserDTO.username() + " already exists.");
        }

        User newUser = new User();
        newUser.setUsername(createUserDTO.username());
        // Lógica de negocio: Encriptar la contraseña antes de guardarla
        newUser.setPassword(passwordEncoder.encode(createUserDTO.password()));
        newUser.setRole(createUserDTO.role());

        User savedUser = userRepository.save(newUser);

        return userMapper.userToUserResponseDTO(savedUser);
    }
}

