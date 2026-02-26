package util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FlashcardFileParser {

    public record ParsedCard(String term, String definition) {
    }

    public static List<ParsedCard> parse(File file) throws IOException {
        String name = file.getName().toLowerCase(Locale.ROOT);

        // detect delimiter: csv / tsv / pipe
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

                // skip header row if it looks like one
                if (firstLine && looksLikeHeader(line)) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;

                String[] parts = splitLine(line, delim);
                if (parts.length < 2) continue;

                String term = parts[0].trim();
                String def = parts[1].trim();

                if (!term.isBlank() && !def.isBlank()) {
                    out.add(new ParsedCard(term, def));
                }
            }
        }
        return out;
    }

    private static boolean looksLikeHeader(String line) {
        String s = line.toLowerCase(Locale.ROOT);
        return s.contains("term") && s.contains("definition");
    }

    // Handles quoted CSV minimally: "a,b",c
    private static String[] splitLine(String line, char delim) {
        if (delim != ',') return line.split(java.util.regex.Pattern.quote(String.valueOf(delim)), 2);

        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                fields.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        fields.add(cur.toString());
        // Only need term + definition
        if (fields.size() >= 2) {
            return new String[]{fields.get(0), fields.get(1)};
        }
        return new String[0];
    }
}