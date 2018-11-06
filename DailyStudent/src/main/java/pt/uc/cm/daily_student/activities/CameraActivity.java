package pt.uc.cm.daily_student.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pt.uc.cm.daily_student.R;

public class CameraActivity extends Activity {
    private final String TAG = CameraActivity.class.getSimpleName();

    private FaceDetector faceDetector;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private SurfaceView cameraView;
    String filepath;

    private int cameraMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        cameraView = findViewById(R.id.svFoto);

        Intent receivedIntent = getIntent();
        cameraMode = receivedIntent.getIntExtra("cameraMode", 0);

        Button btnTakePic = findViewById(R.id.takeP);

        faceDetector = new FaceDetector.Builder(this)
                .setProminentFaceOnly(true)
                .build();

        faceDetector.setProcessor(new Detector.Processor<Face>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Face> detections) {
            }
        });

        btnTakePic.setVisibility(View.VISIBLE);
        cameraSource = new CameraSource
                .Builder(this, faceDetector)
                .setRequestedPreviewSize(800, 480)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .build();
        if (cameraMode == 2) {
            barcodeDetector = new BarcodeDetector.Builder(this)
                    .setBarcodeFormats(Barcode.QR_CODE)
                    .build();

            btnTakePic.setVisibility(View.INVISIBLE);
            cameraSource = new CameraSource
                    .Builder(this, barcodeDetector)
                    .setRequestedPreviewSize(1280, 720)
                    .setAutoFocusEnabled(true)
                    .setFacing(Camera.CameraInfo.CAMERA_FACING_FRONT)
                    .build();
        }

        cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(cameraView.getHolder());
                } catch (IOException ie) {
                    Log.e(TAG, ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        if (cameraMode == 2) {
            barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                @Override
                public void release() {
                }

                @Override
                public void receiveDetections(Detector.Detections<Barcode> detections) {
                    final SparseArray<Barcode> barCodes = detections.getDetectedItems();

                    if (barCodes.size() != 0) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("connectionDetails", barCodes.valueAt(0).displayValue);
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource != null)
            cameraSource.release();
    }

    public void onButtonTakePickClick(View view) {
        switch (view.getId()) {
            case R.id.takeP:
                cameraSource.takePicture(myShutterCallback, myPictureCallback_JPG);
        }
    }

    CameraSource.ShutterCallback myShutterCallback = new CameraSource.ShutterCallback() {
        public void onShutter() {
        }
    };

    CameraSource.PictureCallback myPictureCallback_JPG = new CameraSource.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes) {
            Log.i(TAG, "picture taken");

            File pictureFile = getOutputMediaFile(1);
            if (pictureFile == null) {
                Log.d(TAG, "Error creating media file, check storage permissions");
                return;
            }

            if (bytes.length == 0)
                Log.d(TAG, "No picture data received");

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(bytes);
                fos.close();
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{pictureFile.getPath()}, null,
                        (path, uri) -> Log.i(TAG, "Scanned " + path));

                if (!pictureFile.exists()) {
                    Log.d(TAG, "File not saved correctly");
                }

            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
            Bundle bundle = new Bundle();
            bundle.putString("TITULO", filepath);

            Intent mIntent = new Intent();

            mIntent.putExtras(bundle);

            setResult(RESULT_OK, mIntent);

            finish();
        }
    };

    public void takePicture(View view) {
        cameraSource.takePicture(myShutterCallback, myPictureCallback_JPG);
        MediaPlayer mp;
        mp = MediaPlayer.create(this, R.raw.camera_click);
        mp.start();
    }

    private File getOutputMediaFile(int type) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "DayliStudentBGnotes");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        filepath = mediaStorageDir.getPath() + File.separator + "IMG_"
                + timeStamp + ".jpg";
        if (type == 1) {
            mediaFile = new File(filepath);
            Log.d(TAG, "Saved in: " + filepath);

        } else {
            return null;
        }

        return mediaFile;
    }
}
