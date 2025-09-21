package lk.kolitha.dana.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class UrlGenerator {
    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern MULTIPLE_DASH = Pattern.compile("-{2,}");

    public static String generate(String input, int maxLength) {
        if (input == null || input.isBlank()) return "program";

        String noWhiteSpace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(noWhiteSpace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        slug = MULTIPLE_DASH.matcher(slug).replaceAll("-")
                .replaceAll("^-|-$", "")
                .toLowerCase();

        if (slug.length() > maxLength) {
            slug = slug.substring(0, maxLength);
            slug = slug.replaceAll("^-|-$", "");
        }
        return slug.isBlank() ? "program" : slug;
    }
}
