package com.stimasoft.obiectivecva.models.db_classes;

/**
 * Container class for the beneficiary stored in the database
 */
public class Beneficiary {

	private int id; // Mandatory
	private int regionID; // Mandatory
	private String name; // Mandatory
	private int type;
	private String cui;
	private String nr_rc;
	private String cnp;
	private int status;
	private String cvaCode;

	public static final int TYPE_INDIVIDUAL = 0;
	public static final int TYPE_LEGAL = 1;

	public static final int INACTIVE = 0;
	public static final int ACTIVE = 1;

	private Beneficiary(int regionID, String name) {
		this.regionID = regionID;
		this.name = name;

		this.id = -1;
		this.type = -1;
		this.cui = "0";
		this.nr_rc = "0";
		this.cnp = "0";
		this.status = 1;

	}

	public Beneficiary() {

	}

	public Beneficiary(int regionID, String name, int type) {
		this(regionID, name);
		this.type = type;
	}

	private Beneficiary(int regionID, String name, int type, String cui) {
		this(regionID, name, type);
		this.cui = cui;
	}

	public Beneficiary(int regionID, String name, int type, String cui, String nr_rc) {
		this(regionID, name, type, cui);
		this.nr_rc = nr_rc;
	}

	private Beneficiary(int regionID, String name, int type, String cui, String nr_rc, String cnp) {
		this(regionID, name, type, cui, nr_rc);
		this.cnp = cnp;
	}

	public Beneficiary(int regionID, String name, int type, String cui, String nr_rc, String cnp, int status) {
		this(regionID, name, type, cui, nr_rc, cnp);
		this.status = status;
	}

	/**
	 * Constructor for the Beneficiary class
	 *
	 * @param id
	 *            The row id of the beneficiary in the database
	 * @param regionID
	 *            Beneficiary region id
	 * @param name
	 *            Beneficiary name
	 * @param type
	 *            Beneficiary type (legal or individual)
	 * @param cui
	 *            Beneficiary CUI
	 * @param nr_rc
	 *            Beneficiary Nr_Rc
	 * @param cnp
	 *            Beneficiary CNP
	 * @param status
	 *            Beneficiary status (Active, Inactive)
	 */
	public Beneficiary(int id, int regionID, String name, int type, String cui, String nr_rc, String cnp, int status) {
		this(regionID, name, type, cui, nr_rc, cnp, status);
		this.id = id;
	}

	// Getters and setters
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}

	public String getNr_rc() {
		return nr_rc;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getCui() {
		return cui;
	}

	public void setCui(String cui) {
		this.cui = cui;
	}

	public String getNrRc() {
		return nr_rc;
	}

	public void setNr_rc(String nr_rc) {
		this.nr_rc = nr_rc;
	}

	public String getCnp() {
		return cnp;
	}

	public void setCnp(String cnp) {
		this.cnp = cnp;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCvaCode() {
		return cvaCode;
	}

	public void setCvaCode(String cvaCode) {
		this.cvaCode = cvaCode;
	}

}
