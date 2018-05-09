package com.touch;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by riyagarg on 5/9/18.
 */

public class DestinationTouchHelperCallback extends ItemTouchHelper.Callback{

    private DestinationTouchHelperAdapter destinationTouchHelperAdapter; //sending to interface

    public DestinationTouchHelperCallback(DestinationTouchHelperAdapter destinationTouchHelperAdapter) { //constructer setting field
        this.destinationTouchHelperAdapter = destinationTouchHelperAdapter;
    }

    @Override
    public boolean isLongPressDragEnabled() { //why able to long press item and move it
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() { //why can swipe items
        return true;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) { //drag up and down and slide left and right
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags); //enable these flags
    }

    @Override
    public boolean onMove(RecyclerView recyclerView,
                          RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) { //which item is moved and where
        destinationTouchHelperAdapter.onDestinationMove( //from and to position
                viewHolder.getAdapterPosition(),
                target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) { //when you swipe out an item
        destinationTouchHelperAdapter.onDestinationDismiss(viewHolder.getAdapterPosition()); //send result to interface that this item is swiped
    }
}
