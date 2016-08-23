package com.stimasoft.obiectivecva.models.db_classes;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Lightweight version of the Objective class, used for listings.
 */
public class ObjectiveLite {
	private int id;
	private String name; // Mandatory
	private String phaseName;
	private int phaseId;
	private Calendar expirationPhase; // Mandatory
	private Calendar authorizationEnd; // Mandatory
	private String beneficiaryName;
	private String constructorName;
	private String city;
	private int regionCode;
	private String cvaCode;

	private SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

	/**
	 * Complete constructor for the ObjectiveLite class
	 *
	 * @param id
	 *            The row id of the objective in the database
	 * @param name
	 *            The name of the objective
	 * @param phaseName
	 *            The name of the objective's current phase
	 * @param phaseId
	 *            The row id of the objective's current phase in the database
	 * @param expirationPhase
	 *            The date at which the current phase expires
	 * @param authorizationEnd
	 *            The date at which the objective's authorization expires
	 */
	public ObjectiveLite(int id, String name, String phaseName, int phaseId, Calendar expirationPhase, Calendar authorizationEnd) {
		this.id = id;
		this.name = name;
		this.phaseName = phaseName;
		this.phaseId = phaseId;
		this.expirationPhase = expirationPhase;
		this.authorizationEnd = authorizationEnd;
	}

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

	public Calendar getExpirationPhase() {
		return expirationPhase;
	}

	public void setExpirationPhase(Calendar expirationPhase) {
		this.expirationPhase = expirationPhase;
	}

	public Calendar getAuthorizationEnd() {
		return authorizationEnd;
	}

	public void setAuthorizationEnd(Calendar authorizationEnd) {
		this.authorizationEnd = authorizationEnd;
	}

	public String getPhaseName() {
		return phaseName;
	}

	public void setPhaseName(String phaseName) {
		this.phaseName = phaseName;
	}

	public int getPhaseId() {
		return phaseId;
	}

	public void setPhaseId(int phaseId) {
		this.phaseId = phaseId;
	}

	public String getAuthEndDateString() {
		return sdf.format(authorizationEnd.getTime());
	}

	public String getBeneficiaryName() {
		return beneficiaryName;
	}

	public void setBeneficiaryName(String beneficiaryName) {
		this.beneficiaryName = beneficiaryName;
	}

	public String getConstructorName() {
		return constructorName;
	}

	public void setConstructorName(String constructorName) {
		this.constructorName = constructorName;
	}

	public String getExpPhaseDateString() {
		Log.d("DBG", expirationPhase.getTime().toString());
		return sdf.format(expirationPhase.getTime());
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public int getRegionCode() {
		return regionCode;
	}

	public void setRegionCode(int regionCode) {
		this.regionCode = regionCode;
	}

	public String getCvaCode() {
		return cvaCode;
	}

	public void setCvaCode(String cvaCode) {
		this.cvaCode = cvaCode;
	}

	@Override
	public String toString() {
		return this.getName();
	}
}
