package com.github.juliherms.auth;


import io.smallrye.mutiny.Uni;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * This class responsible to provide auth endpoint
 */
@Path("/api/v1/auth")
public class AuthResource {
    private final AuthService authService;

    @Inject
    public AuthResource(AuthService authService) {
        this.authService = authService;
    }

    /**
     * This method responsible to authenticate user in the application
     * @param request
     * @return
     */
    @PermitAll
    @POST
    @Path("/login")
    public Uni<String> login(AuthRequest request) {
        return authService.authenticate(request);
    }
}
