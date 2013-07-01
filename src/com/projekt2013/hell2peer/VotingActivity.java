/**
 * Activity which displays a voting screen to the user.
 */

package com.projekt2013.hell2peer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.RadioGroup.OnCheckedChangeListener;
import de.keyboardsurfer.android.widget.crouton.Configuration;
import de.keyboardsurfer.android.widget.crouton.Crouton;

import java.util.ArrayList;

import static com.projekt2013.hell2peer.R.string.start_service;

public class VotingActivity extends Activity {

    private static final String TAG = "VotingActivity";
    private static final Configuration CONFIGURATION_INFINITE = new Configuration.Builder()
            .setDuration(Configuration.DURATION_INFINITE).build();

    private static String e;
    private static String n;
    private static String hEmail;
    private static String hPassword;

    private Typeface fontThin;
    private RadioGroup rg;
    private RadioButton radioButtonSelected = null;
    private TextView mTitleView;
    private Button button;
    private View customCroutonView;
    private ProgressBar progressBar;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_voting);
        Log.d(TAG, "onCreate");

        context = getApplicationContext();

        Typeface fontLight = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Light.ttf");
        fontThin = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf");

        rg = (RadioGroup) findViewById(R.id.radioGroup);

        mTitleView = (TextView) findViewById(R.id.votingTitle);
        mTitleView.setTypeface(fontLight);

        TextView mTitleSmall = (TextView) findViewById(R.id.titleSmall);
        mTitleSmall.setTypeface(fontThin);
        mTitleSmall.setText(Html
                .fromHtml("<FONT COLOR=\"#99cc00\" >Android </Font>"
                        + "<FONT COLOR=\"#ffffff\" >Hell<b>2</b>Peer</Font>"
                        + "<FONT COLOR=\"#ff4444\" > Alpha</Font>"));

        button = (Button) findViewById(R.id.buttonVoting);
        button.setTypeface(fontLight);

        progressBar = (ProgressBar) findViewById(R.id.voting_progress);

        customCroutonView = getLayoutInflater().inflate(
                R.layout.crouton_voting, null);
        Button buttonService = (Button) customCroutonView
                .findViewById(R.id.buttonService);
        buttonService.setTypeface(fontLight);
        buttonService.setText(start_service);

        Intent intent = getIntent();

        ArrayList<String> candidatesArr = intent
                .getStringArrayListExtra("candidates");

        e = intent.getStringExtra("e");
        n = intent.getStringExtra("n");

        hEmail = intent.getStringExtra("hEmail");
        Log.d(TAG, hEmail);
        hPassword = intent.getStringExtra("hPassword");
        Log.d(TAG, hPassword);

        createRadioButtonGroup(candidatesArr);

        rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (!button.isEnabled()) {
                    button.setEnabled(true);
                    mTitleView.setTextColor(getResources().getColor(
                            R.color.holo_blue_light));
                }
                RadioButton radioButton = (RadioButton) findViewById(checkedId);
                radioButton.setTextColor(getResources().getColor(
                        R.color.holo_blue_light));
                if (radioButtonSelected != null) {
                    radioButtonSelected.setTextColor(getResources().getColor(
                            R.color.white));
                }
                radioButtonSelected = radioButton;
                button.setText(Html.fromHtml("Głosuję na <b>"
                        + radioButtonSelected.getText() + "</b>"));
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vote();
            }
        });

        buttonService.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent service = new Intent(context, BackgroundService.class);

                Bundle extras = new Bundle();
                extras.putString("VOTE", radioButtonSelected.getText()
                        .toString());
                extras.putString("e", e);
                extras.putString("n", n);
                extras.putString("hEmail", hEmail);
                extras.putString("hPassword", hPassword);

                service.putExtras(extras);
                Crouton.cancelAllCroutons();
                Vibrator vibrator = (Vibrator) getSystemService(getBaseContext().VIBRATOR_SERVICE);
                vibrator.vibrate(300);
                startService(service);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        Crouton.cancelAllCroutons();
    }

    private void createRadioButtonGroup(ArrayList<String> candidatesArr) {
        Log.d(TAG, "createRadioButtonGroup");

        final int size = candidatesArr.size();
        final RadioButton[] rb = new RadioButton[size];

        for (int i = 0; i < size; i++) {
            rb[i] = new RadioButton(this);
            rb[i].setTextSize(22);
            rb[i].setTypeface(fontThin);
            rg.addView(rb[i]);
            rb[i].setText(candidatesArr.get(i));
            Log.d(TAG, i + ": " + candidatesArr.get(i));
        }
    }

    private void disableRadioButtonGroup() {
        Log.d(TAG, "disableRadioButtonGroup");

        for (int i = 0; i < rg.getChildCount(); i++) {
            rg.getChildAt(i).setEnabled(false);
        }
    }

    private void showCustomViewCrouton() {
        Log.d(TAG, "showCustomViewCrouton");
        Crouton crouton = Crouton.make(this, customCroutonView);
        crouton.setConfiguration(CONFIGURATION_INFINITE);
        crouton.show();
    }

    private void vote() {
        Log.d(TAG, "vote");

        button.setEnabled(false);
        disableRadioButtonGroup();
        showCustomViewCrouton();

        progressBar.setVisibility(View.VISIBLE);
    }
}
