package it.unicam.cs.bdslab.extrarnas.controller;

import it.unicam.cs.bdslab.extrarnas.models.StructureInfo;
import it.unicam.cs.bdslab.extrarnas.models.StructureStatus;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

final class PreprocessedStructureStore {

    private PreprocessedStructureStore() {
    }

    static List<StructureInfo> list(Path preprocessedFolder) throws IOException {
        if (!Files.isDirectory(preprocessedFolder)) {
            return List.of();
        }

        List<StructureInfo> result = new ArrayList<>();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(preprocessedFolder, "*.pdb")) {
            for (Path file : files) {
                String fileName = file.getFileName().toString();
                String baseName = fileName.substring(0, fileName.length() - 4);
                String[] parts = baseName.split("_");
                String moleculeId = parts.length > 0 ? parts[0] : baseName;
                String chain = parts.length > 1
                        ? String.join("_", Arrays.copyOfRange(parts, 1, parts.length))
                        : "";

                result.add(new StructureInfo(
                        moleculeId,
                        chain,
                        preprocessedFolder.toString(),
                        StructureStatus.LOADED));
            }
        }

        result.sort(Comparator.comparing(StructureInfo::getName).thenComparing(StructureInfo::getChain));
        return result;
    }

    static void delete(Path preprocessedFolder, StructureInfo structure) throws IOException {
        Path normalizedFolder = preprocessedFolder.toAbsolutePath().normalize();
        String baseName = structure.getName() + "_" + structure.getChain();
        Path pdbFile = normalizedFolder.resolve(baseName + ".pdb").normalize();
        Path cifFile = normalizedFolder.resolve(baseName + ".cif").normalize();

        if (!normalizedFolder.equals(pdbFile.getParent()) || !normalizedFolder.equals(cifFile.getParent())) {
            throw new IOException("Invalid preprocessed structure path: " + baseName);
        }

        Files.deleteIfExists(pdbFile);
        Files.deleteIfExists(cifFile);
    }
}
