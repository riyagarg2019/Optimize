package com.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.data.AppDatabase;
import com.data.Destination;
import com.example.riyagarg.optimize.R;
import com.touch.DestinationTouchHelperAdapter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by riyagarg on 5/15/18.
 */

public class ResultsRecyclerAdapter extends RecyclerView.Adapter<com.adapter.DestinationRecyclerAdapter.ViewHolder>
             {

    private List<Destination> destinationList;
    private List<String> stringDestinationList;
    private Context context;

    public ResultsRecyclerAdapter(List<Destination> dests, Context context){
        destinationList = dests;
        this.context = context;
    }

    public void deleteDestinations() {
        destinationList.clear();
        notifyDataSetChanged();
    }

    public List<Destination> getDestinationList() {
        return destinationList;
    }

    @Override
    public com.adapter.DestinationRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewRow = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_row, parent, false);
        return new com.adapter.DestinationRecyclerAdapter.ViewHolder(viewRow);
    }

    @Override
    public void onBindViewHolder(com.adapter.DestinationRecyclerAdapter.ViewHolder holder, int position) {
        holder.tvLocation.setText(destinationList.get(holder.getAdapterPosition()).getLocation());
        holder.tvNumber.setText(String.valueOf(position + 1) + ".");
    }

    public void addDestination(Destination dest) {
        destinationList.add(dest);
        notifyDataSetChanged();
    }

    public void updateDestination(Destination dest) {
        int editPos = findPlaceIndexByDestinationId(dest.getDestinationId());
        destinationList.set(editPos,dest);
        notifyItemChanged(editPos);

    }

    private int findPlaceIndexByDestinationId(long todoId){
        for(int i = 0; i < destinationList.size(); i++){
            if(destinationList.get(i).getDestinationId() == todoId){
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return destinationList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvLocation;
        private TextView tvNumber;

        public ViewHolder(View itemView){
            super(itemView);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvNumber = itemView.findViewById(R.id.tvNumber);
        }
    }
}


