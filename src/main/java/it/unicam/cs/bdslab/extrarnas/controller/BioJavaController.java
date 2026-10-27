package it.unicam.cs.bdslab.extrarnas.controller;

import org.biojava.nbio.structure.*;
import org.biojava.nbio.core.util.FileDownloadUtils;
import org.biojava.nbio.structure.io.CifFileReader;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.io.PDBFileReader;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;

public class BioJavaController {

    private static final BioJavaController instance = new BioJavaController();
    private final PDBFileReader reader;
    private final CifFileReader cifReader;

    public BioJavaController() {
        FileParsingParameters parameters = new FileParsingParameters();
        parameters.setAlignSeqRes(true);
        reader = new PDBFileReader();
        reader.setFileParsingParameters(parameters);
        cifReader = new CifFileReader();
        cifReader.setFileParsingParameters(parameters);
    }

    public static BioJavaController getInstance() {
        return instance;
    }

    public List<Structure> filterById(Path path, String chainId) throws IOException {
        return filterByModelAndChain(path, chainId, "*").stream()
                .map(FilteredStructure::structure)
                .toList();
    }

    public List<Structure> filterByStar(Path path) throws IOException {
        return filterByModelAndChain(path, "*", "*").stream()
                .map(FilteredStructure::structure)
                .toList();
    }

    public List<FilteredStructure> filterByModelAndChain(Path path, String chainId,
                                                          String modelFilter) throws IOException {
        var structure = reader.getStructure(path.toFile());
        var filter = getFilter("*");
        filter = getFilter(chainId);
        Set<Integer> requestedModels = parseModelFilter(modelFilter, structure.nrModels());
        var structures = new ArrayList<FilteredStructure>();
        for (int model = 0; model < structure.nrModels(); model++) {
            if (!requestedModels.contains(model)) {
                continue;
            }
            for (Chain chain : structure.getModel(model)) {
                if (filter.test(chain)) {
                    StructureImpl singleChainStructure = new StructureImpl();
                    singleChainStructure.addChain(chain);
                    structures.add(new FilteredStructure(singleChainStructure, model));
                }
            }
        }
        return structures;
    }

    private Set<Integer> parseModelFilter(String modelFilter, int modelCount) throws IOException {
        if (modelFilter == null || modelFilter.isBlank() || modelFilter.equals("*")) {
            return java.util.stream.IntStream.range(0, modelCount).boxed()
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        }
        Set<Integer> models = new LinkedHashSet<>();
        for (String value : modelFilter.split(";")) {
            try {
                int model = Integer.parseInt(value.trim());
                if (model < 0 || model >= modelCount) {
                    throw new IOException("Requested model " + model
                            + " is outside the available range 0-" + (modelCount - 1));
                }
                models.add(model);
            } catch (NumberFormatException e) {
                throw new IOException("Invalid model filter: " + value, e);
            }
        }
        if (models.isEmpty()) {
            throw new IOException("The model filter is empty");
        }
        return models;
    }

    public record FilteredStructure(Structure structure, int model) {
    }

    public void save(Structure structure, Path dst) throws IOException {
        String pdbContent = structure.toPDB();
        String cifContent = structure.toMMCIF().replace(",", ".");
        try (FileWriter writer = new FileWriter(dst.resolveSibling(dst.getFileName()+".pdb").toFile())) {
            writer.write(pdbContent);
        }
        try (FileWriter writer = new FileWriter(dst.resolveSibling(dst.getFileName()+".cif").toFile())) {
            writer.write(cifContent);
        }

    }

    public Path downloadPDB(String pdbId, String outputFolderPath) throws StructureException, IOException {
        pdbId = normalizePdbId(pdbId);
        Path outputDirectory = Paths.get(outputFolderPath);
        Files.createDirectories(outputDirectory);
        Path pdbPath = outputDirectory.resolve(pdbId + ".pdb");
        Path cifPath = outputDirectory.resolve(pdbId + ".cif");
        Structure structure = StructureIO.getStructure(pdbId);

        if (!Files.exists(pdbPath)) {
            Files.writeString(pdbPath, structure.toPDB());
        }

        if (!Files.exists(cifPath)) {
            downloadOriginalCif(pdbId, cifPath);
        }
        
        return pdbPath;
    }
    
    private void downloadOriginalCif(String pdbId, Path destination) throws IOException {
        pdbId = normalizePdbId(pdbId);
        if (Files.exists(destination)) {
            validateOriginalCif(destination, pdbId);
            return;
        }

        URL url = URI.create("https://files.rcsb.org/download/" + pdbId + ".cif").toURL();
        Path temporaryFile = Files.createTempFile(
                destination.getParent(), destination.getFileName().toString(), ".download");
        try {
            FileDownloadUtils.downloadFile(url, temporaryFile.toFile());
            validateOriginalCif(temporaryFile, pdbId);
            try {
                Files.move(temporaryFile, destination,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                        java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, destination,
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException | RuntimeException e) {
            Files.deleteIfExists(temporaryFile);
            throw e;
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private void validateOriginalCif(Path cifPath, String pdbId) throws IOException {
        String content = Files.readString(cifPath, StandardCharsets.UTF_8);
        if (!content.contains("_entity_poly_seq")) {
            Files.deleteIfExists(cifPath);
            throw new IOException("Original CIF for " + pdbId
                    + " does not contain _entity_poly_seq: " + cifPath);
        }
    }


    private String normalizePdbId(String pdbId) {
        if (pdbId == null) {
            throw new IllegalArgumentException("PDB ID must not be null");
        }
        String normalized = pdbId.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9]{4}")) {
            throw new IllegalArgumentException(
                    "PDB ID must contain exactly four alphanumeric characters: " + pdbId);
        }
        return normalized;
    }

    public FullChainSequence readFullChainSequence(Path cifPath, String chainId)
            throws IOException {
        Structure structure = cifReader.getStructure(cifPath.toFile());
        for (Chain chain : structure.getChains()) {
            if (chainId.equals(chain.getName()) || chainId.equals(chain.getId())) {
                List<Group> groups = chain.getSeqResGroups().stream()
                        .filter(group -> group.getType() == GroupType.NUCLEOTIDE)
                        .toList();
                StringBuilder sequence = new StringBuilder(groups.size());
                List<Boolean> modelled = new ArrayList<>(groups.size());
                for (Group group : groups) {
                    sequence.append(group.getPDBName().charAt(0)); // get 1-letter code
                    modelled.add(group.has3D());
                }
                return new FullChainSequence(sequence.toString(), modelled);
            }
        }
        throw new IOException("Chain " + chainId + " was not found in " + cifPath);
    }

    public record FullChainSequence(String sequence, List<Boolean> modelled) {
        public FullChainSequence {
            modelled = List.copyOf(modelled);
            if (sequence.length() != modelled.size()) {
                throw new IllegalArgumentException("Sequence and modelled flags have different lengths");
            }
        }

        public int originalIndexForCompactedIndex(int compactedIndex) {
            int compacted = 0;
            for (int original = 0; original < modelled.size(); original++) {
                if (modelled.get(original)) {
                    if (compacted == compactedIndex) {
                        return original;
                    }
                    compacted++;
                }
            }
            if (compacted == compactedIndex) {
                return modelled.size() - 1; // allow index at the end of the sequence
            }
            throw new IllegalArgumentException("Compacted index outside modelled sequence: " + compactedIndex);
        }
    }

    public String getSequence(Structure structure) {
        StringBuilder sequence = new StringBuilder();
        for (Chain chain : structure.getChains()) {
            for (Group group : chain.getAtomGroups()) {
                if (group.getType().equals(GroupType.NUCLEOTIDE)) {
                    sequence.append(StructureTools.get1LetterCode(group.getPDBName()));
                }
            }
        }
        return sequence.toString();
    }

    private Predicate<Chain> getFilter(String allowedIds) {
        Predicate<Chain> idFilter;
        // accept all ids
        if (allowedIds.equals("*"))
            idFilter = chain -> true; // accept all IDs
            // accept only specified id
        else {
            Set<String> allowedSet = new HashSet<>(List.of(allowedIds.split(";")));
            // check both getName() (auth_asym_id, es. "2") and getId() (label_asym_id, e.g. "C").
            // The mapping/CSV are with auth_asym_id, retrivable with BioJava with getName().
            idFilter = chain -> allowedSet.contains(chain.getName())
                    || allowedSet.contains(chain.getId());
        }
        // combine id filter with rna filter
        return idFilter.and(getRNAFilter());
    }

    private Predicate<Chain> getRNAFilter() {
        return chain -> {
            String seq = chain.getAtomSequence().toUpperCase();
            return seq.matches(".*U.*"); // only RNA residues
        };
    }

}
