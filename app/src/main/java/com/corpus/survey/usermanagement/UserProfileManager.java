package com.corpus.survey.usermanagement;

import android.util.Log;

import com.corpus.survey.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    private String currentUserEmail;

    public static UserProfileManager getInstance() {
        if (null == instance) {
            instance = new UserProfileManager();
        }
        return instance;
    }

    public int performSignIn(String email, String password) throws IOException {
        InputStream is = null;
        OutputStreamWriter wr = null;
        String data = URLEncoder.encode("ID", "UTF-8")
                + "=" + URLEncoder.encode(email, "UTF-8");

        data += "&" + URLEncoder.encode("Pwd", "UTF-8") + "="
                + URLEncoder.encode(password, "UTF-8");

        // Only display the first 500 characters of the retrieved web page content.
        int len = 500;

        try {
            URL url = new URL("http://sms.sirentext.com/sms.aspx");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(7000);
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
        }
        else
        {
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


}
