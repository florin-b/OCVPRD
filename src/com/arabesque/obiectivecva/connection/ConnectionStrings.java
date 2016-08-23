package com.arabesque.obiectivecva.connection;

public class ConnectionStrings {

	private static ConnectionStrings instance = new ConnectionStrings();

	private String myUrl;
	private String myNamespace;

	private ConnectionStrings() {

		//PRD
		myUrl = "http://10.1.0.58/androidwebservices/service1.asmx";
		myNamespace = "http://SmartScan.org/";
		
		//TEST
		//myUrl = "http://10.1.0.58/AndroidWebServices/TESTService.asmx";
		//myNamespace = "http://SFATest.org/";

	}

	public static ConnectionStrings getInstance() {
		return instance;
	}

	public String getUrl() {
		return this.myUrl;
	}

	public String getNamespace() {
		return this.myNamespace;
	}

}
