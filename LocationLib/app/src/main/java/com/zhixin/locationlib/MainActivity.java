package com.zhixin.locationlib;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.zhixin.roav.location.output.ILocationManagerWrapper;
import com.zhixin.roav.location.output.ILocationRecordHelper;
import com.zhixin.roav.location.output.LocationChangeVo;
import com.zhixin.roav.location.output.LocationInstanceManager;
import com.zhixin.roav.location.output.LocationRecordListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.zhixin.roav.location.output.LocationConstant.MY_PERMISSIONS_REQUEST_LOCATION;

public class MainActivity extends AppCompatActivity {
    boolean isLocating = false;
    boolean isRecording = false;
    private ILocationManagerWrapper locationManagerWrapper;
    private Button lastLocationControl;
    private ILocationRecordHelper locationHelper;
    private TextView lastLocationTV;
    private Button locateControl;
    private TextView locate;
    private TextView recordControl;
    private TextView record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);
        locationManagerWrapper = LocationInstanceManager.getLocationManagerWrapper();
        locationHelper = LocationInstanceManager.getLocationHelper();
        lastLocationControl = findViewById(R.id.last_location_control);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);
        lastLocationControl.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!queryPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) ||
                        !queryPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    return;
                }
                Location lastLocation = locationManagerWrapper.getLastLocation(getApplicationContext());
                if (lastLocation != null) {
                    String showStr = "lat:" + lastLocation.getLatitude() + ":lng" + lastLocation.getLongitude();
                    lastLocationTV.setText(showStr);
                } else {
                    lastLocationTV.setText("no location");
                }
            }
        });
        lastLocationTV = findViewById(R.id.last_location);
        locateControl = findViewById(R.id.locate_control);
        locateControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!queryPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) ||
                        !queryPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    return;
                }
                locate.setText("");
                locationHelper.cancelRecord();
                isRecording = false;
                isLocating = !isLocating;
                if (isLocating) {
                    locationManagerWrapper.cancelLocationRequest(getApplicationContext());
                    locationManagerWrapper.startLocationRequest(getApplicationContext());
                } else {
                    locationManagerWrapper.cancelLocationRequest(getApplicationContext());
                }

                locateControl.setText(isLocating ? "locate stop" : "locate start");
            }
        });
        locate = findViewById(R.id.locate);
        recordControl = findViewById(R.id.record_control);
        recordControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!queryPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) ||
                        !queryPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    return;
                }
                record.setText("");
                locationManagerWrapper.cancelLocationRequest(getApplicationContext());
                isLocating = false;
                isRecording = !isRecording;
                if (isRecording) {
                    locationHelper.cancelRecord();
                    locationHelper.startRecordLocation(new LocationRecordListener() {
                        @Override
                        public void onFailed() {
                            record.setText("failed record");
                        }

                        @Override
                        public void onSuccess(Location location) {
                            String showStr = "lat:" + location.getLatitude() + " lng:" + location.getLongitude();
                            record.setText(showStr);
                        }
                    });
                } else {
                    locationHelper.cancelRecord();
                }
            }
        });
        record = findViewById(R.id.record);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationget(LocationChangeVo vo) {
        if (!isLocating) {
            return;
        }
        Location location = vo.getLocation();
        if (location == null) {
            return;
        }
        String showStr = "lat:" + location.getLatitude() + " Lng:" + location.getLongitude();
        locate.setText(showStr);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /**
     * Query a pointed permission that whether has be granted.
     *
     * @param context    The context.
     * @param permission The permission that to query.
     * @return Whether the permission has be granted.
     */
    public static boolean queryPermission(Context context, String permission) {
        // For Android < Android M, self permissions are always granted.
        boolean result = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int targetSdkVersion = 0;
            try {
                final PackageInfo info = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), 0);
                targetSdkVersion = info.applicationInfo.targetSdkVersion;
            } catch (PackageManager.NameNotFoundException e) {
                // never happened
            }
            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                // targetSdkVersion >= Android M, we can
                // use Context#checkSelfPermission
                result = context.checkSelfPermission(permission)
                        == PackageManager.PERMISSION_GRANTED;
            } else {
                // targetSdkVersion < Android M, we have to use PermissionChecker
                result = PermissionChecker.checkSelfPermission(context, permission)
                        == PermissionChecker.PERMISSION_GRANTED;
            }
        }
        return result;
    }
}
