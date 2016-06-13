package com.corpus.survey.sms;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import com.corpus.survey.R;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by devadas.vijayan on 6/13/16.
 */
class OnlineSMSSendingTask extends BaseSmsSendingTask {

    public OnlineSMSSendingTask(String message, SendSMSActivity activity) {
        super(message, activity);
    }

    @Override
    String performBackgroundTask() {
        int targetNumbersCount = numbers.length;
        Log.d("SendSMS", "inside doInBackground of OnlineSMSSendingTask with targetNumbersCount = " + targetNumbersCount);
        for (int i = 0; i < targetNumbersCount; i++) {
            sendSMSviaOnlineGateway(numbers[i]);
            publishProgress((i + 1) * 100 / targetNumbersCount);
        }
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, "All SMS have been sent", Toast.LENGTH_SHORT).show();
            }
        });
        return "SUCCESS";
    }


    @Override
    protected void onCancelled(String result) {
        // TODO: Implement it
    }

    protected void onPostExecute(String result) {
        progressDialog.dismiss();
    }

    private void sendSMSviaOnlineGateway(String mobileNumber) {
        // Replace with your username
        String user = "devadastv";

        // Replace with your API KEY (We have sent API KEY on activation email, also available on panel)
        String apikey = "dfK3GuD02aiwPs0lrHII";

        // Replace with the destination mobile Number to which you want to send sms
        String mobile = mobileNumber;

        // Replace if you have your own Sender ID, else donot change
        String senderid = "MYTEXT";

        // For Plain Text, use "txt" ; for Unicode symbols or regional Languages like hindi/tamil/kannada use "uni"
        String type = "txt";

        //Prepare Url
        URLConnection myURLConnection = null;
        URL myURL = null;
        BufferedReader reader = null;

        //encoding message
        String encoded_message = URLEncoder.encode(message);

        //Send SMS API
        String mainUrl = "http://smshorizon.co.in/api/sendsms.php?";

        //Prepare parameter string
        StringBuilder sbPostData = new StringBuilder(mainUrl);
        sbPostData.append("user=" + user);
        sbPostData.append("&apikey=" + apikey);
        sbPostData.append("&message=" + encoded_message);
        sbPostData.append("&mobile=" + mobile);
        sbPostData.append("&senderid=" + senderid);
        sbPostData.append("&type=" + type);

        //final string
        mainUrl = sbPostData.toString();
        try {
            //prepare connection
            myURL = new URL(mainUrl);
            myURLConnection = myURL.openConnection();
            myURLConnection.connect();
            reader = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
            //reading response
            String response;
            while ((response = reader.readLine()) != null)
                //print response
                Log.d("SendSMS", response);
            System.out.println(response);

            //finally close connection
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
