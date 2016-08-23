package com.stimasoft.obiectivecva.models.db_classes;

/**
 * Container class for the branch stored in the database
 */
public class Branch {

    private int id;
    private String name;
    private String regionCode;

    public Branch(String name, String regionCode) {
        this.name = name;
        this.regionCode = regionCode;
    }

    /**
     * Constructor for the Branch class
     *
     * @param id The row id of the branch in the database
     * @param name The name of the branch
     * @param regionCode The region code of the branch
     */
    public Branch(int id, String name, String regionCode) {
        this(name,regionCode);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }
}
