package com.greetingsapp.imagesapi.services;

import com.greetingsapp.imagesapi.domain.users.Role;
import com.greetingsapp.imagesapi.domain.users.User;
import com.greetingsapp.imagesapi.domain.users.UserMapper;
import com.greetingsapp.imagesapi.dto.users.CreateUserDTO;
import com.greetingsapp.imagesapi.dto.users.UserResponseDTO;
import com.greetingsapp.imagesapi.infra.errors.DuplicateResourceException;
import com.greetingsapp.imagesapi.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService; //aqui se injectan los mocks(simulaciones) declaradas arriba

    @Test
    void shouldCreateUser_whenUsernameIsUnique() {
        // --- ARRANGE (Preparar el escenario) ---

        // 1. Datos de entrada.
        CreateUserDTO createDTO = new CreateUserDTO(
                "newUser",
                "password123",
                Role.ADMIN);

        // 2. Simula la entidad que se guardará (sin ID todavía).
        User newUser = new User();
        newUser.setUsername(createDTO.username());
        newUser.setPassword("encrypted_password");
        newUser.setRole(createDTO.role());

        // 3. Simula la entidad después de ser guardada (con ID).
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(newUser.getUsername());
        savedUser.setPassword(newUser.getPassword());
        savedUser.setRole(newUser.getRole());

        // 4. El DTO de respuesta que esperamos que el mapper devuelva.
        UserResponseDTO expectedDTO = new UserResponseDTO(1L, "newUser", "ADMIN");

        // 5. Configuramos el comportamiento de los mocks.
        when(userRepository.findByUsername(createDTO.username())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(createDTO.password())).thenReturn("encrypted_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // --- NUEVO MOCK ---
        // "Cuando el mapper reciba la entidad guardada, devuelve el DTO esperado".
        when(userMapper.userToUserResponseDTO(savedUser)).thenReturn(expectedDTO);

        // --- ACT (Actuar) ---

        // 6. Llamamos al metodo, que ahora devuelve un UserResponseDTO.
        UserResponseDTO result = userService.createUser(createDTO);

        // --- ASSERT (Verificar) ---

        // 7. Verificamos que el DTO devuelto es el correcto.
        assertNotNull(result);
        assertEquals(expectedDTO.id(), result.id());
        assertEquals(expectedDTO.username(), result.username());
        assertEquals(expectedDTO.role(), result.role());

        // 8. Verificamos las interacciones.
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).userToUserResponseDTO(savedUser);
    }

    @Test
    void shouldThrowException_whenCreatingUserWithExistingUsername() {
        // --- ARRANGE ---
        CreateUserDTO createDTO = new CreateUserDTO("existingUser", "password123", Role.ADMIN);

        // Simula que el username SÍ existe en la base de datos.
        when(userRepository.findByUsername(createDTO.username())).thenReturn(Optional.of(new User()));

        // --- ACT & ASSERT ---
        // Verifica que se lanza la excepción correcta.
        assertThrows(DuplicateResourceException.class, () -> {
            userService.createUser(createDTO);
        });

        // Verifica que el metodo save NUNCA fue llamado.
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldEncodePassword_whenUserIsCreated() {
        // --- ARRANGE (Preparar el escenario) ---

        // 1. Datos de entrada con una contraseña en texto plano.
        CreateUserDTO createDTO = new CreateUserDTO("testUser", "plain-password", Role.CLIENT);

        String expectedEncodedPassword = "hashed_password_from_encoder"; //contra esperada

        // 2. Configuramos el comportamiento de los mocks.
        when(userRepository.findByUsername(createDTO.username())).thenReturn(Optional.empty());

        // "Cuando se llame al encoder con la contraseña en texto plano, devuelve esta versión encriptada".
        when(passwordEncoder.encode("plain-password")).thenReturn(expectedEncodedPassword);

        // --- ACT (Actuar) ---

        // 3. Llamamos al metodo que queremos probar.
        userService.createUser(createDTO);

        // --- ASSERT (Verificar) ---

        // 4. Creamos un "capturador" para el objeto User, con el fin de capturar todos los datos recibidos desde el servicio
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        // 5. Verificamos que el metodo save() fue llamado y capturamos el argumento que se le pasó.
        verify(userRepository).save(userArgumentCaptor.capture());

        // 6. Obtenemos el objeto User que fue capturado.
        User savedUser = userArgumentCaptor.getValue();

        // 7. Verificamos que la contraseña en el objeto que se intentó guardar
        //    es la versión encriptada, no la original. ¡Esta es la prueba clave!
        assertEquals(expectedEncodedPassword, savedUser.getPassword());
        assertNotEquals("plain-password", savedUser.getPassword());
    }
}