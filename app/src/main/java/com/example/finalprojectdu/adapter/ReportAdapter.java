package com.example.finalprojectdu.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalprojectdu.R;
import com.example.finalprojectdu.model.Report;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder>{

    List<Report> reportsList;
    Context context;

    public ReportAdapter(List<Report> reportsList, Context context) {
        this.reportsList = reportsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ReportAdapter.ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view=layoutInflater.inflate(R.layout.sample_daily_upload,parent,false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportAdapter.ReportViewHolder holder, int position) {
        holder.name.setText(reportsList.get(position).getName());
        holder.time.setText(reportsList.get(position).getTime() + " " + reportsList.get(position).getDate());
        holder.info.setText(reportsList.get(position).getRoadCondition());
        if (reportsList.get(position).getImage()!=null) {
            holder.imageView.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(reportsList.get(position).getImage())
                    .placeholder(R.drawable.img_loading)
                    .centerCrop()
                    .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return reportsList.size();
    }


    public class ReportViewHolder extends RecyclerView.ViewHolder{

        TextView name, time, info;
        ImageView imageView;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name_id);
            time = itemView.findViewById(R.id.time_id);
            info = itemView.findViewById(R.id.info_id);
            imageView = itemView.findViewById(R.id.image_id);
        }
    }
}
