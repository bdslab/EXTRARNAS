package it.unicam.cs.bdslab.extrarnas.models;

public enum StructureStatus {
    QUEUED,
    PROCESSING,
    LOADED,
    PROCESSED,
    ERROR;

    public String translate() {
        return switch (this) {
            case QUEUED -> "Queued";
            case PROCESSING -> "Processing";
            case LOADED -> "Loaded";
            case PROCESSED -> "Processed";
            case ERROR -> "Error";
        };
    }
}
