package com.bupt.indooranalysis.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bupt.indoorPosition.bean.Buildings;
import com.bupt.indoorPosition.bean.IndoorSignalRecord;
import com.bupt.indoorPosition.model.ModelService;
import com.bupt.indooranalysis.MainActivity;
import com.bupt.indooranalysis.R;
import com.sails.engine.SAILS;
import com.sails.engine.SAILSMapView;
import com.sails.engine.core.model.GeoPoint;
import com.sails.engine.overlay.ListOverlay;
import com.sails.engine.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DataFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String LOG_TAG = "DataFragment";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

//    private String currentBuilding;
//    private String currentFloor;
    private String currentNetwork;
    private String currentDataType;

    private ArrayList<String> locationList;
    private ArrayList<String> floorList;

//    Spinner buildingSpinner;
//    Spinner floorSpinner;
    Spinner networkSpinner;
    Spinner dataTypeSpinner;

    ArrayAdapter<String> buildingAdapter;
    ArrayAdapter<String> floorAdapter;
    private EditText locationEditX;
    private EditText locationEditY;

    private Button heatMapButton;

    private OnFragmentInteractionListener mListener;
    private Context mcontext = null;
    private MainActivity mainActivity;

    static Map<String, SAILS> mSailsList = new HashMap();
    static SAILSMapView mSailsMapView;
    byte zoomSav = 0;

    GeoPoint geoPointLocationLB = new GeoPoint(39.96289894781549, 116.35293035811996);
    GeoPoint geoPointLocationRT = new GeoPoint(39.96304388207584, 116.35312012440777);

    Map<String,ListOverlay> listOverlayMap = new HashMap<>();
    List<IndoorSignalRecord> listForHeatMap = new ArrayList<>();
    Map<Integer, List<Integer>> mapForHeatMap = new HashMap<>();
    Map<Integer, Integer[]> mapForHeatMapLevel = new HashMap<>();
    private Datahanler datahandler;

    public DataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DataFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DataFragment newInstance(String param1, String param2) {
        DataFragment fragment = new DataFragment();
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
        View view = inflater.inflate(R.layout.fragment_data, container, false);

        mcontext = getContext();
        datahandler = new Datahanler();

//        locationEditX = (EditText) view.findViewById(R.id.locationEditX);
//        locationEditY = (EditText) view.findViewById(R.id.locationEditY);
//        locationEditX.setVisibility(View.GONE);
//        locationEditY.setVisibility(View.GONE);
        heatMapButton = (Button) view.findViewById(R.id.btn_ok);
//        heatMapButton.setText("网格热力图");
//        heatMapButtonForPoint = (Button) view.findViewById(R.id.btn_updatedata);
//        heatMapButtonForPoint.setText("点状热力图");
//
//        heatMapButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                heatMapButton.setText("正在下载热力图数据");
//                heatMapButton.setClickable(false);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String buildingName = Buildings.buildingName.get(currentBuilding) + "";
//                        String Floor = Integer.valueOf(currentFloor) + "";
//                        listForHeatMap = ModelService.uploadForSignalHeatMap(mcontext, buildingName, Floor);
//                        if (listForHeatMap.size() != 0) {
//                            Message msg = new Message();
//                            msg.what = 0x01;
//                            datahandler.sendMessage(msg);
//                        } else {
//                            Message msg = new Message();
//                            msg.what = 0x02;
//                            datahandler.sendMessage(msg);
//
//                        }
//
//                    }
//                }).start();
//                heatMap();
//            }
//        });

//        heatMapButtonForPoint.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                heatMapButtonForPoint.setText("正在下载热力图数据");
//                heatMapButtonForPoint.setClickable(false);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        String buildingName = Buildings.buildingName.get(currentBuilding) + "";
//                        String Floor = Integer.valueOf(currentFloor) + "";
//                        listForHeatMap = ModelService.uploadForSignalHeatMap(mcontext, buildingName, Floor);
//                        if (listForHeatMap.size() != 0) {
//                            Message msg = new Message();
//                            msg.what = 0x03;
//                            datahandler.sendMessage(msg);
//                        } else {
//                            Message msg = new Message();
//                            msg.what = 0x04;
//                            datahandler.sendMessage(msg);
//
//                        }
//
//                    }
//                }).start();
//                heatMap();
//            }
//        });

        initSpinners(view);

        // new and insert a SAILS MapView from layout resource.
        mSailsMapView = new SAILSMapView(mcontext);
        mSailsMapView.enableRotate(false);
        mSailsMapView.post(updateBuildingMaps);
        ((FrameLayout) view.findViewById(R.id.SAILSMap_FragmentMap)).addView(mSailsMapView);
        // configure SAILS map after map preparation finish.

        return view;
    }

    public void initSpinners(View view){
//        buildingSpinner = (Spinner) view.findViewById(R.id.spinner_buildings_data);
//        floorSpinner = (Spinner) view.findViewById(R.id.spinner_floor_data);
//        locationList = new ArrayList<String>();
//        for (String key : Buildings.BuildingsList.keySet()) {
//            locationList.add(key);
//        }
//        buildingAdapter = new ArrayAdapter<String>(mcontext, android.R.layout.simple_spinner_item, locationList);
//        buildingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        buildingSpinner.setAdapter(buildingAdapter);
//        buildingSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
////                if (!mSailsMapView.getCurrentBrowseFloorName().equals(mSails.getFloorNameList().get(position)))
////                    mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(position));
//                currentBuilding = buildingSpinner.getSelectedItem().toString();
//                if (floorSpinner.getSelectedItem() != null)
//                    currentFloor = mSailsMapView.getCurrentBrowseFloorName();
//                if (mSailsList.containsKey(Buildings.BuildingsList.get(currentBuilding).getCode())) {
//                    mapViewInitial(mSailsList.get(Buildings.BuildingsList.get(currentBuilding).getCode()));
//                } else {
//                    mSailsMapView.post(updateBuildingMaps);
//                }
//                Log.i(LOG_TAG, "Current building is changed to " + currentBuilding);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        try{
//            currentBuilding = buildingSpinner.getSelectedItem().toString();
//        }catch (NullPointerException e){
//            Log.e(LOG_TAG, "NullPointerException "+e.toString());
//        }

        networkSpinner = (Spinner) view.findViewById(R.id.spinner_network);
        ArrayList<String> network = new ArrayList<>();
        network.add("2G");
        network.add("3G");
        network.add("4G");
        network.add("WIFI");
        ArrayAdapter networkAdapter = new ArrayAdapter(mcontext,android.R.layout.simple_spinner_item,network);
        networkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        networkSpinner.setAdapter(networkAdapter);
        networkSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentNetwork = networkSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        currentNetwork = networkSpinner.getSelectedItem().toString();

        dataTypeSpinner = (Spinner) view.findViewById(R.id.spinner_data_type);
        ArrayList<String> dataType = new ArrayList<>();
        dataType.add("RSSI");
        dataType.add("Rate");
        dataType.add("Latency");
        ArrayAdapter dataTypeAdapter = new ArrayAdapter(mcontext,android.R.layout.simple_spinner_item,dataType);
        dataTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dataTypeSpinner.setAdapter(dataTypeAdapter);
        dataTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentDataType = dataTypeSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        currentDataType = dataTypeSpinner.getSelectedItem().toString();

    }


    void mapViewInitial(SAILS mSail) {
        final SAILS mSails = mSail;
        // establish a connection of SAILS engine into SAILS MapView.
        mSailsMapView.setSAILSEngine(mSails);

        // set location pointer icon.
        mSailsMapView.setLocationMarker(R.drawable.circle, R.drawable.arrow, null, 35);

        // set location marker visible.
        mSailsMapView.setLocatorMarkerVisible(true);

        // load first floor map in package.
        mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(0));

        //设置GeoPoint

        geoPointLocationLB = Buildings.getLBGeoPoint(Buildings.currentBuilding,mSails.getFloorDescList().get(0));
        geoPointLocationRT = Buildings.getRTGeoPoint(Buildings.currentBuilding,mSails.getFloorDescList().get(0));

        // Auto Adjust suitable map zoom level and position to best view
        // position.
        mSailsMapView.autoSetMapZoomAndView();

        // design some action in floor change call back.
        mSailsMapView.setOnFloorChangedListener(new SAILSMapView.OnFloorChangedListener() {
            @Override
            public void onFloorChangedBefore(String floorName) {
                // get current map view zoom level.
                zoomSav = mSailsMapView.getMapViewPosition().getZoomLevel();
                Log.d("Data", zoomSav + "");
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
//                floorSpinner.setSelection(position);
            }
        });

//        floorList = (ArrayList) mSails.getFloorDescList();
//        Log.i(LOG_TAG, "Floor list:" + floorList.toString() + '\n' + mSails.getFloorNumberList().toString());
//        floorAdapter = new ArrayAdapter<String>(mcontext, android.R.layout.simple_spinner_item, floorList);
//        floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        floorSpinner.setAdapter(floorAdapter);
//        floorSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
////                if (!mSailsMapView.getCurrentBrowseFloorName().equals(mSails.getFloorNameList().get(position)))
////                    mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(position));
//                if (!mSailsMapView.getCurrentBrowseFloorName().equals(mSails.getFloorNameList().get(position))) {
//                    mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(position));
//                    Log.i(LOG_TAG, mSails.getFloorNameList().toString());
//                    currentFloor = mSailsMapView.getCurrentBrowseFloorName();
//                    String buildingName = Buildings.buildingName.get(currentBuilding) + "";
//                    String Floor = Integer.valueOf(currentFloor) + "";
//                    if(mSailsMapView!=null)
//                        mSailsMapView.getOverlays().clear();
//                    if(mSailsMapView!=null && listOverlayMap.containsKey("Grid"+buildingName+Floor)){
//                        mSailsMapView.getOverlays().clear();
//                        mSailsMapView.getOverlays().add(listOverlayMap.get("Grid"+buildingName+Floor));
//                        mSailsMapView.redraw();
//                    }else if(mSailsMapView!=null && listOverlayMap.containsKey("Point"+buildingName+Floor)){
//                        mSailsMapView.getOverlays().clear();
//                        mSailsMapView.getOverlays().add(listOverlayMap.get("Point"+buildingName+Floor));
//                        mSailsMapView.redraw();
//                    }
//                    //
//                    Toast.makeText(getActivity(), floorList.get(position), Toast.LENGTH_SHORT).show();
//                    //设置GeoPoint
//
//                    geoPointLocationLB = Buildings.getLBGeoPoint(currentBuilding,mSails.getFloorDescList().get(position));
//                    geoPointLocationRT = Buildings.getRTGeoPoint(currentBuilding,mSails.getFloorDescList().get(position));
//                } else {
//                    Toast.makeText(getActivity(), "已显示该楼层", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//        try{
//            currentFloor = mSailsMapView.getCurrentBrowseFloorName().toString();
//        }catch (NullPointerException e){
//            Log.i(LOG_TAG,"mSailsMapView is null");
//        }

    }

    Runnable updateBuildingMaps = new Runnable() {
        @Override
        public void run() {
            // please change token and building id to your own building
            // project in cloud.
            String buidingCode = Buildings.BuildingsList.get(Buildings.currentBuilding).getCode();
            // new a SAILS engine.
            final SAILS mSails = new SAILS(mcontext);
            // set location mode.
            mSails.setMode(SAILS.BLE_GFP_IMU);
            // set floor number sort rule from descending to ascending.
            mSails.setReverseFloorList(true);
            // create location change call back.
            mSails.loadCloudBuilding("ef608be1ea294e3ebcf6583948884a2a", buidingCode, // keyanlou
                    // 57e381af08920f6b4b0004a0 meetingroom
                    //"57eb81cf08920f6b4b00053a" keyanlou
                    new SAILS.OnFinishCallback() {
                        @Override
                        public void onSuccess(String response) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mSailsList.put(Buildings.BuildingsList.get(Buildings.currentBuilding).getCode(), mSails);
                                        mapViewInitial(mSails);
                                    } catch (IndexOutOfBoundsException e) {
                                        Log.e(LOG_TAG, "mapViewInitial出错:" + e.toString());
                                    }
                                }
                            });
                        }

                        //没有网络链接时程序崩溃,待解决
                        @Override
                        public void onFailed(String response) {
//                            Toast t = Toast.makeText(mcontext,
//                                    "Load cloud project fail, please check network connection.",
//                                    Toast.LENGTH_SHORT);
//                            t.show();
                        }
                    });
        }
    };

    static Map<Integer, Bitmap[]> creatBitmapMap(int color1, int color2) {

        Map<Integer, Bitmap[]> bits = new HashMap<>();
        for (int ratio = 0; ratio < 10; ratio++) {
            Bitmap[] bitmaplist = new Bitmap[13];
            for (int i = 0; i < 12; i++) {
                Bitmap bitmap = Bitmap.createBitmap(100, 100,
                        Bitmap.Config.ARGB_8888);

                int alpha = (int) Double.valueOf(Color.alpha(color2) * ((ratio + 1) / 10.0)).intValue();

                int red = (int) ((Color.red(color2) - Color.red(color1)) * i * (1 / 11.0) + Color.red(color1));

                int green = (int) ((Color.green(color2) - Color.green(color1)) * i * (1 / 11.0) + Color.green(color1));

                int blue = (int) ((Color.blue(color2) - Color.blue(color1)) * i * (1 / 11.0) + Color.blue(color1));

                bitmap.eraseColor(Color.argb(alpha, red, green, blue));
                bitmaplist[i] = bitmap;
            }

            Bitmap bitmap = Bitmap.createBitmap(100, 100,
                    Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(Color.argb(0, 0, 0, 0));
            bitmaplist[12] = bitmap;

            bits.put(ratio, bitmaplist);
        }
        return bits;
    }

    //
    static Bitmap[] creatBitmapList(int color1, int color2) {

        Bitmap[] bitmaplist = new Bitmap[13];
        for (int i = 0; i < 12; i++) {
            Bitmap bitmap = Bitmap.createBitmap(100, 100,
                    Bitmap.Config.ARGB_8888);

            int alpha = (int) Color.alpha(color2);

            int red = (int) ((Color.red(color2) - Color.red(color1)) * i * (1 / 11.0) + Color.red(color1));

            int green = (int) ((Color.green(color2) - Color.green(color1)) * i * (1 / 11.0) + Color.green(color1));

            int blue = (int) ((Color.blue(color2) - Color.blue(color1)) * i * (1 / 11.0) + Color.blue(color1));

            bitmap.eraseColor(Color.argb(alpha, red, green, blue));
            bitmaplist[i] = bitmap;
        }

        Bitmap bitmap = Bitmap.createBitmap(100, 100,
                Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.argb(0, 0, 0, 0));
        bitmaplist[12] = bitmap;

        return bitmaplist;
    }

    public void createBitmap(int color, float ratio) {
        Bitmap bitmap = Bitmap.createBitmap(100, 100,
                Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(color);

    }


    public void heatMapForGrid(List<IndoorSignalRecord> list) {


        ListOverlay listOverlay = new ListOverlay();
        mapForHeatMap = new HashMap<>();
        mapForHeatMapLevel = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            IndoorSignalRecord signal = list.get(i);
            int tempX = signal.getPositionX() / (1680 / 64);
            int tempY = signal.getPositionY() / (1680 / 64);
            int temp = tempX + tempY * 64;
            if (!mapForHeatMap.containsKey(temp)) {
                List<Integer> signalStrength = new ArrayList<>();
                signalStrength.add(signal.getSignalStrength());
                mapForHeatMap.put(temp, signalStrength);
            } else {
                mapForHeatMap.get(temp).add(signal.getSignalStrength());
            }
        }
        int color1 = Color.argb(100, 0, 255, 0);
        int color2 = Color.argb(100, 255, 0, 0);
        Map<Integer, Bitmap[]> bitmaps = creatBitmapMap(color1, color2);
//        Bitmap[] bitmapslist = creatBitmapList(color1, color2);
        int sum = 64;
        GeoPoint[][] geoPoint = new GeoPoint[sum][sum];
        Marker[][] marker = new Marker[sum][sum];
        for (int i = 0; i < sum; i++) {
            for (int j = 0; j < sum; j++) {
//                geoPoint[i][j] = new GeoPoint(
//                        geoPointLocationLB.latitude
//                                - (geoPointLocationLB.latitude - geoPointLocationRT.latitude) * i / 64.0,
//                        geoPointLocationLB.longitude
//                                - (geoPointLocationLB.longitude - geoPointLocationRT.longitude) * j / 64.0);
                int tempkey = i * 64 + j;
                int tempLevel = 0;
                if (mapForHeatMap.containsKey(tempkey)) {
                    List<Integer> listInMap = mapForHeatMap.get(tempkey);
                    for (int k = 0; k < listInMap.size(); k++) {
                        tempLevel += listInMap.get(k);
                    }
                    tempLevel = tempLevel / listInMap.size();
                    Integer[] levelAndAlpha = new Integer[3];
                    levelAndAlpha[0] = tempLevel;
                    levelAndAlpha[1] = 1;//含有的level个数
                    levelAndAlpha[2] = 5;//透明度
                    mapForHeatMapLevel.put(tempkey, levelAndAlpha);
                }
            }
        }
        //注意防止自增，计算中心点处跳过
        int change = 1;
        while (change != 0) {
            change = 0;
            for (int i = 0; i < sum; i++) {
                for (int j = 0; j < sum; j++) {
                    int tempkey = i * 64 + j;
                    if (!mapForHeatMapLevel.containsKey(tempkey)) {
                        for (int a = i - 1; a < i + 2; a++) {
                            for (int b = j - 1; b < j + 2; b++) {
                                if (a >= 0 && b >= 0 && a < 64 && b < 64) {
                                    if (mapForHeatMapLevel.containsKey(a * 64 + b) && (!((a * 64 + b) == tempkey))) {
                                        if (!mapForHeatMapLevel.containsKey(tempkey)) {
                                            Integer[] levelAndAlpha = new Integer[3];
                                            levelAndAlpha[0] = mapForHeatMapLevel.get(a * 64 + b)[0];
                                            levelAndAlpha[1] = 1;
                                            levelAndAlpha[2] = mapForHeatMapLevel.get(a * 64 + b)[2];
                                            mapForHeatMapLevel.put(tempkey, levelAndAlpha);
                                        } else {
                                            mapForHeatMapLevel.get(tempkey)[0] += mapForHeatMapLevel.get(a * 64 + b)[0];
                                            mapForHeatMapLevel.get(tempkey)[1] += 1;
                                            if (mapForHeatMapLevel.get(tempkey)[2] < mapForHeatMapLevel.get(a * 64 + b)[2])
                                                mapForHeatMapLevel.get(tempkey)[2] = mapForHeatMapLevel.get(a * 64 + b)[2];
                                        }
                                    }
                                }
                            }
                        }
                        if (mapForHeatMapLevel.containsKey(tempkey)) {
                            int level = mapForHeatMapLevel.get(tempkey)[0];
                            int count = mapForHeatMapLevel.get(tempkey)[1];

                            level = level / count;
                            count = 1;
                            int ratio = mapForHeatMapLevel.get(tempkey)[2];
                            if (ratio != 0) {
                                ratio -= 1;
                            }
                            if (ratio != 0) {
                                mapForHeatMapLevel.get(tempkey)[0] = level;
                                mapForHeatMapLevel.get(tempkey)[1] = count;
                                mapForHeatMapLevel.get(tempkey)[2] = ratio;

                                change += 1;
                            } else {
                                mapForHeatMapLevel.remove(tempkey);
                            }
                        }
                    }
                }
            }
        }

        listOverlay.getOverlayItems().clear();

        for (int i = 0; i < sum; i++) {
            for (int j = 0; j < sum; j++) {
                geoPoint[i][j] = new GeoPoint(
                        geoPointLocationLB.latitude
                                - (geoPointLocationLB.latitude - geoPointLocationRT.latitude) * i / 64.0,
                        geoPointLocationLB.longitude
                                - (geoPointLocationLB.longitude - geoPointLocationRT.longitude) * j / 64.0);
                int tempkey = i * 64 + j;
                int tempLevel = 0;
                int count = 0;
                int ratio = 0;
                if (mapForHeatMapLevel.containsKey(tempkey)) {
                    tempLevel = mapForHeatMapLevel.get(tempkey)[0];
                    count = mapForHeatMapLevel.get(tempkey)[1];
                    ratio = mapForHeatMapLevel.get(tempkey)[2];
                }

                if (ratio != 0) {
                    if (tempLevel == 0) {
                        Drawable drawable = new BitmapDrawable(bitmaps.get(ratio - 1)[12]);
                        marker[i][j] = new Marker(geoPoint[i][j],
                                Marker.boundCenterBottom(drawable));
                        listOverlay.getOverlayItems().add(marker[i][j]);
                    } else if (tempLevel < -100) {
                        Drawable drawable = new BitmapDrawable(bitmaps.get(ratio - 1)[11]);
                        marker[i][j] = new Marker(geoPoint[i][j],
                                Marker.boundCenterBottom(drawable));
                        listOverlay.getOverlayItems().add(marker[i][j]);
                    } else if (tempLevel >= -90) {
                        Drawable drawable = new BitmapDrawable(bitmaps.get(ratio - 1)[0]);
                        marker[i][j] = new Marker(geoPoint[i][j],
                                Marker.boundCenterBottom(drawable));
                        listOverlay.getOverlayItems().add(marker[i][j]);
                    } else if (tempLevel < -90 && tempLevel >= -100) {
                        Drawable drawable = new BitmapDrawable(bitmaps.get(ratio - 1)[-tempLevel - 90]);
                        marker[i][j] = new Marker(geoPoint[i][j],
                                Marker.boundCenterBottom(drawable));
                        listOverlay.getOverlayItems().add(marker[i][j]);

                    }
                }
//                int ratio = (int) (((i + j) / 128.0) / (0.02));
//                    Drawable drawable = new BitmapDrawable(bitmaps[ratio]);
//                marker[i][j] = new Marker(geoPoint[i][j],
//                        Marker.boundCenterBottom(drawable));
            }
        }

        mSailsMapView.getOverlays().clear();
        mSailsMapView.getOverlays().add(listOverlay);

        listOverlayMap.put("Grid"+Buildings.currentBuilding+Buildings.currentFloor,listOverlay);
        mSailsMapView.redraw();
    }

    //
    public void heatMap(List<IndoorSignalRecord> list) {

        ListOverlay listOverlay = new ListOverlay();
        int sum = list.size();
        GeoPoint geoPoint[] = new GeoPoint[sum];
        Marker marker[] = new Marker[sum];
        for (int i = 0; i < sum; i++) {
            geoPoint[i] = new GeoPoint(
                    geoPointLocationLB.latitude
                            - (geoPointLocationLB.latitude - geoPointLocationRT.latitude) * (list.get(i).getPositionY() / 1680.0),
                    geoPointLocationLB.longitude
                            - (geoPointLocationLB.longitude - geoPointLocationRT.longitude) * (list.get(i).getPositionX() / 1680.0));
            if (list.get(i).getSignalStrength() < -100) {
                marker[i] = new Marker(geoPoint[i],
                        Marker.boundCenterBottom(getResources().getDrawable(R.drawable.red_c)));
            } else if (list.get(i).getSignalStrength() < -90 && list.get(i).getSignalStrength() >= -100) {
                marker[i] = new Marker(geoPoint[i],
                        Marker.boundCenterBottom(getResources().getDrawable(R.drawable.yellow_c)));
            } else {
                marker[i] = new Marker(geoPoint[i],
                        Marker.boundCenterBottom(getResources().getDrawable(R.drawable.green_c)));
            }

        }

        listOverlay.getOverlayItems().clear();
        for (int i = 0; i < sum; i++) {
            listOverlay.getOverlayItems().add(marker[i]);
        }
        mSailsMapView.getOverlays().clear();
        mSailsMapView.getOverlays().add(listOverlay);
        //
        String buildingName = Buildings.currentBuilding;
        String Floor = Buildings.currentFloor;
        listOverlayMap.put("Point"+buildingName+Floor,listOverlay);
        mSailsMapView.redraw();
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    class Datahanler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x01) {
                heatMapForGrid(listForHeatMap);
                heatMapButton.setText("网格热力图");
                heatMapButton.setClickable(true);
            } else if (msg.what == 0x02) {
                heatMapButton.setText("未成功，请再试");
                heatMapButton.setClickable(true);
            } else if (msg.what == 0x03) {
                heatMap(listForHeatMap);

            } else if (msg.what == 0x04) {

            }
            super.handleMessage(msg);
        }
    }

}
