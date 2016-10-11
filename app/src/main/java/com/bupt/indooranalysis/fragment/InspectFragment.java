package com.bupt.indooranalysis.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bupt.indoorPosition.bean.InspectedBeacon;
import com.bupt.indoorPosition.callback.InspectUpdateCallback;
import com.bupt.indoorPosition.dao.DBManager;
import com.bupt.indoorPosition.model.ModelService;
import com.bupt.indoorPosition.uti.Constants;
import com.bupt.indoorPosition.uti.MessageUtil;
import com.bupt.indooranalysis.MainActivity;
import com.bupt.indooranalysis.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Button button;
    private TextView textView;

    private OnFragmentInteractionListener mListener;

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
            }
        });
        return view;
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
    private class StartListener implements View.OnClickListener {
        @Override
        public void onClick(View arg0) {

            final Intent intent = new Intent();
            intent.setAction("com.bupt.indoorpostion.ScanService");
            if (!MessageUtil.checkLogin(mcontext)) {
                return;
            }
            //此处设置开始巡检的启动状况
//            if (startScanning == false) {
//                startScanning = true;
//                btnStart.setText(R.string.btnStarting);
//                btnimage.setImageResource(images[0]);
//                ((FragmentServiceCallback) parent).startOrStopActivityService(
//                        intent, true);
//            } else {
//                startScanning = false;
//                btnStart.setText(R.string.btnStartContent);
//                // bAdapter.disable();
//                btnimage.setImageResource(images[1]);
//                ((FragmentServiceCallback) parent).startOrStopActivityService(
//                        intent, false);
//            }
        }
    }
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
