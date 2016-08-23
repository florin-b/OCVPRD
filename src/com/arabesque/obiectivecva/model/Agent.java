package com.arabesque.obiectivecva.model;

public class Agent {

	private String nume;
	private String cod;

	public Agent() {

	}

	public Agent(String nume, String cod) {
		super();
		this.nume = nume;
		this.cod = cod;
	}

	public String getNume() {
		return nume;
	}

	public void setNume(String nume) {
		this.nume = nume;
	}

	public String getCod() {
		return cod;
	}

	public void setCod(String cod) {
		this.cod = cod;
	}

	public String toString() {
		return nume;
	}

}