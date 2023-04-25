package com.github.juliherms.user;

import com.github.juliherms.project.Project;
import com.github.juliherms.task.Task;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.hibernate.ObjectNotFoundException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * This class responsible to implements business logic from users
 */
@ApplicationScoped
public class UserService {


    private final JsonWebToken jwt;

    @Inject
    public UserService(JsonWebToken jwt) {
        this.jwt = jwt;
    }

    /**
     * Find user by id and throws exception for user not found
     * @param id
     * @return
     */
    public Uni<User> findById(long id) {
        return User.<User>findById(id)
                .onItem().ifNull().failWith(() -> new ObjectNotFoundException(id, "User"));
    }

    /**
     * Find user by name
     * @param name
     * @return
     */
    public Uni<User> findByName(String name) {
        return User.find("name", name).firstResult();
    }

    /**
     * Lista all users in the application
     * @return
     */
    public Uni<List<User>> list() {
        return User.listAll();
    }

    /**
     * Create and encrypt user password
     * bcrypt is a password-hashing function based on the Blowfish cipher.
     * bcrypt is especially recommended for hashing passwords because it is a slow and expensive algorithm.
     * The slowness of the function makes it ideal to store passwords because it helps mitigate brute-force attacks
     * by reducing the number of hashes per second an attacker can use when performing a dictionary attack
     * @param user
     * @return
     */
    @ReactiveTransactional
    public Uni<User> create(User user) {
        user.password = BcryptUtil.bcryptHash(user.password);
        return user.persistAndFlush();
    }

    /**
     * Update user
     * @param user
     * @return
     */
    @ReactiveTransactional
    public Uni<User> update(User user) {
        return findById(user.id)
                .chain(u -> User.getSession())
                .chain(s -> s.merge(user));
    }

    /**
     * Delete user
     * @param id
     * @return
     */
    @ReactiveTransactional
    public Uni<Void> delete(long id) {
        return findById(id)
                .chain(u -> Uni.combine().all().unis(
                                        Task.delete("user.id", u.id),
                                        Project.delete("user.id", u.id)
                                ).asTuple()
                                .chain(t -> u.delete())
                );
    }

    /**
     * Get current user
     * @return
     */
    public Uni<User> getCurrentUser() {
        return findByName(jwt.getName());
    }

    /**
     * Verify that an authentication request password matches the one in the database
     * @param user
     * @param password
     * @return
     */
    public static boolean matches(User user, String password) {
        return BcryptUtil.matches(password, user.password);
    }

    /**
     * Method responsible to update password for user
     * @param currentPassword
     * @param newPassword
     * @return
     */
    @ReactiveTransactional
    public Uni<User> changePassword(String currentPassword, String newPassword) {
        return getCurrentUser()
                .chain(u -> {
                    if (!matches(u, currentPassword)) {
                        throw new ClientErrorException("Current password does not match", Response.Status.CONFLICT);
                    }
                    u.setPassword(BcryptUtil.bcryptHash(newPassword));
                    return u.persistAndFlush();
                });
    }
}
