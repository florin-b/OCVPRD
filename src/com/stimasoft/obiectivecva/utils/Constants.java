package com.stimasoft.obiectivecva.utils;

/**
 * Storage class for constants used throughout the application
 */
public class Constants {
    // User Types
    public static final int USER_DIRECTOR_RETAIL = 0;
    public static final int USER_DIRECTOR = 1;
    public static final int USER_CONSULTANT = 2;

    // Branch Codes
    public static final String CODE_BRANCH01 = "BC07";
    public static final String CODE_BRANCH02 = "CJ08";

    // User Codes
    public static final String CODE_USER01 = "98756329";
    public static final String CVA_CODE = "cvaCode";
    
    
    // Intent Keys
    public static final String KEY_USER_DETAILS = "userDetails";
    public static final String KEY_COORDINATES = "coordinates";
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_PURPOSE = "purpose";

    // ActivityForResult codes;
    public static final int CODE_ADD_FROM_LIST = 0;
    public static final int CODE_ADD_FROM_MAP = 1;
    public static final int CODE_EDIT_FROM_LIST = 2;
    public static final int CODE_EDIT_FROM_MAP = 3;

    // Values
    public static final String VALUE_ADD = "add";
    public static final String VALUE_EDIT = "edit";

    public static final int ALARM_REQUEST_CODE = 123;
    public static final String NOTIFICATIONS = "com.stimasoft.obiectivecva.NOTIFICATIONS";
    public static final int OBJECTIVE_EXPIRES_NOTIFICATION = 1337;

    public static final String USER_DATE_FORMAT = "dd-MM-yyyy";
    public static final String DB_DATE_FORMAT = "yyyy-MM-dd 00:00:01";


    // Objectives purpose
    public static final int OBJECTIVES_ONGOING = 1;
    public static final int OBJECTIVES_ARCHIVE = 0;
    public static final String OBJECTIVES_MODE = "objectives_mode";

    public static final String KEY_FLAG = "flag";
    public static final int FLAG_FILTERS_OPEN = 10;
    public static final int FLAG_FILTERS_CLOSED = 11;

    // START and END alarm interval
    public static int ALARM_START_TIME = 9;
    public static int ALARM_END_TIME = 19;

    public static String KEY_MAP_LAUNCH = "launched_from_map";
    public static String KEY_LIST_LAUNCH = "launched_from_list";
}
