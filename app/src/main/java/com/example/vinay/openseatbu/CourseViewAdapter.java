package com.example.vinay.openseatbu;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;
import java.util.Set;

/**
 * Created by Vinay on 5/11/15.
 */
public class CourseViewAdapter extends RecyclerView.Adapter<CourseViewAdapter.ViewHolder> {
    private ArrayList<String[]> mDataset;
    private Context context;
    private SharedPreferences settings;
    private static final String PREFS_NAME = "OPENSEAT_BU_PREFS";
    private static final String STRING_SET_KEY = "OPENSEAT_BU_COURSE_SET";


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public RelativeLayout mRelativeLayout;
        public ViewHolder(RelativeLayout v) {
            super(v);
            mRelativeLayout = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CourseViewAdapter(ArrayList<String[]> myDataset, Context c) {
        mDataset = myDataset;
        context = c;
        settings = c.getSharedPreferences(PREFS_NAME, 0);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CourseViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        RelativeLayout v = (RelativeLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_recycle_view, parent, false);

        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        System.out.println("still running bind");
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final String[] data = mDataset.get(position);

        holder.mRelativeLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mDataset.remove(position);
                notifyItemRemoved(position);

                Set<String> courses = settings.getStringSet(STRING_SET_KEY, null);
                if (courses != null && courses.contains(data[0].trim())) {
                    courses.remove(data[0].trim());
                    settings.edit().remove(STRING_SET_KEY).apply();
                    settings.edit().putStringSet(STRING_SET_KEY, courses).apply();
                    Toast.makeText(context, "Course Removed", Toast.LENGTH_LONG);
                } else if (!courses.contains(data[0].trim())) {
                    Toast.makeText(context, "DATA NOT FOUND", Toast.LENGTH_LONG);
                }

                return false;
            }

        });

        TextView courseTitle = (TextView) holder.mRelativeLayout.findViewById(R.id.course_title);
        TextView courseStatus = (TextView) holder.mRelativeLayout.findViewById(R.id.course_status);
        TextView courseSeats = (TextView) holder.mRelativeLayout.findViewById(R.id.seats);

        if (data.length == 1) {
            courseTitle.setText(data[0].trim().replaceAll(",", " "));
            courseSeats.setText("N/A");
            courseStatus.setText("Course Not Found");
            courseStatus.setTypeface(null, Typeface.BOLD);
            return;
        }

        courseTitle.setText(data[0].trim().replaceAll(","," "));
        courseSeats.setText(data[1].trim() + " Seats");

        if (data[2].trim().equals("O")) courseStatus.setText("Status: Course Open");
        else if (data[2].trim().equals("C")) courseStatus.setText("Status: Course Close");
        else if (data[2].trim().equals("F")) courseStatus.setText("Status: Course Full");
        else courseStatus.setText("Status: " + data[2]);

        if (Integer.parseInt(data[1].trim()) == 0 || data[2].trim().equals("F") || !data[2].trim().equals("C")) {
            holder.mRelativeLayout.setAlpha(.7f);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
