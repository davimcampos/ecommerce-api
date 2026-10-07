package com.ecommerce.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressRequest(
        @NotBlank(message = "A rua é obrigatória")
        @Size(max = 150, message = "A rua deve ter no máximo 150 caracteres")
        String street,

        @NotBlank(message = "O número é obrigatório")
        @Size(max = 20, message = "O número deve ter no máximo 20 caracteres")
        String number,

        @Size(max = 100, message = "O complemento deve ter no máximo 100 caracteres")
        String complement,

        @NotBlank(message = "O bairro é obrigatório")
        @Size(max = 100, message = "O bairro deve ter no máximo 100 caracteres")
        String neighborhood,

        @NotBlank(message = "A cidade é obrigatória")
        @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
        String city,

        @NotBlank(message = "O estado é obrigatório")
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "O estado deve ser a sigla com 2 letras")
        String state,

        @NotBlank(message = "O CEP é obrigatório")
        @Pattern(regexp = "^\\d{8}$", message = "O CEP deve conter 8 dígitos")
        String zipCode) {
}
