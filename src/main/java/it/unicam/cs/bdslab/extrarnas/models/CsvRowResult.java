package it.unicam.cs.bdslab.extrarnas.models;

public record CsvRowResult(String pdbId, String chainFilter, boolean successful, String error) {

    public static CsvRowResult success(String pdbId, String chainFilter) {
        return new CsvRowResult(pdbId, chainFilter, true, "");
    }

    public static CsvRowResult failure(String pdbId, String chainFilter, String error) {
        return new CsvRowResult(pdbId, chainFilter, false, error == null ? "Unknown preprocessing error" : error);
    }
}
