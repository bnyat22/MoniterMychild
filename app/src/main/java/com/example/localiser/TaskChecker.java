package com.example.localiser;

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

public class TaskChecker{
    public static List<ActivityManager.RunningTaskInfo> getForegroundApplication(Context context){
        ActivityManager am=(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> foreground=am.getRunningTasks(Integer.MAX_VALUE);
        return foreground;
    }
}