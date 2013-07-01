/**
 * Activity which displays a login screen to the user.
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
import android.os.StrictMode;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;

public class LoginActivity extends Activity {
    /**
     * Tag used in debugging
     */
    private static final String TAG = "LoginActivity";
    private static final Configuration CONFIGURATION_INFINITE = new Configuration.Builder()
            .setDuration(Configuration.DURATION_INFINITE).build();

    private static String e;
    private static String n;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    // Values for email and password at the time of the login attempt.
    private String mEmail;
    private String mPassword;
    private String hEmail;
    private String hPassword;
    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private Button signInButton;
    private ProgressBar mLoginProgress;
    private View customCroutonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        StrictMode.ThreadPolicy policy = new StrictMode.
                ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Typeface fontLight = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Light.ttf");
        Typeface fontThin = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf");

        // Set up the login form.
        TextView mTitleView = (TextView) findViewById(R.id.title);
        mTitleView.setTypeface(fontLight);

        mTitleView.setText(Html
                .fromHtml("<FONT COLOR=\"#99cc00\" >Android </Font>"
                        + "<FONT COLOR=\"#ffffff\" >Hell<b>2</b>Peer</Font>"
                        + "<FONT COLOR=\"#ff4444\" > Alpha</Font>"));

        mEmailView = (EditText) findViewById(R.id.email);
        mEmailView.setTypeface(fontThin);
        mEmailView.setText(mEmail);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setTypeface(fontThin);
        mPasswordView
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int id,
                                                  KeyEvent keyEvent) {
                        if (id == R.id.login || id == EditorInfo.IME_NULL) {
                            attemptLogin();
                            return true;
                        }
                        return false;
                    }
                });

        mLoginProgress = (ProgressBar) findViewById(R.id.login_progress);

        // TODO
        // mEmailView.setText("login@mail.com");
        // mPasswordView.setText("haslo1");

        signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setTypeface(fontLight);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        /**
         * Crouton
         */
        customCroutonView = getLayoutInflater().inflate(R.layout.crouton_login,
                null);

        // cancel button
        Button cancelCroutonButton = (Button) customCroutonView
                .findViewById(R.id.croutonDelete);
        cancelCroutonButton.setTypeface(fontLight);
        cancelCroutonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Crouton.cancelAllCroutons();
            }
        });

        // new account button
        Button newAccountButton = (Button) customCroutonView
                .findViewById(R.id.croutonOk);
        newAccountButton.setTypeface(fontLight);
        newAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent register = new Intent(LoginActivity.this,
                        RegisterActivity.class);
                Bundle extras = new Bundle();
                extras.putString("mEmail", mEmail);
                // extras.putString("mPassword", mPassword);
                register.putExtras(extras);
                startActivity(register);
            }
        });

        // title
        TextView croutonRegisterTitle = (TextView) customCroutonView
                .findViewById(R.id.croutonLoginTitle);
        croutonRegisterTitle.setTypeface(fontThin);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }

	/*
     * @Override protected void onPause() { Log.d(TAG, "onPause");
	 * super.onPause(); Crouton.cancelAllCroutons(); //finish(); }
	 * 
	 * @Override protected void onStop() { Log.d(TAG, "onStop"); super.onStop();
	 * Crouton.cancelAllCroutons(); //finish(); }
	 */

    // Hash function
    String toHash(String credential) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest md = MessageDigest.getInstance("SHA-512", "BC");
        byte[] digestCredential = md.digest(credential.getBytes());

        return String.format("%0128x", new BigInteger(1, digestCredential));
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    void attemptLogin() {
        Log.d(TAG, "attemptLogin");
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        mEmail = mEmailView.getText().toString();
        mPassword = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password.
        if (TextUtils.isEmpty(mPassword)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        } else if (mPassword.length() < 6) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(mEmail)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!mEmail.contains("@")) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            try {
                hEmail = toHash(mEmail);
                hPassword = toHash(mPassword);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            }

            showProgress(true);
            disableButton(true);
            mAuthTask = new UserLoginTask();
            mAuthTask.execute((Void) null);
        }
    }

    private void showProgress(final boolean show) {
        mLoginProgress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    private void disableButton(final boolean enable) {
        signInButton.setEnabled(!enable);
    }

    //TODO

    /**
     * Simulating server connection and checking if credentials are valid
     */
    private String serverConnection() {
        Log.d(TAG, "serverConnection");
        String parameters = "reg=0&login=" + hEmail + "&haslo=" + hPassword;

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

    private void showCustomViewCrouton() {
        Log.d(TAG, "showCustomViewCrouton");
        Crouton croutonRegister = Crouton.make(this, customCroutonView);
        croutonRegister.setConfiguration(CONFIGURATION_INFINITE);
        croutonRegister.show();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private class UserLoginTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            return serverConnection();
        }

        @Override
        protected void onPostExecute(String result) {
            mAuthTask = null;
            showProgress(false);
            if (result.charAt(0) == '0') {
                Log.d(TAG, "Succesfully logged in. Starting new activity");
                Intent voting = new Intent(LoginActivity.this,
                        VotingActivity.class);
                Bundle extras = new Bundle();

                e = result.substring(2, 156);
                n = result.substring(157, 774);
                ArrayList<String> candidates = new ArrayList<String>(
                        Arrays.asList(result.substring(775, result.length()-1).split(":")));

                //TODO
                Log.d(TAG, e);
                Log.d(TAG, n);

                for (int i=0; i<candidates.size();i++){
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
                mPasswordView
                        .setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            } else if (result.charAt(0) == '2') {
                Log.d(TAG, "login failed: incorrect login");
                disableButton(false);

                showCustomViewCrouton();

                mEmailView.setError(getString(R.string.error_invalid_email));
                mEmailView.requestFocus();
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
}
