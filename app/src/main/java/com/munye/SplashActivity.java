package com.munye;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.munye.user.R;
import com.munye.parse.AsyncTaskCompleteListener;
import com.munye.parse.HttpRequester;
import com.munye.utils.AndyUtils;
import com.munye.utils.Const;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.Message;

public class SplashActivity extends ActionBarBaseActivity implements View.OnClickListener, AsyncTaskCompleteListener {

    private Button btnSplashSignIn , btnSplashRegister;
    private Timer checkInternetTimer;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        preferenceHelper.putTimeZone(getTimeZone());
        btnSplashSignIn = (Button)findViewById(R.id.btnSplashSignIn);
        btnSplashRegister = (Button)findViewById(R.id.btnSplashRegister);
        btnSplashSignIn.setOnClickListener(this);
        btnSplashRegister.setOnClickListener(this);

        if (AppStatus.getInstance(this).isOnline()) {

           // Toast.makeText(getApplicationContext(), "Online", Toast.LENGTH_SHORT).show();

        } else {

          //  Toast.makeText(getApplicationContext(), "Offline", Toast.LENGTH_SHORT).show();
        }

    }

    public static class AppStatus {

        private static AppStatus instance = new AppStatus();
         static Context context;
        ConnectivityManager connectivityManager;
        NetworkInfo wifiInfo, mobileInfo;
        boolean connected = false;

        public static AppStatus getInstance(Context ctx) {
            context = ctx.getApplicationContext();
            return instance;
        }

        public boolean isOnline() {
            try {
                connectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                connected = networkInfo != null && networkInfo.isAvailable() &&
                        networkInfo.isConnected();
                return connected;


            } catch (Exception e) {
                System.out.println("CheckConnectivity Exception: " + e.getMessage());

            }
            return connected;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopInternetCheck();
        checkInternetStatus();
        if(!AndyUtils.isNetworkAvailable(this))
            startInternetCheck();

    }

    private void checkInternetStatus(){
        if(AndyUtils.isNetworkAvailable(this) && preferenceHelper.getId() == null){
            stopInternetCheck();
            getSettings();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btnSplashRegister.setVisibility(View.VISIBLE);
                    btnSplashSignIn.setVisibility(View.VISIBLE);
                }
            });
            closeInternetDialog();
        }
        else if(AndyUtils.isNetworkAvailable(this) && preferenceHelper.getId() != null){
            getSettings();
        }
        else {
            openInternetDialog(this);
        }
    }


    private void startInternetCheck(){

        checkInternetTimer = new Timer();
        TimerTask taskCheckInterner = new TimerTask() {
            @Override
            public void run() {
                checkInternetStatus();
            }
        };
        checkInternetTimer.scheduleAtFixedRate(taskCheckInterner, 0 , 5000);
    }

    private void stopInternetCheck(){
        if(checkInternetTimer != null){
            checkInternetTimer.cancel();
            checkInternetTimer.purge();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        stopInternetCheck();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnSplashSignIn:
                checkInternetConenction();
                startActivity(new Intent(SplashActivity.this , SignInActivity.class));
                break;

            case R.id.btnSplashRegister:
                startActivity(new Intent(SplashActivity.this , RegisterActivity.class));
                checkInternetConenction();
                break;

            default:
                AndyUtils.generateLog("No action");
                break;
        }
    }

    private String getTimeZone()
    {
        return java.util.TimeZone.getDefault().getID();
    }



    private void getSettings(){
        HashMap<String , String> map = new HashMap<>();
        map.put(Const.URL , Const.ServiceType.GET_SETTINGS);

        new HttpRequester(this , map , Const.ServiceCode.GET_SETTINGS , Const.httpRequestType.POST , this);
    }


    @Override
    public void onTaskCompleted(String response, int serviceCode) {

        if(serviceCode == Const.ServiceCode.GET_SETTINGS && dataParser.isSuccess(response)){
            dataParser.parseSettings(response);
            if(preferenceHelper.getId() != null){
                stopInternetCheck();
                startActivity(new Intent(this , MapActivity.class));
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopInternetCheck();
    }

    private InputStream openHttpConnection(String urlStr) {
        InputStream in = null;
        int resCode = -1;

        try {
            URL url = new URL(urlStr);
            URLConnection urlConn = url.openConnection();

            if (!(urlConn instanceof HttpURLConnection)) {
                throw new IOException("URL is not an Http URL");
            }

            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("POST");
            httpConn.connect();
            resCode = httpConn.getResponseCode();

            if (resCode == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        }catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }

    //Checking network state
    private boolean checkInternetConenction() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec
                =(ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() ==
                android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
            Toast.makeText(this, " Connected to a network", Toast.LENGTH_LONG).show();
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() ==
                                android.net.NetworkInfo.State.DISCONNECTED  ) {
            Toast.makeText(this, " Not Connected to a network ", Toast.LENGTH_LONG).show();

            return false;
        }
        return false;
    }

    private void downloadImage(String urlStr) {
        progressDialog = ProgressDialog.show(this, "", "Downloading Image from " + urlStr);
        final String url = urlStr;

        new Thread() {
            public void run() {
                InputStream in = null;
                try {
                    in = openHttpConnection(url);
                    in.close();
                }catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }.start();
    }
}
