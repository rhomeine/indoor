package com.bupt.indooranalysis.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bupt.indooranalysis.R;
import com.sails.engine.SAILS;
import com.sails.engine.SAILSMapView;
import com.sails.engine.core.model.GeoPoint;
import com.sails.engine.overlay.ListOverlay;
import com.sails.engine.overlay.Marker;

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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Button button;
    private  Button heatMapButton;
    private OnFragmentInteractionListener mListener;

    private Context mcontext = null;

    static SAILS mSails;
    static SAILSMapView mSailsMapView;
    Spinner floorList;
    ArrayAdapter<String> adapter;
    byte zoomSav=0;

    GeoPoint geoPointLocationLB = new GeoPoint(39.96289894781549, 116.35293035811996);
    GeoPoint geoPointLocationRT = new GeoPoint(39.96304388207584, 116.35312012440777);

    ListOverlay listOverlay = new ListOverlay();

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
        button = (Button) view.findViewById(R.id.buttonRound);
        heatMapButton=(Button)view.findViewById(R.id.heatmap_button);
        floorList = (Spinner) view. findViewById(R.id.spinner_datafragment);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        heatMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heatMap();
            }
        });

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
        ((FrameLayout) view.findViewById(R.id.SAILSMap_FragmentMap)).addView(mSailsMapView);
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
}
