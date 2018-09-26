package com.galvani.egon.connectionmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.galvani.egon.connectionmonitor.Utils.ConnectionUtils;
import com.galvani.egon.connectionmonitor.Utils.DatabaseOpenHelper;

import java.io.IOException;

import es.dmoral.toasty.Toasty;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/*
 * @author Egon Galvani
 */

public class SplashActivity extends AppCompatActivity implements DatabaseOpenHelper.DBEvent {

    // splash screen duration
    private static final int SPLASH_MILLIS = 3000;

    // the time when the splashActivity is created is saved
    private long millis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        millis = System.currentTimeMillis();

        // Load the gif image
        GifImageView gifImageView = (GifImageView) findViewById(R.id.background_gif);
        try {
            GifDrawable gifFromResource = new GifDrawable(getResources(), R.drawable.world);
            gifImageView.setImageDrawable(gifFromResource);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // check if the network is available, if it is prepare the database
        // otherwise show an error message and close the app
        ConnectionUtils connectionUtils = new ConnectionUtils(this);
        if (connectionUtils.isNetworkAvailable()) {
            // initialize the database helper
            ((App) getApplication()).databaseOpenHelper = new DatabaseOpenHelper(this, getApplicationContext(), getFilesDir().getAbsolutePath());

            // try to prepare the database
            try {
                ((App) getApplication()).databaseOpenHelper.prepareDB();
            } catch (IOException e) {
                e.printStackTrace();
                Toasty.error(this, getString(R.string.open_db_error)).show();
                finish();
            }

        } else {
            Toasty.error(this, getString(R.string.no_connection_error)).show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 2000);
        }
    }

    @Override
    public void onDbHelperReady() {
        // when the database is prepared check if the splash time is passed,
        // if it is start mainActivity otherwise wait until it is passed

        long timePassed = System.currentTimeMillis() - millis;
        if (timePassed >= SPLASH_MILLIS) {
            startMainActivity();
        } else {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startMainActivity();
                }
            }, SPLASH_MILLIS - timePassed);
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
