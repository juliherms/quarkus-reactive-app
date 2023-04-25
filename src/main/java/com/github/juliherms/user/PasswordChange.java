package com.github.juliherms.user;

/**
 * This class responsible to represents PasswordChange DTO
 * @param currentPassword
 * @param newPassword
 */
public record PasswordChange(String currentPassword, String newPassword) {
}