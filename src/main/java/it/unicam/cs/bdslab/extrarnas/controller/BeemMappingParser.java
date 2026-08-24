package it.unicam.cs.bdslab.extrarnas.controller;

import org.apache.commons.csv.CSVFormat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class BeemMappingParser {

    private BeemMappingParser() {
    }

    record Entry(String bundleFile, String newChainId, String originalChainId) {
    }

    record ConversionResult(Set<Path> bundlePaths, int mappingCount) {
    }

    static ConversionResult convertToCsv(Path inputPath, Path outputPath) throws IOException {
        Set<Path> bundlePaths = new LinkedHashSet<>();
        int mappingCount = 0;

        try (BufferedReader reader = Files.newBufferedReader(inputPath);
             BufferedWriter writer = Files.newBufferedWriter(outputPath)) {

            writer.write("File,New_chain_ID,Original_chain_ID");
            writer.newLine();

            String currentFile = null;
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.contains("New chain ID")) {
                    continue;
                }

                if (line.endsWith(".pdb:")) {
                    currentFile = line.substring(0, line.length() - 1).trim();
                    bundlePaths.add(inputPath.getParent().resolve(currentFile));
                    continue;
                }

                String[] parts = line.split("\\s+");
                if (parts.length != 2) {
                    throw new IOException("Malformed BeEM mapping line in " + inputPath + ": " + line);
                }
                if (currentFile == null) {
                    throw new IOException("BeEM chain mapping found before a bundle header in " + inputPath + ": " + line);
                }

                writer.write(String.format("%s,%s,%s", currentFile, parts[0], parts[1]));
                writer.newLine();
                mappingCount++;
            }
        }

        if (bundlePaths.isEmpty() || mappingCount == 0) {
            throw new IOException("BeEM mapping contains no bundle chain mappings: " + inputPath);
        }

        return new ConversionResult(Set.copyOf(bundlePaths), mappingCount);
    }

    static List<Entry> readCsv(Path mappingPath) throws IOException {
        List<Entry> entries = new ArrayList<>();
        var format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();

        try (Reader reader = Files.newBufferedReader(mappingPath)) {
            for (var record : format.parse(reader)) {
                entries.add(new Entry(
                        record.get("File").trim(),
                        record.get("New_chain_ID").trim(),
                        record.get("Original_chain_ID").trim()));
            }
        }

        if (entries.isEmpty()) {
            throw new IOException("Converted BeEM mapping contains no entries: " + mappingPath);
        }
        return entries;
    }

    static List<Entry> selectByOriginalAuthChains(List<Entry> entries, Set<String> requestedAuthChains) {
        if (requestedAuthChains == null) {
            return List.copyOf(entries);
        }
        return entries.stream()
                .filter(entry -> requestedAuthChains.contains(entry.originalChainId()))
                .toList();
    }
}
