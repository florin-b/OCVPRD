package com.stimasoft.obiectivecva.models.db_classes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Container class for the stage stored in the database
 */
public class Stage implements Parcelable {

    private int id;
    private String name;
    private int hierarchy;
    private int status;

    public static final int INACTIVE = 0;
    public static final int ACTIVE = 1;

    public Stage(String name, int hierarchy) {
        this.name = name;
        this.hierarchy = hierarchy;
        this.status = 0;
    }

    public Stage(int id, String name, int hierarchy) {
    	this.id = id;
        this.name = name;
        this.hierarchy = hierarchy;
        this.status = 0;
    }
    
    public Stage(String name, int hierarchy, int status) {
        this(name, hierarchy);
        this.status = status;
    }

    /**
     * Constructor for the Stage class
     *
     * @param id The row id of the stage in the database
     * @param name The name of the stage
     * @param hierarchy The hierarchical position of the stage amongst the other stages
     * @param status The status of the stage
     */
    public Stage(int id, String name, int hierarchy, int status){
        this(name, hierarchy, status);
        this.id = id;
    }

    // Required procedure to enable the Stage class to be parcelable. This allows sending of the stage
    // details via Intent.
    protected Stage(Parcel in) {
        id = in.readInt();
        name = in.readString();
        hierarchy = in.readInt();
        status = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeInt(hierarchy);
        parcel.writeInt(status);
    }

    public static final Creator<Stage> CREATOR = new Creator<Stage>() {
        @Override
        public Stage createFromParcel(Parcel in) {
            return new Stage(in);
        }

        @Override
        public Stage[] newArray(int size) {
            return new Stage[size];
        }
    };

    // Getters and setters
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

    public int getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(int hierarchy) {
        this.hierarchy = hierarchy;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }


}

