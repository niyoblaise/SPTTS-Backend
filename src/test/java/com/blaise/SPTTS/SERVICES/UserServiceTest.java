package com.blaise.SPTTS.SERVICES;

import com.blaise.SPTTS.DTOs.RegisterRequest;
import com.blaise.SPTTS.DTOs.UserDto;
import com.blaise.SPTTS.Mapper.TransportMapper;
import com.blaise.SPTTS.entity.ENUMS.UserType;
import com.blaise.SPTTS.entity.User;
import com.blaise.SPTTS.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository repo;
    @Mock private PasswordEncoder encoder;
    @Mock private TransportMapper mapper;

    @InjectMocks
    private UserService userService;

    @Test
    void add_ShouldCreateUser_WhenEmailNotTaken() {
        RegisterRequest req = new RegisterRequest("Bob", "b@b.com", "0788111111", "secret", UserType.BUS_OPERATOR, "LIC123", "BusCo", null);
        when(repo.findByEmail("b@b.com")).thenReturn(Optional.empty());
        when(encoder.encode("secret")).thenReturn("encoded");
        User saved = User.builder().userId(UUID.randomUUID()).email("b@b.com").fullName("Bob").build();
        when(repo.save(any())).thenReturn(saved);
        when(mapper.toDto(saved)).thenReturn(new UserDto(saved.getUserId(), "b@b.com", "Bob", "0788111111", UserType.BUS_OPERATOR, "LIC123", "BusCo", null));

        UserDto result = userService.add(req);

        assertEquals("Bob", result.fullName());
        assertEquals("b@b.com", result.email());
        verify(repo).save(any());
    }

    @Test
    void add_ShouldThrow_WhenEmailTaken() {
        when(repo.findByEmail("b@b.com")).thenReturn(Optional.of(new User()));
        assertThrows(IllegalArgumentException.class, () ->
            userService.add(new RegisterRequest("Bob", "b@b.com", "0788111111", "secret", UserType.BUS_OPERATOR, null, null, null)));
        verify(repo, never()).save(any());
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenFound() {
        User user = User.builder().userId(UUID.randomUUID()).email("b@b.com").fullName("Bob").build();
        when(repo.findByEmail("b@b.com")).thenReturn(Optional.of(user));
        when(mapper.toDto(user)).thenReturn(new UserDto(user.getUserId(), "b@b.com", "Bob", null, null, null, null, null));

        UserDto result = userService.findByEmail("b@b.com");

        assertEquals("Bob", result.fullName());
    }

    @Test
    void findByEmail_ShouldThrow_WhenNotFound() {
        when(repo.findByEmail("x@y.com")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.findByEmail("x@y.com"));
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        when(repo.findAll()).thenReturn(List.of(
            User.builder().userId(UUID.randomUUID()).email("a@a.com").build(),
            User.builder().userId(UUID.randomUUID()).email("b@b.com").build()
        ));
        when(mapper.toDto(any(User.class))).thenAnswer(i ->
            new UserDto(((User) i.getArgument(0)).getUserId(), ((User) i.getArgument(0)).getEmail(), null, null, null, null, null, null));

        List<UserDto> result = userService.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void delete_ShouldRemoveUser() {
        UUID id = UUID.randomUUID();
        userService.delete(id);
        verify(repo).deleteById(id);
    }

    @Test
    void update_ShouldModifyNameAndPhone() {
        UUID id = UUID.randomUUID();
        User existing = User.builder().userId(id).fullName("Old").phone("000").build();
        UserDto input = new UserDto(id, null, "New", "111", null, null, null, null);

        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);
        when(mapper.toDto(existing)).thenReturn(new UserDto(id, null, "New", "111", null, null, null, null));

        UserDto result = userService.update(id, input);

        assertEquals("New", result.fullName());
        assertEquals("111", result.phone());
    }

    @Test
    void update_ShouldThrow_WhenNotFound() {
        when(repo.findById(any())).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () ->
            userService.update(UUID.randomUUID(), new UserDto(null, null, "X", null, null, null, null, null)));
    }
}
