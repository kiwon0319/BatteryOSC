package com.example.batteryosc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.OSCSerializerAndParserBuilder;
import com.illposed.osc.argument.ArgumentHandler;
import com.illposed.osc.argument.handler.Activator;
import com.illposed.osc.transport.OSCPortOut;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

public class OSCService extends Service{
    private final int SERVICE_ID = 1;
    private Looper serviceLooper;

    private InetSocketAddress socketAddress;

    private SharedPreferences mprefs;
    private SharedPreferences.Editor editor;

    private OSCPortOut sender = null;

    private String ip, port, prmtPath, prmtNm_batteryLevel, prmtNm_isCharging;

    static final String NOTIFICATION_CHANNEL_ID = "1001";

    private NotificationManager buildChannel(String t_id){
        NotificationManager notificationManager
                = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //안드로이드 Oreo 미만에서 버전 분기
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence channelName = "Status";
            String description = "show sending status";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(
                    t_id,
                    channelName,
                    importance
            );
            channel.setDescription(description);

            assert notificationManager != null;

            notificationManager.createNotificationChannel(channel);
        }

        return notificationManager;
    }

    private LoopThread thread;

    private boolean isCharging;
    private float   batteryPct;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate(){
        mprefs = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE);
        editor = mprefs.edit();

        ip = mprefs.getString(MainActivity.KEY_IP, "192.168.0.1");
        port = mprefs.getString(MainActivity.KEY_PORT, "9000");
        prmtPath = mprefs.getString(MainActivity.KEY_PRMT_PATH, "/avatar/parameters");
        prmtNm_batteryLevel = mprefs.getString(MainActivity.KEY_PRMT_BATTERYLEVEL, "battery_level");
        prmtNm_isCharging = mprefs.getString(MainActivity.KEY_PRMT_ISCHARGING, "is_charging");

        socketAddress = new InetSocketAddress(ip, Integer.parseInt(port));
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        Intent notiIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notiIntent, PendingIntent.FLAG_IMMUTABLE);

        //알림 선언
        NotificationCompat.Builder builder
                = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("데이터 전송 중")
                .setContentText("현재 배터리정보가 전송되고 있습니다.")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true);

        NotificationManager notificationManager = buildChannel(NOTIFICATION_CHANNEL_ID);

        Notification noti = builder.build();
        noti.flags = Notification.FLAG_ONGOING_EVENT;

        startForeground(SERVICE_ID, noti);

        thread = new LoopThread();

        try{
            OSCSerializerAndParserBuilder serializer = new OSCSerializerAndParserBuilder();
            serializer.setUsingDefaultHandlers(false);
            List<ArgumentHandler> defaultParserTypes = Activator.createSerializerTypes();
            defaultParserTypes.remove(16);
            char typeChar = 'a';
            for (ArgumentHandler argumentHandler:defaultParserTypes) {
                serializer.registerArgumentHandler(argumentHandler, typeChar);
                typeChar++;
            }
            sender = new OSCPortOut(serializer, socketAddress);
        }catch (Exception e){
            Log.e("error", "onCreate: " + e.toString());
            thread.interrupt();
        }

        thread.start();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent){
        //this service for start method so do nothing
        return null;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        thread.interrupt();

        stopForeground(true);
        stopSelf();
    }

    private void updateData(){
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        Intent batteryStatus = getApplicationContext().registerReceiver(null, intentFilter);
        assert batteryStatus != null;
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        batteryPct = level / (float) scale;
    }

    class LoopThread extends Thread{
        public void run(){
            super.run();

            while(true){
                try{
                    Thread.sleep(1000);
                }catch (InterruptedException e){
                    Log.d("trace", "service activity thread interrupted");
                    break;
                }catch (Exception e){
                    Log.e("trace", e.toString());
                    break;
                }

                updateData();

                Object[] obj_batlv = {batteryPct};
                Object[] obj_ischarging = {isCharging};
                OSCMessage mbatLv = new OSCMessage(prmtPath + prmtNm_batteryLevel, Arrays.asList(obj_batlv));
                OSCMessage misCharging = new OSCMessage(prmtPath + prmtNm_isCharging, Arrays.asList(obj_ischarging));

                try {
                    sender.send(mbatLv);
                    sender.send(misCharging);
                    Log.d("trace", String.format("pct:%s status:%s", mbatLv.getArguments().toString(), isCharging));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
