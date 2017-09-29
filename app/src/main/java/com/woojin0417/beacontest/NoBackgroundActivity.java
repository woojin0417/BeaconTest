package com.woojin0417.beacontest;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.List;
import java.util.UUID;

public class NoBackgroundActivity extends AppCompatActivity {

    private BeaconManager beaconManager;
    private Region region;
    private TextView tvID;
    public int soundSet=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_background);


        tvID=(TextView)findViewById(R.id.tvID);
        beaconManager=new BeaconManager(this);

        beaconManager.setRangingListener(new BeaconManager.RangingListener(){
            Context context = getApplicationContext();
            AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {

                if(!list.isEmpty()){
                    Beacon nearestBeacon=list.get(0);
                    Log.d("Airport","Nearest place: "+nearestBeacon.getRssi());
                    tvID.setText(nearestBeacon.getRssi()+""); //수신강도 나타내기

                    //301 기준 수신강도가 95보다 크면 실내 무음모드 전환
                    if(nearestBeacon.getRssi()>-93) {
                        if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) //2
                        {
                            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        }
                        else if(mAudioManager.getRingerMode()==AudioManager.RINGER_MODE_SILENT) //0
                        {
                            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        }
                        else if(mAudioManager.getRingerMode()==AudioManager.RINGER_MODE_VIBRATE) //1
                        {
                            mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                        }

                    }

                    else {
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
                    }

                }
                else {
                    tvID.setText("연결이 없습니다");


                }


            }
        });
        region = new Region("ranged region", UUID.fromString("74278BDA-B644-4520-8F0C-720EAF059935"),4660,64001);
        moveTaskToBack(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback(){

            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }
    @Override
    protected void onPause(){
        super.onPause();
    }
}