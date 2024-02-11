package com.example.batteryosc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SeslSwitchBar;
import androidx.appcompat.widget.SeslToggleSwitch;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.regex.Pattern;

import dev.oneuiproject.oneui.layout.ToolbarLayout;

public class MainActivity extends AppCompatActivity {

    static final String PREFS = "BatOSC";
    static final String KEY_PORT = "port";
    static final String KEY_IP = "ip";
    static final String KEY_PRMT_BATTERYLEVEL = "batteryLevel";
    static final String KEY_PRMT_ISCHARGING = "isCharging";

    static final String KEY_LABS_SHOW_PRAMETER_PATH = "labs_prmtPath";

    final String IP_VALIDATION = "^[0-9]{1,3}\\.[0-9]{0,3}\\.[0-9]{0,3}\\.[0-9]{0,3}$";
    static final String NOTIFICATION_CHANNEL_ID = "1001";
    private final String[] permissions = {
            Manifest.permission.POST_NOTIFICATIONS
    };

    private SharedPreferences mprefs;
    private SharedPreferences.Editor editor;

    private String prmt_ipAddr,prmt_port, prmtNm_batteryLevel, prmtNm_isCharge;
    private boolean setting_showPrmtPath;

    private int batteryLevel;
    private boolean isCharge;

    private CardView card_ip, card_port, card_prmt, card_isCharging;
    private SeslSwitchBar swichbar;

    private TextView text_batteryLevel;

    private UiHander uiHander;
    private BackgoundThread thread;

    @SuppressLint("NotificationPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //툴바레이아웃 설정
        ToolbarLayout toolbarLayout = (ToolbarLayout) findViewById(R.id.toolbarLayout);
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) toolbarLayout.findViewById(dev.oneuiproject.oneui.design.R.id.toolbarlayout_collapsing_toolbar);

        CollapsingToolbarLayout.LayoutParams layoutParams = new CollapsingToolbarLayout.LayoutParams(
                CollapsingToolbarLayout.LayoutParams.MATCH_PARENT,
                CollapsingToolbarLayout.LayoutParams.WRAP_CONTENT
        );

        View extendedView = View.inflate(getApplicationContext(), R.layout.m_appbar_extended, null);
        text_batteryLevel = extendedView.findViewById(R.id.text_batteryLevel);

        toolbarLayout.setCustomTitleView(extendedView, layoutParams);


        //알림 선언
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        //권한 설정
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O && PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)){
            // 푸쉬 권한 없음
            ActivityCompat.requestPermissions(this, permissions, 100);
        }

        NotificationCompat.Builder builder
                = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                //.setContentTitle("")
                //.setContentText("")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = buildChannel(NOTIFICATION_CHANNEL_ID);

        Notification noti = builder.build();
        noti.flags = Notification.FLAG_ONGOING_EVENT;

        //스위치 뷰 선언
        swichbar = (SeslSwitchBar) findViewById(R.id.toggle_Send);
        swichbar.addOnSwitchChangeListener(new SeslSwitchBar.OnSwitchChangeListener(){
            @Override
            public void onSwitchChanged(SwitchCompat switchCompat, boolean isChecked) {
                if(isChecked){

                    notificationManager.notify(123, noti);
                }else {

                }
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        //환경설정 핸들
        mprefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        editor = mprefs.edit();

        Log.d("trace", "loaded save data:" + mprefs.getAll());

        //첫 데이터 초기화
        prmt_ipAddr = mprefs.getString(KEY_IP, "192.168.0.1");
        prmt_port = mprefs.getString(KEY_PORT, "9001");
        prmtNm_batteryLevel = mprefs.getString(KEY_PRMT_BATTERYLEVEL, "battery_level");
        prmtNm_isCharge = mprefs.getString(KEY_PRMT_ISCHARGING, "is_charging");
        setting_showPrmtPath = mprefs.getBoolean(KEY_LABS_SHOW_PRAMETER_PATH, false);

        //핸들러
        uiHander = new UiHander();
        thread = new BackgoundThread();
        thread.start();

        update();
    }

    @Override
    protected void onPause(){
        super.onPause();

        thread.interrupt();
    }

    //<커스텀 함수
    private void update(){
        card_ip = (CardView) findViewById(R.id.ip_addr);
        card_ip.setSummaryText(prmt_ipAddr);

        card_port = (CardView) findViewById(R.id.port_name);
        card_port.setSummaryText(prmt_port);

        card_prmt = (CardView) findViewById(R.id.parm_batteryLevel);
        card_prmt.setSummaryText(prmtNm_batteryLevel);

        card_isCharging = (CardView) findViewById(R.id.parm_IsCharging);
        card_isCharging.setSummaryText(prmtNm_isCharge);
    }

    private boolean checkIp(EditText et){
        int c1 = ContextCompat.getColor(getApplicationContext(), R.color.color_error);
        int c2 = ContextCompat.getColor(getApplicationContext(), dev.oneuiproject.oneui.design.R.color.oui_primary_text_color);
        boolean p = Pattern.matches(IP_VALIDATION, et.getText().toString());

        if (p){
            et.setTextColor(c2);
            return true;
        }else{
            et.setTextColor(c1);
            return false;
        }
    }

    private boolean isEmpty(EditText et){
        String text = et.getText().toString();

        if(text.equals("")){
            return true;
        }else{
            return false;
        }
    }

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
    //</커스텀 함수>

    //<툴바 메뉴>
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == R.id.more_info) {
            Intent intent = new Intent(MainActivity.this, AppInfoActivity.class);
            startActivity(intent);
            return true;
        }
        else
            return super.onOptionsItemSelected(item);
    }
    //</툴바 메뉴>

    //<이벤트>
    public void btnIpAddr(View view){
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View textfiled = inflater.inflate(R.layout.dialog_textedit, null);

        //텍스트필드 설정
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                Pattern ps = Pattern.compile("^[0-9.]*$");

                if(source.equals("") || ps.matcher(source).matches()){
                    return source;
                }

                return "";
            }
        };

        InputFilter[] filters = new InputFilter[]{
                filter,
                new InputFilter.LengthFilter(15)
        };

        EditText et = textfiled.findViewById(R.id.dlg_editText);
        et.setText(prmt_ipAddr);
        et.setFilters(filters);
        et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_PHONE);

        AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
        dlg.setTitle("IP 주소 설정");
        dlg.setMessage("이 기기의 배터리 정보를 전달할 클라이언트의 IP 주소를 입력합니다.");
        dlg.setView(textfiled);

        //완료버튼
        dlg.setPositiveButton(dev.oneuiproject.oneui.design.R.string.oui_common_done, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){
                Dialog f = (Dialog) dialog;

                EditText textfield = (EditText) f.findViewById(R.id.dlg_editText);
                prmt_ipAddr = textfield.getText().toString();

                editor.putString(KEY_IP, prmt_ipAddr);
                editor.apply();
                update();
            }
        });

        //취소버튼
        dlg.setNegativeButton(dev.oneuiproject.oneui.design.R.string.oui_common_cancel, new DialogInterface.OnClickListener(){
           public void onClick(DialogInterface dialog, int whichButton){

           }
        });

        AlertDialog dialog = dlg.create();

        //입력 오류검사
        et.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){}
            @Override
            public void afterTextChanged(Editable editable){}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){
                if(!checkIp(et)){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }else{
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        et.selectAll();
        dialog.show();
    }

    public void btnPort(View view){
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View textfiled = inflater.inflate(R.layout.dialog_textedit, null);

        //텍스트 필드 설정
        EditText et = textfiled.findViewById(R.id.dlg_editText);
        et.setText(prmt_port);
        et.setInputType(InputType.TYPE_CLASS_NUMBER);

        AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
        dlg.setTitle("포트 설정");
        dlg.setMessage("이 기기의 배터리 정보를 전달할 클라이언트의 포트를 입력합니다.");
        dlg.setView(textfiled);

        //완료버튼
        dlg.setPositiveButton(dev.oneuiproject.oneui.design.R.string.oui_common_done, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){
                Dialog f = (Dialog) dialog;

                EditText textfield = (EditText) f.findViewById(R.id.dlg_editText);
                prmt_port = textfield.getText().toString();

                editor.putString(KEY_PORT, prmt_port);
                editor.apply();
                update();
            }
        });

        //취소버튼
        dlg.setNegativeButton(dev.oneuiproject.oneui.design.R.string.oui_common_cancel, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){

            }
        });

        AlertDialog dialog = dlg.create();

        //입력 오류검사
        et.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){}
            @Override
            public void afterTextChanged(Editable editable){}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){
                if(isEmpty(et)){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }else{
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        et.selectAll();
        dialog.show();
    }

    public void btnParmBatteryLevel(View view){
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View textField = inflater.inflate(R.layout.dialog_textedit, null);

        EditText et = textField.findViewById(R.id.dlg_editText);
        et.setText(prmtNm_batteryLevel);

        AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
        dlg.setTitle("파라미터 설정");
        dlg.setMessage("이 기기의 배터리 정보를 전달될 파라미터명을 입력합니다.");
        dlg.setView(textField);

        //완료 버튼
        dlg.setPositiveButton(dev.oneuiproject.oneui.design.R.string.oui_common_done, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){
                Dialog f = (Dialog) dialog;

                EditText textField = (EditText) f.findViewById(R.id.dlg_editText);
                prmtNm_batteryLevel = textField.getText().toString();

                editor.putString(KEY_PRMT_BATTERYLEVEL, prmtNm_batteryLevel);
                editor.apply();
                update();
            }
        });

        //취소 버튼
        dlg.setNegativeButton(dev.oneuiproject.oneui.design.R.string.oui_common_cancel, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){}
        });

        AlertDialog dialog = dlg.create();

        //입력 오류 검사
        et.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){}
            @Override
            public void afterTextChanged(Editable editable){}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){
                if(isEmpty(et)){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }else if(et.getText().toString().equals(prmtNm_isCharge)){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
                else{
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        et.selectAll();
        dialog.show();
    }

    public void btnParmIsCharging(View view){
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View textField = inflater.inflate(R.layout.dialog_textedit, null);

        EditText et = textField.findViewById(R.id.dlg_editText);
        et.setText(prmtNm_isCharge);

        AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
        dlg.setTitle("파라미터 설정");
        dlg.setMessage("이 기기의 배터리 정보를 전달될 파라미터명을 입력합니다.");
        dlg.setView(textField);

        //완료 버튼
        dlg.setPositiveButton(dev.oneuiproject.oneui.design.R.string.oui_common_done, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){
                Dialog f = (Dialog) dialog;

                EditText textField = (EditText) f.findViewById(R.id.dlg_editText);
                prmtNm_isCharge = textField.getText().toString();

                editor.putString(KEY_PRMT_ISCHARGING, prmtNm_isCharge);
                editor.apply();

                update();
            }
        });

        //취소 버튼
        dlg.setNegativeButton(dev.oneuiproject.oneui.design.R.string.oui_common_cancel, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int whichButton){}
        });

        AlertDialog dialog = dlg.create();

        //입력 오류 검사
        et.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){}
            @Override
            public void afterTextChanged(Editable editable){}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){
                if(isEmpty(et)){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }else if(et.getText().toString().equals(prmtNm_batteryLevel)){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
                else{
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        et.selectAll();
        dialog.show();
    }

    public void btn_labs(View view){
        Intent intent = new Intent(MainActivity.this, LabsActivity.class);
        startActivity(intent);
    }
    //</이벤트>

    class UiHander extends Handler{
        @Override
        public void handleMessage (@NonNull Message msg){
            super.handleMessage(msg);

            Bundle bundle = msg.getData();

            batteryLevel = bundle.getInt("s_batLv");
            isCharge = bundle.getBoolean("b_isCharging");

            text_batteryLevel.setText(String.format("%d%%", batteryLevel));
            if(isCharge){
                text_batteryLevel.append("(충전중)");
            }
        }
    }

    // 백그라운드 쓰레드
    class BackgoundThread extends Thread{

        public void run(){
            super.run();

            Log.d("trace", "Thread started");

            int old_batterypct = 0;
            boolean old_isCharging = false;

            while(true){
                try{
                    Thread.sleep(100);
                }catch (InterruptedException e){
                    Log.d("trace", "main activity thread interrupted");
                    break;
                }catch (Exception e){
                    Log.e("trace", e.toString());
                    break;
                }

                IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

                Intent batteryStatus = getApplicationContext().registerReceiver(null, intentFilter);
                assert batteryStatus != null;
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

                boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                float batteryPct = level * 100 / (float) scale;

                Message message = uiHander.obtainMessage();

                Bundle bundle = new Bundle();
                bundle.putInt("s_batLv", (int) batteryPct);
                bundle.putBoolean("b_isCharging", isCharging);

                message.setData(bundle);

                if(old_batterypct != (int)batteryPct || old_isCharging != isCharging){

                    old_batterypct = (int)batteryPct;
                    old_isCharging = isCharging;
                    uiHander.sendMessage(message);
                }
            }
        }
    }
}
