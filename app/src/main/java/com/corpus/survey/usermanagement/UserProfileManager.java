package com.corpus.survey.usermanagement;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.corpus.survey.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by devadas.vijayan on 6/7/16.
 */
public class UserProfileManager {
    private static UserProfileManager instance;

    // HTTP Response 'contains' strings defined by http://sms.sirentext.com/Client/SMSAPI.aspx
//    public static final String AUTHENTICATION_SUCCESS_STRING = null; // Not defined
    public static final String AUTHENTICATION_FAILED_STRING = "Authentication Failed";
    public static final String AUTHENTICATION_FIELD_MISSING_STRING = "UserID, Password is required";
    public static final String AUTHENTICATION_ACCOUNT_DEACTIVATED_STRING = "Your Account is not active";
    public static final String MESSAGE_SUBMISSION_SUCCESS_STRING = "Message Submitted";
    public static final String MESSAGE_SUBMISSION_FAILED_INSUFFICIENT_BALANCE_STRING = "Insufficient Credit";


    // Internal response codes within the app
    public static final int AUTHENTICATION_SUCCESS = 1;
    public static final int AUTHENTICATION_FAILED = 2;
    public static final int AUTHENTICATION_FIELD_MISSING = 3;
    public static final int AUTHENTICATION_ACCOUNT_DEACTIVATED = 4;
    public static final int MESSAGE_SUBMISSION_SUCCESS = 5;
    public static final int MESSAGE_SUBMISSION_FAILED_INSUFFICIENT_BALANCE = 6;
    public static final int NOT_CONNECTED_TO_NETWORK = 7;

    public static final String MAIN_URL = "http://sms.sirentext.com/sms.aspx";

    public static final int MAX_TARGET_NUMBERS_PER_HTTP_POST = 100;

    private String currentUserEmail;
    private String password;

    public static UserProfileManager getInstance() {
        if (null == instance) {
            instance = new UserProfileManager();
        }
        return instance;
    }

    public int performSignIn(String email, String password, Activity activity) throws IOException {
        // Check network availability first.
        ConnectivityManager connMgr = (ConnectivityManager)
                activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            return NOT_CONNECTED_TO_NETWORK;
        }
        InputStream is = null;
        OutputStreamWriter wr = null;
        String data = URLEncoder.encode("ID", "UTF-8")
                + "=" + URLEncoder.encode(email, "UTF-8");

        data += "&" + URLEncoder.encode("Pwd", "UTF-8") + "="
                + URLEncoder.encode(password, "UTF-8");

        // Only display the first 500 characters of the retrieved web page content.
        int len = 500;

        try {
            URL url = new URL(MAIN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();
            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            int response = conn.getResponseCode();
            Log.d("Login", "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            Log.d("Login", "The contentAsString is: " + contentAsString);
            int authenticationStatus = getAuthenticationStatusFromResponseString(contentAsString);
            if (authenticationStatus == AUTHENTICATION_SUCCESS) {
                this.currentUserEmail = email;
                this.password = password;
            }
            return authenticationStatus;
        } finally {
            if (is != null) {
                is.close();
            }
            if (wr != null) {
                wr.close();
            }
        }
    }

    private int getAuthenticationStatusFromResponseString(String contentAsString) {
        int authenticationStatus = AUTHENTICATION_SUCCESS;
        if (null != contentAsString) {
            if (contentAsString.contains(AUTHENTICATION_FAILED_STRING)) {
                authenticationStatus = AUTHENTICATION_FAILED;
            } else if (contentAsString.contains(AUTHENTICATION_FIELD_MISSING_STRING)) {
                authenticationStatus = AUTHENTICATION_FIELD_MISSING;
            } else if (contentAsString.contains(AUTHENTICATION_ACCOUNT_DEACTIVATED_STRING)) {
                authenticationStatus = AUTHENTICATION_ACCOUNT_DEACTIVATED;
            }
        } else {
            authenticationStatus = AUTHENTICATION_FAILED;
        }
        //"Message Submitted" and ""Insufficient Credit" strings will not reach this method since it is response for SMS sending only.
        //Even if it reaches, it means that the authentication was successful.
        return authenticationStatus;
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    public int getUserImageId() {
        return R.mipmap.user_image;
    }

    public String getUserName() {
        return "Volkswagen Koramangala";
    }

    public String getUserEmail() {
        return currentUserEmail;
    }

    public int sendSMSviaOnlineGateway(String[] targetMobileNumbers, String message, Activity activity) throws IOException {
        // Check network availability first.
        ConnectivityManager connMgr = (ConnectivityManager)
                activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            return NOT_CONNECTED_TO_NETWORK;
        }
        InputStream is = null;
        OutputStreamWriter wr = null;
        String formattedTargetMobileNumbers = getFormattedTargetMobileNumbers(targetMobileNumbers);
        String data = URLEncoder.encode("ID", "UTF-8")
                + "=" + URLEncoder.encode(currentUserEmail, "UTF-8");
        data += "&" + URLEncoder.encode("Pwd", "UTF-8") + "="
                + URLEncoder.encode(password, "UTF-8");
        data += "&" + URLEncoder.encode("PhNo", "UTF-8") + "="
                + URLEncoder.encode(formattedTargetMobileNumbers, "UTF-8");
        data += "&" + URLEncoder.encode("Text", "UTF-8") + "="
                + URLEncoder.encode(message, "UTF-8");

        // Only display the first 500 characters of the retrieved web page content.
        int len = 500;

        try {
            URL url = new URL(MAIN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();
            wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            int response = conn.getResponseCode();
            Log.d("SendSMS", "The response is: " + response + " for message = " + message
                    + " to formattedTargetMobileNumbers = " + formattedTargetMobileNumbers);
            is = conn.getInputStream();

            // Convert the InputStream into a string
            String contentAsString = readIt(is, len);
            Log.d("SendSMS", "The contentAsString is: " + contentAsString);
            int authenticationStatus = getAuthenticationStatusFromResponseString(contentAsString);
            return authenticationStatus;
        } finally {
            if (is != null) {
                is.close();
            }
            if (wr != null) {
                wr.close();
            }
        }

    }

    private String getFormattedTargetMobileNumbers(String[] targetMobileNumbers) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < targetMobileNumbers.length; i++) {
            buffer.append(targetMobileNumbers[i]);
            if (i != targetMobileNumbers.length - 1) {
                buffer.append(",");
            }
        }
        return buffer.toString();
    }
}
