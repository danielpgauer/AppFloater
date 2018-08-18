package tt.co.justins.appfloater;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

public class MainActivity extends AppCompatActivity {
    public static final int PERMISSION_REQ_CODE = 1234;
    public static final int OVERLAY_PERMISSION_REQ_CODE = 1235;

    String[] perms = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    FloatService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("AppFloater", "onCreate");

        checkPerms();

        setContentView(R.layout.activity_main);

        final Button startStopButton = (Button) findViewById(R.id.startStopButton);
        FloatService.create(this, new IFloatService() {
            @Override
            public void created(FloatService service) {
                mService = service;
                Log.v("AppFloater", "svc created");
            }

            @Override
            public void onShowHide(boolean show) {
                startStopButton.setText(show ? "Stop" : "Start");
            }
        });


        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mService.isFloating()) {
                    mService.hide();
                } else {
                    mService.show();
                }
            }
        });
    }

    public void checkPerms() {
        // Checking if device version > 22 and we need to use new permission model
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP_MR1) {
            // Checking if we can draw window overlay
            if (!Settings.canDrawOverlays(this) && BuildConfig.BUILD_TYPE.contentEquals("debug")) {
                // Requesting permission for window overlay(needed for all react-native apps)
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            }
            for(String perm : perms){
                // Checking each persmission and if denied then requesting permissions
                if(checkSelfPermission(perm) == PackageManager.PERMISSION_DENIED){
                    requestPermissions(perms, PERMISSION_REQ_CODE);
                    break;
                }
            }
        }
    }

    // Window overlay permission intent result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE && BuildConfig.BUILD_TYPE.contentEquals("debug")) {
            checkPerms();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("AppFloater", "Activity onDestroy called");

        mService.unbind();
    }

}
