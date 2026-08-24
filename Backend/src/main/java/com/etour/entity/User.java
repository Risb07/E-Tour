package com.etour.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @JsonBackReference
    private Role role;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    // Defense in depth: even if a User entity ever leaks into a response
    // by accident, Jackson will never serialize this field out.
    //
    // Stays NOT NULL even for Google-only accounts: those are given a random
    // unusable hash at creation (see GoogleOAuth2UserService), which keeps
    // the column contract unchanged and means no password can ever match.
    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 20)
    private String phone;

    @Column(name = "preferred_language", length = 10)
    private String preferredLanguage = "en";

    @Column(nullable = false)
    private Boolean status = true;

    // ---- Federated sign-in (Google OAuth 2.0) -----------------------------
    // All three are nullable and default to the local-password behaviour, so
    // every pre-existing row stays valid without a migration.

    /**
     * How this account authenticates: "LOCAL" for email/password (the
     * default, and what every existing row is treated as) or "GOOGLE" for an
     * account created through Google sign-in. Purely informational - it never
     * gates a login decision, it just lets the UI say "signed in with Google"
     * and makes support questions answerable.
     */
    @Column(name = "auth_provider", length = 20)
    private String authProvider = "LOCAL";

    /**
     * Google's immutable subject identifier ("sub") for this user. Stored
     * because an email address can be reassigned by a Workspace admin while
     * the sub never changes, so this is the stable link to the Google account.
     */
    @JsonIgnore
    @Column(name = "google_sub", length = 100)
    private String googleSub;

    /** Profile picture URL supplied by Google, if any. */
    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    public User() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(String authProvider) {
        this.authProvider = authProvider;
    }

    public String getGoogleSub() {
        return googleSub;
    }

    public void setGoogleSub(String googleSub) {
        this.googleSub = googleSub;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}