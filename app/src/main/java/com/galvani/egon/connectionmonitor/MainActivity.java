package com.galvani.egon.connectionmonitor;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;

import com.galvani.egon.connectionmonitor.Object.Connection;
import com.galvani.egon.connectionmonitor.Object.ConnectionMarker;
import com.galvani.egon.connectionmonitor.Object.LocatedConnection;
import com.galvani.egon.connectionmonitor.Object.WrapContentHeightViewPager;
import com.galvani.egon.connectionmonitor.Observer.ConnectionObserver;
import com.galvani.egon.connectionmonitor.Utils.Converter;
import com.galvani.egon.connectionmonitor.Utils.DatabaseOpenHelper;
import com.galvani.egon.connectionmonitor.Utils.LocatorManager;
import com.galvani.egon.connectionmonitor.ViewPager.ViewPagerAdapter;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class MainActivity extends AppCompatActivity
        implements ConnectionObserver.ObserverEvents, OnMapReadyCallback, LocatorManager.LocatorEvents, GoogleMap.OnMarkerClickListener {

    // key for the delay field in sharedPreferences
    private final static String DELAY_KEY = "timer_delay_key";

    // list of connection and marker data
    private ArrayList<ConnectionMarker> connectionMarkers;

    // google maps
    private GoogleMap mMap;

    // database helper
    private DatabaseOpenHelper databaseOpenHelper;

    // view pager
    private WrapContentHeightViewPager viewPager;

    // progress bar for the refresh countdown
    private MaterialProgressBar refreshProgressBar;

    // handler and runnable for the refresh
    private Handler handler = new Handler();
    private Runnable runnable;

    // counter used to save how many times the runnable has been executed
    private int counter = 0;

    // time between one scan and the next
    private int refreshDelay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // try to initialize the map
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } catch (NullPointerException e) {
            e.printStackTrace();
            Toasty.error(this, getString(R.string.map_error)).show();
        }

        // open sharedPref and get the refreshDelay, if it is not saved use the default delay
        SharedPreferences sharedPreferences = getSharedPreferences(Settings.SHARED_PREF_TIMER_NAME, MODE_PRIVATE);
        refreshDelay = sharedPreferences.getInt(DELAY_KEY, Settings.DEFAULT_REFRESH_DELAY);

        // set variables
        connectionMarkers = new ArrayList<>();
        databaseOpenHelper = ((App) getApplication()).databaseOpenHelper;
        viewPager = (WrapContentHeightViewPager) findViewById(R.id.pager);

        // initialize the progress bar (countdown for the refresh)
        refreshProgressBar = (MaterialProgressBar) findViewById(R.id.progress_bar);
        refreshProgressBar.setMax(refreshDelay);
        refreshProgressBar.setProgress(0);

        // click listener to open the menu to set the refresh delay
        ImageView timeImgBtn = (ImageView) findViewById(R.id.timer);
        timeImgBtn.setOnClickListener(new TimerClickListener());

        // check if there are problems with the database helper
        if (databaseOpenHelper == null) {
            Toasty.error(this, getString(R.string.open_db_error)).show();
            finish();
        }

        // start the first scan
        refresh();
    }

    @Override
    protected void onResume() {
        // set the handler
        setHandler();

        super.onResume();
    }

    private void setHandler() {

        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                counter++; // counter of seconds passed from the last scan

                // update the progress bar countdown
                if (android.os.Build.VERSION.SDK_INT >= 24) {
                    refreshProgressBar.setProgress(counter, true);
                } else refreshProgressBar.setProgress(counter);

                // if the counter is >= of the refreshDelay start the scanning process
                // it is used >= and not == to avoid problems id the refreshDelay is set to a
                // lower value than counter thanks to the "Delay Picker"
                if (counter >= refreshDelay) {
                    refresh();
                    counter = 0;
                }

                // to repeat the tunnable every 1000 millis (1 second)
                handler.postDelayed(runnable, 1000);
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    private void refresh() {
        // fade out the viewpager container
        final View viewpagerContainer = findViewById(R.id.view_pager_container);
        if (viewpagerContainer.getVisibility() == View.VISIBLE) {
            viewpagerContainer.animate().alpha(0.0f).setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            viewpagerContainer.clearAnimation();
                            viewpagerContainer.setVisibility(View.GONE);
                        }
                    });
        }

        //clear view pager adapter
        viewPager.setAdapter(null);

        // clear map (delete markers)
        for (ConnectionMarker connectionMarker : connectionMarkers)
            connectionMarker.getMarker().remove();

        // delete markers data
        connectionMarkers.clear();

        // start the scan
        ConnectionObserver connectionObserver = new ConnectionObserver(MainActivity.this);
        connectionObserver.execute();
    }

    @Override
    public void onConnectionParsed(ArrayList<Connection> parsedConns) {
        // convert the connection parsed
        ArrayList<Connection> convertedConns = new ArrayList<>();

        for (Connection parsedConn : parsedConns) {
            convertedConns.add(Converter.convert(parsedConn));
        }

        // locate the connections
        LocatorManager locatorManager = new LocatorManager(databaseOpenHelper, this, this);
        locatorManager.setConnections(convertedConns);
        locatorManager.execute();
    }

    @Override
    public void onConnectionLocated(LocatedConnection locatedConnection) {
        // get position latitude and longitude
        float latitude = locatedConnection.getPosition().getLatitude();
        float longitude = locatedConnection.getPosition().getLongitude();

        // if latitude and longitude are different from 0 (both)
        if (latitude != 0.0 && longitude != 0.0) {
            boolean markerAdded = false;
            LatLng connectionPos = new LatLng(latitude, longitude);

            // check if there is already a marker with its position,
            // if there is add the connection to the marker connection list
            // otherwise create a new marker
            for (ConnectionMarker connectionMarker : connectionMarkers) {
                LatLng markerPosition = connectionMarker.getMarker().getPosition();

                if (markerPosition.equals(connectionPos)) {
                    connectionMarker.addConnection(locatedConnection.getConnection());
                    markerAdded = true;
                    break;
                }
            }

            if (!markerAdded) {
                ArrayList<Connection> connections = new ArrayList<>();
                connections.add(locatedConnection.getConnection());
                Marker marker = mMap.addMarker(new MarkerOptions().position(connectionPos));
                connectionMarkers.add(new ConnectionMarker(marker, locatedConnection.getPosition(), connections));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // save the map and set the settings
        mMap = googleMap;
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        // trovo il marker corrispondente
        for (ConnectionMarker connectionMarker : connectionMarkers) {
            if (connectionMarker.getMarker().equals(marker)) {

                // fade in the viewpgaercontainer
                final View viewpagerContainer = findViewById(R.id.view_pager_container);
                if (viewpagerContainer.getVisibility() == View.GONE) {
                    viewpagerContainer.setAlpha(0.0f);
                    viewpagerContainer.setVisibility(View.VISIBLE);

                    viewpagerContainer.animate().alpha(1.0f).setDuration(500).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            viewpagerContainer.clearAnimation();
                        }
                    });
                }

                // clear the past viewpager adapter
                viewPager.setAdapter(null);

                // create a new adapter
                ViewPagerAdapter pagerAdapter = new ViewPagerAdapter(this, connectionMarker.getPosition(), connectionMarker.getConnections(), new ViewPagerAdapter.OnCloseBtnClicked() {
                    @Override
                    public void OnClick() {
                        // if the close button is clicked -> fade out the viewpager container
                        viewpagerContainer.animate().alpha(0.0f).setDuration(500)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        viewpagerContainer.clearAnimation();
                                        viewpagerContainer.setVisibility(View.GONE);
                                    }
                                });
                    }
                });

                // set the adapter
                viewPager.setAdapter(pagerAdapter);

                // set the page indicator
                CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.page_indicator);
                circlePageIndicator.setViewPager(viewPager);

                break;
            }
        }

        return true;
    }

    // timer imageview click listener
    private class TimerClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // create a dialog
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
            dialogBuilder.setCancelable(true);

            // create the dialog view
            View dialogView = getLayoutInflater().inflate(R.layout.delay_picker_dialog, null);

            // set the numbepicker MAX, MIN, CURRENT_VALUE
            final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.numberPicker);
            numberPicker.setMinValue(10);
            numberPicker.setMaxValue(60);
            numberPicker.setValue(refreshDelay);

            // set number picker custom view and title
            dialogBuilder.setView(dialogView);
            dialogBuilder.setTitle(getString(R.string.delay_picker));

            // set number picker button
            dialogBuilder.setPositiveButton(getString(R.string.set), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // set the refreshDelay to the value selected by the user
                    refreshDelay = numberPicker.getValue();

                    // save the value selected in the sharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences(Settings.SHARED_PREF_TIMER_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt(DELAY_KEY, refreshDelay);
                    editor.apply();

                    // set the max value of the progress bar to the new refreshDelay
                    refreshProgressBar.setMax(refreshDelay);

                    // set Handler(the runnable is defined)
                    setHandler();
                }
            });

            // remove the runnable from the handler (while the dialog is opened the countdown is stopped)
            handler.removeCallbacks(runnable);

            // create the alert dialog and show it
            AlertDialog dialog = dialogBuilder.create();
            dialog.show();
        }
    }


}
