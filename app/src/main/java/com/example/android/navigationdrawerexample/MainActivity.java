/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.navigationdrawerexample;

import java.text.DecimalFormat;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;

/**
 * This example illustrates a common usage of the DrawerLayout widget
 * in the Android support library.
 * <p/>
 * <p>When a navigation (left) drawer is present, the host activity should detect presses of
 * the action bar's Up affordance as a signal to open and close the navigation drawer. The
 * ActionBarDrawerToggle facilitates this behavior.
 * Items within the drawer should fall into one of two categories:</p>
 * <p/>
 * <ul>
 * <li><strong>View switches</strong>. A view switch follows the same basic policies as
 * list or tab navigation in that a view switch does not create navigation history.
 * This pattern should only be used at the root activity of a task, leaving some form
 * of Up navigation active for activities further down the navigation hierarchy.</li>
 * <li><strong>Selective Up</strong>. The drawer allows the user to choose an alternate
 * parent for Up navigation. This allows a user to jump across an app's navigation
 * hierarchy at will. The application should treat this as it treats Up navigation from
 * a different task, replacing the current task stack using TaskStackBuilder or similar.
 * This is the only form of navigation drawer that should be used outside of the root
 * activity of a task.</li>
 * </ul>
 * <p/>
 * <p>Right side drawers should be used for actions, not navigation. This follows the pattern
 * established by the Action Bar that navigation should be to the left and actions to the right.
 * An action should be an operation performed on the current contents of the window,
 * for example enabling or disabling a data overlay on top of the current content.</p>
 */
public class MainActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mPlanetTitles;

    private Handler uiUpdater;
    ServiceConnection mConnection;
    boolean mBound = false;
    HeadTracker mService;


    //private SensorManager mSensorManager;
    //static final String TAG = "RotationSensor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiUpdater = new Handler();

        Intent intent = new Intent(this, HeadTracker.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        //mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        mTitle = mDrawerTitle = getTitle();
        mPlanetTitles = getResources().getStringArray(R.array.planets_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mPlanetTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
        case R.id.action_websearch:
            // create intent to perform web search for this planet
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
            // catch event that there's no activity to handle intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.app_not_available, Toast.LENGTH_LONG).show();
            }
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments
        Fragment fragment = new PlanetFragment();
        Bundle args = new Bundle();
        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mPlanetTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Fragment that appears in the "content_frame", shows a planet
     */
    class PlanetFragment extends Fragment {
        public static final String ARG_PLANET_NUMBER = "planet_number";
        public int counter;

       /* protected int mLastAccuracy;
        private Sensor mRotationVectorSensor;
        float[] result = new float[3];
        private final float[] mRotationMatrix = new float[9];
        protected float mScaleFactor = 1;
        float rx,ry,rz;

        float tx, ty;

        static final float INVALID = 10;
        protected static final float ANGLE_RANGE_Y = (float) (Math.PI/16);
        protected static final float ANGLE_RANGE_X = (float) (Math.PI/8);
        float mEdgeX = INVALID,
                mEdgeY = INVALID;
        protected Float MAX_SCROLL_X=null;
        protected Float MAX_SCROLL_Y=null;

        protected static final int SENSOR_RATE = 5 * 1000;*/

        public PlanetFragment() {
           // mRotationVectorSensor = mSensorManager.getDefaultSensor(
           //         Sensor.TYPE_ROTATION_VECTOR);
           // mSensorManager.registerListener(this, mRotationVectorSensor, 10000);
        }



        Runnable run = new Runnable() {
            @Override
            public void run() {

                float num = mService.getSensor();

                DecimalFormat df = new DecimalFormat("0.00");
                TextView panTextView = (TextView) findViewById(R.id.PanValTextView);
                panTextView.setText(String.valueOf(df.format(num)));

                TextView pitchTextView = (TextView) findViewById(R.id.PitchValTextView2);
                pitchTextView.setText(String.valueOf(df.format(counter++)));

                TextView yawTextView = (TextView) findViewById(R.id.YawValTextView);
                yawTextView.setText(String.valueOf(df.format(num)));
                uiUpdater.postDelayed(this,500);
            }
        };


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_planet, container, false);
            int i = getArguments().getInt(ARG_PLANET_NUMBER);
            String planet = getResources().getStringArray(R.array.planets_array)[i];

            int imageId = getResources().getIdentifier(planet.toLowerCase(Locale.getDefault()),
                            "drawable", getActivity().getPackageName());
            ((ImageView) rootView.findViewById(R.id.imageView)).setImageResource(imageId);
            getActivity().setTitle(planet);
            uiUpdater.post(run);
            return rootView;
        }

        private ServiceConnection mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className,
                                           IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                HeadTracker.LocalBinder binder = (HeadTracker.LocalBinder) service;
                mService = binder.getService();
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mBound = false;
            }
        };


        //public void onStart() {
        //    super.onStart();
            // enable our sensor when the activity is resumed, ask for
            // 10 ms updates.
        //    mSensorManager.registerListener(this, mRotationVectorSensor, 10000);
       // }


        //public void onStop() {
        //    super.onStop();
            // make sure to turn our sensor off when the activity is paused
        //    mSensorManager.unregisterListener(this);
        //}

        /*public void onSensorChanged(SensorEvent event) {
            // we received a sensor event. it is a good practice to check
            // that we received the proper event
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                // convert the rotation-vector to a 4x4 matrix. the matrix
                // is interpreted by Open GL as the inverse of the
                // rotation-vector, which is what we want.


                //float w = mGLSurfaceView.getMeasuredWidth(),
                //        h = mGLSurfaceView.getMeasuredHeight();

                float[] orientation = new float[3];
                SensorManager.getRotationMatrixFromVector(mRotationMatrix , event.values);
                SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRotationMatrix);
                SensorManager.getOrientation(mRotationMatrix, orientation);

                ry = orientation[0];
                rx = orientation[1];
                rz = orientation[2];


                //float velocityX = (float) (w/2 * (1/ANGLE_RANGE_X));
                //float velocityY = (float) (h/2 * (1/ANGLE_RANGE_Y));

                //if (MAX_SCROLL_X == null) MAX_SCROLL_X = w * (1-1/mScaleFactor)/2;
                //if (MAX_SCROLL_Y == null) MAX_SCROLL_Y = h * (1-1/mScaleFactor)/2;

                //if (mEdgeX == INVALID) mEdgeX = ry;
                //if (mEdgeY == INVALID) mEdgeY = rx;

                //float dx = radbox(mEdgeX - ry),
                //       dy = radbox(mEdgeY - rx);

                //if (Math.abs(dx) > MAX_SCROLL_X / velocityX) {
                //    float mx = Math.signum(dx) * (Math.abs(dx) - MAX_SCROLL_X / velocityX);
                //   mEdgeX = radbox(mEdgeX - mx);
                // }

                //if (Math.abs(dy) > MAX_SCROLL_Y / velocityY) {
                //float = Math.signum(dy) * (Math.abs(dy) - MAX_SCROLL_Y / velocityY);
                //  mEdgeY = radbox(mEdgeY - my);
                //}

                // tx = dx * velocityX;
                // ty = dy * velocityY;

                result[0] = (float) orientation[0]; //Yaw
                result[1] = (float) orientation[1]; //Pitch
                result[2] = (float) orientation[2]; //Roll
                //Log.d(TAG, "Yaw =" + ry);
                Log.d(TAG, "Pitch =" + rx);
                //Log.d(TAG, "Roll =" + rz);

                DecimalFormat df = new DecimalFormat("0.00");
                TextView panTextView = (TextView) this.getActivity().findViewById(R.id.PanValTextView);
                panTextView.setText(String.valueOf(df.format(ry)));

                TextView pitchTextView = (TextView) this.getActivity().findViewById(R.id.PitchValTextView2);
                pitchTextView.setText(String.valueOf(df.format(rx)));

                TextView yawTextView = (TextView) this.getActivity().findViewById(R.id.YawValTextView);
                yawTextView.setText(String.valueOf(df.format(rz)));
            }

            ////Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, -tx, -ty, rz, upX, upY, upZ);
            //Matrix.multiplyMM();


            // Matrix.rotateM();
            // Matrix.rotateM
            // Matrix.rotateM()
        }

        protected float radbox(float x) {
            // keep radian in [-pi: pi]
            return (float) (x + (x>Math.PI ? -2*Math.PI : x<-Math.PI ? 2*Math.PI : 0));
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }*/

    }
}