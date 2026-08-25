package it.unicam.cs.bdslab.extrarnas.models;

import java.util.List;

public record CsvPreprocessingResult(List<StructureInfo> workspaceStructures, List<CsvRowResult> rowResults) {

    public CsvPreprocessingResult {
        workspaceStructures = List.copyOf(workspaceStructures);
        rowResults = List.copyOf(rowResults);
    }
}
