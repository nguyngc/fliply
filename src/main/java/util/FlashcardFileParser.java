package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Parses flashcard imports from delimited text files and returns term/definition pairs.
 *
 * <p>Old two-column files still work. Localized imports can include columns for
 * English, Arabic, Finnish, Korean, Lao, and Vietnamese definitions.
 */
public class FlashcardFileParser {

    public static final List<String> SUPPORTED_LANGUAGE_CODES = List.of("en", "ar", "fi", "ko", "lo", "vi");

    public record ParsedCard(String term, String definition, Map<String, String> definitions) {
        public ParsedCard(String term, String definition) {
            this(term, definition, singleEnglishDefinition(definition));
        }

        public ParsedCard {
            definitions = definitions == null ? Map.of() : new LinkedHashMap<>(definitions);
            definition = firstText(definition, definitions.get("en"));
        }

        public String definitionFor(String language) {
            String localized = definitions.get(normalizeLanguage(language));
            return firstText(localized, definition);
        }
    }

    /** Parses the given file and returns a list of term/definition pairs.
     * Supported delimiters are inferred from the file extension: comma for .csv, tab for .tsv, and pipe for .psv/.pipe.
     * @param file the input file to parse
     * @return a list of parsed cards containing term and definition pairs
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static List<ParsedCard> parse(File file) throws IOException {
        String name = file.getName().toLowerCase(Locale.ROOT);

        // Infer delimiter from extension; default to CSV for unknown extensions.
        char delim = ',';
        if (name.endsWith(".tsv")) delim = '\t';
        else if (name.endsWith(".psv") || name.endsWith(".pipe")) delim = '|';

        List<ParsedCard> out = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;
            Map<Integer, String> languageColumns = Map.of();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isBlank()) continue;

                String[] parts = splitLine(line, delim);
                if (parts.length < 2) continue;

                if (firstLine && looksLikeHeader(parts)) {
                    languageColumns = mapLanguageColumns(parts);
                    firstLine = false;
                    continue;
                }
                firstLine = false;

                String term = parts[0].trim();
                Map<String, String> definitions = extractDefinitions(parts, languageColumns);
                String def = firstText(definitions.get("en"), definitions.values().stream()
                        .filter(value -> value != null && !value.isBlank())
                        .findFirst()
                        .orElse(""));

                if (!term.isBlank() && !def.isBlank()) {
                    out.add(new ParsedCard(term, def, definitions));
                }
            }
        }
        return out;
    }

    /**
     * Lightweight header heuristic for common templates such as "term,definition".
     * @param line the line to check for header-like content
     * @return true if the line contains both "term" and "definition" (case-insensitive), false otherwise
     */
    private static boolean looksLikeHeader(String[] fields) {
        boolean hasTerm = false;
        boolean hasDefinition = false;
        for (String field : fields) {
            String normalized = normalizeHeader(field);
            hasTerm = hasTerm || "term".equals(normalized);
            hasDefinition = hasDefinition || normalized.startsWith("definition")
                    || SUPPORTED_LANGUAGE_CODES.contains(resolveLanguageCode(normalized));
        }
        return hasTerm && hasDefinition;
    }

    private static Map<Integer, String> mapLanguageColumns(String[] fields) {
        Map<Integer, String> columns = new LinkedHashMap<>();
        for (int i = 1; i < fields.length; i++) {
            String language = resolveLanguageCode(normalizeHeader(fields[i]));
            if (SUPPORTED_LANGUAGE_CODES.contains(language)) {
                columns.put(i, language);
            }
        }
        if (columns.isEmpty() && fields.length > 1) {
            columns.put(1, "en");
        }
        return columns;
    }

    private static Map<String, String> extractDefinitions(String[] fields, Map<Integer, String> languageColumns) {
        Map<String, String> definitions = new LinkedHashMap<>();
        if (languageColumns == null || languageColumns.isEmpty()) {
            definitions.put("en", fields[1].trim());
            return definitions;
        }

        for (Map.Entry<Integer, String> entry : languageColumns.entrySet()) {
            int column = entry.getKey();
            if (column < fields.length) {
                String value = fields[column].trim();
                if (!value.isBlank()) {
                    definitions.put(entry.getValue(), value);
                }
            }
        }
        return definitions;
    }

    private static String resolveLanguageCode(String header) {
        return switch (header) {
            case "en", "eng", "english", "definition", "definitionen", "definitionenglish" -> "en";
            case "ar", "ara", "arabic", "definitionar", "definitionarabic" -> "ar";
            case "fi", "fin", "finnish", "definitionfi", "definitionfinnish" -> "fi";
            case "ko", "kor", "korean", "definitionko", "definitionkorean" -> "ko";
            case "lo", "lao", "definitionlo", "definitionlao" -> "lo";
            case "vi", "vie", "vietnamese", "definitionvi", "definitionvietnamese" -> "vi";
            default -> "";
        };
    }

    private static String normalizeHeader(String header) {
        return header == null
                ? ""
                : header.trim()
                .toLowerCase(Locale.ROOT)
                .replace("_", "")
                .replace("-", "")
                .replace(" ", "");
    }

    private static String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) {
            return "en";
        }
        return language.toLowerCase(Locale.ROOT);
    }

    private static String firstText(String first, String fallback) {
        return first == null || first.isBlank() ? fallback : first.trim();
    }

    private static Map<String, String> singleEnglishDefinition(String definition) {
        Map<String, String> definitions = new LinkedHashMap<>();
        definitions.put("en", definition);
        return definitions;
    }

    /**
     * Splits one record into fields.
     *
     * <p>For non-CSV delimiters, split is limited to two parts because only term and
     * definition are consumed. CSV mode uses a minimal quote-state parser so commas inside
     * quoted text do not split fields.
     * @param line the input line to split
     * @param delim the delimiter character to split on (e.g. ',', '\t', '|')
     * @return an array of fields extracted from the line
     */
    private static String[] splitLine(String line, char delim) {
        if (delim != ',') return line.split(java.util.regex.Pattern.quote(String.valueOf(delim)), -1);

        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                // Toggle quote mode; escaped quotes are not handled by this lightweight parser.
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                fields.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        fields.add(cur.toString());

        return fields.toArray(String[]::new);
    }
}
