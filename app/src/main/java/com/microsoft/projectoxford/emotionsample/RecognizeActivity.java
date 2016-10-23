//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license.
//
// Microsoft Cognitive Services (formerly Project Oxford): https://www.microsoft.com/cognitive-services
//
// Microsoft Cognitive Services (formerly Project Oxford) GitHub:
// https://github.com/Microsoft/Cognitive-Emotion-Android
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
package com.microsoft.projectoxford.emotionsample;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.FaceRectangle;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;
import com.microsoft.projectoxford.emotionsample.helper.ImageHelper;

import com.microsoft.projectoxford.emotionsample.initialization.MusicObject;
import com.microsoft.projectoxford.emotionsample.initialization.SongListModel;
import com.microsoft.projectoxford.emotionsample.musicPlayer.PlayerService;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecognizeActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    /**
     * Music player
     */
    private PlayerService musicSrv;
    private Intent playIntent;
    private boolean musicBound=false;
    private List<MusicObject> songList = new ArrayList<>();
    private SongListModel mModel;

    private TextView mTitleText;
    private ImageButton mPlayPause;
    private ImageButton mForward;
    private ImageButton mPrevious;
    private SeekBar mSeekbar;

    private Bitmap mPlayImg;
    private Bitmap mPauseImg;

    private String mEmotion = "Happy";

    // The button to select an image
    private Button mButtonSelectImage;

    // The URI of the image selected to detect.
    private Uri mImageUri;

    // The image selected to detect.
    private Bitmap mBitmap;

    private EmotionServiceClient client;

    private int currentCameraId = 1;

    //TEST
    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    Camera.PictureCallback jpegCallback;
    Camera.ShutterCallback shutterCallback;
    Camera.PictureCallback rawCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);

        //Music player code
        mModel = new SongListModel(this);
        mTitleText = (TextView)findViewById(R.id.title_text);
        mSeekbar = (SeekBar)findViewById(R.id.seek_bar);
        mPlayPause = (ImageButton)findViewById(R.id.play_pause);
        mForward = (ImageButton)findViewById(R.id.forward);
        mPrevious = (ImageButton)findViewById(R.id.previous);
        mPauseImg = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_pause);
        mPlayImg = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_play);

        disablePlayer();
        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                musicSrv.pauseSong();
                musicSrv.setGetTimeResults(false);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicSrv.setSongTime(mSeekbar.getProgress());
                musicSrv.resumeSong();
                musicSrv.setGetTimeResults(true);
            }
        });

        //Other Code
        if (client == null) {
            client = new EmotionServiceRestClient(getString(R.string.subscription_key));
        }

        mButtonSelectImage = (Button) findViewById(R.id.buttonSelectImage);
        mButtonSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    captureImage(v);
                }catch (Exception e){

                }
            }
        });

        //NEW
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        camera = Camera.open(currentCameraId);
        camera.setDisplayOrientation(90); //CAREFUL for rotation in IMAGE HELPER
        int rotation = this.getWindowManager().getDefaultDisplay().getRotation();
        Log.e("Rotation", String.valueOf(rotation));

        jpegCallback = new Camera.PictureCallback() {

            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                File pictureFile = getOutputMediaFile();
                if (pictureFile == null) {
                    return;
                }

                FileOutputStream outStream = null;
                try {

                    outStream = new FileOutputStream(pictureFile);
                    outStream.write(data);
                    outStream.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }

                mImageUri = Uri.fromFile(pictureFile);
                mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                        mImageUri, getContentResolver());

                if (mBitmap != null) {
                    doRecognize();
                }

                Toast.makeText(getApplicationContext(), "Picture Saved", Toast.LENGTH_LONG).show();
                refreshCamera();
            }

            private File getOutputMediaFile() {
                File mediaStorageDir = new File(
                        Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "MyCameraApp");
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.d("MyCameraApp", "failed to create directory");
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
        };


    }



    public void captureImage(View v) throws IOException {
        camera.takePicture(null, null, jpegCallback);

    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            return;
        }

        try {
            camera.stopPreview();
        } catch (Exception e) {
        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera = Camera.open();
        } catch (RuntimeException e) {
            System.err.println(e);
            return;
        }

        Camera.Parameters param;
        param = camera.getParameters();

        // modify parameter
        List<Camera.Size> sizes = param.getSupportedPreviewSizes();
        Camera.Size selected = sizes.get(0);
        param.setPreviewSize(selected.width,selected.height);
        camera.setParameters(param);


        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            System.err.println(e);
            return;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        refreshCamera();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recognize, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void doRecognize() {
        mButtonSelectImage.setEnabled(false);

        // Do emotion detection using auto-detected faces.
        try {
            new doRequest(false).execute();
        } catch (Exception e) {
            Log.e("Error Exception: ", e.toString());
        }

        String faceSubscriptionKey = getString(R.string.faceSubscription_key);
        if (faceSubscriptionKey.equalsIgnoreCase("Please_add_the_face_subscription_key_here")) {
            Log.e("Fix: ", "There is no face subscription key in res/values/strings.xml.");
        }else {
            // Do emotion detection using face rectangles provided by Face API.
            try {
                new doRequest(true).execute();
            } catch (Exception e) {
                Log.e("Error Exception: ", e.toString());
            }
        }
    }


    private List<RecognizeResult> processWithAutoFaceDetection() throws EmotionServiceException, IOException {
        Log.d("emotion", "Start emotion detection with auto-face detection");

        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long startTime = System.currentTimeMillis();
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE STARTS HERE
        // -----------------------------------------------------------------------

        List<RecognizeResult> result = null;
        //
        // Detect emotion by auto-detecting faces in the image.
        //
        result = this.client.recognizeImage(inputStream);

        String json = gson.toJson(result);
        Log.d("result", json);

        Log.d("emotion", String.format("Detection done. Elapsed time: %d ms", (System.currentTimeMillis() - startTime)));
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE ENDS HERE
        // -----------------------------------------------------------------------
        return result;
    }

    private List<RecognizeResult> processWithFaceRectangles() throws EmotionServiceException, com.microsoft.projectoxford.face.rest.ClientException, IOException {
        Log.d("emotion", "Do emotion detection with known face rectangles");
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long timeMark = System.currentTimeMillis();
        Log.d("emotion", "Start face detection using Face API");
        FaceRectangle[] faceRectangles = null;
        String faceSubscriptionKey = getString(R.string.faceSubscription_key);
        FaceServiceRestClient faceClient = new FaceServiceRestClient(faceSubscriptionKey);
        Face faces[] = faceClient.detect(inputStream, false, false, null);
        Log.d("emotion", String.format("Face detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));

        if (faces != null) {
            faceRectangles = new FaceRectangle[faces.length];

            for (int i = 0; i < faceRectangles.length; i++) {
                // Face API and Emotion API have different FaceRectangle definition. Do the conversion.
                com.microsoft.projectoxford.face.contract.FaceRectangle rect = faces[i].faceRectangle;
                faceRectangles[i] = new com.microsoft.projectoxford.emotion.contract.FaceRectangle(rect.left, rect.top, rect.width, rect.height);
            }
        }

        List<RecognizeResult> result = null;
        if (faceRectangles != null) {
            inputStream.reset();

            timeMark = System.currentTimeMillis();
            Log.d("emotion", "Start emotion detection using Emotion API");
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE STARTS HERE
            // -----------------------------------------------------------------------
            result = this.client.recognizeImage(inputStream, faceRectangles);

            String json = gson.toJson(result);
            Log.d("result", json);
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE ENDS HERE
            // -----------------------------------------------------------------------
            Log.d("emotion", String.format("Emotion detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));
        }
        return result;
    }

    private class doRequest extends AsyncTask<String, String, List<RecognizeResult>> {
        // Store error message
        private Exception e = null;
        private boolean useFaceRectangles = false;

        public doRequest(boolean useFaceRectangles) {
            this.useFaceRectangles = useFaceRectangles;
        }

        @Override
        protected List<RecognizeResult> doInBackground(String... args) {
            if (this.useFaceRectangles == false) {
                try {
                    return processWithAutoFaceDetection();
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            } else {
                try {
                    return processWithFaceRectangles();
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<RecognizeResult> result) {
            super.onPostExecute(result);
            // Display based on error existence

            if (this.useFaceRectangles == false) {
                Log.e("Doing: ","Recognizing emotions with auto-detected face rectangles...");
            } else {
                Log.e("Doing: ", "Recognizing emotions with existing face rectangles from Face API...");
            }
            if (e != null) {
                Log.e("Error Exception: ", e.toString());
                this.e = null;
            } else {
                if (result.size() == 0) {
                    Log.e("L","No emotion detected :(");
                } else {
                    Integer count = 0;
                    // Covert bitmap to a mutable bitmap by copying it
                    Bitmap bitmapCopy = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas faceCanvas = new Canvas(bitmapCopy);
                    faceCanvas.drawBitmap(mBitmap, 0, 0, null);
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(5);
                    paint.setColor(Color.RED);
                    for (RecognizeResult r : result) {
                        faceCanvas.drawRect(r.faceRectangle.left,
                                r.faceRectangle.top,
                                r.faceRectangle.left + r.faceRectangle.width,
                                r.faceRectangle.top + r.faceRectangle.height,
                                paint);
                    }

                    int c = 0;
                    for (RecognizeResult r : result){
                        Log.e("Face Count", String.valueOf(c));
                        Log.e("Anger", String.valueOf(r.scores.anger * 100));
                        Log.e("Contempt", String.valueOf(r.scores.contempt * 100));
                        Log.e("Disgust", String.valueOf(r.scores.disgust * 100));
                        Log.e("Fear", String.valueOf(r.scores.fear * 100));
                        Log.e("Happiness", String.valueOf(r.scores.happiness * 100));
                        Log.e("Neutral", String.valueOf(r.scores.neutral * 100));
                        Log.e("Sadness", String.valueOf(r.scores.sadness * 100));
                        Log.e("Surprise", String.valueOf(r.scores.surprise * 100));
                        Log.e("Face rectangle: ", String.valueOf(r.faceRectangle.left) + " " + String.valueOf(r.faceRectangle.top) + " " + String.valueOf(r.faceRectangle.width) + " " + String.valueOf(r.faceRectangle.height));
                        Log.e(" ", " ");
                        c++;
                    }

                }
            }

            mButtonSelectImage.setEnabled(true);

            /**
             * Start the music playing here
             */
            enablePlayer();
            musicSrv.setList(mModel.getCategoryList(null));
            musicSrv.playSong();
        }

    }

    /**
     * MUSIC PLAYER FUNCTIONS
     */
    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayerService.MusicBinder binder = (PlayerService.MusicBinder) service;
            //get service
            musicSrv = binder.getService();
            binder.setListener(new PlayerService.MusicPlayerListener() {
                @Override
                public void sendProgress(int progress) {
                    mSeekbar.setProgress(progress);
                }

                @Override
                public void sendPlayerInfo(String title, String artist) {
                    mTitleText.setText(mEmotion+":    " + title);

                }
                @Override
                public void setMax(int maxTime){
                    mSeekbar.setMax(maxTime);
                }
            });
            //pass list
            //musicSrv.setList(mModel.getCategoryList(null));
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    public void disablePlayer(){
        mSeekbar.setEnabled(false);
        mPlayPause.setEnabled(false);
        mForward.setEnabled(false);
        mPrevious.setEnabled(false);
        mPlayPause.setColorFilter(getResources().getColor(R.color.button_disabled_background));
        mForward.setColorFilter(getResources().getColor(R.color.button_disabled_background));
        mPrevious.setColorFilter(getResources().getColor(R.color.button_disabled_background));

    }
    public void enablePlayer(){
        mSeekbar.setEnabled(true);
        mPlayPause.setEnabled(true);
        mForward.setEnabled(true);
        mPrevious.setEnabled(true);

        mPlayPause.setColorFilter(getResources().getColor(R.color.black));
        mForward.setColorFilter(getResources().getColor(R.color.black));
        mPrevious.setColorFilter(getResources().getColor(R.color.black));

    }
    public void playPauseClick(View v){
        if(musicSrv.pause_startSong()){
            mPlayPause.setImageBitmap(mPlayImg);
        }else{
            mPlayPause.setImageBitmap(mPauseImg);
        }
    }
    public void forwardClick(View v){
        musicSrv.skipSong();
    }
    public void previousClick(View v){
        musicSrv.prevSong();
    }



    /**
     * Lifecycle functions for player
     */
    @Override
    public void onStart(){
        super.onStart();
        if(playIntent==null) {
            playIntent = new Intent(this, PlayerService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }
    }
    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicSrv=null;
        super.onDestroy();
    }

}
