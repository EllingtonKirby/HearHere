package com.example.ellioc.hearhere;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CalibrationFragment.OnSubmitCalibrationValuesListener{
    public static String PREF_FILE_NAME = "HearHere_Preferences";
    private int A_CALIBRATION;
    private int B_CALIBRATION;
    private int C_CALIBRATION;
    private int D_CALIBRATION;
    private int E_CALIBRATION;
    private int F_CALIBRATION;
    private ArrayList<Integer> calibValues;

    final int PERMISSIONS_RECORD_AUDIO = 1;
    final int PERMISSIONS_WRITE_STORAGE = 2;

    final int CALIBRATION_REQUEST = 1;

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    private boolean IS_CALIBRATED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.nvView);

        drawerToggle = setupDrawerToggle();
        mDrawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        if(!hasWriteExternalStoragePermission()) {
            requestWriteExternalStoragePermission();
        }
        if(!hasRecordAudioPermission()) {
            requestRecordAudioPermission();
        }

        if (findViewById(R.id.flContent) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            WelcomeScreenFragment firstFragment = WelcomeScreenFragment.newInstance();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.flContent, firstFragment);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            // Setup drawer view
        }
        setupDrawerContent(nvDrawer);
    }


    private boolean hasRecordAudioPermission(){
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED);

        Log.i("Main Activity", "Has RECORD_AUDIO permission? " + hasPermission);
        return hasPermission;
    }

    private boolean hasWriteExternalStoragePermission(){
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        Log.i("Main Activity", "Has WRITE_EXTERNAL_STORAGE permission? " + hasPermission);
        return hasPermission;
    }

    private void requestRecordAudioPermission(){

        String requiredPermission = Manifest.permission.RECORD_AUDIO;

        // If the user previously denied this permission then show a message explaining why
        // this permission is needed
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(getApplicationContext(), "This app needs to record audio through the microphone....",
                    Toast.LENGTH_SHORT).show();
        }

        // request the permission.
        ActivityCompat.requestPermissions(this, new String[]{requiredPermission}, PERMISSIONS_RECORD_AUDIO);
    }

    private void requestWriteExternalStoragePermission(){
        String requiredPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;

        // If the user previously denied this permission then show a message explaining why
        // this permission is needed
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(getApplicationContext(), "This app needs to write to external storage....",
                    Toast.LENGTH_SHORT).show();
        }

        // request the permission.
        ActivityCompat.requestPermissions(this, new String[]{requiredPermission}, PERMISSIONS_WRITE_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {

        // This method is called when the user responds to the permissions dialog
        switch(requestCode){
            case PERMISSIONS_RECORD_AUDIO:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
            }
            case PERMISSIONS_WRITE_STORAGE:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //TODO implement logging functionality to make logging a settable option
                }
            }

        }
    }

    public void onSubmitCalibrationValues(int requestCode, int resultCode, Intent data){
        if(requestCode == CALIBRATION_REQUEST) {
            if (resultCode == RESULT_OK) {
//                A_CALIBRATION  = data.getIntExtra("left_calibration", 14);
//                B_CALIBRATION  = data.getIntExtra("top_mid_calibration", 0);
//                C_CALIBRATION  = data.getIntExtra("right_calibration", -17);
//                D_CALIBRATION  = data.getIntExtra("bot_left_calibration", 0);
//                E_CALIBRATION  = data.getIntExtra("bot_mid_calibration", 0);
//                F_CALIBRATION  = data.getIntExtra("bot_right_calibration", 0);
                IS_CALIBRATED = true;
                calibValues = data.getIntegerArrayListExtra(GameFragment.KEY_CALIBRATION);
                GameFragment fragment = GameFragment.newInstance(
                        calibValues
                );
                // Insert the fragment by replacing any existing fragment
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.flContent, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE! Make sure to override the method with only a single `Bundle` argument
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass = null;
        switch(menuItem.getItemId()) {
            case R.id.nav_first_fragment:
                fragmentClass = CalibrationFragment.class;
                break;
            case R.id.nav_second_fragment:
                if(!IS_CALIBRATED){
                    SharedPreferences sharedPreferences = getSharedPreferences(PREF_FILE_NAME, 0);
                    String prefString = sharedPreferences.getString(GameFragment.KEY_CALIBRATION, "");
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    calibValues = new ArrayList<>();
                    builder.setTitle("Alert!!!!");
                    if(prefString.equals("")) {
                        builder.setMessage("Cannot begin HearHere until locations are calibrated!");
                        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        fragmentClass = CalibrationFragment.class;
                    }
                    else{
                        String[] exploded = prefString.split(",");
                        for(String val : exploded){
                            calibValues.add(Integer.parseInt(val));
                        }
                        builder.setMessage("Starting HearHere with calibration values from last setting");
                        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        fragmentClass = GameFragment.class;
                    }
                    builder.create().show();
                }
                else {
                    fragmentClass = GameFragment.class;
                }
                break;
            case R.id.nav_third_fragment:
                fragmentClass = WelcomeScreenFragment.class;
                break;
            default:
                fragmentClass = WelcomeScreenFragment.class;
        }

        try {
            if(fragmentClass != GameFragment.class) {
                fragment = (Fragment) fragmentClass.newInstance();
            }
            else {
                fragment = GameFragment.newInstance(calibValues);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.flContent, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }
    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);

    }

}
