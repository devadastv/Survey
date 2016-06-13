package com.corpus.survey.sms;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.corpus.survey.R;

/**
 * Created by devadas.vijayan on 6/13/16.
 */
abstract class BaseSmsSendingTask extends AsyncTask<String, Integer, String> {

    final String message;
    SendSMSActivity activity;
    String[] numbers;
    ProgressDialog progressDialog;

    public BaseSmsSendingTask(String message, SendSMSActivity activity) {
        this.message = message;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        Log.d("SendSMS", "inside onPreExecute of BaseSmsSendingTask. About to display the progress dialog");
        progressDialog = new ProgressDialog(activity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(activity.getResources().getString(R.string.sending));
        progressDialog.setMessage(activity.getResources().getString(R.string.sending_wait_message));
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                cancel(true);
            }
        });
        progressDialog.show();
    }


    @Override
    protected final String doInBackground(String... numbers) {
        this.numbers = numbers;
        performBackgroundTask();
        return null;
    }

    // Implementations should perform background tasks in this method
    abstract String performBackgroundTask();


    @Override
    protected void onProgressUpdate(Integer... progress) {
        progressDialog.setProgress(progress[0].intValue());
    }


    @Override
    protected void onCancelled(String result) {
        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, "USER CANCELLED SMS SENDING PROCESS. Please check the status and try again", Toast.LENGTH_LONG).show();
            }
        });
    }
}
