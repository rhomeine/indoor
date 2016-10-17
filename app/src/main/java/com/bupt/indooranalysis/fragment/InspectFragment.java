package com.bupt.indooranalysis.fragment;


import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bupt.indoorPosition.bean.Beacon;
import com.bupt.indoorPosition.bean.InspectedBeacon;
import com.bupt.indoorPosition.bean.Sim;
import com.bupt.indoorPosition.callback.InspectUpdateCallback;
import com.bupt.indoorPosition.dao.DBManager;
import com.bupt.indoorPosition.model.ModelService;
import com.bupt.indoorPosition.uti.Constants;
import com.bupt.indoorPosition.uti.MessageUtil;
import com.bupt.indooranalysis.IndoorLocationActivity;
import com.bupt.indooranalysis.MainActivity;
import com.bupt.indooranalysis.R;
import com.bupt.indooranalysis.ScanService;
import com.sails.engine.MapViewPosition;
import com.sails.engine.SAILS;
import com.sails.engine.SAILSMapView;
import com.sails.engine.core.model.BoundingBox;
import com.sails.engine.core.model.GeoPoint;
import com.sails.engine.overlay.ListOverlay;
import com.sails.engine.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Spinner spinner;
    private ArrayList<String> locationList;
    private ArrayAdapter<String> arrayAdapter;
    private String location;

    private Button btnClear;
    private Button btnUpload;
    private boolean startScanning = false;
    private MainActivity activity = null;
    private Context mcontext = null;
    private List<Map<String, Object>> listData = null;
    private Bundle savedState;
    private boolean isDeleting = false;
    private boolean isUpdating = false;

    private Button button;
    private TextView textView;

    private OnFragmentInteractionListener mListener;


    static SAILS mSails;
    static SAILSMapView mSailsMapView;
    ImageView zoomin;
    ImageView zoomout;
    ImageView lockcenter;
    EditText editText1;
    EditText editText2;
    Button locationButton;
    Button clearButton;
    Button saveButton;
    TextView locationTextView;
    Vibrator mVibrator;
    Spinner floorList;
    ArrayAdapter<String> adapter;
    byte zoomSav = 0;

    GeoPoint geoPointCenter;
    // picture
    // GeoPoint geoPointLocationLB = new GeoPoint(39.96289053181642,
    // 116.35293102867222);
    // GeoPoint geoPointLocationRT = new GeoPoint(39.963035980045404,
    // 116.35312079496002);
    // drawable
    GeoPoint geoPointLocationLB = new GeoPoint(39.96289894781549, 116.35293035811996);
    GeoPoint geoPointLocationRT = new GeoPoint(39.96304388207584, 116.35312012440777);
    BoundingBox boundingBox;
    MapViewPosition mapViewPositionBase;
    int tempX = 0;
    int tempY = 0;
    ListOverlay listOverlay = new ListOverlay();

    int flag = 0;

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_inspect, container, false);
        btnUpload = (Button) view.findViewById(R.id.btn_updatedata);
        btnClear = (Button) view.findViewById(R.id.btn_cleardata);
        btnClear.setOnClickListener(new ClearListener());
        btnUpload.setOnClickListener(new UploadListener());

        spinner = (Spinner) view.findViewById(R.id.spinner);
        button = (Button) view.findViewById(R.id.buttonRound);
        textView = (TextView) view.findViewById(R.id.inspectTextView1);
        locationList = new ArrayList<String>();
        locationList.add("北邮科技大厦");
        locationList.add("郑州大厦");
        locationList.add("北航大楼");

        arrayAdapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, locationList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        mcontext = getContext();
        activity = (MainActivity )getActivity();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("click!");
                final Intent intent = new Intent(mcontext, ScanService.class);
                intent.setAction("com.bupt.indooranalysis.ScanService");
                if (!MessageUtil.checkLogin(mcontext)) {
                    return;
                }
                //更新数据
                updateBeacon();

                //此处设置开始巡检的启动状况
//                if (startScanning == false) {
//                    startScanning = true;
////                btnStart.setText(R.string.btnStarting);
////                btnimage.setImageResource(images[0]);
//                    ((FragmentServiceCallback) activity).startOrStopActivityService(
//                            intent, true);
//                } else {
//                    startScanning = false;
////                    btnStart.setText(R.string.btnStartContent);
//                    // bAdapter.disable();
////                btnimage.setImageResource(images[1]);
//                    ((FragmentServiceCallback) activity).startOrStopActivityService(
//                            intent, false);
//                }
            }
        });

        handler = new MAHandler();

        zoomin = (ImageView) view.findViewById(R.id.zoomin);
        zoomout = (ImageView)  view.findViewById(R.id.zoomout);
        lockcenter = (ImageView) view. findViewById(R.id.lockcenter);
        locationButton = (Button)  view.findViewById(R.id.locationButton);
        clearButton = (Button)  view.findViewById(R.id.clearButton);
        saveButton = (Button)  view.findViewById(R.id.saveButton);
        editText1 = (EditText)  view.findViewById(R.id.editText1);
        editText2 = (EditText)  view.findViewById(R.id.editText2);
        editText1.setVisibility(view.GONE);
        editText2.setVisibility(view.GONE);
        locationTextView = (TextView) view. findViewById(R.id.locationText);
        floorList = (Spinner) view. findViewById(R.id.spinner);


        zoomin.setOnClickListener(controlListener);
        zoomout.setOnClickListener(controlListener);
        lockcenter.setOnClickListener(controlListener);
        locationButton.setOnClickListener(controlListener);
        clearButton.setOnClickListener(controlListener);

        // new a SAILS engine.
        mSails = new SAILS(mcontext);
        // set location mode.
        mSails.setMode(SAILS.BLE_GFP_IMU);
        // set floor number sort rule from descending to ascending.
        mSails.setReverseFloorList(true);
        // create location change call back.

        // new and insert a SAILS MapView from layout resource.
        mSailsMapView = new SAILSMapView(mcontext);
        ((FrameLayout) view.findViewById(R.id.SAILSMap)).addView(mSailsMapView);
        // configure SAILS map after map preparation finish.
        mSailsMapView.post(new Runnable() {
            @Override
            public void run() {
                // please change token and building id to your own building
                // project in cloud.
                mSails.loadCloudBuilding("ef608be1ea294e3ebcf6583948884a2a", "57eb81cf08920f6b4b00053a", // keyanlou
                        // 57e381af08920f6b4b0004a0 meetingroom
                        new SAILS.OnFinishCallback() {
                            @Override
                            public void onSuccess(String response) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mapViewInitial();
                                    }
                                });

                            }

                            @Override
                            public void onFailed(String response) {
                                Toast t = Toast.makeText(mcontext,
                                        "Load cloud project fail, please check network connection.",
                                        Toast.LENGTH_SHORT);
                                t.show();
                            }
                        });
            }
        });


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

        // Auto Adjust suitable map zoom level and position to best view
        // position.
        mSailsMapView.autoSetMapZoomAndView();

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
                floorList.setSelection(position);
            }
        });

        adapter = new ArrayAdapter<String>(mcontext, android.R.layout.simple_spinner_item, mSails.getFloorDescList());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        floorList.setAdapter(adapter);
        floorList.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!mSailsMapView.getCurrentBrowseFloorName().equals(mSails.getFloorNameList().get(position)))
                    mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    View.OnClickListener controlListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == zoomin) {
                // set map zoomin function.
                mSailsMapView.zoomIn();

            } else if (v == zoomout) {
                // set map zoomout function.
                mSailsMapView.zoomOut();
            } else if (v == lockcenter) {
                //tempX = Integer.valueOf(editText1.getText().toString());
                //tempY = Integer.valueOf(editText2.getText().toString());
                geoPointLocationRT = mSailsMapView.getProjection().fromPixels(tempX, tempY);
                Marker marker = new Marker(geoPointLocationRT,
                        Marker.boundCenterBottom(getResources().getDrawable(R.drawable.red_cir)));
                listOverlay.getOverlayItems().clear();
                listOverlay.getOverlayItems().add(marker);
                mSailsMapView.getOverlays().clear();
                mSailsMapView.getOverlays().add(listOverlay);
                mSailsMapView.redraw();
                // locationTextView.setText(geoPointLocationRT.latitude + " " + geoPointLocationRT.longitude);
            } else if (v == locationButton) {
                // drawPosition(flag * 50, flag * 50);
                heatMap();
                flag++;
            } else if (v == clearButton) {
                mSailsMapView.autoSetMapZoomAndView();
                mSailsMapView.getOverlays().clear();
                mSailsMapView.redraw();
                flag = 0;
            } else if (v == saveButton) {

            }
        }
    };


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
            btnUpload.setText(R.string.btnStartUpload);
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

        }else if (msg.what == Constants.MSG.UPDATE) {
            Log.i("Inspect227", "msg.what "
                    + msg.what);
            textView.setText("更新完毕");
            Bundle b = msg.getData();
            boolean status = b.getBoolean("status");
            if (status) {
                Toast.makeText(activity, "更新成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "更新失败", Toast.LENGTH_SHORT).show();
            }
            if(b.getBoolean("statusForLoacalization")){
                startActivity(new Intent(mcontext, IndoorLocationActivity.class));
            }
            isUpdating = false;
        }

    }

    class ClearListener implements  View.OnClickListener{
        @Override
        public void onClick(View arg0){
            if (isDeleting)
                return;
            new AlertDialog.Builder(activity).setTitle("删除确认")
                    .setMessage("确定清空所有巡检记录吗？")
                    .setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isDeleting = true;
                            btnClear.setText("正在清除...");
                            Log.i("fragmentInspect202", "delete");
                            DBManager dbManager = new DBManager(mcontext);
                            // dbManager.deleteAllBeaconInfo();
                            dbManager.deleteAllIndoorRecord();
                            dbManager.deleteAllNeighborList();
                            dbManager.deleteAllSpeedList();
                            dbManager.deleteInspection();
                            dbManager.deleteAllBeaconInfo();
                            dbManager.deleteAllBeaconDebug();
                            dbManager.deleteAllTrainData();
                            btnClear.setText("清除数据");
                            isDeleting = false;
                            Toast.makeText(activity, "数据清除完成", Toast.LENGTH_SHORT).show();

                        }
                    }).setNegativeButton("否", null).show();

        }
    }
    class UploadListener implements View.OnClickListener {
        @Override
        public void onClick(View arg0) {
            if (startScanning == true) {
                Toast.makeText(mcontext, "请先结束巡检", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!MessageUtil.checkLogin(mcontext)) {
                return;
            }
            btnUpload.setText(R.string.btnUploadding);
            btnUpload.setClickable(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("upload", "开始上传报告");
                    boolean status = ModelService.uploadRecord(mcontext);
                    boolean neighbor = ModelService.uploadNeighbor(mcontext);
                    boolean inspection = ModelService.uploadInspection(mcontext);
                    Message msg = new Message();
                    msg.what = Constants.MSG.UPLOAD;
                    Bundle b = new Bundle();
                    b.putBoolean("status", status && neighbor && inspection);
                    msg.setData(b);
                    msg.what = Constants.MSG.UPLOAD;
                    Log.d("上传测试", "" + status);
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
                // locationTextView.setText(b.getInt("list3x") + " " + b.getInt("list3y"));
                //drawPosition(b.getInt("list3x"), b.getInt("list3y"));
            }
        }
    }

    public void drawPosition(int x, int y) {

        GeoPoint geoPointNow = new GeoPoint(
                geoPointLocationLB.latitude - (geoPointLocationLB.latitude - geoPointLocationRT.latitude) / 1680 * y,
                geoPointLocationLB.longitude
                        - (geoPointLocationLB.longitude - geoPointLocationRT.longitude) / 1680 * x);
        Marker marker = new Marker(geoPointNow,
                Marker.boundCenterBottom(getResources().getDrawable(R.drawable.red_cir)));
        listOverlay.getOverlayItems().clear();
        listOverlay.getOverlayItems().add(marker);
        mSailsMapView.getOverlays().clear();
        mSailsMapView.getOverlays().add(listOverlay);
        mSailsMapView.redraw();

    }

    public void heatMap() {
        int sum = 1000;
        GeoPoint geoPoint[] = new GeoPoint[sum];
        Marker marker[] = new Marker[sum];
        for (int i = 0; i < sum; i++) {
            geoPoint[i] = new GeoPoint(
                    geoPointLocationLB.latitude
                            - (geoPointLocationLB.latitude - geoPointLocationRT.latitude) * Math.random(),
                    geoPointLocationLB.longitude
                            - (geoPointLocationLB.longitude - geoPointLocationRT.longitude) * Math.random());
            if (i < 800) {
                marker[i] = new Marker(geoPoint[i],
                        Marker.boundCenterBottom(getResources().getDrawable(R.drawable.green_c)));
            } else if (i < 950) {
                marker[i] = new Marker(geoPoint[i],
                        Marker.boundCenterBottom(getResources().getDrawable(R.drawable.yellow_c)));
            } else {
                marker[i] = new Marker(geoPoint[i],
                        Marker.boundCenterBottom(getResources().getDrawable(R.drawable.red_c)));
            }

        }

        listOverlay.getOverlayItems().clear();
        for (int i = 0; i < sum; i++) {
            listOverlay.getOverlayItems().add(marker[i]);
        }
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
        Toast.makeText(mcontext,"正在更新",Toast.LENGTH_SHORT);
//        changeListItemName(1, "正在更新...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                Sim sim = ModelService.getPhoneInfo(activity.telephonyManager);
                boolean status = ModelService.updateDb(activity, sim);
                //新增定位模块更新
                boolean statusForLoacalization = ModelService.updateLocalization(activity);
                //

                Message msg = new Message();
                msg.what = Constants.MSG.UPDATE;
                Bundle b = new Bundle();
                b.putBoolean("status", status&&statusForLoacalization);
                b.putBoolean("statusForLoacalization",statusForLoacalization);
                msg.setData(b);
                msg.what = Constants.MSG.UPDATE;
                activity.handler.sendMessage(msg);
            }
        }).start();
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

    // ///////////////////////////////
    // 取出状态数据
    // ///////////////////////////////
    private void restoreState() {
        if (savedState != null) {
            // 比如
            startScanning = savedState.getBoolean("startScanning");
            Log.i("Infragment295", "" + startScanning);
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
        state.putBoolean("startScanning", startScanning);
        return state;
    }


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
}
