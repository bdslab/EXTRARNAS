package it.unicam.cs.bdslab.extrarnas.view.utils;

import java.util.Locale;
import java.util.Map;

public enum TOOL {
    RNAVIEW("RNAVIEW"),
    RNAPOLIS_ANNOTATOR("RNAPOLIS_ANNOTATOR"),
    BARNABA("BARNABA"),
    BPNET("BPNET"),
    FR3D("FR3D"),
    X3DNA("X3DNA"),
    MC_ANNOTATE("MC_ANNOTATE");

    private final String name;

    TOOL(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /**
     * Utiltiy method to get the file name to parse based on the selected tool.
     * @param pdbId the PDB ID of the structure
     * @param chainId the chain ID of the structure
     * @return the file name to parse based on the selected tool, e.g., "1ABC_A.out" for RNAVIEW.
     */
    public String getFileNameToParse(String pdbId, String chainId) {
        return String.format("%s_%s.out", pdbId, chainId);
    }

    @Override
    public String toString() {
        return name.replaceAll("_", " ");
    }

    public boolean giveStructure() {
        return switch (this) {
            case X3DNA, FR3D, RNAVIEW -> false;
            case BARNABA, BPNET, MC_ANNOTATE, RNAPOLIS_ANNOTATOR -> true;
        };
    }

    public boolean visible() {
        return switch (this) {
            case RNAVIEW, MC_ANNOTATE, RNAPOLIS_ANNOTATOR, BARNABA, BPNET, FR3D, X3DNA  -> true;
        };
    }

    private static Map<String, TOOL> stringToToolMap = Map.of(
            "rw", RNAVIEW,
            "ra", RNAPOLIS_ANNOTATOR,
            "ba", BARNABA,
            "bp", BPNET,
            "fr", FR3D,
            "x3", X3DNA,
            "ma", MC_ANNOTATE
    );

    public static TOOL fromString(String s) {
        return stringToToolMap.getOrDefault(s.toLowerCase(Locale.ROOT), null);
    }

    public static String[] getAvailableTools() {
        return stringToToolMap.keySet().toArray(new String[0]);
    }


}
