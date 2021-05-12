package com.example.localiser;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PicJobService extends JobService {
    private  static StorageReference storageReference;
    private  static DatabaseReference reference;
    private  static FirebaseAuth auth;
    JobParameters mRunningParams;
    final Handler mHandler = new Handler();
    final Runnable mWorker = new Runnable() {
        @Override public void run() {
            startJobService(PicJobService.this);
            jobFinished(mRunningParams, false);
        }
    };
    private static final String TAG = PicJobService.class.getSimpleName();
    {
        Log.d(TAG, "This class object instance: " + this.toString() + ", " + jobinfoinststr());
    }
    // The root URI of the media provider, to monitor for generic changes to its content.
    static final Uri MEDIA_URI = Uri.parse("content://" + MediaStore.AUTHORITY + "/");

    // Path segments for image-specific URIs in the provider.
    static final List<String> EXTERNAL_PATH_SEGMENTS
            = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPathSegments();

    // The columns we want to retrieve about a particular image.
    static final String[] PROJECTION = new String[] {
            MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA
    };
    static final int PROJECTION_ID = 0;
    static final int PROJECTION_DATA = 1;

    // This is the external storage directory where cameras place pictures.
    static final String DCIM_DIR = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM).getPath();

    private static String jobinfoinststr() {
        return (
                (JOB_INFO == null) ?
                        "null" : (
                        JOB_INFO.getClass().getSimpleName()
                                + "@"
                                + Integer.toHexString(java.lang.System.identityHashCode(JOB_INFO))
                )
        );
    }
    //static final Uri MEDIA_URI = Uri.parse("content://" + MediaStore.AUTHORITY + "/");
    public static final int JOBSERVICE_JOB_ID = 499; // any number but avoid conflicts
    private static JobInfo JOB_INFO;

    private static boolean isRegistered(Context context) {
        Log.d(TAG, "isRegistered() ?");
        JobScheduler js = context.getSystemService(JobScheduler.class);
        List<JobInfo> jobs = js.getAllPendingJobs();
        if (jobs == null) {
            Log.d(TAG, "JobService not registered ");
            return false;
        }
        for (int i = 0; i < jobs.size(); i++) {
            if (jobs.get(i).getId() == JOBSERVICE_JOB_ID) {
                Log.d(TAG, "JobService is registered: " + jobinfoinststr());
                return true;
            }
        }
        Log.d(TAG, "JobService is not registered");
        return false;
    }

    public static void startJobService(Context context) {
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(auth.getCurrentUser().getUid()).child("images");
        storageReference = FirebaseStorage.getInstance().getReference();
        Log.d(TAG, "registerJob(): JobService init");
        if (!isRegistered(context)) {
            Log.d(TAG, "JobBuilder executes");
            JobInfo.Builder builder = new JobInfo.Builder(JOBSERVICE_JOB_ID,
                    new ComponentName(context, PicJobService.class.getName()));
            // Look for specific changes to images in the provider.
            builder.addTriggerContentUri(
                    new JobInfo.TriggerContentUri(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS));
            // Also look for general reports of changes in the overall provider.
            //builder.addTriggerContentUri(new JobInfo.TriggerContentUri(MEDIA_URI, 0));
            // Get all media changes within a tenth of a second.
            builder.setTriggerContentUpdateDelay(1);
            builder.setTriggerContentMaxDelay(100);

            JOB_INFO = builder.build();
            Log.d(TAG, "JOB_INFO created " + jobinfoinststr());

            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            int result = scheduler.schedule(JOB_INFO);
            if (result == JobScheduler.RESULT_SUCCESS) {
                Log.d(TAG, "JobScheduler OK");
            } else {
                Log.d(TAG, " JobScheduler fails " + result);
            }
        }
    }

    public static void stopJobService(Context context) {
        Log.d(TAG, "cancelJob() " + jobinfoinststr());
        JobScheduler js =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        js.cancel(JOBSERVICE_JOB_ID);
        isRegistered(context);
    }

    @Override
    public boolean onStartJob(JobParameters params) {

        Log.i("PhotosContentJob", "JOB STARTED!");
        mRunningParams = params;

        // Instead of real work, we are going to build a string to show to the user.
        StringBuilder sb = new StringBuilder();

        // Did we trigger due to a content change?
        if (params.getJobId() == JOBSERVICE_JOB_ID) {
            boolean rescanNeeded = false;

            if (params.getTriggeredContentAuthorities() != null) {
                // If we have details about which URIs changed, then iterate through them
                // and collect either the ids that were impacted or note that a generic
                // change has happened.
                System.out.println("rsm");
                ArrayList<String> ids = new ArrayList<>();
                for (Uri uri : params.getTriggeredContentUris()) {
                    List<String> path = uri.getPathSegments();
                    if (path != null && path.size() == EXTERNAL_PATH_SEGMENTS.size()+1) {
                        // This is a specific file.
                        ids.add(path.get(path.size()-1));
                    } else {
                        // Oops, there is some general change!
                        rescanNeeded = true;
                    }

                }

                if (ids.size() > 0) {
                    // If we found some ids that changed, we want to determine what they are.
                    // First, we do a query with content provider to ask about all of them.
                    StringBuilder selection = new StringBuilder();
                    for (int i=0; i<ids.size(); i++) {
                        if (selection.length() > 0) {
                            selection.append(" OR ");
                        }
                        selection.append(MediaStore.Images.ImageColumns._ID);
                        selection.append("='");
                        selection.append(ids.get(i));
                        selection.append("'");
                    }

                    // Now we iterate through the query, looking at the filenames of
                    // the items to determine if they are ones we are interested in.
                    Cursor cursor = null;
                    boolean haveFiles = false;
                    try {
                        cursor = getContentResolver().query(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                PROJECTION, selection.toString(), null, null);
                        while (cursor.moveToNext()) {
                            // We only care about files in the DCIM directory.
                            String dir = cursor.getString(PROJECTION_DATA);
                            System.out.println(PROJECTION_DATA);
                            if (dir.startsWith(DCIM_DIR)) {
                                File file = new File(dir);
                                Uri uri = Uri.fromFile(file);
                             //   System.out.println("file " + files[i]);
                                StorageReference st = storageReference.child("images/" + file.getName() + ".jpg");


                                st.putFile(uri).addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri1 -> {
                                    this.reference.push().setValue(uri1.toString());
                                }));



                                if (!haveFiles) {
                                    haveFiles = true;

                                    sb.append("New photos:\n");
                                }
                                sb.append(cursor.getInt(PROJECTION_ID));
                                sb.append(": ");
                                sb.append(dir);
                                sb.append("\n");
                            }
                        }
                    } catch (SecurityException e) {
                        sb.append("Error: no access to media!");
                    } finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }

            } else {
                // We don't have any details about URIs (because too many changed at once),
                // so just note that we need to do a full rescan.
                rescanNeeded = true;
            }

            if (rescanNeeded) {
                sb.append("Photos rescan needed!");
            }
        } else {
            sb.append("(No photos content)");
        }
        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();

        // We will emulate taking some time to do this work, so we can see batching happen.
     //   mHandler.postDelayed(mWorker, 10*1000);
        // manual reschedule
        ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(JOBSERVICE_JOB_ID);
        startJobService(getApplicationContext());

        return true;
     /*   Log.d(TAG, "onStartJob() " + this.toString() + ", "
                + ((JOB_INFO == null) ? "null" : JOB_INFO.getClass().getSimpleName() + "@" + Integer.toHexString(java.lang.System.identityHashCode(JOB_INFO))));
        if (params.getJobId() == JOBSERVICE_JOB_ID) {
            if (params.getTriggeredContentAuthorities() != null) {
                for (Uri uri : params.getTriggeredContentUris()) {
                    Log.d(TAG, "onStartJob() JobService Uri=" + uri.toString());
                }
            }
        }
        this.jobFinished(params, false);  // false = do not reschedule

        // manual reschedule
        ((JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(JOBSERVICE_JOB_ID);
        startJobService(getApplicationContext());

        return true; // false =  no threads inside*/
    }

    //This method is called if the system has determined that you must stop execution of your job
    //even before you've had a chance to call {@link #jobFinished(JobParameters, boolean)}.
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob() " + this.toString() + ", " + jobinfoinststr());
        return false; // no restart from here
    }
}