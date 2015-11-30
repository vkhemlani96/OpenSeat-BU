package com.example.vinay.openseatbu;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.getbase.floatingactionbutton.AddFloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Random;
import java.util.Set;


public class MainActivity extends ActionBarActivity {

    String PREFS_NAME = "OPENSEAT_BU_PREFS";
    String STRING_SET_KEY = "OPENSEAT_BU_COURSE_SET";
    String REQUEST_URL_PREFIX = "http://thscioly.com/vinay.php?params=";

    RecyclerView courseView = null;

    SharedPreferences settings = null;

    private Dictionary<String, Integer> notifications = new Hashtable<String, Integer>();

    NotificationManager mNotificationManager = null;
    private static final int ONGOING_NOTIFICATION_ID = 380;

    DialogInterface.OnClickListener dialogAdd = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            String college = ((EditText) ((AlertDialog) dialog).findViewById(R.id.college))
                    .getText().toString();
            String dept = ((EditText) ((AlertDialog) dialog).findViewById(R.id.dept))
                    .getText().toString();
            String course = ((EditText) ((AlertDialog) dialog).findViewById(R.id.course))
                    .getText().toString();
            String section = ((EditText) ((AlertDialog) dialog).findViewById(R.id.section))
                    .getText().toString();

            //TODO validate data (length, type, etc.)

            System.out.println(college + "," + dept + "," + course + "," + section);

            Set<String> courses = settings.getStringSet(STRING_SET_KEY, null);
            if (courses == null) courses = new HashSet<String>();

            courses.add(college + "," + dept + "," + course + "," + section);
            settings.edit().remove(STRING_SET_KEY).apply();
            settings.edit().putStringSet(STRING_SET_KEY, courses).apply();

            findViewById(R.id.progress).setVisibility(View.VISIBLE);

            if (courses.size() == 1) {
                findViewById(R.id.no_classes_view).setVisibility(View.GONE);
                findViewById(R.id.add_button).bringToFront();
                makeCourseRequest();
            } else {
                Toast.makeText(MainActivity.this, "Course will appear on next refresh", Toast.LENGTH_LONG).show();
            }

            dialog.cancel();
        }
    };
    DialogInterface.OnClickListener dialogCancel = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    };

    View.OnClickListener addListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            // 2. Chain together various setter methods to set the dialog characteristics
            builder.setTitle("Add a Course")
                    .setView(R.layout.course_dialog_view)
                    .setPositiveButton("Add", dialogAdd)
                    .setNegativeButton("Cancel", dialogCancel);

            // 3. Get the AlertDialog from create()
            AlertDialog dialog = builder.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        AddFloatingActionButton addButton = (AddFloatingActionButton) findViewById(R.id.add_button);
        addButton.setOnClickListener(addListener);

        settings = getSharedPreferences(PREFS_NAME, 0);
        Set<String> courses = settings.getStringSet(STRING_SET_KEY, null);

        if (courses != null && courses.size() > 0) {
            findViewById(R.id.no_classes_view).setVisibility(View.GONE);
            findViewById(R.id.progress).setVisibility(View.VISIBLE);

            courseView = new RecyclerView(this);

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
            courseView.setLayoutManager(mLayoutManager);

            courseView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));

            courseView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

            ((RelativeLayout) findViewById(R.id.parent_layout)).addView(courseView, 0);
            addButton.bringToFront();

            makeCourseRequest();

            Notification ongoingNotif = getOngoingNotification().setContentText("Checking for classes...").build();
            mNotificationManager.notify(ONGOING_NOTIFICATION_ID, ongoingNotif);
//            startForeground
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void makeCourseRequest() {
        final Set<String> courses = settings.getStringSet(STRING_SET_KEY, null);

        if (courses == null || courses.size() == 0) {
            findViewById(R.id.no_classes_view).setVisibility(View.VISIBLE);
            courseView.setVisibility(View.GONE);
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = REQUEST_URL_PREFIX;
        for (String course : courses) {
            url += course + ";";
        }
        url = url.substring(0,url.length()-1);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        System.out.println("hello");
                        parseResponse(courses, response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        RetryPolicy policy = new DefaultRetryPolicy(100000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);

        queue.add(stringRequest);

    }

    private void parseResponse(Set<String> courses, String response) {
        String[] requestResults = response.split("<br/>");
        ArrayList<String[]> courseResult = new ArrayList<String[]>();
        for (String course : courses) {
            boolean found = false;
            String foundResult = "";
            for (String result : requestResults) {
                if (result.contains(course)) {
                    found = true;
                    foundResult = result;
                }
            }

            if (found) courseResult.add(foundResult.split("-"));
            else {
                String[] defaultArray = {course};
                courseResult.add(defaultArray);
            }
        }

        if (courseView == null) {
            findViewById(R.id.no_classes_view).setVisibility(View.GONE);

            courseView = new RecyclerView(this);

            LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
            courseView.setLayoutManager(mLayoutManager);

            courseView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT));

            courseView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

            ((RelativeLayout) findViewById(R.id.parent_layout)).addView(courseView, 0);
        }

        findViewById(R.id.progress).setVisibility(View.GONE);
        courseView.setAdapter(new CourseViewAdapter(courseResult, this));

        Calendar c = Calendar.getInstance();
        String hour = String.valueOf(c.get(Calendar.HOUR));
        String minute  = String.valueOf(c.get(Calendar.MINUTE));
        minute = minute.length() == 1 ? "0" + minute : minute;
        int amPmId = c.get(Calendar.AM_PM);
        String ampm = amPmId == 1 ? "PM" : "AM";

        Notification ongoingNotif = getOngoingNotification()
                .setContentText("Last Checked at " + hour + ":" + minute + " " + ampm).build();
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID, ongoingNotif);

        for (String[] data : courseResult) {
            Integer notifId = notifications.get(data[0].trim());
            if (notifId == null) {
                Random r = new Random();
                notifId = r.nextInt(100);
                notifications.put(data[0].trim(), notifId);
            }
            if (Integer.parseInt(data[1].trim()) > 0 && !data[2].trim().equals("F") && !data[2].trim().equals("C")) {
                    //Content Intent
                    String url = "https://www.bu.edu/link/bin/uiscgi_studentlink.pl/1431456077?ModuleName=menu.pl&NewMenu=Academics";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

                    long[] vibrate = {0,1000};

                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(this)
                                    .setSmallIcon(R.drawable.ic_stat_notification_logo)
                                    .setContentTitle(data[0].trim().replaceAll(","," ") + " is Open")
                                    .setContentText("There are " + data[1].trim() + " open seat(s). Click to register.")
                                    .setPriority(NotificationCompat.PRIORITY_MAX)
                                    .setVibrate(vibrate)
                                    .setColor(getResources().getColor(R.color.accent))
                                    .setOnlyAlertOnce(true)
                                    .setLights(R.color.accent, 2000,1000)
                                    .setContentIntent(contentIntent);

                    mNotificationManager.notify(notifId, mBuilder.build());

//                }
            }
        }



            final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                makeCourseRequest();
            }
        }, 15000);

    }

    private NotificationCompat.Builder getOngoingNotification() {
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notification_logo)
                .setContentTitle("OpenSeat BU is Running")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .setOnlyAlertOnce(true);
    }

}
