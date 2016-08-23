package com.arabesque.obiectivecva;

public class ObjectivePhase {

	private int id;
	private int phase_id;
	private int objective_id;
	private int days;
	private String phase_start;
	private String phase_end;
	private String cvaCode;

	public ObjectivePhase() {

	}

	public ObjectivePhase(int id, int phase_id, int objective_id, int days, String phase_start, String phase_end) {
		super();
		this.id = id;
		this.phase_id = phase_id;
		this.objective_id = objective_id;
		this.days = days;
		this.phase_start = phase_start;
		this.phase_end = phase_end;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPhase_id() {
		return phase_id;
	}

	public void setPhase_id(int phase_id) {
		this.phase_id = phase_id;
	}

	public int getObjective_id() {
		return objective_id;
	}

	public void setObjective_id(int objective_id) {
		this.objective_id = objective_id;
	}

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public String getPhase_start() {
		return phase_start;
	}

	public void setPhase_start(String phase_start) {
		this.phase_start = phase_start;
	}

	public String getPhase_end() {
		return phase_end;
	}

	public void setPhase_end(String phase_end) {
		this.phase_end = phase_end;
	}

	public String getCvaCode() {
		return cvaCode;
	}

	public void setCvaCode(String cvaCode) {
		this.cvaCode = cvaCode;
	}

	@Override
	public String toString() {
		return "ObjectivePhase [id=" + id + ", phase_id=" + phase_id + ", objective_id=" + objective_id + ", days=" + days + ", phase_start=" + phase_start
				+ ", phase_end=" + phase_end + ", cvaCode=" + cvaCode + "]";
	}

}
