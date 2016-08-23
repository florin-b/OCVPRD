package com.stimasoft.obiectivecva.models.db_classes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Container class for the user stored in the database
 */

public class User implements Parcelable {

    private int id;
    private int userType;
    private String branchCode;
    private String code;

    private String name;
    private String surName;

    public static final int TYPE_DIRECTOR = 0;
    public static final int TYPE_DVA = 1;
    public static final int TYPE_CVA = 2;

    public User(){
    }

    public User(int userType, String branchCode, String name, String surName, String code){
        this();
        this.userType = userType;
        this.branchCode = branchCode;
        this.name = name;
        this.surName = surName;
        this.code = code;
    }

    /**
     * Complete constructor for the User class
     *
     * @param id The row id of the user in the database
     * @param code The user's code
     * @param userType The user's type (CVA / DVA)
     * @param branchCode The user's branch code
     * @param name The user's name
     * @param surName The user's surname
     */
    public User(int id, String code, int userType, String branchCode, String name, String surName){
        this(userType, branchCode, name, surName, code);
        this.id = id;
    }

    // Required procedure to enable the Stage class to be parcelable. This allows sending of the stage
    // details via Intent.
    private User(Parcel in){
        userType = in.readInt();
        branchCode = in.readString();
        name = in.readString();
        surName = in.readString();
        code = in.readString();
        id = in.readInt();
    }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(userType);
        parcel.writeString(branchCode);
        parcel.writeString(name);
        parcel.writeString(surName);
        parcel.writeString(code);
        parcel.writeInt(id);
    }

    // Getters and setters
    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

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
}
