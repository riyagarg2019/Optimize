package com.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.data.AppDatabase;
import com.data.Place;
import com.example.riyagarg.optimize.R;
import com.touch.PlaceTouchHelperAdapter;

import java.util.Collections;
import java.util.List;

/**
 * Created by riyagarg on 5/9/18.
 */

public class PlaceRecyclerAdapter extends RecyclerView.Adapter<PlaceRecyclerAdapter.ViewHolder> implements PlaceTouchHelperAdapter {


    private List<Place> placeList;
    private Context context;

    public PlaceRecyclerAdapter(List<Place> places, Context context){
        placeList = places;
        this.context = context;
    }

    public void deletePlaces() {
        placeList.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewRow = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_row, parent, false);

        return new ViewHolder(viewRow);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.tvLocation.setText(placeList.get(holder.getAdapterPosition()).getLocation());
        holder.tvDescription.setText(placeList.get(holder.getAdapterPosition()).getDescription());

    }

    public void addPlace(Place place) {
        placeList.add(place);
        notifyDataSetChanged();
    }

    public void updatePlace(Place place) {
        int editPos = findPlaceIndexByPlaceId(place.getPlaceId());
        placeList.set(editPos,place);
        notifyItemChanged(editPos);

    }

    private int findPlaceIndexByPlaceId(long todoId){
        for(int i = 0; i < placeList.size(); i++){
            if(placeList.get(i).getPlaceId() == todoId){
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return placeList.size();
    }

    @Override
    public void onPlaceDismiss(int position) {
        //should remove item at position from list
        final Place Remove = placeList.remove(position);

        notifyItemRemoved(position);
        new Thread(){
            @Override
            public void run() {
                AppDatabase.getAppDatabase(context).placeDao().delete(Remove);
            }
        }.start();

    }

    @Override
    public void onPlaceMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(placeList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(placeList, i, i - 1);
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
