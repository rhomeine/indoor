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
import android.widget.TextView;
import android.widget.Toast;

import com.bupt.indoorPosition.adapter.CardViewAdapter;
import com.bupt.indoorPosition.bean.Buildings;
import com.bupt.indoorPosition.bean.InspectedBeacon;
import com.bupt.indooranalysis.R;
import com.bupt.indooranalysis.Util.ArcProgress;
import com.yalantis.phoenix.PullToRefreshView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    private ArcProgress arcProgress;
    private HisHandler hisHandler;

    private List<InspectedBeacon> inspectedBeacons = new ArrayList<InspectedBeacon>();
    private OnFragmentInteractionListener mListener;
    //加速度计计步测试用数据
    public static List<Float> blueTime = new ArrayList<Float>();
    public static List<Float> blueTime1 = new ArrayList<Float>();

    public HistoryFragment() {
        // Required empty public constructor
    }

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
        arcProgress = (ArcProgress) view.findViewById(R.id.progress);
        hisHandler = new HisHandler();
        //设置进度控件方法，默认max为100
//        arcProgress.setMax(100);
//        arcProgress.setProgress(39);

        arcProgress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message();
                msg.what = 0x01;
                hisHandler.sendMessage(msg);
            }

        });

        return view;
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
                    Iterator<Map.Entry<String, Integer>> it = Buildings.InspectHistory.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, Integer> entry = it.next();
                        if (entry.getValue() == 1)
                            count += 1;
                    }
                    count = (100 * count) / Buildings.InspectHistory.size();
                    arcProgress.setProgress(count);
                    arcProgress.invalidate();
                }
            }
            super.handleMessage(msg);
        }
    }

}
