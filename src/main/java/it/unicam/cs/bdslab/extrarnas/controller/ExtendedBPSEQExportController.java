package it.unicam.cs.bdslab.extrarnas.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import it.unicam.cs.bdslab.extrarnas.parser.listeners.JSON.JSONLexer;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.JSON.JSONParser;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.barnaba.BarnabaCustomListener;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.barnaba.BarnabaGrammarLexer;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.barnaba.BarnabaGrammarParser;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.bpnet.BpnetGrammarLexer;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.bpnet.BpnetGrammarParser;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.bpnet.BpnetParserCustomListener;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.fred.JSONFr3dListener;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.mcannotate.McAnnotateCustomListener;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.mcannotate.McAnnotateGrammarLexer;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.mcannotate.McAnnotateGrammarParser;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.rnapolis.RNApolisCustomListener;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.rnapolis.RNApolisGrammarLexer;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.rnapolis.RNApolisGrammarParser;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.rnaview.RNAviewCustomListener;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.rnaview.RNAviewGrammarLexer;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.rnaview.RNAviewGrammarParser;
import it.unicam.cs.bdslab.extrarnas.parser.listeners.x3dna.JSONX3dnaListener;
import it.unicam.cs.bdslab.extrarnas.parser.models.ExtendedRNASecondaryStructure;
import it.unicam.cs.bdslab.extrarnas.parser.output.RNASecondaryStructurePrinter;
import it.unicam.cs.bdslab.extrarnas.view.utils.TOOL;

public class ExtendedBPSEQExportController {

    public static final Logger logger = Logger.getLogger(ExtendedBPSEQExportController.class.getName());

    private static final ExtendedBPSEQExportController instance = new ExtendedBPSEQExportController();
    private final RNASecondaryStructurePrinter printer = new RNASecondaryStructurePrinter();

    private ExtendedBPSEQExportController() {
    }

    public static ExtendedBPSEQExportController getInstance() {
        return instance;
    }

    public int exportForTool(TOOL tool, Path sharedDirectory,
        RNASecondaryStructurePrinter.OutputFormat secondaryStrcutureFormat,
        RNASecondaryStructurePrinter.OutputFormat extendendStructureFormat
    ) throws IOException {
        if (tool == null || sharedDirectory == null) return 0;

        List<ExportItem> structures = loadStructures(tool, sharedDirectory);
        if (structures.isEmpty()) {
            logger.info("No structures found for tool " + tool.getName() + " in " + sharedDirectory);
            return 0;
        }

        Path outputDir = sharedDirectory.resolve("output");
        Files.createDirectories(outputDir);

        int exported = 0;
        for (ExportItem item : structures) {
            BioJavaController.FullChainSequence fullSequence =
                    loadFullChainSequence(sharedDirectory, item.baseName());
            if (tool == TOOL.RNAPOLIS_ANNOTATOR || tool == TOOL.RNAVIEW) {
                item.structure().remapPositions(fullSequence::originalIndexForCompactedIndex);
            }
            item.structure().setSequence(fullSequence.sequence());

            if (secondaryStrcutureFormat != null) {
                String content = printer.printCanonicalBPSEQ(item.structure());
                String fileName = sanitize(item.baseName()) + item.suffix() + "_" + tool.getName() + ".bpseq.txt";
                Path outputFile = outputDir.resolve(fileName);
                Files.writeString(outputFile, content, StandardCharsets.UTF_8);
            }
            if (extendendStructureFormat != null) {
                String content = printer.printExtendedBPSEQ(item.structure());
                String fileName = sanitize(item.baseName()) + item.suffix() + "_" + tool.getName() + ".bpseqe.txt";
                Path outputFile = outputDir.resolve(fileName);
                Files.writeString(outputFile, content, StandardCharsets.UTF_8);
            }
            exported++;
        }

        return exported;
    }

    private BioJavaController.FullChainSequence loadFullChainSequence(
            Path sharedDirectory, String baseName) throws IOException {
        int separator = baseName.indexOf('_');
        if (separator < 1 || separator == baseName.length() - 1) {
            throw new IOException("Cannot determine chain from tool output name: " + baseName);
        }
        String pdbId = baseName.substring(0, separator);
        String chainId = baseName.substring(separator + 1);
        Path cifPath = sharedDirectory.resolve(pdbId + ".cif");
        // If the Cif file does not exist and there is no PDB file, we have an error.
        // If the Cif file does not exist but there is a PDB file, we have a manual PDB
        if (!Files.isRegularFile(cifPath) && !Files.exists(sharedDirectory.resolve(pdbId + ".pdb"))) {
            throw new IOException("Cannot find CIF file for " + pdbId + " in " + sharedDirectory);
        } else {
            if (!Files.isRegularFile(cifPath)) {
                logger.warning("CIF file for " + pdbId + " does not exist in " + sharedDirectory
                        + ", but a PDB file exists. Using PDB file instead.");
                cifPath = sharedDirectory.resolve(pdbId + ".pdb");
            }
        }
        return BioJavaController.getInstance().readFullChainSequence(cifPath, chainId);
    }

    public Path getOutputDirectory(Path sharedDirectory) {
        return sharedDirectory.resolve("output");
    }

    public List<ExportItem> loadStructures(TOOL tool, Path sharedDirectory) throws IOException {
        return switch (tool) {
            case RNAVIEW -> parseRNAView(sharedDirectory.resolve("rnaview-output"));
            case RNAPOLIS_ANNOTATOR -> parseRNApolis(sharedDirectory.resolve("rnapolis-output"));
            case BARNABA -> parseBarnaba(sharedDirectory.resolve("barnaba-output"));
            case BPNET -> parseBPNET(sharedDirectory.resolve("bpnet-output"));
            case FR3D -> parseFR3D(sharedDirectory.resolve("fr3d-output"));
            case X3DNA -> parseX3DNA(sharedDirectory.resolve("x3dna-output"));
            case MC_ANNOTATE -> parseMCAnnotate(sharedDirectory.resolve("mc-annotate-output"));
        };
    }

    private List<ExportItem> parseRNAView(Path folder) throws IOException {
        List<ExportItem> result = new ArrayList<>();
        for (Path file : listFiles(folder, "pdb.out")) {
            var lexer = new RNAviewGrammarLexer(CharStreams.fromPath(file));
            var parser = new RNAviewGrammarParser(new CommonTokenStream(lexer));
            var listener = new RNAviewCustomListener();
            ParseTreeWalker.DEFAULT.walk(listener, parser.rnaviewFile());
            result.add(new ExportItem(baseNameFor(TOOL.RNAVIEW, file), "", listener.getStructure()));
        }
        return result;
    }

    private List<ExportItem> parseRNApolis(Path folder) throws IOException {
        List<ExportItem> result = new ArrayList<>();
        for (Path file : listFiles(folder, ".3db")) {
            var lexer = new RNApolisGrammarLexer(CharStreams.fromPath(file));
            var parser = new RNApolisGrammarParser(new CommonTokenStream(lexer));
            var listener = new RNApolisCustomListener();
            ParseTreeWalker.DEFAULT.walk(listener, parser.rnapolisFile());
            List<ExtendedRNASecondaryStructure> structures = listener.getStructures();
            for (int i = 0; i < structures.size(); i++) {
                String suffix = structures.size() > 1 ? "_" + (i + 1) : "";
                result.add(new ExportItem(baseNameFor(TOOL.RNAPOLIS_ANNOTATOR, file), suffix, structures.get(i)));
            }
        }
        return result;
    }

    
    private List<ExportItem> parseBarnaba(Path folder) throws IOException {
        List<ExportItem> result = new ArrayList<>();
        for (Path file : listFiles(folder, ".ANNOTATE.pairing.out")) {
            var lexer = new BarnabaGrammarLexer(CharStreams.fromPath(file));
            var parser = new BarnabaGrammarParser(new CommonTokenStream(lexer));
            var listener = new BarnabaCustomListener();
            ParseTreeWalker.DEFAULT.walk(listener, parser.barnabaFile());
            result.add(new ExportItem(baseNameFor(TOOL.BARNABA, file), "", listener.getStructure()));
        }
        return result;
    }

    private List<ExportItem> parseBPNET(Path folder) throws IOException {
        List<ExportItem> result = new ArrayList<>();
        for (Path file : listFiles(folder, ".out")) {
            var lexer = new BpnetGrammarLexer(CharStreams.fromPath(file));
            var parser = new BpnetGrammarParser(new CommonTokenStream(lexer));
            var listener = new BpnetParserCustomListener();
            ParseTreeWalker.DEFAULT.walk(listener, parser.bpnetFile());
            result.add(new ExportItem(baseNameFor(TOOL.BPNET, file), "", listener.getStructure()));
        }
        return result;
    }

    private List<ExportItem> parseFR3D(Path folder) throws IOException {
        List<ExportItem> result = new ArrayList<>();
        for (Path file : listFiles(folder, ".json")) {
            var lexer = new JSONLexer(CharStreams.fromPath(file));
            var parser = new JSONParser(new CommonTokenStream(lexer));
            var listener = new JSONFr3dListener();
            ParseTreeWalker.DEFAULT.walk(listener, parser.json());
            result.add(new ExportItem(baseNameFor(TOOL.FR3D, file), "", listener.getStructure()));
        }
        return result;
    }

    private List<ExportItem> parseX3DNA(Path folder) throws IOException {
        List<ExportItem> result = new ArrayList<>();
        for (Path file : listFiles(folder, ".json")) {
            var lexer = new JSONLexer(CharStreams.fromPath(file));
            var parser = new JSONParser(new CommonTokenStream(lexer));
            var listener = new JSONX3dnaListener();
            ParseTreeWalker.DEFAULT.walk(listener, parser.json());
            result.add(new ExportItem(baseNameFor(TOOL.X3DNA, file), "", listener.getStructure()));
        }
        return result;
    }

    private List<ExportItem> parseMCAnnotate(Path folder) throws IOException {
        List<ExportItem> result = new ArrayList<>();
        for (Path file : listFiles(folder, ".txt")) {
            var lexer = new McAnnotateGrammarLexer(CharStreams.fromPath(file));
            var parser = new McAnnotateGrammarParser(new CommonTokenStream(lexer));
            var listener = new McAnnotateCustomListener();
            ParseTreeWalker.DEFAULT.walk(listener, parser.mcAnnotateFile());
            result.add(new ExportItem(baseNameFor(TOOL.MC_ANNOTATE, file), "", listener.getStructure()));
        }
        return result;
    }

    private List<Path> listFiles(Path folder, String suffix) throws IOException {
        if (!Files.exists(folder) || !Files.isDirectory(folder)) {
            return List.of();
        }

        try (Stream<Path> stream = Files.list(folder)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(suffix))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
        }
    }

    private String baseNameFor(TOOL tool, Path file) {
        String name = file.getFileName().toString();

        return Arrays.stream((switch (tool) {
            case RNAVIEW -> stripSuffixes(name, ".pdb.out", ".out");
            case RNAPOLIS_ANNOTATOR -> stripSuffixes(name, ".3db");
            case BARNABA -> stripSuffixes(name, ".pdb.ANNOTATE.pairing.out", ".pdb.ANNOTATE.stacking.out", ".out", ".cif.ANNOTATE.pairing.out");
            case BPNET -> {
                String cleaned = stripSuffixes(name, ".out");
                int split = cleaned.indexOf('.');
                yield split > 0 ? cleaned.substring(0, split) : cleaned;
            }
            case FR3D -> stripSuffixes(name, "_basepair.json", ".json");
            case X3DNA -> stripSuffixes(name, "_dssr.json", ".json");
            case MC_ANNOTATE -> stripSuffixes(name, ".txt");
        }).split("_"))
                .limit(2)
                .collect(Collectors.joining("_"))
                .toUpperCase(Locale.ROOT);
    }

    private static String stripSuffixes(String value, String... suffixes) {
        String result = value;
        for (String suffix : suffixes) {
            if (result.endsWith(suffix)) {
                result = result.substring(0, result.length() - suffix.length());
                break;
            }
        }
        return result;
    }

    private String sanitize(String value) {
        String normalized = Objects.requireNonNullElse(value, "unknown").trim();
        if (normalized.isEmpty()) return "unknown";
        return normalized
                .replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .toUpperCase(Locale.ROOT);
    }

    public record ExportItem(String baseName, String suffix, ExtendedRNASecondaryStructure structure) {
    }
}