package it.unicam.cs.bdslab.extrarnas.controller;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.biojava.nbio.structure.StructureException;
import org.junit.jupiter.api.Test;

import it.unicam.cs.bdslab.extrarnas.controller.ExtendedBPSEQExportController.ExportItem;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.rnapolis.RNApolisCustomListener;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.rnapolis.RNApolisGrammarLexer;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.rnapolis.RNApolisGrammarParser;
import it.unicam.cs.bdslab.extrarnas.parser.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.extrarnas.view.utils.TOOL;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FullChainSequenceTest {

    @Test
    void mapsCompactedPositionsAcrossUnmodelledResidues() {
        List<Boolean> modelled = new ArrayList<>(Collections.nCopies(76, true));
        modelled.set(60, false);
        modelled.set(61, false);
        modelled.set(62, false);
        modelled.set(63, false);
        modelled.set(64, false);
        BioJavaController.FullChainSequence sequence =
                new BioJavaController.FullChainSequence(
                        "A".repeat(76), modelled);

        assertEquals(76, sequence.sequence().length());
        assertEquals(59, sequence.originalIndexForCompactedIndex(59));
        assertEquals(65, sequence.originalIndexForCompactedIndex(60));
        assertThrows(IllegalArgumentException.class,
                () -> sequence.originalIndexForCompactedIndex(71));
    }

    @Test 
    void maps_4PLXC() throws Exception, Exception, URISyntaxException {
        String pathTo4PLXC = this.getClass().getResource("/4PLX_C.3db").getPath();
        File file4plxcRnaPolis = new File(pathTo4PLXC);
        // download cif with Biojavacontroller
        BioJavaController bioJavaController = BioJavaController.getInstance();
        Path downloadedPDB = bioJavaController.downloadPDB("4PLX", this.getClass().getResource("/out").toURI().getPath().toString().substring(1));
        // change .pdb to .cif
        Path downloadedCIF = Path.of(downloadedPDB.toString().replace(".pdb", ".cif"));
        BioJavaController.FullChainSequence sequence = bioJavaController.readFullChainSequence(downloadedCIF, "C");
        var rnapolisStructure = parseRNApolis(Path.of(file4plxcRnaPolis.getAbsolutePath()));
        rnapolisStructure.remapPositions(sequence::originalIndexForCompactedIndex);
        
    }

    private ExtendedRNASecondaryStructure parseRNApolis(Path file) throws IOException {
        var lexer = new RNApolisGrammarLexer(CharStreams.fromPath(file));
        var parser = new RNApolisGrammarParser(new CommonTokenStream(lexer));
        var listener = new RNApolisCustomListener();
        ParseTreeWalker.DEFAULT.walk(listener, parser.rnapolisFile());
        List<ExtendedRNASecondaryStructure> structures = listener.getStructures();
        return structures.isEmpty() ? null : structures.get(0);
    }
}
