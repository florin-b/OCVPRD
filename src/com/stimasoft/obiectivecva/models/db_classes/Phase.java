package com.stimasoft.obiectivecva.models.db_classes;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Container class for the phase stored in the database
 */
public class Phase implements Parcelable {

	private int id;
	private int stageId;
	private String name;
	private int days;
	private int hierarchy;
	private int status;

	public static final int INACTIVE = 0;
	public static final int ACTIVE = 1;

	public Phase(String name, int days, int hierarchy) {
		this.name = name;
		this.days = days;
		this.hierarchy = hierarchy;
		this.stageId = 0;
		this.status = 0;
	}
	
	public Phase(int id, String name, int hierarchy) {
    	this.id = id;
        this.name = name;
        this.hierarchy = hierarchy;
        this.status = 0;
    }
	
	public Phase(String name, int days, int hierarchy, int stageId) {
		this(name, days, hierarchy);
		this.stageId = stageId;
	}

	public Phase(String name, int days, int hierarchy, int stageId, int status) {
		this(name, days, hierarchy, stageId);
		this.status = status;
	}

	/**
	 * Constructor for the Phase class
	 *
	 * @param id
	 *            The row id of the phase in the database
	 * @param stageId
	 *            The id of the stage that this phase belongs to
	 * @param name
	 *            The name of the phase
	 * @param days
	 *            The default duration of the phase
	 * @param hierarchy
	 *            The hierarchical position of the phase in the stage
	 * @param status
	 *            The phase's status
	 */
	public Phase(int id, int stageId, String name, int days, int hierarchy, int status) {
		this(name, days, hierarchy, stageId, status);
		this.id = id;
	}

	// Required procedure to enable the Phase class to be parcelable. This
	// allows sending of the phase
	// details via Intent.
	protected Phase(Parcel in) {
		id = in.readInt();
		stageId = in.readInt();
		name = in.readString();
		days = in.readInt();
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
		parcel.writeInt(stageId);
		parcel.writeString(name);
		parcel.writeInt(days);
		parcel.writeInt(hierarchy);
		parcel.writeInt(status);
	}

	public static final Creator<Phase> CREATOR = new Creator<Phase>() {
		@Override
		public Phase createFromParcel(Parcel in) {
			return new Phase(in);
		}

		@Override
		public Phase[] newArray(int size) {
			return new Phase[size];
		}
	};

	// Getters and setters
	public int getStageId() {
		return stageId;
	}

	public void setStageId(int stageId) {
		this.stageId = stageId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Phase [id=" + id + ", stageId=" + stageId + ", name=" + name + ", days=" + days + ", hierarchy=" + hierarchy + ", status=" + status + "]";
	}

}

