package com.ryokusasa.w3033901.cut_in_app_2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationManagerCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    /* サービス関連 */
    private boolean isConnection = false;   //サービスにつながってるか
    private CutinService mService;             //サービス

    //コネクション作成
    private ServiceConnection serviceConnection;

    /* パーミッション関連 */
    private final int OVERLAY_PERMISSION_REQUEST_CODE = 893;    //リクエストコード
    private final int NOTIFICATION_PERMISSION_REQUEST_CODE = 810;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                    //Binderからservice取得
                    mService = ((CutinService.ServiceBinder)service).getService();
                    Log.i("MainActivity", "onConnected");
                    isConnection = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
                Log.i("MainActivity", "Disnnected");
                isConnection = false;
            }
        };

        Button startButton = (Button)findViewById(R.id.button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //サービス開始
                if (checkOverlayPermission(v.getContext()) && !isConnection){
                    Intent intent = new Intent(MainActivity.this, CutinService.class);
                    startService(intent);
                    bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
                }
            }
        });

        Button endButton = (Button)findViewById(R.id.button2);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("MainActivity", "onClick");
                if(checkOverlayPermission(v.getContext()) && isConnection){
                    Intent intent = new Intent(MainActivity.this, CutinService.class);
                    unbindService(serviceConnection);
                    stopService(intent);
                    isConnection = false;
                }
            }
        });

        //権限確認
        if (!checkOverlayPermission(this)) requestOverlayPermission();
    }

    //メニュー生成
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    //メニュー選択時
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.request_overlay:
                requestOverlayPermission();
                return true;
            case R.id.request_notification:
                requestNotificationPermission();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //再表示されたとき
    @Override
    protected void onResume()
    {
        super.onResume();
        if (!checkOverlayPermission(this)){
            Toast.makeText(this, "オーバーレイの権限がないと実行できません", Toast.LENGTH_SHORT).show();
        }
        if (!checkNotificationPermission(this)){
            Toast.makeText(this, "通知アクセス権限がないと実行できません", Toast.LENGTH_SHORT).show();
        }
    }

    //オーバーレイ権限チェック
    public Boolean checkOverlayPermission(Context context){
        return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(context); //APILevel23未満は常時ON
    }

    //オーバーレイリクエスト処理
    public void requestOverlayPermission()
    {
        //なんかワーニング出てるけどcheckOverlayPermissionで23以下は排除済み
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        this.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
    }

    //通知アクセス権限チェック
    private boolean checkNotificationPermission(Context context){
        if (Build.VERSION.SDK_INT >= 18) {
            for (String service : NotificationManagerCompat.getEnabledListenerPackages(context)) {
                if (service.equals(getPackageName()))
                    return true;
            }
        }
        return false;
    }

    //通知アクセスリクエスト処理
    private void requestNotificationPermission(){
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        startActivityForResult(intent, NOTIFICATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE){
            if(!checkOverlayPermission(this)){
                //オーバレイの権限がない場合
                Toast.makeText(this, "オーバーレイの権限がないと実行できません", Toast.LENGTH_SHORT).show();
            }
        }else if(requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE)
            if(!checkNotificationPermission(this)){
                Toast.makeText(this, "通知アクセス権限がないと実行できません", Toast.LENGTH_SHORT).show();
            }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
