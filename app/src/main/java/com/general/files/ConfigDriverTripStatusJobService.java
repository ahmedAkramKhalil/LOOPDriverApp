package com.general.files;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.utils.Logger;


public class ConfigDriverTripStatusJobService extends Worker {
    public ConfigDriverTripStatusJobService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        Logger.e("ConfigDriverTripStatus", "::JobService::Create::");
    }

//    @Override
//    public void onCreate() {
//        super.onCreate();
//        Logger.e("ConfigDriverTripStatus", "::JobService::CREATE::");
//    }
//
//    @Override
//    public boolean onStartJob(JobParameters params) {
//        Logger.e("ConfigDriverTripStatus", "::JobService::START::");
//        try {
//            if (MyApp.getInstance().getCurrentAct() != null) {
//                ConfigDriverTripStatus.getInstance().executeTaskRun(() -> ConfigDriverTripStatusJobService.this.jobFinished(params, true));
//            } else {
//                ConfigDriverTripStatusJobService.this.jobFinished(params, true);
//            }
//        } catch (Exception e) {
//            ConfigDriverTripStatusJobService.this.jobFinished(params, true);
//        }
//        return true;
//    }
//
//    @Override
//    public boolean onStopJob(JobParameters params) {
//        Logger.e("ConfigDriverTripStatus", "::JobService::STOP::");
//        return true;
//    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Logger.e("ConfigDriverTripStatus", "::JobService::Start::");
            if (MyApp.getInstance().getCurrentAct() != null) {
                ConfigDriverTripStatus.getInstance().executeTaskRun();
            } else {
              //  ConfigDriverTripStatusJobService.this.stop();
            }
        } catch (Exception e) {
          //  ConfigDriverTripStatusJobService.this.stop();
        }
        return Result.success();
    }

    @Override
    public void onStopped() {
        Logger.e("ConfigDriverTripStatus", "::JobService::Stop::");
        super.onStopped();
    }
}
