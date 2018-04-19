package com.munye;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.Toast;

import com.munye.utils.PreferenceHelper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by adminmini on 2018/04/16.
 */

public class BackgroundTask extends AsyncTask<String,Void, String> {

    Context ctx;
    private PreferenceHelper preferenceHelper;

    BackgroundTask(Context ctx){

        this.ctx = ctx;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        String reg_url = "http://192.168.0.102/webapp/register.php";
        String login_url = "http://192.168.0.102/webapp/login.php";
        String method = params[0];

        if (method.equals("register")){

            String userName = params[1];
            //String email = params[2];
            String user_name = params[3];
            String userEmail = params[4];
            String userContact = params[5];
            String userAddress = params[6];

            try {
                URL url = new URL(reg_url);

                //Create an object of HttpURLConnection
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                OutputStream os = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

                String data = URLEncoder.encode("name", "UTF-8")+"="+URLEncoder.encode(userName, "UTF-8")+"&"+
                        //URLEncoder.encode("email", "UTF-8")+"="+URLEncoder.encode(email, "UTF-8")+"&"+
                        URLEncoder.encode("user_name", "UTF-8")+"="+URLEncoder.encode(user_name, "UTF-8")+"&"+
                        URLEncoder.encode("user_pass", "UTF-8")+"="+URLEncoder.encode(userEmail, "UTF-8")+"&"+
                        URLEncoder.encode("contact", "UTF-8")+"="+URLEncoder.encode(userContact, "UTF-8")+"&"+
                        URLEncoder.encode("address", "UTF-8")+"="+URLEncoder.encode(userAddress, "UTF-8");
                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                os.close();

                //Get the respond from the server
                InputStream inputStream = httpURLConnection.getInputStream();
                inputStream.close();
                return "Registration Successful...";
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(String result) {
        Toast.makeText(ctx, result, Toast.LENGTH_LONG).show();
    }

    public String execute(String method, EditText edtRegisterUserName, EditText edtRegisterEmail,
                          EditText edtRegisterPassword, EditText edtRegisterConatctNo, EditText edtRegisterAddress) {

       edtRegisterUserName.getText().toString();
       edtRegisterEmail.getText().toString();
       edtRegisterPassword.getText().toString();
       edtRegisterConatctNo.getText().toString();
       edtRegisterAddress.getText().toString();

       if (method.isEmpty()){
           return "Error while performing the process";
       }
        return method;
    }
}
