package it.unicam.cs.bdslab.extrarnas.controller;

import it.unicam.cs.bdslab.extrarnas.models.StructureInfo;
import it.unicam.cs.bdslab.extrarnas.models.StructureStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreprocessedStructureStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void listsEveryExistingPreprocessedPdbInStableOrder() throws Exception {
        Files.writeString(tempDir.resolve("6GQ1_4.pdb"), "PDB");
        Files.writeString(tempDir.resolve("4V3P_L3.pdb"), "PDB");
        Files.writeString(tempDir.resolve("4V3P_L3.cif"), "CIF");
        Files.writeString(tempDir.resolve("ignored.txt"), "ignored");

        List<StructureInfo> structures = PreprocessedStructureStore.list(tempDir);

        assertEquals(2, structures.size());
        assertEquals("4V3P", structures.get(0).getName());
        assertEquals("L3", structures.get(0).getChain());
        assertEquals(StructureStatus.LOADED, structures.get(0).getStatus());
        assertEquals("6GQ1", structures.get(1).getName());
        assertEquals("4", structures.get(1).getChain());
    }

    @Test
    void deletesThePdbAndMmcifPair() throws Exception {
        Path pdb = tempDir.resolve("4V3P_L3.pdb");
        Path cif = tempDir.resolve("4V3P_L3.cif");
        Files.writeString(pdb, "PDB");
        Files.writeString(cif, "CIF");
        StructureInfo structure = new StructureInfo("4V3P", "L3", tempDir.toString());

        PreprocessedStructureStore.delete(tempDir, structure);

        assertFalse(Files.exists(pdb));
        assertFalse(Files.exists(cif));
    }

    @Test
    void rejectsPathsOutsideThePreprocessedFolder() {
        StructureInfo invalid = new StructureInfo("../outside", "chain", tempDir.toString());

        IOException error = assertThrows(IOException.class,
                () -> PreprocessedStructureStore.delete(tempDir, invalid));

        assertTrue(error.getMessage().contains("Invalid preprocessed structure path"));
    }
}
