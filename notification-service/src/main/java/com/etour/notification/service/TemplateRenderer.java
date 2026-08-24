package com.etour.notification.service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

/**
 * Substitutes {{placeholders}} in a template.
 *
 * <p>Deliberately not a full expression language. A template engine that can
 * evaluate expressions turns "an admin edits a template" into remote code
 * execution, and nothing here needs conditionals or loops - so this does one
 * thing: replace a name with a value.
 */
@Component
public class TemplateRenderer {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([A-Za-z0-9_.-]+)\\s*}}");

    /**
     * @param template the text containing {{name}} slots
     * @param variables values to substitute; a name with no value renders as an
     *                  empty string rather than leaving the raw {{name}} visible
     *                  in a customer-facing email
     */
    public String render(String template, Map<String, String> variables) {
        if (template == null || template.isEmpty()) {
            return "";
        }

        Map<String, String> values = variables == null ? Map.of() : variables;
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder out = new StringBuilder();

        while (matcher.find()) {
            String name = matcher.group(1);
            String value = values.getOrDefault(name, "");
            // quoteReplacement: a value containing $ or \ would otherwise be
            // interpreted as a group reference and throw at runtime.
            matcher.appendReplacement(out, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(out);

        return out.toString();
    }
}
