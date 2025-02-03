package ru.kata.spring.boot_security.demo.controller;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.*;

import javax.validation.Valid;
import java.security.Principal;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public String showUserList(Model model, Principal principal) {
        model.addAttribute("users", userService.findAll());
        User user = userService.findUserByUsername(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("page", "PAGE_ADMIN");
        model.addAttribute("newUser", new User());
        model.addAttribute("roles", roleService.getRoles());
        return "admin";
    }

    @GetMapping("/new")
    public String createUser(User user, Model model) {
        model.addAttribute("user", user);
        model.addAttribute("roles", roleService.getRoles());
        return "admin";
    }

    @PostMapping("/new")
    public String createUser(@ModelAttribute("user") @Valid User user) {

        userService.saveUser(user);
        return "redirect:/admin";
    }

    @PatchMapping("/update{id}")
    public String updateUser(@ModelAttribute("user") User user) {
        userService.updateUser(user);
        return "redirect:/admin";
    }

    @DeleteMapping("/delete{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userService.deleteUserById(id);
        return "redirect:/admin";
    }
}
