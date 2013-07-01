/**
 * Activity which displays a registration screen to the user.
 */

package com.projekt2013.hell2peer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;

public class RegisterActivity extends Activity {

    private static final String TAG = "RegisterActivity";
    private static final Configuration CONFIGURATION_INFINITE = new Configuration.Builder()
            .setDuration(Configuration.DURATION_INFINITE).build();
    private static String e;
    private static String n;
    private String mEmail;
    private String mPassword;
    private String mPasswordA;
    private String hEmail;
    private String hPassword;
    private EditText mEmailViewReg;
    private EditText passwordReg;
    private EditText passwordRegA;
    private Button registerButton;
    private ProgressBar registerProgress;
    private View customCroutonView;
    private UserRegisterTask mAuthTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        Typeface fontLight = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Light.ttf");
        Typeface fontThin = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf");

        // Set up the login form.
        TextView mTitleView = (TextView) findViewById(R.id.titleReg);
        mTitleView.setTypeface(fontLight);

        mEmailViewReg = (EditText) findViewById(R.id.emailReg);
        mEmailViewReg.setTypeface(fontThin);


        passwordReg = (EditText) findViewById(R.id.passwordReg);
        passwordReg.setTypeface(fontThin);

        passwordRegA = (EditText) findViewById(R.id.passwordRegA);
        passwordRegA.setTypeface(fontThin);

        registerProgress = (ProgressBar) findViewById(R.id.registerProgress);

        mTitleView.setText(Html
                .fromHtml("<FONT COLOR=\"#99cc00\" >Android </Font>"
                        + "<FONT COLOR=\"#ffffff\" >Hell<b>2</b>Peer</Font>"
                        + "<FONT COLOR=\"#ff4444\" > Alpha</Font>"));

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        setmEmail(extras.getString("mEmail"));
        mEmailViewReg.setText(mEmail);
        //setmPassword(extras.getString("hPassword"));

        registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setTypeface(fontLight);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });
    }

    void attemptRegister() {
        Log.d(TAG, "attemptRegister");
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailViewReg.setError(null);
        passwordReg.setError(null);

        // Store values at the time of the login attempt.
        mEmail = mEmailViewReg.getText().toString();
        Log.d(TAG, "login: " + mEmail);
        mPassword = passwordReg.getText().toString();
        Log.d(TAG, "haslo: " + mPassword);
        mPasswordA = passwordRegA.getText().toString();


        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            passwordReg.setError(getString(R.string.error_field_required));
            focusView = passwordReg;
            cancel = true;
        } else if (mPassword.length() < 6) {
            passwordReg.setError(getString(R.string.error_invalid_password));
            focusView = passwordReg;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mEmail)) {
            mEmailViewReg.setError(getString(R.string.error_field_required));
            focusView = mEmailViewReg;
            cancel = true;
        } else if (!mEmail.contains("@")) {
            mEmailViewReg.setError(getString(R.string.error_invalid_email));
            focusView = mEmailViewReg;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            try {
                hEmail = toHash(mEmail);
                Log.d(TAG, hEmail);
                hPassword = toHash(mPassword);
                Log.d(TAG, hPassword);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            }

            showProgress(true);
            disableButton(true);
            mAuthTask = new UserRegisterTask();
            mAuthTask.execute((Void) null);
        }
    }

    private void showProgress(final boolean show) {
        registerProgress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        registerProgress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void disableButton(final boolean enable) {
        registerButton.setEnabled(!enable);
    }

    //TODO

    /**
     * Simulating server connection and checking if credentials are valid
     */
    private String serverConnection() {
        Log.d(TAG, "serverConnection");
        String parameters = "reg=1&login=" + hEmail + "&haslo=" + hPassword;

        try {
            URL url = new URL("http://vote-polska.jelastic.dogado.eu/vexternal");
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
            return response;
        } catch (IOException e) {
            // TODO
        }
        return "3";
    }

    //TODO
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    // Hash function
    String toHash(String credential) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest md = MessageDigest.getInstance("SHA-512", "BC");
        byte[] digestCredential = md.digest(credential.getBytes());

        return String.format("%0128x", new BigInteger(1, digestCredential));
    }

    private void showCustomViewCrouton() {
        Log.d(TAG, "showCustomViewCrouton");
        Crouton croutonRegister = Crouton.make(this, customCroutonView);
        croutonRegister.setConfiguration(CONFIGURATION_INFINITE);
        croutonRegister.show();
    }

    /**
     * @param mEmail the mEmail to set
     */
    void setmEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class UserRegisterTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            Log.d(TAG, "serverConnection");
            return serverConnection();
        }

        @Override
        protected void onPostExecute(String result) {
            mAuthTask = null;
            showProgress(false);
            if (result.charAt(0) == '0') {
                Log.d(TAG, "Succesfully logged in. Starting new activity");
                Intent voting = new Intent(RegisterActivity.this,
                        VotingActivity.class);
                Bundle extras = new Bundle();
                e = result.split(":")[1];
                n = result.split(":")[2];
                //e = result.substring(2, 156);
                //n = result.substring(157, 774);
                ArrayList<String> candidates = new ArrayList<String>(
                        Arrays.asList(result.substring(776, result.length() - 1).split(":")));

                //TODO
                Log.d(TAG, e);
                Log.d(TAG, n);

                for (int i = 0; i < candidates.size(); i++) {
                    Log.d(TAG, candidates.get(i));
                }

                extras.putString("e", e);
                extras.putString("n", n);
                extras.putString("hEmail", hEmail);
                extras.putString("hPassword", hPassword);
                extras.putStringArrayList("candidates", candidates);

                voting.putExtras(extras);
                startActivity(voting);
                finish();

            } else if (result.charAt(0) == '1') {
                Log.d(TAG, "login failed: incorrect password");
                disableButton(false);
                passwordReg.setError(getString(R.string.error_incorrect_password));
                passwordReg.requestFocus();
            } else if (result.charAt(0) == '2') {
                Log.d(TAG, "login failed: incorrect login");
                disableButton(false);

                showCustomViewCrouton();

                mEmailViewReg.setError(getString(R.string.error_invalid_email));
                mEmailViewReg.requestFocus();
            } else {
                Log.d(TAG, "something went wrong");
                disableButton(false);
            }
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "on cancel");
            mAuthTask = null;
            showProgress(false);
            disableButton(false);
        }
    }

    /**
     * @param mPassword the mPassword to set
     */
    /*
    void setmPassword(String mPassword) {
        this.hPassword = mPassword;
    }*/
}
