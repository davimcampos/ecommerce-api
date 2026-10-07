package com.ecommerce.address;

public record AddressResponse(
        Long id,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String zipCode) {

    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getStreet(),
                address.getNumber(),
                address.getComplement(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState(),
                address.getZipCode());
    }
}
