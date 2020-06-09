package com.icbc.roomservice.demo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.icbc.roomservice.base.BaseActivity;
import com.icbc.roomservice.base.VideoServiceActivity;
import com.icbc.roomservice.kernelcode.utils.LogToFileUtils;

import static com.icbc.roomservice.base.data.ZegoDataCenter.ZEGO_USER;

public class RoomChooseActivity extends BaseActivity implements View.OnClickListener {

    private final static int PERMISSIONS_REQUEST_CODE = 101;

    private EditText mEtRoomID;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        mEtRoomID = findViewById(R.id.et_room_id);

        findViewById(R.id.bt_join_room).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bt_join_room:
                // 先检查权限
                if (checkOrRequestPermission(PERMISSIONS_REQUEST_CODE)) {
                    startVideoServiceActivity();
                }
                break;
        }
    }

    private void startVideoServiceActivity() {
        String roomID = mEtRoomID.getText().toString();
        LogToFileUtils.write("the imput roomID:"+roomID);
        if (TextUtils.isEmpty(roomID)) {
            roomID = ZEGO_USER.userID + "";
        }

        Intent intent = new Intent(this, VideoServiceActivity.class);
        intent.putExtra(VideoServiceActivity.EXTRA_KEY_ROOM_ID, roomID);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE: {
                boolean allPermissionGranted = true;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        allPermissionGranted = false;
                        Toast.makeText(this, String.format("获取%s权限失败 ", permissions[i]), Toast.LENGTH_LONG).show();
                    }
                }
                if (!allPermissionGranted) {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + this.getPackageName()));
                    startActivity(intent);
                }
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
