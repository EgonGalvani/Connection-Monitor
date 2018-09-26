package com.galvani.egon.connectionmonitor.ViewPager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.galvani.egon.connectionmonitor.Object.Connection;
import com.galvani.egon.connectionmonitor.Object.Position;
import com.galvani.egon.connectionmonitor.Observer.Proto;
import com.galvani.egon.connectionmonitor.R;
import com.galvani.egon.connectionmonitor.Utils.AppInfo;
import com.galvani.egon.connectionmonitor.Utils.HexUtils;

import java.util.ArrayList;
import java.util.Locale;

/*
* @author Egon Galvani
 */
public class ViewPagerAdapter extends PagerAdapter {

    // connections position
    private Position position;

    // list of connections shown
    private ArrayList<Connection> connections;

    // context
    private Context context;

    // interface for close click event
    private OnCloseBtnClicked onCloseBtnClicked;

    public ViewPagerAdapter(Context context, Position position, ArrayList<Connection> connections, OnCloseBtnClicked onCloseBtnClicked) {
        this.position = position;
        this.connections = connections;
        this.context = context;
        this.onCloseBtnClicked = onCloseBtnClicked;
    }

    @Override
    public int getCount() {
        return connections.size();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int index) {

        // create the viewPager view
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.carousel_element, null);

        // get the current connection and the info about the app (from uid)
        Connection currentConnection = connections.get(index);
        AppInfo appInfo = new AppInfo(Integer.parseInt(currentConnection.getUid()), context);

        // set the app icon
        ((ImageView) view.findViewById(R.id.app_icon)).setImageDrawable(appInfo.getAppIcon());

        // set the connection ip info (ip address:port)
        String ipInfo = currentConnection.getAddress() + ":" + currentConnection.getPort();
        ((TextView) view.findViewById(R.id.ip)).setText(ipInfo);

        // set the app name
        ((TextView) view.findViewById(R.id.app_name)).setText(appInfo.getAppName());

        // set the app package
        ((TextView) view.findViewById(R.id.app_package)).setText(appInfo.getAppPackage());

        // set the server position (City, Region, Country)
        String location = position.getCity() + ", " + position.getRegion() + ", " + position.getCountry();
        ((TextView) view.findViewById(R.id.position)).setText(location);

        // set the server coordinates (latitude longitude)
        String coords = String.format(Locale.getDefault(), "%.2f", position.getLatitude()) + " "
                + String.format(Locale.getDefault(), "%.2f", position.getLongitude());
        ((TextView) view.findViewById(R.id.coords)).setText(coords);

        // set the connection protocol
        ((TextView) view.findViewById(R.id.proto)).setText(currentConnection.getProto().toString());

        // set the connection state
        int stateCode = HexUtils.hexToInt((currentConnection.getConnectionState()));
        ((TextView) view.findViewById(R.id.connection_state)).setText(Proto.getConnectionStateString(stateCode));

        // call the interface event when the close button is clicked
        ((ImageView) view.findViewById(R.id.close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCloseBtnClicked.OnClick();
            }

        });

        // add the view to the viewpager
        ((ViewPager) container).addView(view);
        return view;
    }

    @Override
    public boolean isViewFromObject(@NonNull android.view.View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    // interface for close button click event
    public interface OnCloseBtnClicked {
        public void OnClick();
    }
}