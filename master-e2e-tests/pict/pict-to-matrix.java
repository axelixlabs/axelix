/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Use "pict model.pict | java pict-to-matrix.java > matrix.json"
 *
 * @author Nikita Kirillov
 */
public class PictToMatrix {

    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9]+");

    public static String slugify(String input) {
        if (input == null) {
          return "";
        }
        return NON_ALPHANUMERIC.matcher(input.toLowerCase())
            .replaceAll("-")
            .replaceAll("^-+|-+$", "");
    }

    public static void main(String[] args) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                System.err.println("No rows read from PICT output on stdin.");
                System.exit(1);
            }

            String[] headers = headerLine.split("\t");
            List<Map<String, String>> matrixEntries = new ArrayList<>();
            List<String> names = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] values = line.split("\t");

                String combinedValues = String.join("-", values);
                String name = slugify(combinedValues);
                names.add(name);

                Map<String, String> entry = new LinkedHashMap<>();
                entry.put("name", name);
                for (int i = 0; i < headers.length; i++) {
                    if (i < values.length) {
                        entry.put(headers[i].toLowerCase(), values[i]);
                    }
                }
                matrixEntries.add(entry);
            }

            if (matrixEntries.isEmpty()) {
                System.err.println("No rows read from PICT output on stdin.");
                System.exit(1);
            }

            Set<String> duplicates = names.stream()
                .filter(name -> Collections.frequency(names, name) > 1)
                .collect(Collectors.toSet());

            if (!duplicates.isEmpty()) {
                System.err.println("Slug collision(s) in generated matrix: " + duplicates);
                System.exit(1);
            }

            System.out.println(formatJsonArray(matrixEntries));

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static String formatJsonArray(List<Map<String, String>> entries) {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < entries.size(); i++) {
            json.append("  {\n");
            Map<String, String> entry = entries.get(i);
            int count = 0;
            for (Map.Entry<String, String> pair : entry.entrySet()) {
                json.append("    \"")
                    .append(escapeJson(pair.getKey()))
                    .append("\": \"").append(escapeJson(pair.getValue()))
                    .append("\"");
                if (++count < entry.size()) {
                    json.append(",");
                }
                json.append("\n");
            }
            json.append("  }");
            if (i < entries.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("]");
        return json.toString();
    }
}
