package com.bupt.indooranalysis.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bupt.indoorPosition.adapter.CardViewAdapter;
import com.bupt.indoorPosition.bean.Buildings;
import com.bupt.indoorPosition.bean.InspectedBeacon;
import com.bupt.indoorPosition.bean.LocalizationBeacon;
import com.bupt.indooranalysis.MainActivity;
import com.bupt.indooranalysis.R;
import com.bupt.indooranalysis.Util.ArcProgress;
import com.sails.engine.SAILS;
import com.sails.engine.SAILSMapView;
import com.sails.engine.core.model.GeoPoint;
import com.sails.engine.overlay.ListOverlay;
import com.sails.engine.overlay.Marker;
import com.yalantis.phoenix.PullToRefreshView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HistoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String LOG_TAG = "HistoryFragment";
    private HisHandler hisHandler;
    private ProgressBar progressBar;
    private TextView text;
    private Spinner buildingSpinner;
    private Spinner floorSpinner;
    private String currentBuilding;
    private String currentFloor;
    private ArrayList<String> locationList;
    private ArrayList<String> floorList;
    private ArrayList<Integer> floorNumberList;
    ArrayAdapter<String> buildingAdapter;
    ArrayAdapter<String> floorAdapter;

    private Context mcontext = null;

    static Map<String, SAILS> mSailsList = new HashMap();
    static SAILSMapView mSailsMapView;
    GeoPoint geoPointLocationLB = new GeoPoint(39.96289894781549, 116.35293035811996);
    GeoPoint geoPointLocationRT = new GeoPoint(39.96304388207584, 116.35312012440777);
    byte zoomSav = 0;
    ListOverlay listOverlay = new ListOverlay();

    private List<InspectedBeacon> inspectedBeacons = new ArrayList<InspectedBeacon>();
    private OnFragmentInteractionListener mListener;
    //加速度计计步测试用数据
    public static List<Float> blueTime = new ArrayList<Float>();
    public static List<Float> blueTime1 = new ArrayList<Float>();

    public HistoryFragment() {
        // Required empty public constructor
    }

    //更新UI的Timer
    private Timer updateProgressBar;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
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
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        mcontext = getContext();
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        updateProgressBar=new Timer();


        text = (TextView) view.findViewById(R.id.inspect_progress_Text);
        hisHandler = new HisHandler();
        //设置进度控件方法，默认max为100
//        arcProgress.setMax(100);
//        arcProgress.setProgress(39);
        progressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = 0x01;
                hisHandler.sendMessage(msg);
            }
        });

        buildingSpinner = (Spinner) view.findViewById(R.id.spinner_buildings_history);
        buildingSpinner.setClickable(false);
        floorSpinner = (Spinner) view.findViewById(R.id.spinner_floor_history);
        floorSpinner.setClickable(false);
        locationList = new ArrayList<String>();
        for (String key : Buildings.BuildingsList.keySet()) {
            locationList.add(key);
        }
        buildingAdapter = new ArrayAdapter<String>(mcontext, android.R.layout.simple_spinner_item, locationList);
        buildingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        buildingSpinner.setAdapter(buildingAdapter);
        buildingSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (!mSailsMapView.getCurrentBrowseFloorName().equals(mSails.getFloorNameList().get(position)))
//                    mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(position));
                currentBuilding = buildingSpinner.getSelectedItem().toString();
                if (floorSpinner.getSelectedItem() != null) {
                    currentFloor = mSailsMapView.getCurrentBrowseFloorName();
                    //使得切换楼层不显示非本楼beacon
                    mSailsMapView.getOverlays().clear();
                    mSailsMapView.redraw();
                    Iterator<Map.Entry<String, LocalizationBeacon>> it = Buildings.InspectHistory.entrySet().iterator();
                    List<LocalizationBeacon> listForDraw = new ArrayList<>();
                    while (it.hasNext()) {
                        Map.Entry<String, LocalizationBeacon> entry = it.next();
                        if ((entry.getValue().getBuildingNumber() == Buildings.buildingName.get(currentBuilding)) && (entry.getValue().getFloor() == Integer.valueOf(currentFloor))) {
                            listForDraw.add(entry.getValue());
                        }
                    }
                    drawBeaconIsInspect(listForDraw);
                }
                if (mSailsList.containsKey(Buildings.BuildingsList.get(currentBuilding).getCode())) {
                    mapViewInitial(mSailsList.get(Buildings.BuildingsList.get(currentBuilding).getCode()));
                } else {
                    mSailsMapView.post(updateBuildingMaps);
                }
                Log.i(LOG_TAG, "Current building is changed to " + currentBuilding);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        currentBuilding = buildingSpinner.getSelectedItem().toString();


        // new and insert a SAILS MapView from layout resource.
        mSailsMapView = new SAILSMapView(mcontext);
        mSailsMapView.enableRotate(false);
        mSailsMapView.post(updateBuildingMaps);
        ((FrameLayout) view.findViewById(R.id.SAILSMap_FragmentMap)).addView(mSailsMapView);
        // configure SAILS map after map preparation finish.

        return view;
    }

    public void updateMap(String building, String floor) {
        currentBuilding = building;
        currentFloor = floor;

        Log.i(LOG_TAG, currentBuilding + " " + currentFloor);

        int indexOfBuilding = locationList.indexOf(currentBuilding);
        buildingSpinner.setSelection(indexOfBuilding);
        int indexOfFloor = floorNumberList.indexOf(Integer.valueOf(currentFloor));
        floorSpinner.setSelection(indexOfFloor);
    }

    public void mapViewInitial(SAILS mSail) {
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

        geoPointLocationLB = Buildings.getLBGeoPoint(currentBuilding, mSails.getFloorDescList().get(0));
        geoPointLocationRT = Buildings.getRTGeoPoint(currentBuilding, mSails.getFloorDescList().get(0));

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

        floorList = (ArrayList) mSails.getFloorDescList();
        floorNumberList = (ArrayList) mSails.getFloorNumberList();
        Log.i(LOG_TAG, "Floor list:" + floorList.toString() + '\n' + mSails.getFloorNumberList().toString());
        floorAdapter = new ArrayAdapter<String>(mcontext, android.R.layout.simple_spinner_item, floorList);
        floorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        floorSpinner.setAdapter(floorAdapter);
        floorSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if (!mSailsMapView.getCurrentBrowseFloorName().equals(mSails.getFloorNameList().get(position)))
//                    mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(position));
                if (!mSailsMapView.getCurrentBrowseFloorName().equals(mSails.getFloorNameList().get(position))) {
                    mSailsMapView.loadFloorMap(mSails.getFloorNameList().get(position));
                    Log.i(LOG_TAG, mSails.getFloorNameList().toString());
                    currentFloor = mSailsMapView.getCurrentBrowseFloorName();
                    //

                    //使得切换楼层不显示非本楼beacon
                    mSailsMapView.getOverlays().clear();
                    mSailsMapView.redraw();
                    Iterator<Map.Entry<String, LocalizationBeacon>> it = Buildings.InspectHistory.entrySet().iterator();
                    List<LocalizationBeacon> listForDraw = new ArrayList<>();
                    while (it.hasNext()) {
                        Map.Entry<String, LocalizationBeacon> entry = it.next();
                        if ((entry.getValue().getBuildingNumber() == Buildings.buildingName.get(currentBuilding)) && (entry.getValue().getFloor() == Integer.valueOf(currentFloor))) {
                            listForDraw.add(entry.getValue());
                        }
                    }
                    drawBeaconIsInspect(listForDraw);
                    String buildingName = Buildings.buildingName.get(currentBuilding) + "";
                    String Floor = Integer.valueOf(currentFloor) + "";
                    //
                    Toast.makeText(getActivity(), floorList.get(position), Toast.LENGTH_SHORT).show();
                    //设置GeoPoint

                    geoPointLocationLB = Buildings.getLBGeoPoint(currentBuilding, mSails.getFloorDescList().get(position));
                    geoPointLocationRT = Buildings.getRTGeoPoint(currentBuilding, mSails.getFloorDescList().get(position));
                } else {
                    Toast.makeText(getActivity(), "已显示该楼层", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        currentFloor = mSailsMapView.getCurrentBrowseFloorName().toString();
    }

    Runnable updateBuildingMaps = new Runnable() {
        @Override
        public void run() {
            // please change token and building id to your own building
            // project in cloud.
            String buidingCode = Buildings.BuildingsList.get(currentBuilding).getCode();
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
                                        mSailsList.put(Buildings.BuildingsList.get(currentBuilding).getCode(), mSails);
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    class HisHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0x01) {
                if (Buildings.InspectHistory.size() != 0) {
                    int count = 0;
                    Iterator<Map.Entry<String, LocalizationBeacon>> it = Buildings.InspectHistory.entrySet().iterator();
                    List<LocalizationBeacon> listForDraw = new ArrayList<>();
                    while (it.hasNext()) {
                        Map.Entry<String, LocalizationBeacon> entry = it.next();
                        if (entry.getValue().getIsInspect() == 1)
                            count += 1;
                        if ((entry.getValue().getBuildingNumber() == Buildings.buildingName.get(currentBuilding)) && (entry.getValue().getFloor() == Integer.valueOf(currentFloor))) {
                            listForDraw.add(entry.getValue());
                        }
                    }
                    drawBeaconIsInspect(listForDraw);
                    count = (100 * count) / Buildings.InspectHistory.size();
                    if(count==100){
                        updateProgressBar.cancel();
                        Toast.makeText(getActivity(),"已经巡检完所有Beacon",Toast.LENGTH_SHORT).show();
                    }
                    progressBar.setProgress(count);
                    text.setText("巡检进度：" + count + "%");
                    progressBar.invalidate();
                }
            }
            super.handleMessage(msg);
        }
    }

    public void drawBeaconIsInspect(List<LocalizationBeacon> list) {
        if (list.size() == 0)
            return;

        //
        int sum = list.size();
        GeoPoint geoPoint[] = new GeoPoint[sum];
        Marker marker[] = new Marker[sum];
        for (int i = 0; i < sum; i++) {
            Log.d("wodeY", list.get(i).getY() + "Y");
            Log.d("wodeY", list.get(i).getX() + "X");
            int tmpY = list.get(i).getY();
            int tmpX = list.get(i).getX();
            int cha = 50;
            if (list.get(i).getY() == 0) {
                tmpY = list.get(i).getY() + cha;
            }
            if (list.get(i).getY() == 1680) {
                tmpY = list.get(i).getY() - cha;
            }
            if (list.get(i).getX() == 0) {
                tmpX = list.get(i).getX() + cha;
            }
            if (list.get(i).getX() == 1680) {
                tmpX = list.get(i).getX() - cha;
            }
            geoPoint[i] = new GeoPoint(
                    geoPointLocationLB.latitude
                            - (geoPointLocationLB.latitude - geoPointLocationRT.latitude) * (tmpY / 1680.0),
                    geoPointLocationLB.longitude
                            - (geoPointLocationLB.longitude - geoPointLocationRT.longitude) * (tmpX / 1680.0));
            if (list.get(i).getIsInspect() == 0) {
                marker[i] = new Marker(geoPoint[i],
                        Marker.boundCenterBottom(getResources().getDrawable(R.drawable.gray_cir)));
            } else {
                marker[i] = new Marker(geoPoint[i],
                        Marker.boundCenterBottom(getResources().getDrawable(R.drawable.yellow_cir)));
            }

        }

        listOverlay.getOverlayItems().clear();
        for (int i = 0; i < sum; i++) {
            listOverlay.getOverlayItems().add(marker[i]);
        }
        //
        mSailsMapView.getOverlays().clear();
        mSailsMapView.getOverlays().add(listOverlay);
        mSailsMapView.redraw();
    }

    public void timerTask() {
        updateProgressBar.schedule(new TimerTask() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 0x01;
                hisHandler.sendMessage(msg);
            }
        },6000,5000);
    }

}
