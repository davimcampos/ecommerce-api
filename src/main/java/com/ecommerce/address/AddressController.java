package com.ecommerce.address;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/addresses")
@RequiredArgsConstructor
@Tag(name = "Endereços")
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    @Operation(summary = "Lista os endereços do usuário autenticado")
    public List<AddressResponse> list() {
        return addressService.list();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastra um endereço")
    public AddressResponse create(@Valid @RequestBody AddressRequest request) {
        return addressService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um endereço")
    public AddressResponse update(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        return addressService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove um endereço")
    public void delete(@PathVariable Long id) {
        addressService.delete(id);
    }
}
