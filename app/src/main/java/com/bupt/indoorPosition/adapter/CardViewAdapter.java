package com.bupt.indoorPosition.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bupt.indoorPosition.bean.InspectedBeacon;
import com.bupt.indooranalysis.R;

import java.util.List;

/**
 * Created by rhomeine on 16/10/18.
 */

public class CardViewAdapter
        extends RecyclerView.Adapter<CardViewAdapter.ViewHolder>
{

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private List<InspectedBeacon> inspectedBeaconList;

    private Context mContext;

    public CardViewAdapter( Context context , List<InspectedBeacon> inspectedBeacons)
    {
        this.mContext = context;
        this.inspectedBeaconList = inspectedBeacons;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i )
    {
        // 给ViewHolder设置布局文件
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.base_cardview_list, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i )
    {
        // 给ViewHolder设置元素
        InspectedBeacon beacon = inspectedBeaconList.get(i);
        viewHolder.mDate.setText(beacon.getDate());
        viewHolder.mLocation.setText(beacon.getBuildingName());
        viewHolder.mDuration.setText(beacon.getDuration());
        if(onItemClickListener!=null){
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = viewHolder.getLayoutPosition();
                    onItemClickListener.onItemClick(viewHolder.itemView,pos);
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        // 返回数据总数
        return inspectedBeaconList == null ? 0 : inspectedBeaconList.size();
    }

    // 重写的自定义ViewHolder
    public static class ViewHolder
            extends RecyclerView.ViewHolder
    {
        public TextView mDate;
        public TextView mLocation;
        public TextView mDuration;

        public ViewHolder( View v )
        {
            super(v);
            mDate = (TextView) v.findViewById(R.id.base_history_item_date);
            mLocation = (TextView) v.findViewById(R.id.base_history_item_location);
            mDuration = (TextView) v.findViewById(R.id.base_history_item_duration);

        }
    }
}