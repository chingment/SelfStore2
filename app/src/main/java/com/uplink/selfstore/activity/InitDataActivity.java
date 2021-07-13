package com.uplink.selfstore.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.uplink.selfstore.BuildConfig;
import com.uplink.selfstore.activity.adapter.LogAdapter;
import com.uplink.selfstore.db.DbManager;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByDS;
import com.uplink.selfstore.deviceCtrl.CabinetCtrlByZS;
import com.uplink.selfstore.deviceCtrl.FingerVeinnerCtrl;
import com.uplink.selfstore.deviceCtrl.ScannerCtrl;
import com.uplink.selfstore.model.LogBean;
import com.uplink.selfstore.model.api.CabinetBean;
import com.uplink.selfstore.model.api.DeviceBean;
import com.uplink.selfstore.model.api.CustomDataByVendingBean;
import com.uplink.selfstore.own.AppCacheManager;
import com.uplink.selfstore.own.Config;
import com.uplink.selfstore.R;
import com.uplink.selfstore.http.HttpResponseHandler;
import com.uplink.selfstore.model.api.ApiResultBean;
import com.uplink.selfstore.model.api.DeviceInitDataResultBean;
import com.uplink.selfstore.model.api.Result;
import com.uplink.selfstore.service.AiotMqttOption;
import com.uplink.selfstore.service.AiotMqttService;
import com.uplink.selfstore.service.AlarmService;
import com.uplink.selfstore.service.MqttService;
import com.uplink.selfstore.service.UpdateAppService;
import com.uplink.selfstore.ui.BaseFragmentActivity;
import com.uplink.selfstore.ui.CameraWindow;
import com.uplink.selfstore.ui.LoadingView;
import com.uplink.selfstore.ui.my.MyListView;
import com.uplink.selfstore.utils.FileUtil;
import com.uplink.selfstore.utils.LocationUtil;
import com.uplink.selfstore.utils.LogUtil;
import com.uplink.selfstore.utils.LongClickUtil;
import com.uplink.selfstore.utils.StringUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class InitDataActivity extends BaseFragmentActivity implements View.OnClickListener {
    private static final String TAG = "InitDataActivity";

    private Handler handler_msg;
    private Button btn_retry;
    private ProgressBar loading_bar;
    private LoadingView loading_ani;
    private TextView loading_msg;
    private TextView txt_deviceId;
    private TextView txt_version;
    private TextView txt_comname;
    private MyListView list_log;
    private View btn_inittool;

    private List<LogBean> logs=new ArrayList<>();
    private CabinetCtrlByDS cabinetCtrlByDS=null;
    private CabinetCtrlByZS cabinetCtrlByZS=null;
    private boolean initIsRun=false;
    private Handler initHandler = new Handler();
    private Runnable initRunable = new Runnable() {
        @Override
        public void run() {

            if(!initIsRun) {
                initIsRun=true;
                setDeviceInitData();
            }

            initHandler.postDelayed(this, 1000);
        }
    };

    public final int WHAT_TIPS = 1;
    public final int WHAT_READ_CONFIG_SUCCESS = 2;
    public final int WHAT_READ_CONFIG_FAILURE = 3;
    public final int WHAT_SET_CONFIG_SUCCESS = 4;
    public final int WHAT_SET_CONFIG_FALURE = 5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initdata);

        //Intent mqttService2 = new Intent(getAppContext(), AiotMqttService.class);
        //startService(mqttService2);


//        IdWorker worker = new IdWorker(1,1,1);
//
//        long id=worker.nextId();
//
//        long id2=worker.nextId();
//
//        long id3=worker.nextId();
//        TestBean testBean=new TestBean();
//        testBean.setName("大毛");
//        testBean.setPhone("13600000001");
//
//      TestBeanDao testBeanDao= DbManager.getInstance().getDaoSession().getTestBeanDao();
//
////插入数据
//        testBeanDao.insert(testBean);
////数据存在则替换，数据不存在则插入
//        testBeanDao.insertOrReplace(testBean);
//
////条件查询10条数据
//        List<TestBean> list=testBeanDao.queryBuilder().where(TestBeanDao.Properties.Name.eq("大毛")).limit(10).build().list();
//
////查询全部
//        List<TestBean> list2=testBeanDao.queryBuilder().build().list();
//
////删除
//        testBeanDao.delete(testBean);


        //因mqtt文件被锁定未被删除造成对象未空，因此进入程序首先删除MqttConnection文件 防止对象未空

        File mqttConnection= getAppContext().getExternalFilesDir("MqttConnection");

        //String dirPath = Environment.getExternalStorageDirectory() + "/Android/data/"+BuildConfig.APPLICATION_ID+"/files/MqttConnection";
        //String dirPath="/storage/emulated/0/Android/data/com.uplink.selfstore/files/MqttConnection/mch_202004220011-tcp112741791851883";


        FileUtil.deleteDirWihtFile(mqttConnection);


        initView();
        initEvent();
        initData();

        initHandler.postDelayed(initRunable, 1000);


        Intent updateAppService = new Intent(this, UpdateAppService.class);
        startService(updateAppService);

        Intent alarmService = new Intent(this, AlarmService.class);
        startService(alarmService);

        Intent mqttService = new Intent(this, MqttService.class);
        stopService(mqttService);

        cabinetCtrlByDS = CabinetCtrlByDS.getInstance();
        cabinetCtrlByZS = CabinetCtrlByZS.getInstance();

        FingerVeinnerCtrl.getInstance().tryGetPermission(InitDataActivity.this);


        DeviceBean device = AppCacheManager.getDevice();

        Map<String, Object> params = new HashMap<>();
        params.put("appId", BuildConfig.APPLICATION_ID);
        params.put("deviceId", device.getDeviceId());
        params.put("lat", LocationUtil.LAT);
        params.put("lng", LocationUtil.LNG);
        params.put("eventCode", "1110");
        params.put("eventRemark", "sdad");


        int msg_id= DbManager.getInstance().saveTripMsg(Config.URL.device_EventNotify,JSON.toJSONString(params));

    }

    private void initView() {
        btn_retry = (Button) findViewById(R.id.btn_retry);
        loading_ani = (LoadingView) findViewById(R.id.loading_ani);
        loading_bar = (ProgressBar) findViewById(R.id.loading_bar);
        loading_msg = (TextView) findViewById(R.id.loading_msg);
        txt_deviceId=(TextView) findViewById(R.id.txt_deviceId);
        txt_version=(TextView) findViewById(R.id.txt_version);
        txt_comname=(TextView) findViewById(R.id.txt_comname);
        list_log=(MyListView)findViewById(R.id.list_log);
        btn_inittool=(View)findViewById(R.id.btn_inittool);

    }


    private void initEvent() {

        btn_retry.setOnClickListener(this);

        LongClickUtil.setLongClick(new Handler(), btn_inittool, 500, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Intent intent = new Intent(getAppContext(), SmRescueToolActivity.class);
                startActivity(intent);
                finish();

                return true;
            }
        });


        loading_ani.start();

        handler_msg = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                String message="";
                Bundle bundle=msg.getData();
                if(bundle!=null) {
                    message = bundle.getString("message", "");
                }

                if(!StringUtil.isEmptyNotNull(message)) {
                    loading_msg.setText(message);

                    LogBean log = new LogBean();

                    Date currentTime = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.CHINA);
                    String dateString = sdf.format(currentTime);

                    log.setDateTime(dateString);
                    log.setContent(message);
                    logs.add(log);

                    List<LogBean> reverseLogs = new ArrayList<>();

                    for (int i = logs.size(); i > 0; i--) {
                        if (reverseLogs.size() > 10) {
                            break;
                        }
                        reverseLogs.add(logs.get(i - 1));
                    }

                    LogAdapter logAdapter = new LogAdapter(InitDataActivity.this, reverseLogs);
                    list_log.setAdapter(logAdapter);
                }


                switch (msg.what) {
                    case WHAT_TIPS:
                        break;
                    case WHAT_READ_CONFIG_SUCCESS:
                        setHandleMessage(WHAT_TIPS, "正在配置设备信息");
                        try {

                            if(bundle==null){
                                setHandleMessage(WHAT_SET_CONFIG_FALURE, "配置设备信息失败：bundle对象为控");
                                return false;
                            }

                            if(bundle.getSerializable("deviceInitDataResultBean")==null) {
                                setHandleMessage(WHAT_SET_CONFIG_FALURE, "配置设备信息失败：初始数据为空");
                                return false;
                            }

                            DeviceInitDataResultBean initData = (DeviceInitDataResultBean) bundle.getSerializable("deviceInitDataResultBean");//全局数据

                            if(initData==null) {
                                setHandleMessage(WHAT_SET_CONFIG_FALURE, "配置设备信息失败：初始对象为空");
                                return false;
                            }

                            DeviceBean device = initData.getDevice();//设备数据

                            if(device==null|| StringUtil.isEmptyNotNull(device.getDeviceId())) {
                                setHandleMessage(WHAT_SET_CONFIG_FALURE, "配置设备信息失败：设备对象为空");
                                return false;
                            }

                            if(StringUtil.isEmptyNotNull(device.getType())) {
                                setHandleMessage(WHAT_SET_CONFIG_FALURE, "配置设备信息失败：未知设备类型");
                                return false;
                            }

                            AppCacheManager.setDevice(initData.getDevice());//保存设备信息

                            CustomDataByVendingBean customDataByVending = JSON.parseObject(JSON.toJSONString(initData.getCustomData()), CustomDataByVendingBean.class);

                            AppCacheManager.setCustomDataByVending(customDataByVending);//设置全局缓存数据

                            AppCacheManager.clearCartSkus();//清空购物车数据

                            ScannerCtrl.getInstance().setComId(device.getScanner().getComId());//设置扫描器串口ID

                            //根据机构类型设置串口信息
                            HashMap<String, CabinetBean> cabinets = device.getCabinets();

                            HashMap<String, String> modelNos = new HashMap<>();

                            for (HashMap.Entry<String, CabinetBean> entry : cabinets.entrySet()) {
                                CabinetBean cabinet = entry.getValue();
                                if (!modelNos.containsKey(cabinet.getModelNo())) {
                                    modelNos.put(cabinet.getModelNo(), cabinet.getComId());
                                }
                            }

                            for (HashMap.Entry<String, String> modelNo : modelNos.entrySet()) {
                                switch (modelNo.getKey()) {
                                    case "dsx01":
                                        cabinetCtrlByDS.setComId(modelNo.getValue());
                                        cabinetCtrlByDS.connect();
                                        cabinetCtrlByDS.firstSet();
                                        break;
                                    case "zsx01":
                                        cabinetCtrlByZS.setComId(modelNo.getValue());
                                        break;
                                }
                            }

                            CameraWindow.setInSampleSize(device.getPicInSampleSize());

                            setHandleMessage(WHAT_SET_CONFIG_SUCCESS, "信息配置完成，正在启动设备恢复原始状态");

                            new Thread(new Runnable() {
                                public void run() {
                                    SystemClock.sleep(6000);

                                    setHandleMessage(WHAT_TIPS, "配置结束，进入购物车界面");

                                    Intent mqttService = new Intent(getAppContext(), MqttService.class);
                                    startService(mqttService);

                                    Intent intent = new Intent(getAppContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }).start();

                        } catch (Exception ex) {
                            setHandleMessage(WHAT_SET_CONFIG_FALURE, "配置设备信息失败：" + ex.getMessage());
                        }
                        break;
                    case WHAT_READ_CONFIG_FAILURE:
                        setHandleMessage(WHAT_TIPS, "重新尝试读取设备信息");
                        initIsRun=false;
                        break;
                }

                return  false;
            }
        });
    }

    private void initData() {
        txt_deviceId.setText(getAppContext().getDeviceId());
        txt_version.setText(BuildConfig.VERSION_NAME);
        txt_comname.setText(BuildConfig.COMNAME);
    }

    @Override
    public void onClick(View v) {


    }

    @Override
    public void onStop() {
        super.onStop();

        if(loading_ani!=null) {
            loading_ani.stop();
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        if(loading_ani!=null) {
            loading_ani.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(loading_ani!=null) {
            loading_ani.stop();
        }

        if(initHandler!=null&&initRunable!=null) {
            initHandler.removeCallbacks(initRunable);
        }

        if(cabinetCtrlByDS!=null) {
            cabinetCtrlByDS.disConnect();
        }

        FingerVeinnerCtrl.getInstance().unregisterReceiver(InitDataActivity.this);
    }

    public void setHandleMessage(int what, String msg, DeviceInitDataResultBean deviceInitDataResult) {
        final Message m = new Message();
        m.what = what;
        Bundle bundle = new Bundle();
        bundle.putString("message", msg);
        if(deviceInitDataResult!=null) {
            bundle.putSerializable("deviceInitDataResultBean", deviceInitDataResult);
        }
        m.setData(bundle);
        handler_msg.sendMessage(m);
    }

    public void setHandleMessage(int what, String msg) {
        setHandleMessage(what,msg,null);
    }

    public void setDeviceInitData() {

        setHandleMessage(WHAT_TIPS, getAppContext().getString(R.string.aty_initdata_tips_settingdevice));

        Map<String, Object> params = new HashMap<>();
        params.put("deviceId", getAppContext().getDeviceId());
        params.put("imeiId",getAppContext().getImeiId());
        params.put("appVersionCode", BuildConfig.VERSION_CODE);
        params.put("appVersionName", BuildConfig.VERSION_NAME);
        params.put("ctrlSdkVersionCode", cabinetCtrlByDS.vesion());
        params.put("macAddress", getAppContext().getMacAddress());

        postByMy(InitDataActivity.this, Config.URL.device_InitData, params,null, false, "", new HttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
                ApiResultBean<DeviceInitDataResultBean> rt = JSON.parseObject(response, new TypeReference<ApiResultBean<DeviceInitDataResultBean>>() {
                });
                if (rt.getResult() == Result.SUCCESS) {
                    setHandleMessage(WHAT_READ_CONFIG_SUCCESS, getAppContext().getString(R.string.aty_initdata_tips_settingdevicecfgreadsuccess),rt.getData());
                } else {
                    setHandleMessage(WHAT_READ_CONFIG_FAILURE, getAppContext().getString(R.string.aty_initdata_tips_settingdevicecfgreadfailure) + ":" + rt.getMessage());
                }
            }

            @Override
            public void onFailure(String msg, Exception e) {
                setHandleMessage(WHAT_READ_CONFIG_FAILURE, getAppContext().getString(R.string.aty_initdata_tips_settingdevicecfgreadfailure) + ":" + msg);
            }
        });
    }
}
