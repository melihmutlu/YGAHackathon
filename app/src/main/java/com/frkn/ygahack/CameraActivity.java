package com.frkn.ygahack;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Melih on 7.5.2016.
 */
public class CameraActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private File pictureFile;
    private ImageView scanner , flashBtn, reqImageView;
    private Animation mAnimation;
    private boolean flash = false ;
    private TextToSpeech tts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        getSupportActionBar().hide();
        mCamera = getCameraInstance();
        tts = new TextToSpeech(this, this);
        if(mCamera!=null)
            mCameraPreview = new CameraPreview(this, mCamera);
        else
            Log.d("INFO","Kamera yok hacıııı");

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        scanner = (ImageView) findViewById(R.id.scanner);
        reqImageView = (ImageView) findViewById(R.id.reqImageView);
        preview.addView(mCameraPreview);



        final ImageView captureButton = (ImageView) findViewById(R.id.button_capture);
        flashBtn = (ImageView) findViewById(R.id.flashBtn);

        flashBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFlashMode();
            }
        });

        //Take picture
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            mCamera.takePicture(null, null, mPicture);
                        }
                    }
                });
                //captureButton.setClickable(false);
            }
        });



    }

    //Changes Status of flash light
    private void changeFlashMode(){

        Camera.Parameters p = mCamera.getParameters();
        //On
        if(flash == false){
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            flash=true;
            flashBtn.setImageResource(R.drawable.ic_flash_white_36dp);
        }else{
            //OFF
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            flashBtn.setImageResource(R.drawable.ic_flash_off_white_36dp);
            flash=false;
        }
        mCamera.setParameters(p);
    }


    /**
     * Helper method to access the camera returns null if it cannot get the
     * camera or does not exist
     *
     * @return
     */
    /*
    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
    }*/
    private Camera getCameraInstance() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
            Camera.getCameraInfo( camIdx, cameraInfo );
            if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK  ) {
                try {
                    cam = Camera.open( camIdx );
                } catch (RuntimeException e) {
                    Log.e("Camera", "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return cam;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }

            //Picture file
            ByteArrayOutputStream blob = new ByteArrayOutputStream();
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, blob);
            reqImageView.setImageBitmap(bitmap);
            reqImageView.setRotation(90);
            byte[] bitmapdata = blob.toByteArray();
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(bitmapdata);
                fos.close();

                //mCameraPreview.getHolder().removeCallback(mCameraPreview);
                //camera.release();
                camera.startPreview();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
            new RequestTask().execute(pictureFile);
            //new OCRTask().execute(pictureFile);
        }
    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "YGAHack_Media");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("YGAHack_Media", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    class OCRTask extends AsyncTask<File,Void,JSONObject> {

        @Override
        protected void onPreExecute() {
            /*scanner.setVisibility(View.VISIBLE);
            mAnimation = new TranslateAnimation(
                    TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                    TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                    TranslateAnimation.RELATIVE_TO_PARENT, -0.5f,
            TranslateAnimation.RELATIVE_TO_PARENT, 0.48f);
            mAnimation.setDuration(3500);
            mAnimation.setRepeatCount(-1);
            mAnimation.setRepeatMode(Animation.REVERSE);
            mAnimation.setInterpolator(new LinearInterpolator());
            scanner.setAnimation(mAnimation);
            */
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(File... params) {
            return OCR.getInstance().postImage(params[0]);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                Log.d("INFO", "result: " + jsonObject.getJSONArray("ParsedResults").getJSONObject(0).getString("ParsedText"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(jsonObject);
        }
    }

    //Uploads the image , waits for result
    class RequestTask extends AsyncTask<File,Void,JSONObject> {

        @Override
        protected void onPreExecute() {
            /*scanner.setVisibility(View.VISIBLE);
            mAnimation = new TranslateAnimation(
                    TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                    TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                    TranslateAnimation.RELATIVE_TO_PARENT, -0.5f,
                    TranslateAnimation.RELATIVE_TO_PARENT, 0.48f);
            mAnimation.setDuration(3500);
            mAnimation.setRepeatCount(-1);
            mAnimation.setRepeatMode(Animation.REVERSE);
            mAnimation.setInterpolator(new LinearInterpolator());
            scanner.setAnimation(mAnimation);
            */
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(File... params) {

            JSONObject obj = Cloudsight.getInstance().postImage(params[0]);
            return obj;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                Log.d("INFO","asking for result for this token: " + jsonObject.getString("token"));
                new AskResult().execute(jsonObject.getString("token"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(jsonObject);
        }
    }
    //Request until recieve a result
    class AskResult extends AsyncTask<String,Void,JSONObject>{
        String token="";
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            try {
                if(!jsonObject.getString("status").equals("completed")){
                    Log.d("INFO",jsonObject.toString());
                    new AskResult().execute(token);}
                else {
                    Log.d("INFO", jsonObject.getString("name"));
                    Toast.makeText(CameraActivity.this, jsonObject.getString("name"), Toast.LENGTH_LONG).show();
                    speakOut(jsonObject.getString("name"), TextToSpeech.QUEUE_FLUSH);

                }
                super.onPostExecute(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        private void speakOut(String text, int mode) {
            tts.speak(text, mode, null);
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            token=params[0];
            return Cloudsight.getInstance().askResult(token);
        }
    }
    /*
    String readFile(String path) throws IOException {
        StringBuilder buf=new StringBuilder();
        InputStream json=getAssets().open(path);
        BufferedReader in=
                new BufferedReader(new InputStreamReader(json, "UTF-8"));
        String str;
        while ((str=in.readLine()) != null) {
            buf.append(str);
        }
        in.close();
        return buf.toString();
    }*/

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.getDefault());

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                //btnSpeak.setEnabled(true);
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }
}