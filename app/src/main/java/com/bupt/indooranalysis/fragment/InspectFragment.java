package com.bupt.indooranalysis.fragment;


import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bupt.indoorPosition.bean.Beacon;
import com.bupt.indoorPosition.bean.Buildings;
import com.bupt.indoorPosition.bean.CalculatePosition;
import com.bupt.indoorPosition.bean.IndoorSignalRecord;
import com.bupt.indoorPosition.bean.InspectedBeacon;
import com.bupt.indoorPosition.bean.Sim;
import com.bupt.indoorPosition.callback.FragmentServiceCallback;
import com.bupt.indoorPosition.callback.InspectUpdateCallback;
import com.bupt.indoorPosition.dao.DBManager;
import com.bupt.indoorPosition.location.LocationProvider;
import com.bupt.indoorPosition.model.ModelService;
import com.bupt.indoorPosition.uti.BeaconUtil;
import com.bupt.indoorPosition.uti.Constants;
import com.bupt.indoorPosition.uti.MessageUtil;
import com.bupt.indooranalysis.AccelerometerService;
import com.bupt.indooranalysis.MainActivity;
import com.bupt.indooranalysis.R;
import com.bupt.indooranalysis.ScanSignalService;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.sails.engine.MapViewPosition;
import com.sails.engine.SAILS;
import com.sails.engine.SAILSMapView;
import com.sails.engine.core.model.BoundingBox;
import com.sails.engine.core.model.GeoPoint;
import com.sails.engine.overlay.ListOverlay;
import com.sails.engine.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.bupt.indoorPosition.bean.Buildings.buildingPara;
import static com.bupt.indoorPosition.bean.Buildings.currentBuilding;
import static com.bupt.indoorPosition.bean.Buildings.currentFloor;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InspectFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InspectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InspectFragment extends Fragment implements
        InspectUpdateCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public static boolean isInArea = false;
    //buiding localization parameters
    public static int buildingCollectNum = 5;
    public static int buildingCollectTime = 5;
    public static int buildingCollectSleep = 0;
    //存储上次定位结果
    private boolean isFirstLocal = true;
    private int[] lastLocal = new int[]{0,0};
    public static int buildingDisThreshold = 0;
    public static int buildingNumber = 0;
    public static int buildingfloor = 0;
    static SAILS mSails;
    static SAILSMapView mSailsMapView;
    public FloatingActionMenu floatingActionMenu;
    public FloatingActionButton actionButton;
    public ArrayList<SubActionButton> floorbuttons;
    EditText editText1;
    EditText editText2;
    TextView locationTextView;
    Vibrator mVibrator;
    ArrayAdapter<String> adapter;
    byte zoomSav = 0;
    GeoPoint geoPointCenter;
    // picture
    // GeoPoint geoPointLocationLB = new GeoPoint(39.96289053181642,
    // 116.35293102867222);
    // GeoPoint geoPointLocationRT = new GeoPoint(39.963035980045404,
    // 116.35312079496002);
    // drawable
    GeoPoint geoPointLocationLB = new GeoPoint(34.7482912, 113.7926698);
    GeoPoint geoPointLocationRT = new GeoPoint(34.7500686, 113.7941235);
    BoundingBox boundingBox;
    MapViewPosition mapViewPositionBase;
    int tempX = 0;
    int tempY = 0;
    ListOverlay listOverlay = new ListOverlay();
    int flag = 0;
    private String LOG_TAG = "InspectFragment";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Spinner spinner;
    private ArrayList<String> locationList;
    private ArrayAdapter<String> arrayAdapter;
    private android.support.design.widget.FloatingActionButton btnClear;
    private android.support.design.widget.FloatingActionButton btnUpload;
    private EditText locationEditX;
    private EditText locationEditY;
    private boolean isScanning = false;
    private MainActivity activity = null;
    private Context mcontext = null;
    private List<Map<String, Object>> listData = null;
    private Bundle savedState;
    private boolean isDeleting = false;
    private boolean isUpdating = false;
    private boolean isFABinit = false;
    private android.support.design.widget.FloatingActionButton inspectButton;
    private TextView floorNumTV;
    private OnFragmentInteractionListener mListener;
    private Set<Beacon> beaconSet;
    private BluetoothAdapter bAdapter;
    private Timer findLostBeaconTimer;
    private Handler handler;
    // 重启BLE扫描计时
    private int scanCount = 0;
    private int localizationCount = 0;
    // 蓝牙没有反应计时
    private int bleNoReactCount = 0;
    private long timeZero;
    private int count = 0;
    private Timer LocalizationTimer;
    private Timer GetBluetoothDeviceTimer;
    private Set<Beacon> beaconMap;
    //测试使用计数器
    private Map<String, Integer> ScanBlueToothTimesInPeriod = new HashMap<String, Integer>();
    //定位boolean
    private int isCalposition;
    //更新flag
    private boolean isUpdatedOver = false;
    //scanServer
    private Intent scanServiceintent;
    //蓝牙计数flag
    private int bluecount = 0;
    private ArrayList<String> floor = new ArrayList<>();
    //spinner默认会click一下，该变量用来区别自动还是手动click
    private boolean bulidingSpinnerClickFirstly = false;
    //是否需要动态调整地图缩放等级
    private boolean zoomFlag = true;
    Runnable updateBuildingMaps = new Runnable() {
        @Override
        public void run() {

            String buidingCode = Buildings.BuildingsList.get(currentBuilding).getCode();
            mSails.loadCloudBuilding("ef608be1ea294e3ebcf6583948884a2a", buidingCode, // keyanlou
                    new SAILS.OnFinishCallback() {
                        @Override
                        public void onSuccess(String response) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mapViewInitial();
                                        initFloorSelectButton();
                                        if (isFABinit) actionButton.setVisibility(View.VISIBLE);
                                    } catch (Exception e) {
                                        Log.e(LOG_TAG, "mapViewInitial出错:" + e.toString());
                                    }
                                }
                            });
                        }

                        //没有网络链接时程序崩溃,待解决
                        @Override
                        public void onFailed(String response) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (isFABinit) actionButton.setVisibility(View.INVISIBLE);
                                    Toast.makeText(mcontext, "Load cloud project fail, " +
                                            "please check network connection.", Toast
                                            .LENGTH_SHORT).show();
                                    Log.e(LOG_TAG, "mapViewInitial onFailed");
                                    floorNumTV.setText("请检查网络连接");
                                }
                            });

                        }
                    });
        }
    };
    private AccelerometerService accelerometerService;
    private int calPositionInsertTimes = 0;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(LOG_TAG, "onServiceConnected");
            accelerometerService = ((AccelerometerService.MyBinder) service).getService();
            accelerometerService.setOnAccelerometerChangeListener(new AccelerometerService.OnAccelerometerChangeListener() {
                @Override
                public void onAccelerationChange(float delta) {
                    //处理加速度传感器传回的加速度差值
                    Log.i(LOG_TAG, "Delta callback");
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(LOG_TAG, "onServiceDisconnected");
        }
    };
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            // Log.d("callback", device.getAddress() + "\n" + rssi);
            // int sec = (int) ((System.currentTimeMillis() - timeZero) / 1000);
            // Log.e("onLeScan", "第" + sec + "s ,发现" + device.getAddress() +
            // "\n有"
            // + beaconSet.size() + "个");
            if (device.getAddress() != null && rssi <= 0) {
                // if(device.getAddress().equals("98:7B:F3:72:23:C5")){
                // System.out.println(123456789);
                // }
                bleNoReactCount = 0;
                int txPower = BeaconUtil.getBeaconTxPower(scanRecord);
                if (device.getAddress().equals("98:7B:F3:72:23:C5")) {
                    System.out.println(123456789 + " txPower " + txPower);
                }
                // 针对某些没有设置txpower的蓝牙芯片，设置默认的参考发射功率
                if (txPower > 0) {
                    txPower = -60;
                }
                int dis = BeaconUtil.calculateAccuracyForLocalization(txPower, rssi);
                if(currentBuilding.equals("郑州中原金融产业园1栋")){
                    dis = BeaconUtil.calculateAccuracyForZz(txPower, rssi);
                }
                if (device.getAddress().equals("80:30:DC:0D:F6:0F")) {
                    // String newMac = device.getAddress();
                    // DBManager dbManager = new
                    // DBManager(IndoorLocationActivity.this);
                    // if (dbManager.isContainLocalization(newMac)) {
                    // Log.d("genxinshujufordistance",
                    // "" + dis + " " + device.getAddress() + " " + rssi + " " +
                    // "txPower:" + txPower);
//                    Log.d("InsepctInsepct","80:30:DC:0D:F6:0F"+" "+rssi);
                    bluecount += 1;
                }
                ModelService.updateBeaconForLocal(mcontext, beaconSet,
                        new Beacon(device.getAddress(), rssi, txPower, dis), beaconMap, ScanBlueToothTimesInPeriod);

            }
        }
    };

    public InspectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InspectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InspectFragment newInstance(String param1, String param2) {
        InspectFragment fragment = new InspectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Intent intent = new Intent(getActivity(), AccelerometerService.class);
        getActivity().bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    //初始化楼层选择按钮
    protected void initFloorSelectButton() {
        //初始化楼层
        destroyFloorSelectButton();
        floor = new ArrayList<>();
        for (String s : mSails.getFloorDescList()) {
            floor.add(s);
        }
        floorbuttons = new ArrayList<SubActionButton>();
        ImageView icon = new ImageView(getContext());
        icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_floor));
        actionButton = new FloatingActionButton.Builder(getActivity()).setContentView(icon).build();
        FloatingActionMenu.Builder builder = new FloatingActionMenu.Builder(getActivity());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dp2px(50), dp2px(50));
        actionButton.setPosition(3, layoutParams);


        for (int i = 0; i < floor.size(); i++) {
            TextView textView = new TextView(getContext());
            textView.setText(floor.get(i));
            SubActionButton.Builder itemBuilder = new SubActionButton.Builder(getActivity());
            floorbuttons.add(itemBuilder.setContentView(textView).build());
            builder.addSubActionView(floorbuttons.get(i));
        }
        builder.setStartAngle(90);
        builder.attachTo(actionButton);
        floatingActionMenu = builder.build();
        Log.i(LOG_TAG, "initFloorSelectButton");

        //待添加其它floor监听器

        for (int i = 0; i < floor.size(); i++) {
            final int finalI = i;
            floorbuttons.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(LOG_TAG, mSailsMapView.getCurrentBrowseFloorName() + "::" + mSails.getFloorNameList().get(finalI));
                    if (!mSailsMapView.getCurrentBrowseFloorName().equals(mSails.getFloorNameList().get(finalI))) {
                        currentFloor = mSails.getFloorNameList().get(finalI);
                        //设置楼宇定位参数
                        if(currentBuilding.equals("郑州中原金融产业园1栋")){
                            buildingCollectNum = Buildings.buildingPara.get(currentFloor)[0];
                            buildingCollectTime = Buildings.buildingPara.get(currentFloor)[1];
                            buildingCollectSleep = Buildings.buildingPara.get(currentFloor)[2];
                            buildingDisThreshold = Buildings.buildingPara.get(currentFloor)[3];
                        }else{
                            buildingCollectNum = 5;
                            buildingCollectTime = 5;
                            buildingCollectSleep = 0;
                            buildingDisThreshold = 0;
                        }
                        if(LocalizationTimer!=null){
                        LocalizationTimer.cancel();
                        LocalizationTimer = new Timer();
                        }
                        inspectButton.setImageResource(R.drawable.ic_inspect_user);
                        isCalposition = 0;
                        if (isScanning) {
                            isScanning = false;
                            ((FragmentServiceCallback) activity).startOrStopActivityService(
                                    scanServiceintent, false);
                            Toast.makeText(mcontext, calPositionInsertTimes + "组定位数据", Toast.LENGTH_SHORT).show();
                            calPositionInsertTimes = 0;
                        }
                        Log.d("BuildingPara1",currentBuilding+ " "+ buildingCollectNum+" "+buildingCollectTime+"   "+buildingCollectSleep);
                        mSailsMapView.loadFloorMap(currentFloor);
                        Toast.makeText(getActivity(), floor.get(finalI), Toast.LENGTH_SHORT).show();
                        activity.updateBulidingAndFloor();
                    } else {
                        //    Toast.makeText(getActivity(), "已显示该楼层", Toast.LENGTH_SHORT).show();
                    }
                    floatingActionMenu.close(true);
                    setMapZoom();
                    activity.updateZoomForFragment();
                }
            });
        }
        isFABinit = true;
    }

    public void destroyFloorSelectButton() {
        if (floatingActionMenu != null)
            floatingActionMenu.close(true);
        floatingActionMenu = null;
        floorbuttons = null;
        if (actionButton != null)
            actionButton.detach();
        actionButton = null;
        floor = null;
        //    Toast.makeText(getContext(),"清除按钮",Toast.LENGTH_SHORT).show();
    }

    public void setFloorSelectButtonVisible(boolean isVisible) {
        if ((floatingActionMenu != null) && (actionButton != null)) {
            if (!isVisible) {
                floatingActionMenu.close(true);
                actionButton.setVisibility(View.GONE);
            } else {
                actionButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_inspect, container, false);
        mcontext = getContext();
        activity = (MainActivity) getActivity();
        scanServiceintent = new Intent(mcontext, ScanSignalService.class);
        scanServiceintent.setAction("com.bupt.indooranalysis.ScanSignalService");
        isCalposition = 0;
        btnUpload = (android.support.design.widget.FloatingActionButton) view.findViewById(R.id.fab_upload);
        btnClear = (android.support.design.widget.FloatingActionButton) view.findViewById(R.id.fab_clear);
        btnClear.setOnClickListener(new ClearListener());
        btnUpload.setOnClickListener(new UploadListener());

        //初始化大楼选择控件
        spinner = (Spinner) view.findViewById(R.id.spinner1);
        inspectButton = (android.support.design.widget.FloatingActionButton) view.findViewById(R.id.fab_inspect);
        inspectButton.setVisibility(Button.VISIBLE);
        floorNumTV = (TextView) view.findViewById(R.id.floorNum);
        locationList = new ArrayList<String>();
        for (String key : Buildings.BuildingsList.keySet()) {
            locationList.add(key);
        }
        arrayAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, locationList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //如果开始scan了，切换楼宇后则停止scan
                if (isCalposition == 2) {
                    stopScan();
                }
                currentBuilding = spinner.getSelectedItem().toString();
                if (bulidingSpinnerClickFirstly) {
                    // Log.d("zhouxiangLog",Buildings.currentBuilding+" "+Buildings.currentFloor);
                    activity.updateBulidingAndFloor();
                }
                bulidingSpinnerClickFirstly = true;
                if (isFABinit) actionButton.setVisibility(View.INVISIBLE);
                mSailsMapView.post(updateBuildingMaps);
                if (mSailsMapView != null)
                    Log.i(LOG_TAG, "Current building is changed to " + currentBuilding + " " + mSailsMapView.getCurrentBrowseFloorName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        LocalizationTimer = new Timer();
        initComponent();

        inspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              /* Log.d("zhouxiangLog", Buildings.buildingCity.get(Buildings.currentBuilding)+ ""+
                        LocationProvider.getCity());*/

                if (Buildings.buildingCity.get(currentBuilding).equals(LocationProvider.getCity())) {
                    switch (isCalposition) {
                        case 0:
                            if (!MessageUtil.checkLogin(mcontext)) {
                                return;
                            }
                            inspectButton.setImageResource(R.drawable.ic_inspect_grey);
                            isCalposition = 1;
                            //通知巡检详情开始更新progressBar
                            activity.updateProgressBarForHistroyFragment();
                            if (!isUpdatedOver) {
                                //更新数据
                                updateBeacon();
                            } else {
                                isCalposition = 2;
                                setTimerTasks();
                                if (isScanning == false) {
                                    isScanning = true;
                                    isInArea = false;
                                    ((FragmentServiceCallback) activity).startOrStopActivityService(
                                            scanServiceintent, true);
                                }
                            }
                            break;
                        case 1:
                            break;
                        case 2:
                            buildingNumber = 0;
                            buildingfloor = 0;
                            LocalizationTimer.cancel();
                            LocalizationTimer = new Timer();
                            inspectButton.setImageResource(R.drawable.ic_inspect_user);
                            isCalposition = 0;
                            if (isScanning) {
                                isScanning = false;
                                ((FragmentServiceCallback) activity).startOrStopActivityService(
                                        scanServiceintent, false);
                                Toast.makeText(mcontext, calPositionInsertTimes + "组定位数据", Toast.LENGTH_SHORT).show();
                                calPositionInsertTimes = 0;
                            }
                            break;
                        default:
                            break;

                    }
                } else {
                    Toast.makeText(activity, "请切换到" + LocationProvider.getProvince() + LocationProvider.getCity() + "地区的地图!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        handler = new MAHandler();
        editText1 = (EditText) view.findViewById(R.id.editText1);
        editText2 = (EditText) view.findViewById(R.id.editText2);
        editText1.setVisibility(View.GONE);
        editText2.setVisibility(View.GONE);
        locationTextView = (TextView) view.findViewById(R.id.locationText);

        // new a SAILS engine.
        mSails = new SAILS(mcontext);
        // set location mode.
        mSails.setMode(SAILS.BLE_GFP_IMU);
        // set floor number sort rule from descending to ascending.
        mSails.setReverseFloorList(true);
        // create location change call back.

        // new and insert a SAILS MapView from layout resource.
        mSailsMapView = new SAILSMapView(mcontext);
        mSailsMapView.enableRotate(false);
        ((FrameLayout) view.findViewById(R.id.SAILSMap)).addView(mSailsMapView);
//        currentBuilding = spinner.getSelectedItem().toString();
        currentBuilding = spinner.getSelectedItem().toString();
        Log.i(LOG_TAG, "Current building is " + currentBuilding);

        final ImageView fabIconNew = new ImageView(mcontext);
        fabIconNew.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_send_now_light));

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dp2px(50), dp2px(50));

        //    mSailsMapView.post(updateBuildingMaps);
        Log.i(LOG_TAG, "onCreateView");

        return view;
    }

    void mapViewInitial() {

        // establish a connection of SAILS engine into SAILS MapView.
        mSailsMapView.setSAILSEngine(mSails);

        // set location pointer icon.
        mSailsMapView.setLocationMarker(R.drawable.circle, R.drawable.arrow, null, 35);

        // set location marker visible.
        mSailsMapView.setLocatorMarkerVisible(true);

        // load first floor map in package.
        mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(0));
//        Buildings.currentBuilding = mSails.getBuildingName();
//        Buildings.currentFloor = mSails.getFloorNameList().get(0);
        //设置显示楼层
        floorNumTV.setText(mSails.getFloorDescList().get(0));

        //设置GeoPoint

        geoPointLocationLB = Buildings.getLBGeoPoint(currentBuilding, mSails.getFloorDescList().get(0));
        geoPointLocationRT = Buildings.getRTGeoPoint(currentBuilding, mSails.getFloorDescList().get(0));
        currentFloor = mSails.getFloorNameList().get(0);
        //设置楼宇定位参数
        if(currentBuilding.equals("郑州中原金融产业园1栋")){
            buildingCollectNum = Buildings.buildingPara.get(currentFloor)[0];
            buildingCollectTime = Buildings.buildingPara.get(currentFloor)[1];
            buildingCollectSleep = Buildings.buildingPara.get(currentFloor)[2];
            buildingDisThreshold = Buildings.buildingPara.get(currentFloor)[3];
        }else{
            buildingCollectNum = 5;
            buildingCollectTime = 5;
            buildingCollectSleep = 0;
            buildingDisThreshold = 0;
        }
        Log.d("BuildingPara2",buildingCollectNum+" "+buildingCollectTime+"   "+buildingCollectSleep);

        Log.i(LOG_TAG, "mapViewInitial: current floor " + currentFloor);
        // Auto Adjust suitable map zoom level and position to best view
        // position.
        // mSailsMapView.autoSetMapZoomAndView();
        setMapZoom();
        activity.updateZoomForFragment();

        // design some action in floor change call back.
        mSailsMapView.setOnFloorChangedListener(new SAILSMapView.OnFloorChangedListener() {
            @Override
            public void onFloorChangedBefore(String floorName) {
                // get current map view zoom level.
                zoomSav = mSailsMapView.getMapViewPosition().getZoomLevel();
            }

            @Override
            public void onFloorChangedAfter(final String floorName) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        // check is locating engine is start and current brows
                        // map is in the locating floor or not.
                        if (mSails.isLocationEngineStarted() && mSailsMapView.isInLocationFloor()) {
                            // change map view zoom level with animation.
                            mSailsMapView.setAnimationToZoom(zoomSav);
                        }
                    }
                };
                new Handler().postDelayed(r, 1000);

                int position = 0;
                for (String mS : mSails.getFloorNameList()) {
                    if (mS.equals(floorName))
                        break;
                    position++;
                }
                //设置GeoPoint


                geoPointLocationLB = Buildings.getLBGeoPoint(currentBuilding, mSails.getFloorDescList().get(position));
                geoPointLocationRT = Buildings.getRTGeoPoint(currentBuilding, mSails.getFloorDescList().get(position));
                //  floorList.setSelection(position);
                floorNumTV.setText(mSails.getFloorDescList().get(position));
            }
        });

        adapter = new ArrayAdapter<String>(mcontext, android.R.layout.simple_spinner_item, mSails.getFloorDescList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    private List<Map<String, Object>> getData() {
        // Log.i("FragmentInspection", "getData size " + showList.size());
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        Map<String, Object> map = new HashMap<String, Object>();
        // map.put("img", R.drawable.position);
        map.put("content", "巡检记录");
        list.add(map);

        listData = list;
        return list;
    }

    private void setData(List<InspectedBeacon> ibList) {
        listData.clear();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("content", "巡检记录");
        listData.add(map);
        for (InspectedBeacon ib : ibList) {
            Map<String, Object> _map = new HashMap<String, Object>();
            _map.put("img", R.drawable.position);
            _map.put("content", ib.getBuildingName() + " | " + ib.getFloor()
                    + " 层 | " + ib.getDescription() + " , 有" + ib.getCount()
                    + " 条巡检记录");
            listData.add(_map);
        }
    }

    @Override
    public void handleUpdateMessage(Message msg) {
        if (msg.what == Constants.MSG.UPLOAD) {
            Bundle b = msg.getData();
            boolean status = b.getBoolean("status");
            if (status) {
                Toast.makeText(activity, "上传成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "上传失败", Toast.LENGTH_SHORT).show();
            }
            btnUpload.setClickable(true);

        } else if (msg.what == Constants.MSG.SHOW_BEACON) {
            Bundle b = msg.getData();
            ArrayList<InspectedBeacon> list = (ArrayList<InspectedBeacon>) b
                    .getSerializable("showList");
            // list 不会是null
            Log.i("FragmentInspection",
                    "handleUpdateMessage size " + list.size());
            setData(list);
//            ((SimpleAdapter) listView.getAdapter()).notifyDataSetChanged();

        } else if (msg.what == Constants.MSG.UPDATE) {
            Log.i("Inspect227", "msg.what "
                    + msg.what);
            Bundle b = msg.getData();
            boolean status = b.getBoolean("status");
            //  Log.d("zhouxiangLog",b.getBoolean("statusForLoacalization")+" ");

            if (b.getBoolean("statusForLoacalization")) {
                setTimerTasks();
                isCalposition = 2;
                Toast.makeText(activity, "更新成功", Toast.LENGTH_SHORT).show();
                //设置楼宇参数初始化
                if(currentBuilding.equals("郑州中原金融产业园1栋")){
                    buildingCollectNum = Buildings.buildingPara.get(currentFloor)[0];
                    buildingCollectTime = Buildings.buildingPara.get(currentFloor)[1];
                    buildingCollectSleep = Buildings.buildingPara.get(currentFloor)[2];
                    buildingDisThreshold = Buildings.buildingPara.get(currentFloor)[3];
                }else{
                    buildingCollectNum = 5;
                    buildingCollectTime = 5;
                    buildingCollectSleep = 0;
                    buildingDisThreshold = 0;
                }
                Log.d("BuildingPara3",currentBuilding+ " "+ buildingCollectNum+" "+buildingCollectTime+"   "+buildingCollectSleep);
                isUpdatedOver = true;
                if (isScanning == false) {
                    isScanning = true;
                    isInArea = false;
                    ((FragmentServiceCallback) activity).startOrStopActivityService(
                            scanServiceintent, true);
                }
//                startActivity(new Intent(mcontext, IndoorLocationActivity.class));
            } else {
                Toast.makeText(activity, "更新失败", Toast.LENGTH_SHORT).show();
                inspectButton.setImageResource(R.drawable.ic_inspect_user);
                isCalposition = 0;

            }
            isUpdating = false;
        }

    }

    private int dp2px(int value) {
        float v = getContext().getResources().getDisplayMetrics().density;
        return (int) (v * value + 0.5f);
    }

    public void drawPosition(int x, int y) {

        //结合当前楼层更改楼层长宽

        Double buildingX = 1680.0;
        Double buildingY = 1680.0;
        if(currentBuilding.equals("郑州中原金融产业园1栋")){
            if(Integer.valueOf(currentFloor) == 6){
                buildingX = 1100.0;
                buildingY = 1500.0;
            }else{
                buildingX = 3300.0;
                buildingY = 5000.0;
            }
        }
        geoPointLocationLB = Buildings.getLBGeoPoint(currentBuilding,currentFloor+"层");
        geoPointLocationRT = Buildings.getRTGeoPoint(currentBuilding,currentFloor+"层");
        GeoPoint geoPointNow = new GeoPoint(geoPointLocationLB.latitude - (geoPointLocationLB.latitude - geoPointLocationRT.latitude) /
                buildingY * y, geoPointLocationLB.longitude - (geoPointLocationLB.longitude - geoPointLocationRT.longitude) / buildingX * x);
        Marker marker = new Marker(geoPointNow,
                Marker.boundCenter(getResources().getDrawable(R.drawable.red_cir)));
        listOverlay.getOverlayItems().clear();
        listOverlay.getOverlayItems().add(marker);
        mSailsMapView.getOverlays().clear();
        mSailsMapView.getOverlays().add(listOverlay);
        mSailsMapView.redraw();


    }

    private void updateBeacon() {

        Log.d("update", "开始更新");
        if (!MessageUtil.checkLogin(activity.getApplicationContext())) {
            return;
        }
        if (isUpdating)
            return;
        isUpdating = true;
        Toast.makeText(mcontext, "正在更新", Toast.LENGTH_SHORT);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Sim sim = ModelService.getPhoneInfo(activity.telephonyManager);
                boolean status = ModelService.updateDb(activity, sim);
                //新增定位模块更新
                boolean statusForLoacalization = ModelService.updateLocalization(activity);
                //设置楼宇定位参数
                boolean statusForBuildingPara = ModelService.updateBuilidingPara(activity);

                Message msg = new Message();
                msg.what = Constants.MSG.UPDATE;
                Bundle b = new Bundle();
                b.putBoolean("status", status && statusForLoacalization);
                b.putBoolean("statusForLoacalization", statusForLoacalization);
                msg.setData(b);
                msg.what = Constants.MSG.UPDATE;
                activity.handler.sendMessage(msg);
            }
        }).start();
    }

    // ///////////////////////////////
    // 取出状态数据
    // ///////////////////////////////
    private void restoreState() {
        if (savedState != null) {
            // 比如
            isScanning = savedState.getBoolean("startScanning");
            Log.i("Infragment295", "" + isScanning);
            //设置开始巡检状态设置
//            if (startScanning) {
//
//                btnStart.setText(R.string.btnStarting);
//                btnimage.setImageResource(images[0]);
//            } else {
//                btnStart.setText(R.string.btnStartContent);
//                btnimage.setImageResource(images[1]);
//            }
        }
    }

    // ////////////////////////////
    // 保存状态数据
    // ////////////////////////////
    private Bundle saveState() {
        Bundle state = new Bundle();
        // 比如
        state.putBoolean("startScanning", isScanning);
        return state;
    }
//    private class StartListener implements View.OnClickListener {
//        @Override
//        public void onClick(View arg0) {
//
//            final Intent intent = new Intent();
//            intent.setAction("com.bupt.indooranalysis.ScanService");
//            if (!MessageUtil.checkLogin(mcontext)) {
//                return;
//            }
//            //此处设置开始巡检的启动状况
//            if (startScanning == false) {
//                startScanning = true;
////                btnStart.setText(R.string.btnStarting);
////                btnimage.setImageResource(images[0]);
//                ((FragmentServiceCallback) activity).startOrStopActivityService(
//                        intent, true);
//            } else {
//                startScanning = false;
//                btnStart.setText(R.string.btnStartContent);
//                // bAdapter.disable();
////                btnimage.setImageResource(images[1]);
//                ((FragmentServiceCallback) activity).startOrStopActivityService(
//                        intent, false);
//            }
//        }
//    }
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//        // Restore State Here
//        if (!restoreStateFromArguments()) {
//            // First Time running, Initialize something here
//        }
//    }

//    @Override
//    public void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        // Save State Here
//        saveStateToArguments();
//    }

//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        // Save State Here
//        saveStateToArguments();
//    }

//    private void saveStateToArguments() {
//        savedState = saveState();
//        if (savedState != null) {
//            Bundle b = getArguments();
//            b.putBundle("internalSavedViewState8954201239547", savedState);
//        }
//    }
//
//    private boolean restoreStateFromArguments() {
//        Bundle b = getArguments();
    //        savedState = b.getBundle("internalSavedViewState8954201239547");
//        if (savedState != null) {
//            restoreState();
//            return true;
//        }
//        return false;
//    }

    @Override
    public void onResume() {
        super.onResume();
        mSailsMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSailsMapView.onPause();
    }

    @Override
    public void onDestroy() {

        getActivity().unbindService(conn);
        if (bAdapter != null)
            bAdapter.stopLeScan(mLeScanCallback);
        if (findLostBeaconTimer != null)
            findLostBeaconTimer.cancel();
        buildingfloor = 0;
        buildingNumber = 0;
        if (LocalizationTimer != null)
            LocalizationTimer.cancel();
        if (GetBluetoothDeviceTimer != null)
            GetBluetoothDeviceTimer.cancel();
        if (isScanning) {
            isScanning = false;
            ((FragmentServiceCallback) activity).startOrStopActivityService(
                    scanServiceintent, false);
        }
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void initComponent() {
        beaconSet = new HashSet<Beacon>();
        beaconMap = new HashSet<Beacon>();
        // 打开蓝牙
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mcontext, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            // finish();
        }
        Log.d("bluetooth", "ok");
        final BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        bAdapter = bluetoothManager.getAdapter();
        bAdapter.enable();

        timeZero = System.currentTimeMillis();
        if (bAdapter != null) {
            if (!bAdapter.isEnabled())
                bAdapter.enable();
            Log.d("bluetooth", "start scaning");
            bAdapter.startLeScan(mLeScanCallback);
            // startTime = new Timestamp(System.currentTimeMillis());
            //
            // positionTimer = new Timer();
            // positionTimer.schedule(new TimerTask() {
            //
            // @Override
            // public void run() {
            //
            // }
            // }, 3000, 1000);
            findLostBeaconTimer = new Timer();
            findLostBeaconTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    synchronized (beaconSet) {
                        int invalidBeacon = BeaconUtil.scanLostBeacon(beaconSet);
                        Beacon max = BeaconUtil.getMax(beaconSet);
                        // Log.i("traindataactivity", max == null ? "无" :
                        // max.getMac());
                        // Bundle b = new Bundle();
                        // b.putString("beacon", max == null ? "无" :
                        // max.getMac());
                        // Message msg = new Message();
                        // msg.setData(b);
                        // msg.what = 0x01;
                        // handler.sendMessage(msg);

                        // 每20* Beacon.TRANSMIT_PERIOD的时间重启一次BLE
                        int restartThreshold = 20;
                        int noReactThreashold = 1;
                        int total = beaconSet.size();
                        if (bleNoReactCount == noReactThreashold - 1) {
                            /**
                             * 蓝牙在noReactThreashold*TRANSMIT_PERIOD时间内没有触发任何回调，
                             * 针对小米等机型，可能通过重启一次蓝牙来缓解这种问题。
                             * 对于华为等机型，蓝牙回调非常频繁，在有Beacon的范围内,
                             * noReactThreashold可能永远保持为零
                             */
                            bleRestart();
                            Log.i("ScansService", "蓝牙未响应重启");
                        } else {
                            if (total == 0 || invalidBeacon == total || scanCount == restartThreshold - 1) {
                                /**
                                 * 蓝牙有响应,但可能没有潜在位置的Beacon可能没有被更新。
                                 * 没有检测到蓝牙，或者蓝牙全部失效，或者时间达到restartThreshold
                                 * *TRANSMIT_PERIOD ， 重启一次蓝牙
                                 */
                                bleRestart();
                                Log.i("ScansService", "beacon失效或周期性重启");

                            }
                        }
                        bleNoReactCount = (bleNoReactCount + 1) % noReactThreashold;
                        scanCount = (scanCount + 1) % restartThreshold;
                    }
                }
            }, 3000, Beacon.TRANSMIT_PERIOD);
        }
    }

    /**
     * 对于小米手机，每个Beacon可能只会被扫描一次，此时需要重启扫描
     */
    private void bleRestart() {
        bAdapter.stopLeScan(mLeScanCallback);
        bAdapter.startLeScan(mLeScanCallback);
    }

    private void setTimerTasks() {
        LocalizationTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                // TODO Auto-generated method stub

                Log.d("InspectCheck","Before is time.");

                Set<Beacon> newbeaconMap1 = new HashSet<Beacon>();
                Set<Beacon> newbeaconMap2 = new HashSet<Beacon>();
                Set<Beacon> newbeaconMap3 = new HashSet<Beacon>();
                synchronized (beaconMap) {
                    Iterator<Beacon> ite = beaconMap.iterator();
                    while (ite.hasNext()) {
                        Beacon b = ite.next();
                        newbeaconMap1.add(new Beacon(b.getMac(), b.getRssi(), b.getTxPower(), b.getDistance(),
                                b.getX(), b.getY(), b.getDislist()));
                        newbeaconMap2.add(new Beacon(b.getMac(), b.getRssi(), b.getTxPower(), b.getDistance(),
                                b.getX(), b.getY(), b.getDislist()));
                        newbeaconMap3.add(new Beacon(b.getMac(), b.getRssi(), b.getTxPower(), b.getDistance(),
                                b.getX(), b.getY(), b.getDislist()));
                        Log.d("InsepctInsepct", b.getMac() + " " + b.getDislist().size());
                        if (buildingNumber == 0) {
                            buildingNumber = ModelService.updateBuilding(mcontext, b.getMac(), buildingNumber);
                            Log.d("buildingNumber", buildingNumber + "");
                        }
                        if (buildingfloor == 0) {
                            buildingfloor = ModelService.updateFloor(mcontext, b.getMac(), buildingfloor);
                            Log.d("buildingfloor", buildingfloor + "");
                        }
                    }
                }
                Set<Beacon> newbeaconSet = new HashSet<Beacon>();
                synchronized (beaconSet) {
                    Iterator<Beacon> ite = beaconSet.iterator();
                    while (ite.hasNext()) {
                        Beacon b = ite.next();
                        newbeaconSet.add(new Beacon(b.getMac(), b.getRssi(), b.getTxPower(), b.getDistance(),
                                b.getX(), b.getY(), b.getDislist()));
                    }
                }
                List<Integer> list1 = ModelService.localizationFuncAA(ModelService.threeLocalizationPredealedAA(newbeaconMap1));
                List<Integer> list2 = ModelService.localizationFunc1(newbeaconSet);
                List<Integer> list3 = ModelService.threePointLocalization(ModelService.threeLocalizationPredealedAA
                        (newbeaconMap2));
                List<Integer> list4 = ModelService.sixPointMassCenter(ModelService.threeLocalizationPredealedAA(newbeaconMap3));
                //输入定位坐标
//                String X = locationEditX.getText().toString();
//                String Y = locationEditY.getText().toString();
                int dataX = 0;
                int dataY = 0;
//                if (X != null && !"".equals(X)) {
//                    dataX = Integer.parseInt(X);
//                }
//                if (Y != null && !"".equals(Y)) {
//                    dataY = Integer.parseInt(Y);
//                }
                if ((!((list4.get(0) == 0) && (list4.get(1) == 0)))) {
                    boolean isinsert = ModelService.recordCalculatePosition(mcontext, new CalculatePosition
                            (list2
                                    .get(0), list2.get(1), list1
                                    .get(0), list1.get(1), list3.get(0), list3.get(1), list4.get(0), list4.get(1), dataX, dataY));
                    if (isinsert) {
                        calPositionInsertTimes += 1;
                    }
                }

                Message msg = new Message();
                Bundle b = new Bundle();
                msg.what = 0x01;
//                b.putInt("list1x", list1.get(0));
//                b.putInt("list1y", list1.get(1));
//                b.putInt("list2x", list2.get(0));
//                b.putInt("list2y", list2.get(1));
//                b.putInt("list3x", list3.get(0));
//                b.putInt("list3y", list3.get(1));
                b.putInt("list4x", list4.get(0));
                b.putInt("list4y", list4.get(1));

                msg.setData(b);
                handler.sendMessage(msg);
                //测试使用计数器
//                Iterator<Map.Entry<String, Integer>> it = ScanBlueToothTimesInPeriod.entrySet().iterator();
//                while (it.hasNext()) {
//                    Map.Entry<String, Integer> entry = it.next();
//                    Log.d("IndoorActivity207", "" + entry.getKey() + " " + entry.getValue());
//                }


                try {
                    Thread.sleep(buildingCollectSleep*1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("InspectCheck","Now is time.");

                ScanBlueToothTimesInPeriod.clear();
                synchronized (beaconSet) {
                    beaconSet.clear();
                }
                synchronized (beaconMap) {
                    beaconMap.clear();
                }
                localizationCount += 1;
                if (localizationCount % 5 == 0) {
                    bleRestart();
                }

                //蓝牙计数
//                HistoryFragment.blueTime.add(X);
                bluecount = 0;
                isInArea = ModelService.recordIndoorSignalData(
                        mcontext, (IndoorSignalRecord) (ScanSignalService.cellState.clone()), ScanSignalService.neighbors,
                        list4, ScanSignalService.speed);
                Log.d("InsepctInsepct", isInArea + "");



            }
        }, 3000, (buildingCollectSleep+buildingCollectTime)*1000);//此处定位周期为采集周期与休眠时间之和
    }
    //为list展示提供参考
//    public class BeaconSimpleAdapter extends SimpleAdapter {
//        private LayoutInflater mInflater;
//        private final float titleFontSize;
//        private final float screenWidth; // 屏幕宽
//        private final float screenHeight; // 屏幕高
//
//        public BeaconSimpleAdapter(Context context,
//                                   List<? extends Map<String, ?>> data, int resource,
//                                   String[] from, int[] to) {
//            super(context, data, resource, from, to);
//
//            // 获取屏幕的长和宽
//            DisplayMetrics dm = new DisplayMetrics();
//            dm = context.getResources().getDisplayMetrics();
//            screenWidth = dm.widthPixels;
//            screenHeight = dm.heightPixels;
//            // 设置标题字体大小
//            titleFontSize = adjustTextSize();
//            mInflater = (LayoutInflater) context
//                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if (position == 0) {
//                // if (convertView == null) {
//                convertView = mInflater.inflate(R.layout.beacon_list_title,
//                        null);
//                // }
    //            } else {
//                // if (convertView == null) {
//                convertView = mInflater
//                        .inflate(R.layout.beacon_list_item, null);
//                // }
//                ImageView im = (ImageView) convertView
//                        .findViewById(R.id.beacon_list_view);
//                TextView tv_content = (TextView) convertView
//                        .findViewById(R.id.beacon_list_tv);
//                // if (position == 0) {
//                // // tv_content.setTextSize(20); // 设置字体大小，
//                // // convertView.setBackgroundColor(Color.WHITE);
//                // // tv_content.setTextColor(Color.BLACK);
//                // } else {
//                // // tv_content.setTextSize(10); // 设置字体大小，
//                // //
//                // convertView.setBackgroundColor(Color.parseColor("#EAEAEA"));
//                // // tv_content.setTextColor(Color.BLACK);
//                // }
//            }
//
//            return super.getView(position, convertView, parent);
//        }
//
//        float adjustTextSize() {
//            float textsize = 12;
//            // 在这实现你自己的字体大小算法，可根据之前计算的屏幕的高和宽来按比例显示
//            textsize = screenWidth / 320 * 12;
//
//            return textsize;
//        }
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    class ClearListener implements View.OnClickListener {
        @Override
        public void onClick(View arg0) {
            if (isDeleting)
                return;
            new AlertDialog.Builder(activity).setTitle("删除确认")
                    .setMessage("确定清空所有巡检记录吗？")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isDeleting = true;
                            Toast.makeText(activity, "正在清除", Toast.LENGTH_SHORT).show();
                            Log.i("fragmentInspect202", "delete");
                            DBManager dbManager = new DBManager(mcontext);
                            // dbManager.deleteAllBeaconInfo();
                            dbManager.deleteAllIndoorRecord();
                            dbManager.deleteAllIndoorSignalRecord();
                            dbManager.deleteAllNeighborList();
                            dbManager.deleteAllSpeedList();
                            dbManager.deleteInspection();
                            dbManager.deleteAllBeaconInfo();
                            dbManager.deleteAllBeaconDebug();
                            dbManager.deleteAllTrainData();
                            dbManager.deleteAllCalposition();
                            isDeleting = false;
                            Toast.makeText(activity, "数据清除完成", Toast.LENGTH_SHORT).show();
                        }
                    }).setNegativeButton("否", null).show();
            // testForMarker();
        }
    }

    class UploadListener implements View.OnClickListener {
        @Override
        public void onClick(View arg0) {
            if (isScanning == true) {
                Toast.makeText(mcontext, "请先结束巡检", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!MessageUtil.checkLogin(mcontext)) {
                return;
            }
            Toast.makeText(activity, "正在上传数据", Toast.LENGTH_SHORT).show();
            btnUpload.setClickable(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean status = ModelService.uploadSignalRecord(mcontext);
                    boolean neighbor = ModelService.uploadNeighbor(mcontext);
                    boolean inspection = ModelService.uploadInspection(mcontext);
                    Message msg = new Message();
                    msg.what = Constants.MSG.UPLOAD;
                    Bundle b = new Bundle();
                    Log.d(LOG_TAG, status + " " + neighbor + " " + inspection);
                    b.putBoolean("status", status && neighbor && inspection);
                    msg.setData(b);
                    msg.what = Constants.MSG.UPLOAD;
                    Log.d(LOG_TAG, "上传测试" + status);
                    activity.handler.sendMessage(msg);
                }
            }).start();

        }
    }

    // 显示当前定位信息
    private class MAHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x01) {
                Bundle b = msg.getData();
                locationTextView.setText(b.getInt("list4x") + " " + b.getInt("list4y"));
                if (!(b.getInt("list4x") == 0 && b.getInt("list4y") == 0)){
                    if(isFirstLocal){
                        isFirstLocal = false;
                        lastLocal[0] = b.getInt("list4x");
                        lastLocal[1] = b.getInt("list4y");
                        drawPosition(b.getInt("list4x"), b.getInt("list4y"));
                    }else{
                        double dis = Math.sqrt( Math.pow(Math.abs(lastLocal[0]-b.getInt("list4x")),2) + Math.pow(Math.abs(lastLocal[1]-b.getInt("list4y")),2) );
                        if(dis > buildingDisThreshold * 100){
                            lastLocal[0] = b.getInt("list4x");
                            lastLocal[1] = b.getInt("list4y");
                            drawPosition(b.getInt("list4x"), b.getInt("list4y"));
                        }
                    }

                }
            }
        }
    }

    public void stopScan() {
        buildingNumber = 0;
        buildingfloor = 0;
        LocalizationTimer.cancel();
        LocalizationTimer = new Timer();
        inspectButton.setImageResource(R.drawable.ic_inspect_user);
        isCalposition = 0;
        if (isScanning) {
            isScanning = false;
            ((FragmentServiceCallback) activity).startOrStopActivityService(
                    scanServiceintent, false);
            Toast.makeText(mcontext, calPositionInsertTimes + "组定位数据", Toast.LENGTH_SHORT).show();
            calPositionInsertTimes = 0;
        }
    }


    public void testForMarker() {
        GeoPoint geoPointNow1 = new GeoPoint(34.7482914, 113.7926622);
        GeoPoint geoPointNow2 = new GeoPoint(34.7500764, 113.7941206);
        Marker marker = new Marker(geoPointNow1,
                Marker.boundCenter(getResources().getDrawable(R.drawable.red_cir)));
        Marker marker2 = new Marker(geoPointNow2,
                Marker.boundCenter(getResources().getDrawable(R.drawable.red_cir)));
        listOverlay.getOverlayItems().clear();
        listOverlay.getOverlayItems().add(marker);
        listOverlay.getOverlayItems().add(marker2);
        mSailsMapView.getOverlays().clear();
        mSailsMapView.getOverlays().add(listOverlay);
        mSailsMapView.redraw();
    }

    public void setMapZoom() {
        switch (currentBuilding) {
            case "北邮科研大楼": {
                byte zoom = 21;
                mSailsMapView.setAnimationToZoom(zoom);
                break;
            }
            case "郑州中原金融产业园1栋": {
                byte zoom = 20;
                mSailsMapView.setAnimationToZoom(zoom);
                break;
            }
        }

    }
}
