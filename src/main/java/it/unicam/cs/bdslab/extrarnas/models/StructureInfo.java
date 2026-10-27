package it.unicam.cs.bdslab.extrarnas.models;

/**
 * Represents the structure information of an RNA loaded from a file
 */
public class StructureInfo {
    public String name;
    public String chain;
    public String model;
    public String location;
    public StructureStatus status;
    public String error;

    public StructureInfo(String name, String chain, String location, StructureStatus status) {
        this(name, chain, "0", location, status, "");
    }

    public StructureInfo(String name, String chain, String location, StructureStatus status, String error) {
        this(name, chain, "0", location, status, error);
    }

    public StructureInfo(String name, String chain, String model, String location,
                         StructureStatus status) {
        this(name, chain, model, location, status, "");
    }

    public StructureInfo(String name, String chain, String model, String location,
                         StructureStatus status, String error) {
        this.name = name;
        this.chain = chain;
        this.model = model == null ? "" : model;
        this.location = location;
        this.status = status;
        this.error = error == null ? "" : error;
    }

    public StructureInfo(String name, String chain, String location) {
        this(name, chain, location, StructureStatus.LOADED);
    }

    public String getName() {
        return name;
    }

    public String getChain() {
        return chain;
    }

    public String getModel() {
        return model;
    }

    public String getLocation() {
        return location;
    }

    public StructureStatus getStatus() {
        return status;
    }

    public void setStatus(StructureStatus status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error == null ? "" : error;
    }
}
