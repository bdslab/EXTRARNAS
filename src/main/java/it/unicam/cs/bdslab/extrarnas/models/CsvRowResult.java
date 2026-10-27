package it.unicam.cs.bdslab.extrarnas.models;

public record CsvRowResult(String pdbId, String chainFilter, String modelFilter,
                           boolean successful, String error) {

    public static CsvRowResult success(String pdbId, String chainFilter, String modelFilter) {
        return new CsvRowResult(pdbId, chainFilter, modelFilter, true, "");
    }

    public static CsvRowResult failure(String pdbId, String chainFilter, String modelFilter, String error) {
        return new CsvRowResult(pdbId, chainFilter, modelFilter, false,
                error == null ? "Unknown preprocessing error" : error);
    }
}
