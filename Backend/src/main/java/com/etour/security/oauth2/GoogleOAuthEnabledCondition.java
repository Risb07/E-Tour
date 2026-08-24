package com.etour.security.oauth2;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * Enables the Google sign-in configuration only when a client id AND secret
 * have actually been supplied.
 *
 * <p>{@code @ConditionalOnProperty} is not usable here: it treats a property
 * that is present but empty as a match, and these properties are always
 * present (they default to an empty string in application.properties so the
 * env-var override works). This condition checks for real text instead.
 *
 * <p>The practical effect is that a developer who clones the project and runs
 * it with no Google credentials gets exactly the application that existed
 * before OAuth was added - the OAuth filter chain and its beans are never
 * created, and email/password login is completely unaffected.
 */
public class GoogleOAuthEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {

        String clientId = context.getEnvironment().getProperty("app.oauth2.google.client-id");
        String clientSecret = context.getEnvironment().getProperty("app.oauth2.google.client-secret");

        return StringUtils.hasText(clientId) && StringUtils.hasText(clientSecret);
    }
}
