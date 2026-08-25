package it.unicam.cs.bdslab.extrarnas.controller;

import it.unicam.cs.bdslab.extrarnas.models.CsvRowResult;
import it.unicam.cs.bdslab.extrarnas.models.StructureInfo;

import java.util.List;

/**
 * Receives row-level preprocessing updates without introducing a JavaFX dependency in the backend.
 */
public interface CsvPreprocessingListener {

    CsvPreprocessingListener NONE = new CsvPreprocessingListener() {
        @Override
        public void onRowStarted(String pdbId, String chainFilter, int rowNumber, int totalRows) {
        }

        @Override
        public void onRowCompleted(CsvRowResult result, int completedRows, int totalRows,
                                   List<StructureInfo> workspaceStructures) {
        }
    };

    void onRowStarted(String pdbId, String chainFilter, int rowNumber, int totalRows);

    void onRowCompleted(CsvRowResult result, int completedRows, int totalRows,
                        List<StructureInfo> workspaceStructures);
}
