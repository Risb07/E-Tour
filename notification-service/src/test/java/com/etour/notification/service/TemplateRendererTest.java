package com.etour.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TemplateRenderer")
class TemplateRendererTest {

    private final TemplateRenderer renderer = new TemplateRenderer();

    @Test
    @DisplayName("substitutes named placeholders")
    void substitutes() {
        assertThat(renderer.render("Hi {{name}}, ref {{ref}}", Map.of("name", "Ada", "ref", "ETR-1")))
                .isEqualTo("Hi Ada, ref ETR-1");
    }

    @Test
    @DisplayName("tolerates whitespace inside the braces")
    void toleratesWhitespace() {
        assertThat(renderer.render("Hi {{ name }}", Map.of("name", "Ada"))).isEqualTo("Hi Ada");
    }

    @Test
    @DisplayName("renders an unknown placeholder as blank, never as raw {{...}}")
    void unknownRendersBlank() {
        assertThat(renderer.render("Hi {{name}}{{missing}}", Map.of("name", "Ada"))).isEqualTo("Hi Ada");
    }

    @Test
    @DisplayName("a value containing $ or backslash is inserted literally")
    void handlesRegexReplacementMetacharacters() {
        // Matcher.appendReplacement treats $1 as a group reference and a lone
        // backslash as an escape - without quoteReplacement this throws at
        // runtime, and a currency amount is exactly the kind of value that hits it.
        assertThat(renderer.render("Total {{amount}}", Map.of("amount", "$1,299\\USD")))
                .isEqualTo("Total $1,299\\USD");
    }

    @Test
    @DisplayName("null and empty inputs are safe")
    void nullSafe() {
        assertThat(renderer.render(null, Map.of())).isEmpty();
        assertThat(renderer.render("", Map.of())).isEmpty();
        assertThat(renderer.render("no placeholders", null)).isEqualTo("no placeholders");
    }

    @Test
    @DisplayName("does not evaluate anything - a template is data, not code")
    void doesNotEvaluateExpressions() {
        Map<String, String> vars = new HashMap<>();
        vars.put("x", "1");
        // A SpEL-style expression must come out as literal text. If this ever
        // renders as "2", template editing has become code execution.
        assertThat(renderer.render("{{x}} and ${7*191}", vars)).isEqualTo("1 and ${7*191}");
    }
}
