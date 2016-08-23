package com.stimasoft.obiectivecva.models.db_classes;

import com.stimasoft.obiectivecva.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Container class for the objective stored in the database
 */
public class Objective {

	private int id;
	private int typeId; // Mandatory
	private String cvaCode; // Mandatory
	private int regionID; // Mandatory
	private String name; // Mandatory
	private Calendar creationDate; // Mandatory
	private int beneficiaryId; // Mandatory
	private int beneficiaryType; // Mandatory
	private Calendar authorizationStart; // Mandatory
	private Calendar authorizationEnd; // Mandatory
	private float estimationValue;
	private String address;
	private int zip;
	private String gps;
	private int stageId; // Mandatory
	private int phaseId; // Mandatory
	private Calendar expirationPhase; // Mandatory
	private int status;

	private int statusId;
	private int categoryId;

	private String numeExecutant;
	private String cuiExecutant;
	private String nrcExecutant;

	// Meserias fields
	private String numeMeserias;
	private String prenMeserias;
	private String telMeserias;
	// Meserias fields

	private String telBenef;

	private String filiala;

	private String phaseValues = null;

	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

	public static final int TYPE_NEW = 0;
	public static final int TYPE_RENOVATION = 1;

	public static final int INACTIVE = 0;
	public static final int ACTIVE = 1;

	private Objective(int typeId, String cvaCode, int regionID, String name, Calendar creationDate, int beneficiaryId, int beneficiaryType,
			Calendar authorizationStart, Calendar authorizationEnd, int stageId, int phaseId, Calendar expirationPhase) {

		this.typeId = typeId;
		this.cvaCode = cvaCode;
		this.regionID = regionID;
		this.name = name;
		this.creationDate = creationDate;
		this.beneficiaryId = beneficiaryId;
		this.beneficiaryType = beneficiaryType;
		this.authorizationStart = authorizationStart;
		this.authorizationEnd = authorizationEnd;
		this.stageId = stageId;
		this.phaseId = phaseId;
		this.expirationPhase = expirationPhase;

		this.estimationValue = 0;
		this.address = "Adresa nespecificata";
		this.zip = 0;
		this.gps = "Locatie nespecificata";
		this.status = 0;

	}

	public Objective() {

	}

	private Objective(int typeId, String cvaCode, int regionID, String name, Calendar creationDate, int beneficiaryId, int beneficiaryType,
			Calendar authorizationStart, Calendar authorizationEnd, int stageId, int phaseId, Calendar expirationPhase, float estimationValue) {

		this(typeId, cvaCode, regionID, name, creationDate, beneficiaryId, beneficiaryType, authorizationStart, authorizationEnd, stageId, phaseId,
				expirationPhase);

		this.estimationValue = estimationValue;
	}

	private Objective(int typeId, String cvaCode, int regionID, String name, Calendar creationDate, int beneficiaryId, int beneficiaryType,
			Calendar authorizationStart, Calendar authorizationEnd, int stageId, int phaseId, Calendar expirationPhase, float estimationValue, String address) {

		this(typeId, cvaCode, regionID, name, creationDate, beneficiaryId, beneficiaryType, authorizationStart, authorizationEnd, stageId, phaseId,
				expirationPhase, estimationValue);

		this.address = address;
	}

	private Objective(int typeId, String cvaCode, int regionID, String name, Calendar creationDate, int beneficiaryId, int beneficiaryType,
			Calendar authorizationStart, Calendar authorizationEnd, int stageId, int phaseId, Calendar expirationPhase, float estimationValue, String address,
			int zip) {

		this(typeId, cvaCode, regionID, name, creationDate, beneficiaryId, beneficiaryType, authorizationStart, authorizationEnd, stageId, phaseId,
				expirationPhase, estimationValue, address);

		this.zip = zip;
	}

	private Objective(int typeId, String cvaCode, int regionID, String name, Calendar creationDate, int beneficiaryId, int beneficiaryType,
			Calendar authorizationStart, Calendar authorizationEnd, int stageId, int phaseId, Calendar expirationPhase, float estimationValue, String address,
			int zip, String gps) {

		this(typeId, cvaCode, regionID, name, creationDate, beneficiaryId, beneficiaryType, authorizationStart, authorizationEnd, stageId, phaseId,
				expirationPhase, estimationValue, address, zip);

		this.gps = gps;
	}

	public Objective(int typeId, String cvaCode, int regionID, String name, Calendar creationDate, int beneficiaryId, int beneficiaryType,
			Calendar authorizationStart, Calendar authorizationEnd, float estimationValue, String address, int zip, String gps, int stageId, int phaseId,
			Calendar expirationPhase, int status) {

		this(typeId, cvaCode, regionID, name, creationDate, beneficiaryId, beneficiaryType, authorizationStart, authorizationEnd, stageId, phaseId,
				expirationPhase, estimationValue, address, zip, gps);

		this.status = status;
	}

	/**
	 * Complete constructor for the Objective class
	 *
	 * @param id
	 *            The row id of the objective in the database
	 * @param typeId
	 *            The type of the objective (new construction / renovation)
	 * @param cvaCode
	 *            The code of the CVA responsible with this objective
	 * @param regionID
	 *            The region in which this objective is assigned
	 * @param name
	 *            The name of the objective
	 * @param creationDate
	 *            The date at which the objective was created
	 * @param beneficiaryId
	 *            The row id of the beneficiary associated with this objective
	 * @param beneficiaryType
	 *            The type of the beneficiary associated with this objective
	 * @param authorizationStart
	 *            The date at which the objective was authorized
	 * @param authorizationEnd
	 *            The date at which the objective's authorization expires
	 * @param estimationValue
	 *            The estimated value of the objective
	 * @param address
	 *            The objective's address
	 * @param zip
	 *            The postal code (zip) of the objective
	 * @param gps
	 *            The gps coordinated of the objective
	 * @param stageId
	 *            The objective's current stage
	 * @param phaseId
	 *            The objective's current phase
	 * @param expirationPhase
	 *            The date at which the current phase expires
	 * @param status
	 *            The objective's status (active / archived)
	 */
	public Objective(int id, int typeId, String cvaCode, int regionID, String name, Calendar creationDate, int beneficiaryId, int beneficiaryType,
			Calendar authorizationStart, Calendar authorizationEnd, float estimationValue, String address, int zip, String gps, int stageId, int phaseId,
			Calendar expirationPhase, int status) {

		this(typeId, cvaCode, regionID, name, creationDate, beneficiaryId, beneficiaryType, authorizationStart, authorizationEnd, estimationValue, address, zip,
				gps, stageId, phaseId, expirationPhase, status);

		this.id = id;
	}

	// Getters and setters
	public String getPhaseValues() {
		return phaseValues;
	}

	public void setPhaseValues(String phaseValues) {
		this.phaseValues = phaseValues;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Calendar getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Calendar creationDate) {
		this.creationDate = creationDate;
	}

	public int getBeneficiaryId() {
		return beneficiaryId;
	}

	public void setBeneficiaryId(int beneficiaryId) {
		this.beneficiaryId = beneficiaryId;
	}

	public int getBeneficiaryType() {
		return beneficiaryType;
	}

	public void setBeneficiaryType(int beneficiaryType) {
		this.beneficiaryType = beneficiaryType;
	}

	public Calendar getAuthorizationStart() {
		return authorizationStart;
	}

	public void setAuthorizationStart(Calendar authorizationStart) {
		this.authorizationStart = authorizationStart;
	}

	public Calendar getAuthorizationEnd() {
		return authorizationEnd;
	}

	public void setAuthorizationEnd(Calendar authorizationEnd) {
		this.authorizationEnd = authorizationEnd;
	}

	public float getEstimationValue() {
		return estimationValue;
	}

	public void setEstimationValue(float estimationValue) {
		this.estimationValue = estimationValue;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getZip() {
		return zip;
	}

	public void setZip(int zip) {
		this.zip = zip;
	}

	public String getGps() {
		return gps;
	}

	public void setGps(String gps) {
		this.gps = gps;
	}

	public int getStageId() {
		return stageId;
	}

	public void setStageId(int stageId) {
		this.stageId = stageId;
	}

	public int getPhaseId() {
		return phaseId;
	}

	public void setPhaseId(int phaseId) {
		this.phaseId = phaseId;
	}

	public Calendar getExpirationPhase() {
		return expirationPhase;
	}

	public void setExpirationPhase(Calendar expirationPhase) {
		this.expirationPhase = expirationPhase;
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

	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}

	public String getCvaCode() {
		return cvaCode;
	}

	public void setCvaCode(String cvaCode) {
		this.cvaCode = cvaCode;
	}

	public int getCreationDateInfo(int field) {
		return creationDate.get(field);
	}

	public int getAuthorizationStartInfo(int field) {
		return authorizationStart.get(field);
	}

	public int getAuthorizationEndInfo(int field) {
		return authorizationEnd.get(field);
	}

	public int getExpirationPhaseInfo(int field) {
		return expirationPhase.get(field);
	}

	public String getCreationDateString() {

		return sdf.format(creationDate.getTime());
	}

	public String getAuthStartDateString() {

		return sdf.format(authorizationStart.getTime());
	}

	public String getAuthEndDateString() {

		return sdf.format(authorizationEnd.getTime());
	}

	public String getExpPhaseDateString() {

		return sdf.format(expirationPhase.getTime());
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}


	// Meserias Getters and Setters Alin

	public String getNumeMeserias() {
		return numeMeserias.trim().length() > 0 ? numeMeserias : " ";
	}

	public void setNumeMeserias(String numeMeserias) {
		this.numeMeserias = numeMeserias;
	}

	public String getPrenMeserias() {
		return prenMeserias.trim().length() > 0 ? prenMeserias : " ";
	}

	public void setPrenMeserias(String prenMeserias) {
		this.prenMeserias = prenMeserias;
	}

	public String getTelMeserias() {
		return telMeserias.trim().length() > 0 ? telMeserias : " ";
	}

	public void setTelMeserias(String telMeserias) {
		this.telMeserias = telMeserias;
	}

	// End Meserias getters and setters
	
	public String getNumeExecutant() {
		return numeExecutant.trim().length() > 0 ? numeExecutant : " ";
	}

	public void setNumeExecutant(String numeExecutant) {
		this.numeExecutant = numeExecutant;
	}

	public String getCuiExecutant() {
		return cuiExecutant.trim().length() > 0 ? cuiExecutant : " ";
	}

	public void setCuiExecutant(String cuiExecutant) {
		this.cuiExecutant = cuiExecutant;
	}

	public String getNrcExecutant() {
		return nrcExecutant.trim().length() > 0 ? nrcExecutant : " ";
	}

	public void setNrcExecutant(String nrcExecutant) {
		this.nrcExecutant = nrcExecutant;
	}

	public String getTelBenef() {
		return telBenef.trim().length() > 0 ? telBenef : " ";
	}

	public void setTelBenef(String telBenef) {
		this.telBenef = telBenef;
	}

	public String getFiliala() {
		return filiala;
	}

	public void setFiliala(String filiala) {
		this.filiala = filiala;
	}


	// Override required to store the details in the change logs
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");
		SimpleDateFormat sdf = new SimpleDateFormat(Constants.DB_DATE_FORMAT);

		result.append("Objective {" + NEW_LINE);
		result.append("Id = " + id + NEW_LINE);
		result.append("Type Id = " + typeId + NEW_LINE);
		result.append("CVA Code = " + cvaCode + NEW_LINE);
		result.append("Region Id = " + regionID + NEW_LINE);
		result.append("Name = " + name + NEW_LINE);
		result.append("Creation Date = " + sdf.format(creationDate.getTime()) + NEW_LINE);
		result.append("Beneficiary Id = " + beneficiaryId + NEW_LINE);
		result.append("Beneficiary Type = " + beneficiaryType + NEW_LINE);
		result.append("Authorization Start = " + sdf.format(authorizationStart.getTime()) + NEW_LINE);
		result.append("Authorization End = " + sdf.format(authorizationEnd.getTime()) + NEW_LINE);
		result.append("Estimated Value = " + estimationValue + NEW_LINE);
		result.append("Address = " + address + NEW_LINE);
		result.append("Zip = " + zip + NEW_LINE);
		result.append("Gps Coordinates = " + gps + NEW_LINE);
		result.append("Stage Id = " + stageId + NEW_LINE);
		result.append("Phase Id = " + phaseId + NEW_LINE);
		result.append("Status Code = " + status + NEW_LINE);
		result.append(phaseValues);
		result.append("Phase End Date = " + sdf.format(expirationPhase.getTime()) + NEW_LINE);

		result.append("StatusID = " + statusId + NEW_LINE);
		result.append("CategoryID = " + categoryId + NEW_LINE);

		result.append("}");

		return result.toString();
	}
}
