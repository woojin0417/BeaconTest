package com.woojin0417.beacontest;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.util.List;
import java.util.UUID;

/**
 * Created by 301-29 on 2017-08-17.
 */

public class MyApplication extends Application{
    private BeaconManager beaconManager;
    public int soundSet=-1;
    //app 설치시

    @Override
    public void onCreate(){
        super.onCreate();
        beaconManager=new BeaconManager(getApplicationContext());

        //설치 후 beaconMonitoring service 시작
        //app 종료 하더라도 service
        beaconManager.connect(new BeaconManager.ServiceReadyCallback(){
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new Region("monitored region",
                        UUID.fromString("11111111-1111-1111-1111-111111111301"),4660,64001
                        ));
            }
        });

        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener(){

            Context context = getApplicationContext();
            AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) { // 110 120 비콘이 잡히면
            soundSet=mAudioManager.getRingerMode();
               if(!list.isEmpty()) {
                   Beacon beacon = list.get(0);
                   if(beacon.getRssi()>-93){ // 93-0
                       if(!isAlreadyRunActivity())
                       {
                           //앱 실행중이 아닐때
                           Intent intent =new Intent(getApplicationContext(),NoBackgroundActivity.class);
                           intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                           intent.putExtra("excuteType","beacon");
                           getApplicationContext().startActivity(intent);
                           showNotification("M4M", "매너 있게 변경되었습니다");
                       }
                       else {
                           //앱 실행중 일때
                           showNotification("M4M", "매너 있게 변경되었습니다");
                       }
                   }
                   else{ //94-110
                       showNotification("M4M", "매너 공간이 아닙니다");
                   }

               }
               else {
                   //list에 없다가 잡혔을 경우에는 ?
                   showNotification("M4M", "매너 있게 바뀌기 직전 입니다.");
               }

            }

            @Override
            public void onExitedRegion(Region region) {
               if(soundSet==0)
                {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                }
                else if(soundSet==1)
                {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                }
                else if(soundSet==2)
                {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                }
               showNotification("M4M","매너를 위해 대기 중입니다");
            }
        });
    }
    public void showNotification(String title, String message){
        Intent notifyIntent = new Intent(this, MainActivity.class);

        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivities(
                this, 0, new Intent[]{notifyIntent}
                , PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification=new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.make)
                .setTicker("[M4M] 모드가 변경되었습니다")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();

        NotificationManager notificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,notification);
    }

    private boolean isAlreadyRunActivity()
    {
        ActivityManager activity_manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> task_info = activity_manager.getRunningTasks(9999);
        String activityName = "";
        for( int i = 0; i < task_info.size(); i++ ) {
            activityName = task_info.get(i).topActivity.getPackageName();
            if( activityName.startsWith("com.woojin0417.beacontest") ) {
                return true; }
        }

        return false;
    }
}
