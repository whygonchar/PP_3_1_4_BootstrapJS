package ru.kata.spring.boot_security.demo.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import javax.annotation.PostConstruct;
import java.util.Set;

@Component
public class DBInit {
    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public DBInit(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @PostConstruct
    public void init() {
        Role admin = new Role("ROLE_ADMIN");
        Role user = new Role("ROLE_USER");

        roleService.addRole(admin);
        roleService.addRole(user);

        userService.saveUser(new User(Set.of(admin),"daniil@gmail.com","admin",
                25, "Goncharov", "Daniil"));
        userService.saveUser(new User(Set.of(user),"user@gmail.com","user",
                01, "1", "user"));
    }
}
