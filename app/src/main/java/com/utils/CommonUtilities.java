package com.utils;


public class CommonUtilities {
    public static final String TOLLURL = "https://tce.cit.api.here.com/2/calculateroute.json?app_id=";

  public static final String SERVER = "https://www.loop.ooo/";

    public static final String SERVER_FOLDER_PATH = "";
    public static final String SERVER_WEBSERVICE_PATH = SERVER_FOLDER_PATH + "webservice_shark.php?";

    public static final String PAYMENTLINK = SERVER + "assets/libraries/webview/payment_configuration_trip.php?";

    public static final String SERVER_URL = SERVER + SERVER_FOLDER_PATH;
    public static final String SERVER_URL_WEBSERVICE = SERVER + SERVER_WEBSERVICE_PATH + "?";
    public static final String SERVER_URL_PHOTOS = SERVER_URL + "webimages/";

    public static final String USER_PHOTO_PATH = CommonUtilities.SERVER_URL_PHOTOS + "upload/Passenger/";
    public static final String PROVIDER_PHOTO_PATH = CommonUtilities.SERVER_URL_PHOTOS + "upload/Driver/";
    public static final String COMPANY_PHOTO_PATH = CommonUtilities.SERVER_URL_PHOTOS + "upload/Company/";

    public static final String LINKEDINLOGINLINK = SERVER + "linkedin-login/linkedin-app.php";
    public static String OriginalDateFormate = "dd MMM, yyyy (EEE)";
    public static String WithoutDayFormat = "dd MMM, yyyy";
    public static String OriginalTimeFormate = "hh:mm aa";

}