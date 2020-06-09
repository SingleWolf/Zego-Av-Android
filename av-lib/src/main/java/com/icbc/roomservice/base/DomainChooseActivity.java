package com.icbc.roomservice.base;

import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.icbc.roomservice.base.data.ZegoDataCenter;
import com.icbc.roomservice.kernelcode.ZegoApiManager;
import com.icbc.roomservice.kernelcode.utils.LogToFileUtils;
import com.zego.zegoavkit2.ZegoExternalVideoCapture;
import com.zego.zegoavkit2.screencapture.ZegoScreenCaptureFactory;
import com.zego.zegoliveroom.constants.ZegoConstants;

public class DomainChooseActivity extends BaseActivity implements View.OnClickListener,AdapterView.OnItemSelectedListener {

    //是否初始化了SDK
    private static boolean InitSuccessState=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_choose);
        initView();
    }

    private void initView() {
        TextView moce=findViewById(R.id.tv_domain_moce);
        TextView gongneng=findViewById(R.id.tv_domain_gongneng);
        TextView kaifa=findViewById(R.id.tv_domain_kaifa);
        TextView shengchan=findViewById(R.id.tv_domain_shengchan);
        moce.setOnClickListener(this);
        gongneng.setOnClickListener(this);
        kaifa.setOnClickListener(this);
        shengchan.setOnClickListener(this);

        //初始化下拉选择框
        String[] IPs={"不开启屏幕共享","开启屏幕共享"};
        Spinner spinner = (Spinner) findViewById(R.id.SpinnerScreenCapture);
        //将可选内容与ArrayAdapter连接起来
        ArrayAdapter<String> adapter= new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,IPs);
        //设置下拉列表的风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        spinner.setAdapter(adapter);
        //添加事件Spinner事件监听
        spinner.setOnItemSelectedListener(this);
        //设置默认值
        spinner.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        if(InitSuccessState){
            if(ZegoApiManager.getInstance().unInitSDK()){
                InitSuccessState=false;
                LogToFileUtils.write("unInitSDK success");
            }
        }
        if(v.getId()==R.id.tv_domain_moce){
            LogToFileUtils.write("use 114.255.225.36:15000");
            ZegoDataCenter.setTokenUrl("http://114.255.225.36:16000/logintoken");
            ZegoApiManager.setDomainName("114.255.225.36:15000");
//            Log.d("QueueListActivity","use 114.255.225.36:15443");//外网域名访问
//            ZegoDataCenter.setTokenUrl("https://rtc1.dccnet.com.cn:16443/logintoken");
//            ZegoApiManager.setUseHttps(true);
//            ZegoApiManager.setDomainName("rtc1.dccnet.com.cn:15443");
        }if(v.getId()==R.id.tv_domain_gongneng){
            LogToFileUtils.write("use 114.255.225.35:15000");
            ZegoDataCenter.setTokenUrl("http://114.255.225.35:16000/logintoken");
            ZegoApiManager.setDomainName("114.255.225.35:15000");
        }if(v.getId()==R.id.tv_domain_kaifa){
            LogToFileUtils.write("use 114.255.225.51:15000");
            ZegoDataCenter.setTokenUrl("http://114.255.225.51:16000/logintoken");
            ZegoApiManager.setDomainName("114.255.225.51:15000");
        }
        if(ZegoApiManager.getInstance().initZegoSDK(ZegoDataCenter.ZEGO_USER,ZegoDataCenter.getAPP_ID(),ZegoDataCenter.getAPP_SIGN())){
            InitSuccessState=true;
            LogToFileUtils.write("initZegoSDK success");
        }
        //Intent intent = new Intent(this, RoomChooseActivity.class);
        //startActivity(intent);
    }

    // 屏幕采集相关类
    private MediaProjectionManager mMediaProjectionManager;
    private static final int REQUEST_CODE = 1001;
    private MediaProjection mMediaProjection;
    // 录屏采集工厂
    private ZegoScreenCaptureFactory screenCaptureFactory;
    //启动屏幕共享
    // ZEGO SDK外部采集类
    private ZegoExternalVideoCapture videoCapture;
    private void startScreenCapture(){
        // 1. 请求录屏权限，等待用户授权
        mMediaProjectionManager =  (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mMediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
    }

    // 2.实现请求录屏权限结果通知接口
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogToFileUtils.write("onActivityResult requestCode:"+requestCode+",resultCode:"+resultCode);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            LogToFileUtils.write("onActivityResult success");

            //3.获取MediaProjection
            mMediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            //4.创建录屏工厂
            screenCaptureFactory = new ZegoScreenCaptureFactory();
            //5.设置MediaProjection
            screenCaptureFactory.setMediaProjection(mMediaProjection);

            //6.外部视频采集设置外部采集工厂
            videoCapture.setVideoCaptureFactory(screenCaptureFactory, ZegoConstants.PublishChannelIndex.AUX);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
        if(position==0){
        }else if (position==1){
            startScreenCapture();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent){
    }
}
