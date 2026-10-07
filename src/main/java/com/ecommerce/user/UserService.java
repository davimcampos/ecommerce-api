package com.ecommerce.user;

import com.ecommerce.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthenticatedUser authenticatedUser;

    public UserResponse me() {
        return UserResponse.from(authenticatedUser.get());
    }
}
