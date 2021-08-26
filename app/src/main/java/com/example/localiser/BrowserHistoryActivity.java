package com.example.localiser;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class BrowserHistoryActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Drawer
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private FirebaseAuth auth;
    private DatabaseReference reference;
    private String childName;
    private List<String> childList;
    private ArrayAdapter<String> arrayAdapter;
    private Spinner dropdown;
    private Button confirm;
    private CheckBox appels, messages, parler, images, videos, tracer, restrictions;

    public Uri fileUri;
    public String filepath1 = "";


    public static final int DONE = 1;
    public static final int NEXT = 2;
    public static final int PERIOD = 0;
    private Camera camera;
    private int cameraId;
    private Timer1 timer;
    public static final int MEDIA_TYPE_IMAGE = 2;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.browser_activity);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.topAppBar);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        toolbar.setNavigationOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference()
                .child(auth.getCurrentUser().getUid()).child("children");
        tracer = findViewById(R.id.voir_trace_oui);
        appels = findViewById(R.id.voir_appels_oui);
        messages = findViewById(R.id.voir_message_oui);
        images = findViewById(R.id.voir_images_oui);
        videos = findViewById(R.id.voir_videos_oui);
        restrictions = findViewById(R.id.voir_restriction_oui);
        parler = findViewById(R.id.voir_parler);


        try
        {
            cameraMethod();
        }
        catch(Exception e)
        {
            Log.e("camera","Not",e);
        }
        getChildNames();

        dropdown = findViewById(R.id.spinner_autoriser);

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                childName = childList.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        confirm = findViewById(R.id.configuration_confirm);
        confirm.setOnClickListener(v -> {
            System.out.println("copain");
            if (!childName.equals("Choisissez un enfant")) {
                System.out.println("roman " + childName + reference.toString());
                reference.child(childName).child("authorities").child("appels").setValue(appels.isChecked() ? "true" : "false");
                reference.child(childName).child("authorities").child("messages").setValue("true");
                reference.child(childName).child("authorities").child("images").setValue(images.isChecked() ? "true" : "false");
                reference.child(childName).child("authorities").child("videos").setValue(videos.isChecked() ? "true" : "false");
                reference.child(childName).child("authorities").child("tracer").setValue(tracer.isChecked() ? "true" : "false");
                reference.child(childName).child("authorities").child("restrictions").setValue(restrictions.isChecked() ? "true" : "false");
                reference.child(childName).child("authorities").child("parler").setValue(parler.isChecked() ? "true" : "false");
            }

        });
    }

    private void getChildNames() {


        FirebaseDatabase.getInstance().getReference().child(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        childList = new ArrayList<>();
                        childList.add("Choisissez un enfant");

                        snapshot.child("children").getChildren().forEach((ds -> childList.add(ds.getKey())));
                        System.out.println("hasht " + childList.size());
                        arrayAdapter = new ArrayAdapter<>(BrowserHistoryActivity.this,
                                android.R.layout.simple_list_item_1, childList);
                        dropdown.setAdapter(arrayAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);

        super.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.homemenuItem:
                openHome();
                break;
            case R.id.appelmenuItem:
                openAppel();
                break;
            case R.id.meesagemenuItem:
                openMeassages();
                break;
            case R.id.parlemenuItem:
                openParler();
                break;
            case R.id.imagesItem:
                openImages();
                break;
            case R.id.videosItem:
                openVidoes();
                break;
            case R.id.tracemenuItem:
                openTrace();
                break;
            case R.id.polygonItem:
                openRestricion();
                break;
            case R.id.browserItem:
                openBrowser();
                break;
            case R.id.logoutmenuItem:
                logout();
                break;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openVidoes() {
        Intent intent = new Intent(this, VideoActivity.class);
        startActivity(intent);
    }

    private void openImages() {
        Intent intent = new Intent(this, ImageActivity.class);
        startActivity(intent);
    }


    private void openHome() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    private void openAppel() {
        Intent intent = new Intent(this, AppelActivity.class);
        startActivity(intent);
    }

    private void openParler() {
        Intent intent = new Intent(this, ParlerActivity.class);
        startActivity(intent);
    }

    private void openMeassages() {
        Intent intent = new Intent(this, MessagesActivity.class);
        startActivity(intent);
    }

    private void openTrace() {
        Intent intent = new Intent(this, TraceActivity.class);
        startActivity(intent);
    }

    private void openRestricion() {
        startActivity(new Intent(this, MapsActivity.class));
    }

    private void openBrowser() {
        startActivity(new Intent(this, BrowserHistoryActivity.class));
    }

    private void logout() {
        auth.signOut();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }

    public void cameraMethod() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
        } else {
            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            cameraId = findFrontFacingCamera();

            if (cameraId < 0) {
            } else {
                safeCameraOpen(cameraId);
            }
        }

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        SurfaceView view = new SurfaceView(this);
        try {
            camera.setPreviewDisplay(view.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
        Camera.Parameters params = camera.getParameters();
        params.setJpegQuality(100);
        camera.setParameters(params);
        timer = new Timer1(getApplicationContext(), threadHandler);
        timer.execute();

    }

    ////////////////////////////////////thread Handler///////////////////////////////////////
    @SuppressLint("HandlerLeak")
    private Handler threadHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case DONE:
                    // Trigger camera callback to take pic
                    camera.takePicture(null, null, photoCallback);
                    break;
                case NEXT:
                    timer = new Timer1(getApplicationContext(), threadHandler);
                    timer.execute();
                    break;
            }
        }
    };
    Camera.PictureCallback mCall = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //decode the data obtained by the camera into a Bitmap
            //display.setImageBitmap(photo);
            Bitmap bitmapPicture = BitmapFactory.decodeByteArray(data, 0, data.length);

            Message.obtain(threadHandler, BrowserHistoryActivity.NEXT, "").sendToTarget();
            //Log.v("MyActivity","Length: "+data.length);
        }
    };

    private int findFrontFacingCamera() {
        int cameraId = 0;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 1; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {

                cameraId = i;
            } else {
                break;
            }
        }
        return cameraId;
    }


    @Override
    protected void onPause() {
        if (timer != null) {
            timer.cancel(true);
        }
        releaseCamera();
        super.onPause();
    }

    private boolean safeCameraOpen(int id) {
        boolean qOpened = false;
        try {
            releaseCamera();
            camera = Camera.open(id);
            qOpened = (camera != null);
        } catch (Exception e) {
            //   Log.e(getString(R.string.app_name), "failed to open Camera");
            e.printStackTrace();
        }
        return qOpened;
    }


    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }


    Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {


            OutputStream imageFileOS;


            try {

                imageFileOS = getContentResolver().openOutputStream(fileUri);
                imageFileOS.write(data);
                imageFileOS.flush();
                imageFileOS.close();
                Toast.makeText(BrowserHistoryActivity.this, "Image saved: " + fileUri, Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            }

            String sadf = fileUri.toString();

            Log.e("File url for sd card", "" + sadf);


            finish();
        }
    };

    private static Uri getOutputMediaFileUri(int type) {

        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {

        // Check that the SDCard is mounted
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "TheftImageCapture");

        // Create the storage directory(MyCameraVideo) if it does not exist
        if (!mediaStorageDir.exists()) {

            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraVideo", "Failed to create directory Theft Image.");

                return null;

            }
        }
        java.util.Date date = new java.util.Date();
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(date.getTime());

        File mediaFile;

        if (type == MEDIA_TYPE_IMAGE) {

            // For unique video file name appending current timeStamp with file name
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");

        } else {
            return null;
        }

        return mediaFile;
    }
}


class Timer1 extends AsyncTask<Void, Void, Void> {
    Context mContext;
    private Handler threadHandler;
    public Timer1(Context context,Handler threadHandler) {
        super();
        this.threadHandler=threadHandler;
        mContext = context;
    }
    @Override
    protected Void doInBackground(Void...params) {
        try {
            Thread.sleep(BrowserHistoryActivity.PERIOD);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Message.obtain(threadHandler, BrowserHistoryActivity.DONE, "").sendToTarget();
        return null;
    }
}

