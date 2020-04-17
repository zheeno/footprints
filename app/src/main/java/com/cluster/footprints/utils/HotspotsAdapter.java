package com.cluster.footprints.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cluster.footprints.R;

import java.util.ArrayList;
import java.util.List;

public class HotspotsAdapter extends RecyclerView.Adapter<HotspotsAdapter.MyViewHolder> {

    private ArrayList<HotspotInfo> hotspotList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, year, description;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            description = (TextView) view.findViewById(R.id.description);
            year = (TextView) view.findViewById(R.id.isCont);
        }
    }


    public HotspotsAdapter(ArrayList<HotspotInfo> hotspotList) {
        this.hotspotList = hotspotList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hotspot_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        HotspotInfo movie = hotspotList.get(position);
        holder.title.setText(movie.getTitle());
        holder.description.setText(movie.getDescription());
//        holder.year.setText(movie.getYear());
    }

    @Override
    public int getItemCount() {
        return hotspotList.size();
    }
}
