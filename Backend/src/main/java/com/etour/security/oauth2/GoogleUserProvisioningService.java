package com.etour.security.oauth2;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.etour.entity.Customer;
import com.etour.entity.Role;
import com.etour.entity.User;
import com.etour.repository.CustomerRepository;
import com.etour.repository.RoleRepository;
import com.etour.repository.UserRepository;

/**
 * Turns a verified Google profile into an eTour {@link User}.
 *
 * <p>This is the one place that decides what a Google sign-in means for the
 * database. It is called from both {@link GoogleOidcUserService} (the normal
 * path, since the {@code openid} scope is requested) and
 * {@link GoogleOAuth2UserService}, so the behaviour cannot drift between them.
 *
 * <p>Three cases are handled:
 * <ol>
 *   <li><b>Known email</b> - the existing account is reused and linked to the
 *       Google identity. Google has already verified ownership of the address,
 *       so this is safe, and it means a customer who registered with a password
 *       can later click "Continue with Google" and land in the same account
 *       with their bookings, cart and wishlist intact.</li>
 *   <li><b>New email</b> - a CUSTOMER user plus the matching Customer profile
 *       row are created, mirroring exactly what self-registration does in
 *       {@code UserServiceImpl}.</li>
 *   <li><b>Disabled account</b> - refused, the same as password login refuses
 *       it via {@code CustomUserDetails.isEnabled()}.</li>
 * </ol>
 *
 * <p>The JWT itself is not minted here - that happens in
 * {@link OAuth2AuthenticationSuccessHandler}, using the same
 * {@code JwtService} the password login uses, so both routes yield an
 * identical token.
 */
@Service
@Conditional(GoogleOAuthEnabledCondition.class)
public class GoogleUserProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(GoogleUserProvisioningService.class);

    /** Self-registration is always CUSTOMER, exactly as in UserServiceImpl. */
    private static final String DEFAULT_SELF_REGISTER_ROLE = "CUSTOMER";

    /**
     * Stand-in for the phone number Google does not give us. Ten digits so it
     * satisfies the existing @Pattern on Customer.phone; all zeroes so it is
     * obviously not a real number if it ever shows up in an admin screen.
     */
    private static final String PLACEHOLDER_PHONE = "0000000000";

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public GoogleUserProvisioningService(UserRepository userRepository,
                                         RoleRepository roleRepository,
                                         CustomerRepository customerRepository,
                                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @param attributes the claims Google returned (userinfo response and/or
     *                   ID token claims)
     * @return the eTour account this Google identity maps to
     */
    @Transactional
    public User provision(Map<String, Object> attributes) {

        String email = asString(attributes.get("email"));
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_missing"),
                    "Your Google account did not share an email address, which eTour needs to identify you.");
        }
        email = email.trim().toLowerCase();

        // Google reports email_verified=false in some Workspace edge cases.
        // Trusting an unverified address would let whoever can set that address
        // on a Google account take over the matching eTour account, so the
        // link-by-email rule below depends on this check.
        Object verified = attributes.get("email_verified");
        if (verified != null && !Boolean.parseBoolean(String.valueOf(verified))) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("email_unverified"),
                    "Your Google email address is not verified.");
        }

        String googleSub = asString(attributes.get("sub"));
        String givenName = asString(attributes.get("given_name"));
        String familyName = asString(attributes.get("family_name"));
        String fullName = asString(attributes.get("name"));
        String picture = asString(attributes.get("picture"));

        final String resolvedEmail = email;

        return userRepository.findByEmail(resolvedEmail)
                .map(existing -> linkExisting(existing, googleSub, picture))
                .orElseGet(() -> createNew(resolvedEmail, googleSub, givenName, familyName, fullName, picture));
    }

    /**
     * Case 1 - the email already has an eTour account. Attach the Google
     * identity and let them in. The password hash, role and status are
     * deliberately left alone: linking must never escalate or downgrade an
     * account, and a customer who already has a password keeps being able to
     * use it.
     */
    private User linkExisting(User existing, String googleSub, String picture) {

        if (Boolean.FALSE.equals(existing.getStatus())) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("account_disabled"),
                    "This account has been disabled. Please contact support.");
        }

        boolean changed = false;

        if (googleSub != null && !googleSub.equals(existing.getGoogleSub())) {
            existing.setGoogleSub(googleSub);
            changed = true;
        }
        // An account created with a password stays marked LOCAL - it can now be
        // reached both ways, and overwriting the marker would lose that fact.
        // Only a row with nothing recorded gets stamped.
        if (existing.getAuthProvider() == null || existing.getAuthProvider().isBlank()) {
            existing.setAuthProvider("LOCAL");
            changed = true;
        }
        if (picture != null && !picture.equals(existing.getAvatarUrl())) {
            existing.setAvatarUrl(picture);
            changed = true;
        }

        return changed ? userRepository.save(existing) : existing;
    }

    /**
     * Case 2 - first time this email has been seen. Creates the same pair of
     * rows self-registration creates, so a Google user is indistinguishable
     * from a self-registered one everywhere downstream: bookings, cart,
     * wishlist, passengers and reviews all hang off the Customer row.
     */
    private User createNew(String email, String googleSub, String givenName,
                           String familyName, String fullName, String picture) {

        Role role = roleRepository.findByRoleName(DEFAULT_SELF_REGISTER_ROLE)
                .orElseThrow(() -> new IllegalStateException(
                        "Default role '" + DEFAULT_SELF_REGISTER_ROLE + "' is not seeded in the database"));

        String firstName = firstNonBlank(givenName, firstWordOf(fullName), email.split("@")[0]);
        String lastName = firstNonBlank(familyName, restOfWords(fullName));

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        // This user never chose a password. A random unusable hash satisfies
        // the NOT NULL contract on password_hash and guarantees /api/auth/login
        // can never succeed for the account: no plaintext exists anywhere that
        // produces this hash, and the random source is discarded immediately.
        user.setPasswordHash(passwordEncoder.encode(randomSecret()));
        user.setPreferredLanguage("en");
        user.setStatus(true);
        user.setRole(role);
        user.setAuthProvider("GOOGLE");
        user.setGoogleSub(googleSub);
        user.setAvatarUrl(picture);

        User saved = userRepository.save(user);

        Customer customer = new Customer();
        customer.setUser(saved);
        customer.setFullName(firstNonBlank(fullName, (firstName + " " + nullToEmpty(lastName)).trim()));
        customer.setEmail(saved.getEmail());
        // Google never supplies a phone number, but Customer.phone is
        // @NotNull and @Pattern("^[0-9]{10}$") - null and "" both fail
        // validation and would abort the insert. A digit placeholder is the
        // only value that satisfies the existing constraint without weakening
        // it. The customer replaces it on their profile or at booking time,
        // where the real number is captured and validated as usual.
        customer.setPhone(PLACEHOLDER_PHONE);
        customerRepository.save(customer);

        log.info("Created a new eTour account from Google sign-in for {}", email);

        return saved;
    }

    private static String randomSecret() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static String firstWordOf(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim().split("\\s+")[0];
    }

    private static String restOfWords(String value) {
        if (value == null || value.isBlank()) return null;
        String[] parts = value.trim().split("\\s+", 2);
        return parts.length > 1 ? parts[1] : null;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
