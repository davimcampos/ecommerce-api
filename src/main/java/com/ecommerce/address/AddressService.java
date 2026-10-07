package com.ecommerce.address;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.security.AuthenticatedUser;
import com.ecommerce.user.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final AuthenticatedUser authenticatedUser;

    @Transactional(readOnly = true)
    public List<AddressResponse> list() {
        User user = authenticatedUser.get();
        return addressRepository.findByUserIdOrderByIdAsc(user.getId()).stream()
                .map(AddressResponse::from)
                .toList();
    }

    @Transactional
    public AddressResponse create(AddressRequest request) {
        Address address = new Address();
        address.setUser(authenticatedUser.get());
        apply(address, request);
        return AddressResponse.from(addressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(Long id, AddressRequest request) {
        Address address = find(id);
        apply(address, request);
        return AddressResponse.from(address);
    }

    @Transactional
    public void delete(Long id) {
        addressRepository.delete(find(id));
    }

    private Address find(Long id) {
        User user = authenticatedUser.get();
        return addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado"));
    }

    private void apply(Address address, AddressRequest request) {
        address.setStreet(request.street().trim());
        address.setNumber(request.number().trim());
        address.setComplement(request.complement() == null ? null : request.complement().trim());
        address.setNeighborhood(request.neighborhood().trim());
        address.setCity(request.city().trim());
        address.setState(request.state().trim().toUpperCase());
        address.setZipCode(request.zipCode());
    }
}
