package com.example.android.navigationdrawerexample;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import java.text.DecimalFormat;

import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

public class HeadTracker extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    static final String TAG = "RotationSensor";
    protected int mLastAccuracy;
    private Sensor mRotationVectorSensor;
    float[] result = new float[3];
    private final float[] mRotationMatrix = new float[9];
    protected float mScaleFactor = 1;
    float rx,ry,rz;

    float tx, ty;
    /** How many bytes per float. */

    static final float INVALID = 10;
    protected static final float ANGLE_RANGE_Y = (float) (Math.PI/16);
    protected static final float ANGLE_RANGE_X = (float) (Math.PI/8);
    float mEdgeX = INVALID,
            mEdgeY = INVALID;
    protected Float MAX_SCROLL_X=null;
    protected Float MAX_SCROLL_Y=null;

    protected static final int SENSOR_RATE = 5 * 1000;
    public float[] orientation = new float[3];

    public class LocalBinder extends Binder {
        HeadTracker getService() {
            return HeadTracker.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
//register your sensor manager listener here

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mRotationVectorSensor = mSensorManager.getDefaultSensor(
                 Sensor.TYPE_ROTATION_VECTOR);
        mSensorManager.registerListener(this, mRotationVectorSensor, 10000);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("HeadTracker", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    private final int getSensor = new int();

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(this);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
            // we received a sensor event. it is a good practice to check
            // that we received the proper event
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                // convert the rotation-vector to a 4x4 matrix. the matrix
                // is interpreted by Open GL as the inverse of the
                // rotation-vector, which is what we want.


                //float w = mGLSurfaceView.getMeasuredWidth(),
                //        h = mGLSurfaceView.getMeasuredHeight();

            SensorManager.getRotationMatrixFromVector(mRotationMatrix , event.values);
            SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRotationMatrix);
            SensorManager.getOrientation(mRotationMatrix, orientation);

            ry = orientation[0];
            rx = orientation[1];
            rz = orientation[2];

            result[0] = (float) orientation[0]; //Yaw
            result[1] = (float) orientation[1]; //Pitch
            result[2] = (float) orientation[2]; //Roll
                //Log.d(TAG, "Yaw =" + ry);
                Log.d(TAG, "Pitch =" + rx);
                //Log.d(TAG, "Roll =" + rz);

                //DecimalFormat df = new DecimalFormat("0.00");
                //TextView panTextView = (TextView) this.getActivity().findViewById(R.id.PanValTextView);
                //panTextView.setText(String.valueOf(df.format(ry)));

                //TextView pitchTextView = (TextView) this.getActivity().findViewById(R.id.PitchValTextView2);
                //pitchTextView.setText(String.valueOf(df.format(rx)));

                //TextView yawTextView = (TextView) this.getActivity().findViewById(R.id.YawValTextView);
                //yawTextView.setText(String.valueOf(df.format(rz)));
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


        public int getSensor() {
           return 200;
        }



        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }


}