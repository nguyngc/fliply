package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Parses flashcard imports from delimited text files and returns term/definition pairs.
 *
 * <p>Behavior is intentionally conservative: only the first two columns are used,
 * blank rows are ignored, and malformed rows are skipped.
 */
public class FlashcardFileParser {

    public record ParsedCard(String term, String definition) {
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

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isBlank()) continue;

                // Header check is only applied to the first non-blank row.
                if (firstLine && looksLikeHeader(line)) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;

                String[] parts = splitLine(line, delim);
                if (parts.length < 2) continue;

                // Import uses a strict 2-column model: term (col 1), definition (col 2).
                String term = parts[0].trim();
                String def = parts[1].trim();

                if (!term.isBlank() && !def.isBlank()) {
                    out.add(new ParsedCard(term, def));
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
    private static boolean looksLikeHeader(String line) {
        String s = line.toLowerCase(Locale.ROOT);
        return s.contains("term") && s.contains("definition");
    }

    /**
     * Splits one record into fields.
     *
     * <p>For non-CSV delimiters, split is limited to two parts because only term and
     * definition are consumed. CSV mode uses a minimal quote-state parser so commas inside
     * quoted text do not split fields.
     * @param line the input line to split
     * @param delim the delimiter character to split on (e.g. ',', '\t', '|')
     * @return an array of fields extracted from the line, with a maximum of two elements for non-CSV delimiters
     */
    private static String[] splitLine(String line, char delim) {
        if (delim != ',') return line.split(java.util.regex.Pattern.quote(String.valueOf(delim)), 2);

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

        // Return only the columns used by the importer.
        if (fields.size() >= 2) {
            return new String[]{fields.get(0), fields.get(1)};
        }
        return new String[0];
    }
}