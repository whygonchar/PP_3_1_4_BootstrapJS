package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminControllerRest {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminControllerRest(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    // Метод для получения всех пользователей
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    // Метод для получения всех ролей
    @GetMapping("/roles")
    public List<Role> getAllRoles() {
        return roleService.getRoles();
    }

    // Метод для сохранения нового пользователя
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        userService.saveUser(user);
        return ResponseEntity.ok(user);
    }

    // Метод для обновления пользователя
    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        Optional<User> existingUserOptional = Optional.ofNullable(userService.findUserById(id));
        if (existingUserOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User existingUser = existingUserOptional.get();
        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setPassword(updatedUser.getPassword());
        existingUser.setRoles(updatedUser.getRoles());
        userService.updateUser(existingUser);

        return ResponseEntity.ok(existingUser);
    }
}
