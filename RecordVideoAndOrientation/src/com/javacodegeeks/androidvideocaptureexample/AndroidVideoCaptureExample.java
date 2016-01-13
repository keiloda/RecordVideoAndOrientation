package com.javacodegeeks.androidvideocaptureexample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidVideoCaptureExample extends Activity implements SensorEventListener {

	private Camera mCamera;
	private CameraPreview mPreview;
	private MediaRecorder mediaRecorder;
	private Button capture, switchCamera;
	private Context myContext;
	private LinearLayout cameraPreview;
	private boolean cameraFront = false;
	private File orientatoinFile;
	private FileOutputStream FOS;
	private SensorManager mSensorManager;
	private Sensor mRotationVector;
	private float[] mVectorOrientation = new float[3];
	private float[] mRotationVectorValues = new float[5];
	private int mScreenOrientation;
	private OrientationView mVectorView, mVectorViewDegrees;
	private OutputStreamWriter myOutWriter;
	private long timeStartRecording,prev;
	private boolean firstCur;
	private TextView recording_signal;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		myContext = this;

		try{
			mVectorView = (OrientationView)findViewById(R.id.vectorOrientationView);
			mVectorView.setTitle("Orientation: Rad");
			mVectorViewDegrees = (OrientationView)findViewById(R.id.vectorOrientationViewDegrees);
			mVectorViewDegrees.setTitle("Orientation: Deg");
			mVectorViewDegrees.degrees(true);

			mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			mRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
			recording_signal=(TextView)findViewById(R.id.record_signal);
		}
		catch(Exception e){
			Log.e("test","error is: "+ e.toString());
		}

		initialize();
	}

	private int findFrontFacingCamera() {
		int cameraId = -1;
		// Search for the front facing camera
		int numberOfCameras = Camera.getNumberOfCameras();
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
				cameraId = i;
				cameraFront = true;
				break;
			}
		}
		return cameraId;
	}

	private int findBackFacingCamera() {
		int cameraId = -1;
		// Search for the back facing camera
		// get the number of cameras
		int numberOfCameras = Camera.getNumberOfCameras();
		// for every camera check
		for (int i = 0; i < numberOfCameras; i++) {
			CameraInfo info = new CameraInfo();
			Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
				cameraId = i;
				cameraFront = false;
				break;
			}
		}
		return cameraId;
	}

	public void onResume() {
		super.onResume();
		//register sensor listeners and add relevant OrientationView
		if (mRotationVector != null) {
			mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
		}
		if (!hasCamera(myContext)) {
			Toast toast = Toast.makeText(myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG);
			toast.show();
			finish();
		}
		if (mCamera == null) {
			// if the front facing camera does not exist
			if (findFrontFacingCamera() < 0) {
				Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
				switchCamera.setVisibility(View.GONE);
			}
			mCamera = Camera.open(findBackFacingCamera());
			mPreview.refreshCamera(mCamera);
		}
		//register sensor listeners and add relevant OrientationView
		if (mRotationVector != null) {
			mSensorManager.registerListener(this, mRotationVector, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	public void initialize() {
		cameraPreview = (LinearLayout) findViewById(R.id.camera_preview);

		mPreview = new CameraPreview(myContext, mCamera);
		cameraPreview.addView(mPreview);

		capture = (Button) findViewById(R.id.button_capture);
		capture.setOnClickListener(captrureListener);

		switchCamera = (Button) findViewById(R.id.button_ChangeCamera);
		switchCamera.setOnClickListener(switchCameraListener);
	}

	OnClickListener switchCameraListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// get the number of cameras
			if (!recording) {
				int camerasNumber = Camera.getNumberOfCameras();
				if (camerasNumber > 1) {
					// release the old camera instance
					// switch camera, from the front and the back and vice versa

					releaseCamera();
					chooseCamera();
				} else {
					Toast toast = Toast.makeText(myContext, "Sorry, your phone has only one camera!", Toast.LENGTH_LONG);
					toast.show();
				}
			}
		}
	};

	public void chooseCamera() {
		// if the camera preview is the front
		if (cameraFront) {
			int cameraId = findBackFacingCamera();
			if (cameraId >= 0) {
				// open the backFacingCamera
				// set a picture callback
				// refresh the preview

				mCamera = Camera.open(cameraId);
				// mPicture = getPictureCallback();
				mPreview.refreshCamera(mCamera);
			}
		} else {
			int cameraId = findFrontFacingCamera();
			if (cameraId >= 0) {
				// open the backFacingCamera
				// set a picture callback
				// refresh the preview

				mCamera = Camera.open(cameraId);
				// mPicture = getPictureCallback();
				mPreview.refreshCamera(mCamera);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
		// when on Pause, release camera in order to be used from other
		// applications
		releaseCamera();
	}

	private boolean hasCamera(Context context) {
		// check if the device has camera
		if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			return true;
		} else {
			return false;
		}
	}

	boolean recording = false;
	OnClickListener captrureListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (recording) {
				// stop recording and release camera
				mediaRecorder.stop(); // stop the recording
				releaseMediaRecorder(); // release the MediaRecorder object
				Toast.makeText(AndroidVideoCaptureExample.this, "Video captured!", Toast.LENGTH_LONG).show();
				try {
					myOutWriter.close();
					FOS.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				recording_signal.setVisibility(View.INVISIBLE);
				recording = false;
			} else {
				if (!prepareMediaRecorder()) {
					Toast.makeText(AndroidVideoCaptureExample.this, "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
					finish();
				}


				// work on UiThread for better performance
				runOnUiThread(new Runnable() {
					public void run() {
						// If there are stories, add them to the table

						try {
							timeStartRecording = System.currentTimeMillis();
							String start=String.valueOf(timeStartRecording);
							myOutWriter.append("start recording:"+start+"\n");
							mediaRecorder.start();
						} catch (final Exception ex) {
							// Log.i("---","Exception in thread");
						}
					}
				});

				recording = true;
			}
		}
	};

	private void releaseMediaRecorder() {
		if (mediaRecorder != null) {
			mediaRecorder.reset(); // clear recorder configuration
			mediaRecorder.release(); // release the recorder object
			mediaRecorder = null;
			mCamera.lock(); // lock camera for later use
		}
	}

	private boolean prepareMediaRecorder() {

		mediaRecorder = new MediaRecorder();

		mCamera.unlock();
		mediaRecorder.setCamera(mCamera);
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
		java.util.Date date= new java.util.Date();
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
		.format(date.getTime());
		mediaRecorder.setOutputFile("/sdcard/myVideo"+timeStamp+".mp4");
		recording_signal.setVisibility(View.VISIBLE);

		
		
		//create the orientationFile
		orientatoinFile=new File("/sdcard/myOrientation"+timeStamp+".txt");
		try {
			orientatoinFile.createNewFile();
			FOS = new FileOutputStream(orientatoinFile);
			myOutWriter =new OutputStreamWriter(FOS);
			firstCur=true;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}



		Log.i("test","Create a File for saving an image or video");
		// Check that the SDCard is mounted
		// File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
		//          Environment.DIRECTORY_PICTURES), "MyCameraVideo");

		//mediaRecorder.setMaxDuration(600000); // Set max duration 60 sec.
		//mediaRecorder.setMaxFileSize(50000000); // Set max file size 50M



		try {
			mediaRecorder.prepare();
		} catch (IllegalStateException e) {
			releaseMediaRecorder();
			return false;
		} catch (IOException e) {
			releaseMediaRecorder();
			return false;
		}
		return true;

	}

	private void releaseCamera() {
		// stop and release camera
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		// TODO Auto-generated method stub
		if(sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
			mRotationVectorValues = sensorEvent.values.clone(); //TODO test on API lower than 18
		}

		if (mRotationVectorValues != null) {
			float[] rotationMatrix = new float[9];
			SensorManager.getRotationMatrixFromVector(rotationMatrix, mRotationVectorValues);
			float[] remappedMatrix = new float[9];
			SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X, remappedMatrix);
			SensorManager.getOrientation(remappedMatrix, mVectorOrientation);
			mVectorView.setOrientation(mVectorOrientation);
			mVectorViewDegrees.setOrientation(mVectorOrientation);
			try {
				if(recording){
					
					long curTime=System.currentTimeMillis();
					String timeFromBegining=String.valueOf(curTime-timeStartRecording);
					if(firstCur){
						prev=curTime;
						firstCur=false;
						myOutWriter.append("time from begining:"+timeFromBegining+", dt:0\n");
					}
					else{
					String dt=String.valueOf(curTime-prev);
					myOutWriter.append("time from begining:"+timeFromBegining+", dt:"+dt+"\n");
					prev=curTime;
					}
					

					for(int i=0;i<mVectorOrientation.length;i++){
						Log.i("tets",""+(Math.toDegrees(mVectorOrientation[i])+360)%360);

						myOutWriter.append(""+((Math.toDegrees(mVectorOrientation[i])+360)%360)+"\n");
					}

					myOutWriter.append("---------------------\n");
				}
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i("tets","---------------------------------------------------");


		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onStart() {
		super.onStart();
		//TODO check device orientation here
		Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		mScreenOrientation = display.getRotation(); // 0 = portrait, 1 = landscape (90), 2 = portrait (180), 3 = landscape (270)
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}
}