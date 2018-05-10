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
import java.util.List;

/**
 * Created by riyagarg on 5/9/18.
 */

public class DestinationRecyclerAdapter extends RecyclerView.Adapter<DestinationRecyclerAdapter.ViewHolder>
        implements DestinationTouchHelperAdapter {

    private List<Destination> destinationList;
    private Context context;

    public DestinationRecyclerAdapter(List<Destination> dests, Context context){
        destinationList = dests;
        this.context = context;
    }

    public void deleteDestinations() {
        destinationList.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewRow = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_row, parent, false);

        return new ViewHolder(viewRow);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.tvLocation.setText(destinationList.get(holder.getAdapterPosition()).getLocation());

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

    @Override
    public void onDestinationDismiss(int position) {
        //should remove item at position from list
        final Destination Remove = destinationList.remove(position);

        notifyItemRemoved(position);
        new Thread(){
            @Override
            public void run() {
                AppDatabase.getAppDatabase(context).destinationDao().delete(Remove);
            }
        }.start();

    }

    @Override
    public void onDestinationMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(destinationList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(destinationList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);

    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvLocation;
        private TextView tvDescription;


        public ViewHolder(View itemView){
            super(itemView);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDescription = itemView.findViewById(R.id.tvDescription);


        }
    }
}
