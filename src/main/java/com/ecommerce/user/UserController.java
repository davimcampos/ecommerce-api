package com.ecommerce.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Retorna os dados do usuário autenticado")
    public UserResponse me() {
        return userService.me();
    }
}
