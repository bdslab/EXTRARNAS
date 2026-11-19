package it.unicam.cs.bdslab.extrarnas.controller;

import it.unicam.cs.bdslab.extrarnas.models.StructureInfo;
import it.unicam.cs.bdslab.extrarnas.models.StructureStatus;
import it.unicam.cs.bdslab.extrarnas.parser.output.RNASecondaryStructurePrinter;
import it.unicam.cs.bdslab.extrarnas.view.HomeController;
import it.unicam.cs.bdslab.extrarnas.view.utils.TOOL;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

/**
 * Singleton controller for loading, saving, and packaging RNA files.
 * Exceptions are propagated upwards.
 */
public class IOController {

    private static final Logger log = Logger.getLogger(IOController.class.getName());
    private static final IOController instance = new IOController();
    private Path sharedDirectory;

    private IOController() {
    }

    public static IOController getInstance() {
        return instance;
    }

    /**
     * Loads all regular files from a directory.
     */
    public void loadDirectory(Path p) throws IOException {
        this.sharedDirectory = p;
    }

    public Path getSharedDirectory() {
        return this.sharedDirectory;
    }

    /**
     * Loads molecules from a CSV file where col0 is the molecule ID and col1 is an auth chain filter.
     */
    public List<StructureInfo> loadMoleculesFromCsv(Path csvPath) throws IOException {
        if (csvPath == null || !Files.isRegularFile(csvPath)) {
            throw new IOException("CSV file not found: " + csvPath);
        }

        List<StructureInfo> result = new ArrayList<>();
        LinkedHashSet<String> seenRows = new LinkedHashSet<>();

        try (BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {
            String line;
            boolean headerSkipped = false;

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                if (!headerSkipped && looksLikeHeader(line)) {
                    headerSkipped = true;
                    continue;
                }

                String[] cols = line.split(",", -1);
                if (cols.length < 2) continue;

                String moleculeId = cols[0].trim();
                String chain = cols[1].trim();
                if (moleculeId.isEmpty() || chain.isEmpty()) continue;

                String dedupKey = moleculeId + "|" + chain;
                if (seenRows.add(dedupKey)) {
                    result.add(new StructureInfo(moleculeId, chain, csvPath.toString(), StructureStatus.LOADED));
                }
            }
        }

        return result;
    }

    private boolean looksLikeHeader(String line) {
        String lower = line.toLowerCase();
        return lower.contains("id") || lower.contains("chain");
    }

    /**
     * Create a metadata file for the exectution, including the timestamp, tools used, and structure information, docker images.
     * @param tools the selected tools used in the execution
     * @param normal the output format for normal structures
     * @param extended the output format for extended structures
     * @param structures the list of structures processed
     */
    public void createMetadata(Collection<TOOL> tools, RNASecondaryStructurePrinter.OutputFormat normal, RNASecondaryStructurePrinter.OutputFormat extended, Collection<StructureInfo> structures) {
        Date timestamp = new Date();
        String name = String.format("metadata_%tF_%tH-%tM-%tS.txt", timestamp, timestamp, timestamp, timestamp);
        // Implementation for creating metadata file
        File metadataFile = new File(sharedDirectory.resolve("output").toString(), name);
        try (FileWriter writer = new FileWriter(metadataFile)) {
            writer.write("Timestamp: " + timestamp + "\n");
            writer.write("Docker image core: " + HomeController.dockerAllToolsImage + ":" + HomeController.dockerAllToolsImageTag + "\n");
            if (DockerController.getInstance().dockerImageExists(HomeController.dockerX3DNAImage)) {
                writer.write("Docker image x3dna: " + HomeController.dockerX3DNAImage + ":" + HomeController.dockerX3DNAImageTag + "\n");
            } else {
                writer.write("Docker image x3dna: not found\n");
            }
            writer.write("Tools used: \n");
            for (TOOL tool : tools) {
                writer.write("- " + tool.getName() + " \n");
            }
            writer.write("Normal output format: " + normal + "\n");
            writer.write("Extended output format: " + extended + "\n");
            writer.write("Structures processed:\n");
            for (StructureInfo structure : structures.stream().filter(s -> s.getStatus() != StructureStatus.ERROR).toList()) {
                writer.write("- " + structure.toString() + "\n");
            }
        } catch (IOException e) {
            log.severe("Error writing metadata file: " + e.getMessage());
        }
    }

    /**
     * Saves and optionally processes RNA files, generates statistics, and zips the result.
     */
    public void saveFiles(List<Path> files) throws IOException {

    }


    private void createZipFile(Path dstPath, String zipFileName, List<Path> generatedFiles) throws IOException {

    }

    public void clear() {
        this.sharedDirectory = null;
    }
}
