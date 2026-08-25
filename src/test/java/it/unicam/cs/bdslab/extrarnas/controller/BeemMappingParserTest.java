package it.unicam.cs.bdslab.extrarnas.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeemMappingParserTest {

    @TempDir
    Path tempDir;

    @Test
    void convertsNewAndOriginalChainIdsWithoutReversingThem() throws Exception {
        Path input = tempDir.resolve("4v5k-chain-id-mapping.txt");
        Path output = tempDir.resolve("4V5K-pdb-mapping.csv");
        Files.writeString(input, """
                    New chain ID            Original chain ID

                4v5k-pdb-bundle2.pdb:
                           B                         BB
                           S                         BV

                4v5k-pdb-bundle4.pdb:
                           B                         DB
                """);

        var result = BeemMappingParser.convertToCsv(input, output);
        List<BeemMappingParser.Entry> entries = BeemMappingParser.readCsv(output);

        assertEquals(3, result.mappingCount());
        assertEquals(2, result.bundlePaths().size());
        assertEquals(List.of(
                tempDir.resolve("4v5k-pdb-bundle2.pdb"),
                tempDir.resolve("4v5k-pdb-bundle4.pdb")
        ), List.copyOf(result.bundlePaths()));
        assertEquals(List.of(
                new BeemMappingParser.Entry("4v5k-pdb-bundle2.pdb", "B", "BB"),
                new BeemMappingParser.Entry("4v5k-pdb-bundle2.pdb", "S", "BV"),
                new BeemMappingParser.Entry("4v5k-pdb-bundle4.pdb", "B", "DB")
        ), entries);
        assertTrue(result.bundlePaths().contains(tempDir.resolve("4v5k-pdb-bundle2.pdb")));
        assertTrue(result.bundlePaths().contains(tempDir.resolve("4v5k-pdb-bundle4.pdb")));
    }

    @Test
    void selectsCsvAuthChainDirectlyWithoutTreatingItAsALabelChain() {
        List<BeemMappingParser.Entry> entries = List.of(
                new BeemMappingParser.Entry("4v5k-pdb-bundle2.pdb", "B", "BB"),
                new BeemMappingParser.Entry("4v5k-pdb-bundle2.pdb", "S", "BV")
        );

        List<BeemMappingParser.Entry> selected =
                BeemMappingParser.selectByOriginalAuthChains(entries, Set.of("BB"));

        assertEquals(List.of(
                new BeemMappingParser.Entry("4v5k-pdb-bundle2.pdb", "B", "BB")
        ), selected);
    }

    @Test
    void preservesCaseForMultiCharacterAuthChains() throws Exception {
        Path mapping = tempDir.resolve("mapping.csv");
        Files.writeString(mapping, """
                File,New_chain_ID,Original_chain_ID
                8fmw-pdb-bundle2.pdb,B,AB
                8fmw-pdb-bundle2.pdb,d,Ad
                """);

        List<BeemMappingParser.Entry> entries = BeemMappingParser.readCsv(mapping);

        assertEquals("AB", entries.get(0).originalChainId());
        assertEquals("Ad", entries.get(1).originalChainId());
    }

    @Test
    void rejectsMalformedMappingLinesInsteadOfSilentlyDroppingThem() throws Exception {
        Path input = tempDir.resolve("invalid-chain-id-mapping.txt");
        Path output = tempDir.resolve("invalid-mapping.csv");
        Files.writeString(input, """
                    New chain ID            Original chain ID

                invalid-pdb-bundle1.pdb:
                           malformed mapping line
                """);

        IOException error = assertThrows(IOException.class,
                () -> BeemMappingParser.convertToCsv(input, output));

        assertTrue(error.getMessage().contains("Malformed BeEM mapping line"));
    }
}
