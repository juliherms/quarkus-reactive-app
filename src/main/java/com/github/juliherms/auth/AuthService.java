package com.github.juliherms.auth;


import com.github.juliherms.user.UserService;
import io.quarkus.security.AuthenticationFailedException;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Duration;
import java.util.HashSet;

/**
 * is the core class for the current chapter and contains the logic to authenticate
 * the user and generate the JWTs with the user’s claims
 */
@ApplicationScoped
public class AuthService {

    private final String issuer;
    private final UserService userService;

    @Inject
    public AuthService(
            @ConfigProperty(name = "mp.jwt.verify.issuer") String issuer,
            UserService userService) {
        this.issuer = issuer;
        this.userService = userService;
    }

    /**
     * This method responsible to authenticate user and generate your jwt token
     * @param authRequest
     * @return
     */
    public Uni<String> authenticate(AuthRequest authRequest) {
        return userService.findByName(authRequest.name())
                .onItem()
                .transform(user -> {
                    if (user == null || !UserService.matches(user, authRequest.password())) {
                        throw new AuthenticationFailedException("Invalid credentials");
                    }
                    return Jwt.issuer(issuer)
                            .upn(user.name)
                            .groups(new HashSet<>(user.roles))
                            .expiresIn(Duration.ofHours(1L)) // 1 hour to expire
                            .sign();
                });
    }
}
