package com.contact.index.sidebar.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.contact.index.R;
import com.contact.index.sidebar.model.City;

import java.util.ArrayList;

/**
 * author:feng.G
 * time:  2019-05-28 16:50
 * desc:
 */
public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private ArrayList<City> mList;
    private LayoutInflater mInflater;

    public CustomAdapter(Context context, ArrayList<City> list) {
        this.mList = list;
        mInflater = LayoutInflater.from(context);
    }

    public ArrayList<City> getDatas() {
        return mList;
    }

    public CustomAdapter setDatas(ArrayList<City> list) {
        mList = list;
        return this;
    }

    @NonNull
    @Override
    public CustomAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final CustomAdapter.ViewHolder holder, final int position) {
        final City info = mList.get(position);
        if (null != info) {
            if (!TextUtils.isEmpty(info.getName())) {
                holder.tv_name.setText(info.getName());
            }
            if (position == mList.size() - 1) {
                holder.view_bottom.setVisibility(View.GONE);
            } else {
                holder.view_bottom.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name;
        View view_bottom;

        private ViewHolder(View itemView) {
            super(itemView);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            view_bottom = (View) itemView.findViewById(R.id.view_bottom);
        }
    }
}
