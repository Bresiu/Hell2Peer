package com.projekt2013.hell2peer;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class BackgroundService extends IntentService {

    private static final String TAG = "BackgroundService";
    private static final int DELAY = 2000;
    private static String vote;
    private static String e;
    private static String n;
    private static String hEmail;
    private static String hPassword;

    public BackgroundService() {
        super("Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        Bundle extras = intent.getExtras();
        vote = extras.getString("VOTE");
        e = extras.getString("e");
        n = extras.getString("n");
        hEmail = intent.getStringExtra("hEmail");
        hPassword = intent.getStringExtra("hPassword");
        Log.d(TAG, hEmail);
        Log.d(TAG, hPassword);

        RSA rsa = new RSA(new BigInteger(e), new BigInteger(n));

        byte[] encrypted = vote.getBytes();
        encrypted = rsa.encrypt(encrypted);
        String encodedVote = Arrays.toString(encrypted);

        serverConnection(encodedVote);

        //TODO
        for (int i = 0; i < 3; i++) {
            Vibrator vibrator = (Vibrator) getSystemService(getBaseContext().VIBRATOR_SERVICE);
            vibrator.vibrate(30);
            SystemClock.sleep(DELAY);
        }
    }
    private void serverConnection(String encodedVote) {
        Log.d(TAG, "serverConnection");
        String parameters = "login=" + hEmail + "&haslo=" + hPassword + "&parcel=" + encodedVote;

        try {
            URL url = new URL("http://vote-polska.jelastic.dogado.eu/parcel");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");

            OutputStreamWriter request = new OutputStreamWriter(connection.getOutputStream());
            request.write(parameters);
            request.flush();
            request.close();
            String line;
            InputStreamReader isr = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            String response = sb.toString();
            Log.d(TAG, response);
            isr.close();
            reader.close();
        } catch (IOException e) {
            // TODO
        }
    }
}
