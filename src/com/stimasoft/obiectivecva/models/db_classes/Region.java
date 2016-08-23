package com.stimasoft.obiectivecva.models.db_classes;

/**
 * Container class for the region stored in the database
 */
public class Region {

    private int id;
    private String name;
    private String code;
    private String gps;

    public Region(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public Region(String name, String code, String gps) {
        this.name = name;
        this.code = code;
        this.gps = gps;
    }

    public Region(int id, String name, String code){
        this(name, code);
        this.id = id;
    }

    /**
     * Constructor for the Region class
     *
     * @param id The row id of the region in the database
     * @param name The name of the region
     * @param code The code of the region
     * @param gps Gps coordinates for the center of the region
     */
    public Region(int id, String name, String code, String gps){
        this(name, code);
        this.id = id;
        this.gps = gps;
    }

    // Getters and setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public void setGps(String gps){ this.gps = gps;}

    public String getGps() {return gps;}
}
