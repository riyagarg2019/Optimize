package com.task;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.data.Destination;
import com.example.riyagarg.optimize.ResultsActivity;

import java.lang.ref.WeakReference;
import java.util.List;

public class AsyncTSPTask extends AsyncTask<List<Destination>, Void, List<Destination>>{


    private WeakReference<Context> contextRef;

    public AsyncTSPTask(Context context) {
        contextRef = new WeakReference<>(context);
    }

    @Override
    protected List<Destination> doInBackground(List<Destination>... lists) {
        return null;
        //Intent intent = new Intent(this, )
    }


    @Override
    protected void onPostExecute(List<Destination> destinations) {
        Context context = contextRef.get();
        if(context != null) {
            Intent intent = new Intent(context, ResultsActivity.class);
        }
    }
}
