package com.laguna.university.studentperformanceanalytics;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    CoordinatorLayout snackbarLayout, notifSnackBar;
    TabLayout tabLayout;
    ViewPager viewPager;
    PagerAdapter adapter;
    SearchView search;
    ListView studentList, notifList;
    LinearLayout searchList;
    CardView notification;
    TextView notifCount;

    static String user = "";
    static String pass = "";
    String selectedSubject = "";
    String selectedSubjectDescription = "";
    String categoryName = "";
    String notifFilterSubject = "", notifFilterBy = "All";
    int notifFilterPosition = 0;
    int categoryId;

    SQLiteDBcontroller db  = new SQLiteDBcontroller(MainActivity.this);
    AlertDialog cancel, notificationDialog;
    ArrayAdapter<String> searchAdapter;
    ArrayList<String> stnd = new ArrayList<>();

    boolean isRunning = true;

    //sem
    String completeAY;
    String sem;

    private PendingIntent pendingIntent;

    static boolean active = false;

    @Override
    public void onStart(){
        super.onStart();
        active = true;
    }

    @Override
    public void onStop(){
        super.onStop();
        active = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //setting content view
        setContentView(R.layout.activity_main);

        snackbarLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        search = (SearchView) findViewById(R.id.search);
        studentList = (ListView) findViewById(R.id.studentList);
        searchList = (LinearLayout) findViewById(R.id.searchList);
        notification = (CardView) findViewById(R.id.notification);
        notifCount = (TextView) findViewById(R.id.notifText);

        db.getSemAndAY();
        completeAY = db.ay.get(0);
        sem = db.sem.get(0);

        //setting up all need to initialize
        user = getIntent().getExtras().getString("username");
        pass = getIntent().getExtras().getString("password");
        db.login(user, pass);
        db.classList(db.loginID.get(0));
        if(db.subcode.size() == 0) {
            Intent intent = new Intent(MainActivity.this, Login.class);
            intent.putExtra("prompt","Please Contact Administrator to add your subject and student to your account");
            startActivity(intent);
            finish();
        }
        db.getTerm(db.loginID.get(0), db.subcode.get(0));
        if(db.term.size() == 0){
            db.setTerm(db.loginID.get(0), db.subcode.get(0), 0);
        }
        db.getSettings(db.loginID.get(0));
        if(db.settingsAutoUpload.size() == 0){
            db.setSettings(db.loginID.get(0), 0, 0);
        }
        db.getPassingGrade(db.loginID.get(0));
        if(db.passingGradeValue.size() == 0){
            showDialogForPassingGrade();
        }
        db.getSyncDate(db.loginID.get(0));
        if(db.syncDate.size() == 0){
            db.setSyncDate(db.loginID.get(0), "", "");
        }
        db.getSettings(db.loginID.get(0));
        new Thread(CheckDatabase).start();
        initTabbed();

        db.getAllStudent(db.loginID.get(0));
        for (int i = 0; i < db.searchStudentName.size(); i++) {
            stnd.add("Name: "+db.searchStudentName.get(i) + "\nStudent No: " + db.searchStudentNo.get(i) + "\nSubject: " + db.searchStudentSubject.get(i));
        }
        searchAdapter = new ArrayAdapter<String>(this, R.layout.zzz_one_textview,stnd);
        studentList.setAdapter(searchAdapter);
        studentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String temp = searchAdapter.getItem(position);

                String name = db.searchStudentName.get(stnd.indexOf(temp));
                String no = db.searchStudentNo.get(stnd.indexOf(temp));
                String subject = db.searchStudentSubject.get(stnd.indexOf(temp));

                Intent intent = new Intent(MainActivity.this, Individual_Statistic.class);
                intent.putExtra("studentid", no);
                intent.putExtra("name", name);
                intent.putExtra("subject", subject);
                intent.putExtra("username", user);
                intent.putExtra("password", pass);
                startActivity(intent);
                search.setQuery("",false);
            }
        });

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchAdapter.getFilter().filter(s);
                if(s.equals("")){
                    tabLayout.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.VISIBLE);
                    searchList.setVisibility(View.GONE);
                }
                else {
                    tabLayout.setVisibility(View.GONE);
                    viewPager.setVisibility(View.GONE);
                    searchList.setVisibility(View.VISIBLE);
                }
                return false;
            }
        });

        notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.classList(db.loginID.get(0));
                showNotification();
            }
        });

        startService();
        refreshNotificationText();
    }

    public void setDefaultCategory(String subject){
        db.getCategory(db.loginID.get(0), subject);
        if(db.category.size() == 0) {
            db.setCategory(db.loginID.get(0), subject, "Exam");
            db.setCategory(db.loginID.get(0), subject, "Attendance");
            db.setCategory(db.loginID.get(0), subject, "Class Standing");
            db.setCategory(db.loginID.get(0), subject, "Recitation");
            db.setCategory(db.loginID.get(0), subject, "Quiz");
            db.getCategory(db.loginID.get(0), subject);
            String array[] = {".40", ".10", ".10", ".20", ".20"};
            for (int i = 0; i < db.category.size(); i++) {
                db.setPercentage(db.loginID.get(0), db.category_id.get(i), Float.parseFloat(array[i]));
            }
        }
    }

    public void refreshNotificationText(){
        //refresh notif text count
        db.getNotification(db.loginID.get(0));
        int size = 0;
        for(int i=0;i<db.notificationStatus.size();i++){
            if(db.notificationStatus.get(i).equals("UNREAD")){
                size++;
            }
        }
        if(size > 0) {
            notifCount.setText("" + size);
        } else {
            notifCount.setText("");
        }
    }

    public void showDialogForPassingGrade(){
        LayoutInflater inflater = this.getLayoutInflater();
        View percentageLayout = inflater.inflate(R.layout.passing_grade, null);
        final CoordinatorLayout layout = (CoordinatorLayout) percentageLayout.findViewById(R.id.showSnackBar);
        final EditText passingValue = (EditText) percentageLayout.findViewById(R.id.passing_value);
        CardView save = (CardView) percentageLayout.findViewById(R.id.save);
        layout.bringToFront();
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(passingValue.getText().length() > 0){
                    AlertDialog.Builder yesno = new AlertDialog.Builder(MainActivity.this);
                    yesno.setTitle("Are you sure?");
                    yesno.setMessage("This can only be submitted once and cannot be edited anymore.");
                    yesno.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            int value = Integer.parseInt(passingValue.getText().toString());
                            db.insertPassingGrade(db.loginID.get(0), value);
                            showSnackBar("Passing Grade Successfully Saved.");
                            cancel.dismiss();
                        }
                    });
                    yesno.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    yesno.show();
                } else {
                    Snackbar snackbar = Snackbar.make(layout, "Passing Grade Cannot be Empty.", Snackbar.LENGTH_LONG);
                    String color = getResources().getString(0+R.color.headerColor);
                    View snackBarView = snackbar.getView();
                    snackBarView.setBackgroundColor(Color.parseColor(String.valueOf(color)));
                    TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextSize(18);
                    snackbar.show();
                }
            }
        });

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        cancel = dialog.create();
        cancel.setCancelable(false);
        cancel.setView(percentageLayout);
        cancel.show();
    }

    /*
    int totalQuestion = 20;
    int filterData[] = new int[totalQuestion];
    public int filterRandom(int randomNumber){
        Arrays.fill(filterData, 0); // ilalagy mo to sa onCreate sa ilalim ng casting ng mga widget kc gngwa nito nillgyan ng zero value ung array na int mo
        int value = 0;
        for(int i=0;i<totalQuestion;i++){
            if(filterData[i] != 0){
                if(filterData[i] == randomNumber){
                    filterRandom(randomNumber++);
                } else {
                    filterData[i] = randomNumber;
                    value = randomNumber;
                }
            } else {
                filterData[i] = randomNumber;
                value = randomNumber;
            }
        }
        return value;
    }
    */

    public void initTabbed(){
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        tabLayout.addTab(tabLayout.newTab().setText("Dashboard"));
        tabLayout.addTab(tabLayout.newTab().setText("Grade"));
        tabLayout.addTab(tabLayout.newTab().setText("Standards"));

        viewPager = (ViewPager) findViewById(R.id.pager);
        adapter = new TabPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        View root = tabLayout.getChildAt(0);
        if (root instanceof LinearLayout) {
            ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(getResources().getColor(R.color.tabDivider));
            drawable.setSize(3, 3);
            ((LinearLayout) root).setDividerPadding(10);
            ((LinearLayout) root).setDividerDrawable(drawable);
        }
    }

    public String EquivalentColor(int grade){
        String color = "";
        if(grade >= 98 && grade <= 100){
            color = getResources().getString(0+R.color.exelent);
        } else if(grade >= 94 && grade <= 97){
            color = getResources().getString(0+R.color.mexelent);
        } else if(grade >= 90 && grade <= 93){
            color = getResources().getString(0+R.color.outstanding);
        } else if(grade >= 86 && grade <= 89){
            color = getResources().getString(0+R.color.vgood);
        } else if(grade == 85){
            color = getResources().getString(0+R.color.good);
        } else if(grade >= 82 && grade <= 84){
            color = getResources().getString(0+R.color.satisfactory);
        } else if(grade >= 80 && grade <= 81){
            color = getResources().getString(0+R.color.fsatisfactory);
        } else if(grade >= 76 && grade <= 79){
            color = getResources().getString(0+R.color.msatisfactory);
        } else if(grade == 75){
            color = getResources().getString(0+R.color.passed);
        } else if(grade <= 74){
            color = getResources().getString(0+R.color.failed);
        }
        return color;
    }

    public void moveTab(){
        viewPager.setCurrentItem(1);
    }

    public void showSnackBar(String msg){
        Snackbar snackbar = Snackbar.make(snackbarLayout, msg, Snackbar.LENGTH_LONG);
        String color = getResources().getString(0+R.color.headerColor);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(Color.parseColor(String.valueOf(color)));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(18);
        snackbar.show();
    }

    public void displayView(String activity) {
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        switch (activity) {
            case "allgrade":
                ft.replace(R.id.gsFrag, new gsAllGrade());
                break;
            case "quiz":
                ft.replace(R.id.gsFrag, new gsQuizFragment());
                break;
            case "attendance":
                ft.replace(R.id.gsFrag, new gsAttendanceFragment());
                break;
            case "exam":
                ft.replace(R.id.gsFrag, new gsExamFragment());
                break;
        }
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();
    }

    public class TabPagerAdapter extends FragmentPagerAdapter {
        int tabCount;

        public TabPagerAdapter(FragmentManager fm, int tabCount) {
            super(fm);
            this.tabCount = tabCount;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    DashboardFragment notifTab = new DashboardFragment();
                    return notifTab;
                case 1:
                    GradeFragment gradeTab = new GradeFragment();
                    return gradeTab;
                case 2:
                    StandardFragment standardTab = new StandardFragment();
                    return standardTab;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return tabCount;
        }
    }

    public void showNotification(){
        db.classList(db.loginID.get(0));
        startService();

        LayoutInflater inflater = this.getLayoutInflater();
        View layout = inflater.inflate(R.layout.notification_layout, null);
        notifList = (ListView) layout.findViewById(R.id.notifList);
        notifSnackBar = (CoordinatorLayout) layout.findViewById(R.id.snackbar);
        final Spinner filterNotif = (Spinner) layout.findViewById(R.id.filter);
        final Spinner subjectNotif = (Spinner) layout.findViewById(R.id.subject);
        TextView hide = (TextView) layout.findViewById(R.id.supportMessage);
        hide.setVisibility(View.GONE);
        ArrayList<String> filterData = new ArrayList<>();
        ArrayList<String> subjectData = new ArrayList<>();

        subjectData.add("Subject");
        subjectData.addAll(db.subcode);
        // Initializing an ArrayAdapter
        final ArrayAdapter<String> subjectAdapter = new ArrayAdapter<String>(this,R.layout.zzz_one_textview,subjectData){
            @Override
            public boolean isEnabled(int position){
                if(position == 0) {
                    return false;
                }
                else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0) {
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        subjectAdapter.setDropDownViewResource(R.layout.zzz_one_textview);
        subjectNotif.setAdapter(subjectAdapter);
        subjectNotif.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                notifFilterSubject = (String) subjectNotif.getItemAtPosition(i);
                if(notifFilterPosition >= 1 && notifFilterPosition <= 4){
                    db.sortNotificationByPriority(db.loginID.get(0), notifFilterBy, notifFilterSubject);
                    refreshFilterData();
                } else {
                    refreshFilterData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        filterData.add("Filter By");
        filterData.add("Low Priority");
        filterData.add("Mid Priority");
        filterData.add("High Priority");
        filterData.add("No Grade");
        filterData.add("Absences");
        filterData.add("Failed");
        // Initializing an ArrayAdapter
        final ArrayAdapter<String> filterAdapter = new ArrayAdapter<String>(this,R.layout.zzz_one_textview,filterData){
            @Override
            public boolean isEnabled(int position){
                if(position == 0) {
                    return false;
                }
                else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0) {
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        filterAdapter.setDropDownViewResource(R.layout.zzz_one_textview);
        filterNotif.setAdapter(filterAdapter);
        filterNotif.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                notifFilterPosition = i;
                if(i == 0){
                    refreshData();
                } else if(i == 1){
                    db.sortNotificationByPriority(db.loginID.get(0), "LP", notifFilterSubject);
                    notifFilterBy = "LP";
                    refreshFilterData();
                } else if(i == 2){
                    db.sortNotificationByPriority(db.loginID.get(0), "MP", notifFilterSubject);
                    notifFilterBy = "MP";
                    refreshFilterData();
                } else if(i == 3){
                    db.sortNotificationByPriority(db.loginID.get(0), "HP", notifFilterSubject);
                    notifFilterBy = "HP";
                    refreshFilterData();
                } else if(i == 4){
                    db.sortNotificationByPriority(db.loginID.get(0), "NG", notifFilterSubject);
                    notifFilterBy = "NG";
                    refreshFilterData();
                } else if(i == 5){
                    db.sortNotificationByGrade(db.loginID.get(0), "absent", notifFilterSubject);
                    notifFilterBy = "absent";
                    refreshFilterData();
                } else if(i == 6){
                    db.sortNotificationByGrade(db.loginID.get(0), "failed", notifFilterSubject);
                    notifFilterBy = "failed";
                    refreshFilterData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        AlertDialog.Builder notif = new AlertDialog.Builder(this);
        notificationDialog = notif.create();
        notificationDialog.setView(layout);
        notificationDialog.show();
    }

    public boolean hideShowAddEditCategory(String subject){
        boolean isHaveGrade = false;
        db.getStudent(db.loginID.get(0), subject);
        for(int i=0;i < db.student.size();i++){
            db.getCategory(db.loginID.get(0), subject);
            for(int x=0;x<db.category.size();x++) {
                db.getPercentage(db.loginID.get(0), db.category_id.get(x));
                db.getGrade("Prelim", db.studentid.get(i), subject, db.category_id.get(x));
                if (db.grade.size() > 0) {
                    isHaveGrade = true;
                }
            }
        }
        return isHaveGrade;
    }

    public void refreshFilterData(){
        if(db.notificationName.size() > 0){
            notifList.setAdapter(new NotificationAdapter());
        } else {
            ArrayList<String> noData = new ArrayList<>();
            noData.add("No Data Found");
            notifList.setAdapter(new ArrayAdapter<>(this, R.layout.zzz_one_textview, noData));
        }
    }

    public void refreshData(){
        db.getNotification(db.loginID.get(0));
        if(db.notificationName.size() > 0) {
            notifList.setAdapter(new NotificationAdapter());
        } else {
            ArrayList<String> noData = new ArrayList<>();
            noData.add("No new Notification");
            notifList.setAdapter(new ArrayAdapter<>(this, R.layout.zzz_one_textview, noData));
        }
        refreshNotificationText();
    }

    //to view notification
    private class NotificationAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if(db.notificationName != null && db.notificationName.size() != 0){
                return db.notificationName.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return db.notificationName.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View contextView, ViewGroup viewGroup) {
            final NotificationAdapterViewHolder holder;
            if(contextView == null){
                holder = new NotificationAdapterViewHolder();
                LayoutInflater inflater = MainActivity.this.getLayoutInflater();
                contextView = inflater.inflate(R.layout.data_notif, null);
                holder.card = (CardView) contextView.findViewById(R.id.cardView);
                holder.color = (CardView) contextView.findViewById(R.id.bgcolor);
                holder.type = (TextView) contextView.findViewById(R.id.type);
                holder.name = (TextView) contextView.findViewById(R.id.notifName);
                holder.desc = (TextView) contextView.findViewById(R.id.notifDes);
                holder.card = (CardView) contextView.findViewById(R.id.cardView);
                holder.container = (LinearLayout) contextView.findViewById(R.id.container);
                contextView.setTag(holder);
            }else{
                holder = (NotificationAdapterViewHolder)  contextView.getTag();
            }
            holder.ref = position;
            holder.id = db.notificationId.get(position);
            holder.type.setText(db.notificationType.get(position));
            holder.name.setText(db.notificationName.get(position));
            holder.desc.setText("Subject: "+db.notificationSubjectCode.get(position)+"\n"+db.notificationDesc.get(position));
            if(db.notificationType.get(position).equals("HP")){
                holder.color.setCardBackgroundColor(Color.RED);
            } else if(db.notificationType.get(position).equals("MP")){
                holder.color.setCardBackgroundColor(Color.DKGRAY);
            } else if(db.notificationType.get(position).equals("LP")){
                holder.color.setCardBackgroundColor(Color.BLUE);
            } else if(db.notificationType.get(position).equals("NG")){
                holder.color.setCardBackgroundColor(Color.GRAY);
            }
            if(db.notificationStatus.get(position).equals("UNREAD")){
                holder.card.setBackgroundColor(Color.LTGRAY);
            } else {
                holder.card.setBackgroundColor(Color.WHITE);
            }

            holder.card.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this){
                public void onSwipeLeft(){
                    float gg = holder.card.getWidth();
                    gg = -gg;
                    Animation animation = new TranslateAnimation(0,gg,0,0);
                    animation.setDuration(500);
                    holder.card.startAnimation(animation);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(400);
                                db.deleteNotification(holder.id);
                                db.getNotification(db.loginID.get(0));
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        notifList.setAdapter(new NotificationAdapter());
                                        Snackbar snackbar = Snackbar.make(notifSnackBar, "Notification deleted", Snackbar.LENGTH_LONG);
                                        String color = getResources().getString(0+R.color.headerColor);
                                        View snackBarView = snackbar.getView();
                                        snackBarView.setBackgroundColor(Color.parseColor(String.valueOf(color)));
                                        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
                                        textView.setTextColor(Color.WHITE);
                                        textView.setTextSize(18);
                                        snackbar.show();
                                    }
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                public void onClick(){
                    Intent intent = new Intent(MainActivity.this, Individual_Statistic.class);
                    intent.putExtra("studentid", db.notificationStudentId.get(position));
                    intent.putExtra("name",holder.name.getText().toString());
                    intent.putExtra("subject",db.notificationSubjectCode.get(position));
                    intent.putExtra("username", user);
                    intent.putExtra("password", pass);
                    if(db.notificationType.get(position).equals("HP")){
                        intent.putExtra("isCriticalGrade", true);
                    } else {
                        intent.putExtra("isCriticalGrade", false);
                    }
                    if(db.notificationDesc.get(position).contains("Prelim")){
                        intent.putExtra("term", "Prelim");
                    } else if(db.notificationDesc.get(position).contains("Midterm")){
                        intent.putExtra("term", "Midterm");
                    } else {
                        intent.putExtra("term", "Finals");
                    }
                    startActivity(intent);
                    db.updateNotification(db.loginID.get(0), holder.id);
                    notificationDialog.dismiss();
                    startService();
                    refreshNotificationText();
                }
            });
            return contextView;
        }
    }
    public class NotificationAdapterViewHolder {
        int id;
        TextView name, desc, type;
        CardView card, color;
        LinearLayout container;
        int ref;
    }

    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener (Context ctx){
            gestureDetector = new GestureDetector(ctx, new GestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 50;
            private static final int SWIPE_VELOCITY_THRESHOLD = 50;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent e){
                onClick();
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                            result = true;
                        }
                    }
                    else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                        result = true;
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public void onClick(){

        }

        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

        public void onSwipeTop() {
        }

        public void onSwipeBottom() {
        }
    }

    Runnable CheckDatabase = new Runnable() {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            while(true) {
                db.getSettings(db.loginID.get(0));
                if(db.settingsAutoDownload.get(0) == 1){
                    isRunning = true;
                } else {
                    isRunning = false;
                }
                if(isNetworkAvailable() && isRunning) {
                    try {
                        String app_sync_date = "";
                        db.getSyncDate(db.loginID.get(0));
                        if(db.syncDate.size() > 0){
                            app_sync_date = db.syncDate.get(0);
                        } else {
                            app_sync_date = "NoDate";
                        }
                        URL url = new URL(mysqlConnectionURI.DownloadData+"?prof_id="+db.loginID.get(0));
                        HttpURLConnection con = (HttpURLConnection) url.openConnection();
                        StringBuilder sb = new StringBuilder();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                        String json;
                        while ((json = bufferedReader.readLine()) != null) {
                            sb.append(json);
                        }

                        JSONObject jsonObject = new JSONObject(sb.toString());

                        //check if php result sync date is not empty then save
                        if (!jsonObject.getString("date").equals("")) {
                            db.getSyncDate(db.loginID.get(0));
                            if (!db.syncDate.get(0).isEmpty()) {
                                if (!db.syncDate.get(0).equals(jsonObject.getString("date"))){
                                    //use if may data na sa server and wla pa sa app
                                    //passing grade
                                    if(jsonObject.has("passingGrade")){
                                        db.truncatePassingGrade(db.loginID.get(0));
                                        db.insertPassingGrade(db.loginID.get(0), jsonObject.getInt("passingGrade"));
                                        System.out.println("Passing Grade Save");
                                    }

                                    //Term
                                    if(jsonObject.has("qTerm_sc")){
                                        db.truncateTerm(db.loginID.get(0));
                                        JSONArray qTerm_sc = jsonObject.getJSONArray("qTerm_sc");
                                        JSONArray qTerm_ct = jsonObject.getJSONArray("qTerm_ct");
                                        for(int i=0;i<qTerm_sc.length();i++){
                                            db.setTerm(db.loginID.get(0), qTerm_sc.get(i).toString(), qTerm_ct.getInt(i));
                                            System.out.println("Term: "+i+"/"+qTerm_sc.length());
                                        }
                                    }

                                    //Category
                                    if(jsonObject.has("qCat_id")){
                                        db.truncateCategory(db.loginID.get(0));
                                        JSONArray qCat_id = jsonObject.getJSONArray("qCat_id");
                                        JSONArray qCat_sc = jsonObject.getJSONArray("qCat_sc");
                                        JSONArray qCat_name = jsonObject.getJSONArray("qCat_name");
                                        for(int i=0;i<qCat_id.length();i++){
                                            db.restoreCategory(db.loginID.get(0), Integer.parseInt(qCat_id.get(i).toString()), qCat_sc.get(i).toString(), qCat_name.get(i).toString());
                                            System.out.println("Category: "+i+"/"+qCat_sc.length());
                                        }
                                    }

                                    //Raw Grade
                                    if(jsonObject.has("qRaw_gradeid")){
                                        db.truncateRawGrade(db.loginID.get(0));
                                        JSONArray qRaw_gradeid = jsonObject.getJSONArray("qRaw_gradeid");
                                        JSONArray qRaw_rs = jsonObject.getJSONArray("qRaw_rs");
                                        for(int i=0;i<qRaw_gradeid.length();i++){
                                            db.setRawGrade(db.loginID.get(0), Integer.parseInt(qRaw_gradeid.get(i).toString()), qRaw_rs.get(i).toString());
                                            System.out.println("Raw Score: "+i+"/"+qRaw_gradeid.length());
                                        }
                                    }

                                    //Perentage
                                    if(jsonObject.has("qGp_catid")){
                                        db.truncatePercentage(db.loginID.get(0));
                                        JSONArray qGp_catid = jsonObject.getJSONArray("qGp_catid");
                                        JSONArray qGp_percent = jsonObject.getJSONArray("qGp_percent");
                                        for(int i=0;i<qGp_catid.length();i++){
                                            db.setPercentage(db.loginID.get(0), Integer.parseInt(qGp_catid.get(i).toString()), Float.parseFloat(qGp_percent.get(i).toString()));
                                            System.out.println("Percentage: "+i+"/"+qGp_catid.length());
                                        }
                                    }

                                    //Prelim grade
                                    if(jsonObject.has("qp_studentno")){
                                        db.truncateGrade("Prelim",db.loginID.get(0));
                                        JSONArray qp_studentno = jsonObject.getJSONArray("qp_studentno");
                                        JSONArray qp_sc = jsonObject.getJSONArray("qp_sc");
                                        JSONArray qp_catid = jsonObject.getJSONArray("qp_catid");
                                        JSONArray qp_caption = jsonObject.getJSONArray("qp_caption");
                                        JSONArray qp_date = jsonObject.getJSONArray("qp_date");
                                        JSONArray qp_grade = jsonObject.getJSONArray("qp_grade");
                                        for(int i=0;i<qp_studentno.length();i++){
                                            db.setGrade("Prelim", db.loginID.get(0), qp_studentno.get(i).toString(), qp_sc.get(i).toString(), Integer.parseInt(qp_catid.get(i).toString()), qp_caption.get(i).toString(), qp_date.get(i).toString() ,Float.parseFloat(qp_grade.get(i).toString()));
                                            System.out.println("Prelim Grade: "+i+"/"+qp_studentno.length());
                                        }
                                    }

                                    //Midterm grade
                                    if(jsonObject.has("qm_studentno")){
                                        db.truncateGrade("Midterm",db.loginID.get(0));
                                        JSONArray qm_studentno = jsonObject.getJSONArray("qm_studentno");
                                        JSONArray qm_sc = jsonObject.getJSONArray("qm_sc");
                                        JSONArray qm_catid = jsonObject.getJSONArray("qm_catid");
                                        JSONArray qm_caption = jsonObject.getJSONArray("qm_caption");
                                        JSONArray qm_date = jsonObject.getJSONArray("qm_date");
                                        JSONArray qm_grade = jsonObject.getJSONArray("qm_grade");
                                        for(int i=0;i<qm_studentno.length();i++){
                                            db.setGrade("Midterm", db.loginID.get(0), qm_studentno.get(i).toString(), qm_sc.get(i).toString(), Integer.parseInt(qm_catid.get(i).toString()), qm_caption.get(i).toString(), qm_date.get(i).toString() ,Float.parseFloat(qm_grade.get(i).toString()));
                                            System.out.println("Midterm Grade: "+i+"/"+qm_studentno.length());
                                        }
                                    }

                                    //Finals grade
                                    if(jsonObject.has("qf_studentno")){
                                        db.truncateGrade("Finals",db.loginID.get(0));
                                        JSONArray qf_studentno = jsonObject.getJSONArray("qf_studentno");
                                        JSONArray qf_sc = jsonObject.getJSONArray("qf_sc");
                                        JSONArray qf_catid = jsonObject.getJSONArray("qf_catid");
                                        JSONArray qf_caption = jsonObject.getJSONArray("qf_caption");
                                        JSONArray qf_date = jsonObject.getJSONArray("qf_date");
                                        JSONArray qf_grade = jsonObject.getJSONArray("qf_grade");
                                        for(int i=0;i<qf_studentno.length();i++){
                                            db.setGrade("Finals", db.loginID.get(0), qf_studentno.get(i).toString(), qf_sc.get(i).toString(), Integer.parseInt(qf_catid.get(i).toString()), qf_caption.get(i).toString(), qf_date.get(i).toString() ,Float.parseFloat(qf_grade.get(i).toString()));
                                            System.out.println("Finals Grade: "+i+"/"+qf_studentno.length());
                                        }
                                    }

                                    //Notification
                                    if(jsonObject.has("notif_studentno")){
                                        db.truncateNotification(db.loginID.get(0));
                                        JSONArray notif_studentno = jsonObject.getJSONArray("notif_studentno");
                                        JSONArray notif_name = jsonObject.getJSONArray("notif_name");
                                        JSONArray notif_sc = jsonObject.getJSONArray("notif_sc");
                                        JSONArray notif_description = jsonObject.getJSONArray("notif_description");
                                        JSONArray notif_type = jsonObject.getJSONArray("notif_type");
                                        JSONArray notif_status = jsonObject.getJSONArray("notif_status");
                                        JSONArray notif_tag = jsonObject.getJSONArray("notif_tag");
                                        for(int i=0;i<notif_studentno.length();i++){
                                            db.setNotification(db.loginID.get(0), notif_studentno.get(i).toString(), notif_name.get(i).toString(), notif_sc.get(i).toString(), notif_description.get(i).toString(), notif_type.get(i).toString(), notif_status.get(i).toString(), notif_tag.get(i).toString());
                                            System.out.println("Notification: "+i+"/"+notif_studentno.length());
                                        }
                                    }

                                    //last update
                                    if(jsonObject.has("lu_date")){
                                        db.truncateUpdateDate(db.loginID.get(0));
                                        db.setUpdatedDate(db.loginID.get(0), jsonObject.getString("lu_date"));
                                        System.out.println("Updated Date Save");
                                    }

                                    //grade to submit
                                    if(jsonObject.has("gts_studentno")){
                                        db.truncateGrateToSubmit(db.loginID.get(0));
                                        JSONArray gts_studentno = jsonObject.getJSONArray("gts_studentno");
                                        JSONArray gts_sc = jsonObject.getJSONArray("gts_sc");
                                        JSONArray gts_prelim = jsonObject.getJSONArray("gts_prelim");
                                        JSONArray gts_midterm = jsonObject.getJSONArray("gts_midterm");
                                        JSONArray gts_finals = jsonObject.getJSONArray("gts_finals");
                                        JSONArray gts_average = jsonObject.getJSONArray("gts_average");
                                        JSONArray gts_remarks = jsonObject.getJSONArray("gts_remarks");
                                        for(int i=0;i<gts_studentno.length();i++){
                                            db.restoreAllGrade(db.loginID.get(0), gts_studentno.get(i).toString(), gts_sc.get(i).toString(), Float.parseFloat(gts_prelim.get(i).toString()), Float.parseFloat(gts_midterm.get(i).toString()), Float.parseFloat(gts_finals.get(i).toString()), Float.parseFloat(gts_average.get(i).toString()), gts_remarks.get(i).toString());
                                            System.out.println("Grade To Submit: "+i+"/"+gts_studentno.length());
                                        }
                                    }

                                    //archive
                                    if(jsonObject.has("archive_studentno")){
                                        db.truncateAllArchive(db.loginID.get(0));
                                        JSONArray archive_studentno = jsonObject.getJSONArray("archive_studentno");
                                        JSONArray archive_name = jsonObject.getJSONArray("archive_name");
                                        JSONArray archive_year = jsonObject.getJSONArray("archive_year");
                                        JSONArray archive_sc = jsonObject.getJSONArray("archive_sc");
                                        JSONArray archive_sd = jsonObject.getJSONArray("archive_sd");
                                        JSONArray archive_ay = jsonObject.getJSONArray("archive_ay");
                                        JSONArray archive_sem = jsonObject.getJSONArray("archive_sem");
                                        JSONArray archive_prelim = jsonObject.getJSONArray("archive_prelim");
                                        JSONArray archive_midterm = jsonObject.getJSONArray("archive_midterm");
                                        JSONArray archive_finals = jsonObject.getJSONArray("archive_finals");
                                        JSONArray archive_average = jsonObject.getJSONArray("archive_average");
                                        JSONArray archive_equivalent = jsonObject.getJSONArray("archive_equivalent");
                                        for(int i=0;i<archive_studentno.length();i++){
                                            db.setArchive(archive_studentno.get(i).toString(), archive_name.get(i).toString(), archive_year.get(i).toString(),
                                                    db.loginID.get(0), archive_sc.get(i).toString(), archive_sd.get(i).toString(), archive_ay.get(i).toString(),
                                                    archive_sem.get(i).toString(), Float.parseFloat(archive_prelim.get(i).toString()),
                                                    Float.parseFloat(archive_midterm.get(i).toString()), Float.parseFloat(archive_finals.get(i).toString()),
                                                    Float.parseFloat(archive_average.get(i).toString()), Float.parseFloat(archive_equivalent.get(i).toString()));
                                            System.out.println("Archive: "+i+"/"+archive_studentno.length());
                                        }
                                    }

                                    //user info
                                    if(jsonObject.has("info_username")){
                                        db.updateLoginAccount(db.loginID.get(0), jsonObject.getString("info_username"), jsonObject.getString("info_password"));
                                        System.out.println("User Account saved");
                                    }

                                    //sync date
                                    if(jsonObject.has("sync_date")){
                                        db.getSyncDate(db.loginID.get(0));
                                        if(db.syncDate.size() == 0){
                                            db.setSyncDate(db.loginID.get(0), "sync_date", jsonObject.getString("sync_date"));
                                        } else {
                                            db.updateSyncDate(db.loginID.get(0), "sync_date", jsonObject.getString("sync_date"));
                                        }
                                        System.out.println("Sync Date Save");
                                    }

                                    Intent intent = getIntent();
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    finish();
                                    startActivity(intent);
                                }
                            } else {
                                //use if may data na sa server and wla pa sa app
                                //passing grade
                                if(jsonObject.has("passingGrade")){
                                    db.truncatePassingGrade(db.loginID.get(0));
                                    db.insertPassingGrade(db.loginID.get(0), jsonObject.getInt("passingGrade"));
                                    System.out.println("Passing Grade Save");
                                }

                                //Term
                                if(jsonObject.has("qTerm_sc")){
                                    db.truncateTerm(db.loginID.get(0));
                                    JSONArray qTerm_sc = jsonObject.getJSONArray("qTerm_sc");
                                    JSONArray qTerm_ct = jsonObject.getJSONArray("qTerm_ct");
                                    for(int i=0;i<qTerm_sc.length();i++){
                                        db.setTerm(db.loginID.get(0), qTerm_sc.get(i).toString(), qTerm_ct.getInt(i));
                                        System.out.println("Term: "+i+"/"+qTerm_sc.length());
                                    }
                                }

                                //Category
                                if(jsonObject.has("qCat_id")){
                                    db.truncateCategory(db.loginID.get(0));
                                    JSONArray qCat_id = jsonObject.getJSONArray("qCat_id");
                                    JSONArray qCat_sc = jsonObject.getJSONArray("qCat_sc");
                                    JSONArray qCat_name = jsonObject.getJSONArray("qCat_name");
                                    for(int i=0;i<qCat_id.length();i++){
                                        db.restoreCategory(db.loginID.get(0), Integer.parseInt(qCat_id.get(i).toString()), qCat_sc.get(i).toString(), qCat_name.get(i).toString());
                                        System.out.println("Category: "+i+"/"+qCat_sc.length());
                                    }
                                }

                                //Raw Grade
                                if(jsonObject.has("qRaw_gradeid")){
                                    db.truncateRawGrade(db.loginID.get(0));
                                    JSONArray qRaw_gradeid = jsonObject.getJSONArray("qRaw_gradeid");
                                    JSONArray qRaw_rs = jsonObject.getJSONArray("qRaw_rs");
                                    for(int i=0;i<qRaw_gradeid.length();i++){
                                        db.setRawGrade(db.loginID.get(0), Integer.parseInt(qRaw_gradeid.get(i).toString()), qRaw_rs.get(i).toString());
                                        System.out.println("Raw Score: "+i+"/"+qRaw_gradeid.length());
                                    }
                                }

                                //Perentage
                                if(jsonObject.has("qGp_catid")){
                                    db.truncatePercentage(db.loginID.get(0));
                                    JSONArray qGp_catid = jsonObject.getJSONArray("qGp_catid");
                                    JSONArray qGp_percent = jsonObject.getJSONArray("qGp_percent");
                                    for(int i=0;i<qGp_catid.length();i++){
                                        db.setPercentage(db.loginID.get(0), Integer.parseInt(qGp_catid.get(i).toString()), Float.parseFloat(qGp_percent.get(i).toString()));
                                        System.out.println("Percentage: "+i+"/"+qGp_catid.length());
                                    }
                                }

                                //Prelim grade
                                if(jsonObject.has("qp_studentno")){
                                    db.truncateGrade("Prelim",db.loginID.get(0));
                                    JSONArray qp_studentno = jsonObject.getJSONArray("qp_studentno");
                                    JSONArray qp_sc = jsonObject.getJSONArray("qp_sc");
                                    JSONArray qp_catid = jsonObject.getJSONArray("qp_catid");
                                    JSONArray qp_caption = jsonObject.getJSONArray("qp_caption");
                                    JSONArray qp_date = jsonObject.getJSONArray("qp_date");
                                    JSONArray qp_grade = jsonObject.getJSONArray("qp_grade");
                                    for(int i=0;i<qp_studentno.length();i++){
                                        db.setGrade("Prelim", db.loginID.get(0), qp_studentno.get(i).toString(), qp_sc.get(i).toString(), Integer.parseInt(qp_catid.get(i).toString()), qp_caption.get(i).toString(), qp_date.get(i).toString() ,Float.parseFloat(qp_grade.get(i).toString()));
                                        System.out.println("Prelim Grade: "+i+"/"+qp_studentno.length());
                                    }
                                }

                                //Midterm grade
                                if(jsonObject.has("qm_studentno")){
                                    db.truncateGrade("Midterm",db.loginID.get(0));
                                    JSONArray qm_studentno = jsonObject.getJSONArray("qm_studentno");
                                    JSONArray qm_sc = jsonObject.getJSONArray("qm_sc");
                                    JSONArray qm_catid = jsonObject.getJSONArray("qm_catid");
                                    JSONArray qm_caption = jsonObject.getJSONArray("qm_caption");
                                    JSONArray qm_date = jsonObject.getJSONArray("qm_date");
                                    JSONArray qm_grade = jsonObject.getJSONArray("qm_grade");
                                    for(int i=0;i<qm_studentno.length();i++){
                                        db.setGrade("Midterm", db.loginID.get(0), qm_studentno.get(i).toString(), qm_sc.get(i).toString(), Integer.parseInt(qm_catid.get(i).toString()), qm_caption.get(i).toString(), qm_date.get(i).toString() ,Float.parseFloat(qm_grade.get(i).toString()));
                                        System.out.println("Midterm Grade: "+i+"/"+qm_studentno.length());
                                    }
                                }

                                //Finals grade
                                if(jsonObject.has("qf_studentno")){
                                    db.truncateGrade("Finals",db.loginID.get(0));
                                    JSONArray qf_studentno = jsonObject.getJSONArray("qf_studentno");
                                    JSONArray qf_sc = jsonObject.getJSONArray("qf_sc");
                                    JSONArray qf_catid = jsonObject.getJSONArray("qf_catid");
                                    JSONArray qf_caption = jsonObject.getJSONArray("qf_caption");
                                    JSONArray qf_date = jsonObject.getJSONArray("qf_date");
                                    JSONArray qf_grade = jsonObject.getJSONArray("qf_grade");
                                    for(int i=0;i<qf_studentno.length();i++){
                                        db.setGrade("Finals", db.loginID.get(0), qf_studentno.get(i).toString(), qf_sc.get(i).toString(), Integer.parseInt(qf_catid.get(i).toString()), qf_caption.get(i).toString(), qf_date.get(i).toString() ,Float.parseFloat(qf_grade.get(i).toString()));
                                        System.out.println("Finals Grade: "+i+"/"+qf_studentno.length());
                                    }
                                }

                                //Notification
                                if(jsonObject.has("notif_studentno")){
                                    db.truncateNotification(db.loginID.get(0));
                                    JSONArray notif_studentno = jsonObject.getJSONArray("notif_studentno");
                                    JSONArray notif_name = jsonObject.getJSONArray("notif_name");
                                    JSONArray notif_sc = jsonObject.getJSONArray("notif_sc");
                                    JSONArray notif_description = jsonObject.getJSONArray("notif_description");
                                    JSONArray notif_type = jsonObject.getJSONArray("notif_type");
                                    JSONArray notif_status = jsonObject.getJSONArray("notif_status");
                                    JSONArray notif_tag = jsonObject.getJSONArray("notif_tag");
                                    for(int i=0;i<notif_studentno.length();i++){
                                        db.setNotification(db.loginID.get(0), notif_studentno.get(i).toString(), notif_name.get(i).toString(), notif_sc.get(i).toString(), notif_description.get(i).toString(), notif_type.get(i).toString(), notif_status.get(i).toString(), notif_tag.get(i).toString());
                                        System.out.println("Notification: "+i+"/"+notif_studentno.length());
                                    }
                                }

                                //last update
                                if(jsonObject.has("lu_date")){
                                    db.truncateUpdateDate(db.loginID.get(0));
                                    db.setUpdatedDate(db.loginID.get(0), jsonObject.getString("lu_date"));
                                    System.out.println("Updated Date Save");
                                }

                                //grade to submit
                                if(jsonObject.has("gts_studentno")){
                                    db.truncateGrateToSubmit(db.loginID.get(0));
                                    JSONArray gts_studentno = jsonObject.getJSONArray("gts_studentno");
                                    JSONArray gts_sc = jsonObject.getJSONArray("gts_sc");
                                    JSONArray gts_prelim = jsonObject.getJSONArray("gts_prelim");
                                    JSONArray gts_midterm = jsonObject.getJSONArray("gts_midterm");
                                    JSONArray gts_finals = jsonObject.getJSONArray("gts_finals");
                                    JSONArray gts_average = jsonObject.getJSONArray("gts_average");
                                    JSONArray gts_remarks = jsonObject.getJSONArray("gts_remarks");
                                    for(int i=0;i<gts_studentno.length();i++){
                                        db.restoreAllGrade(db.loginID.get(0), gts_studentno.get(i).toString(), gts_sc.get(i).toString(), Float.parseFloat(gts_prelim.get(i).toString()), Float.parseFloat(gts_midterm.get(i).toString()), Float.parseFloat(gts_finals.get(i).toString()), Float.parseFloat(gts_average.get(i).toString()), gts_remarks.get(i).toString());
                                        System.out.println("Grade To Submit: "+i+"/"+gts_studentno.length());
                                    }
                                }

                                //archive
                                if(jsonObject.has("archive_studentno")){
                                    db.truncateAllArchive(db.loginID.get(0));
                                    JSONArray archive_studentno = jsonObject.getJSONArray("archive_studentno");
                                    JSONArray archive_name = jsonObject.getJSONArray("archive_name");
                                    JSONArray archive_year = jsonObject.getJSONArray("archive_year");
                                    JSONArray archive_sc = jsonObject.getJSONArray("archive_sc");
                                    JSONArray archive_sd = jsonObject.getJSONArray("archive_sd");
                                    JSONArray archive_ay = jsonObject.getJSONArray("archive_ay");
                                    JSONArray archive_sem = jsonObject.getJSONArray("archive_sem");
                                    JSONArray archive_prelim = jsonObject.getJSONArray("archive_prelim");
                                    JSONArray archive_midterm = jsonObject.getJSONArray("archive_midterm");
                                    JSONArray archive_finals = jsonObject.getJSONArray("archive_finals");
                                    JSONArray archive_average = jsonObject.getJSONArray("archive_average");
                                    JSONArray archive_equivalent = jsonObject.getJSONArray("archive_equivalent");
                                    for(int i=0;i<archive_studentno.length();i++){
                                        db.setArchive(archive_studentno.get(i).toString(), archive_name.get(i).toString(), archive_year.get(i).toString(),
                                                db.loginID.get(0), archive_sc.get(i).toString(), archive_sd.get(i).toString(), archive_ay.get(i).toString(),
                                                archive_sem.get(i).toString(), Float.parseFloat(archive_prelim.get(i).toString()),
                                                Float.parseFloat(archive_midterm.get(i).toString()), Float.parseFloat(archive_finals.get(i).toString()),
                                                Float.parseFloat(archive_average.get(i).toString()), Float.parseFloat(archive_equivalent.get(i).toString()));
                                        System.out.println("Archive: "+i+"/"+archive_studentno.length());
                                    }
                                }

                                //user info
                                if(jsonObject.has("info_username")){
                                    db.updateLoginAccount(db.loginID.get(0), jsonObject.getString("info_username"), jsonObject.getString("info_password"));
                                    System.out.println("User Account saved");
                                }

                                //sync date
                                if(jsonObject.has("sync_date")){
                                    db.getSyncDate(db.loginID.get(0));
                                    if(db.syncDate.size() == 0){
                                        db.setSyncDate(db.loginID.get(0), "sync_date", jsonObject.getString("sync_date"));
                                    } else {
                                        db.updateSyncDate(db.loginID.get(0), "sync_date", jsonObject.getString("sync_date"));
                                    }
                                    System.out.println("Sync Date Save");
                                }

                                Intent intent = getIntent();
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                finish();
                                startActivity(intent);
                            }
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Logout");
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(MainActivity.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alertDialog.show();
    }

    public void startService(){
        Intent alarmIntent = new Intent(MainActivity.this, ReceiverCall.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, 0);

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000, pendingIntent);
        System.out.println("loginID size: "+db.loginID.size());
    }

    public static void Tooltip(View v, Toast toast) {
        int xOffset = 0;
        int yOffset = 0;
        Rect gvr = new Rect();

        View parent = (View) v.getParent();
        int parentHeight = parent.getHeight();

        if (v.getGlobalVisibleRect(gvr)) {
            View root = v.getRootView();

            int halfWidth = root.getRight() / 2;
            int halfHeight = root.getBottom() / 2;

            int parentCenterX = ((gvr.right - gvr.left) / 2) + gvr.left;

            int parentCenterY = ((gvr.bottom - gvr.top) / 2) + gvr.top;

            if (parentCenterY <= halfHeight) {
                yOffset = -(halfHeight - parentCenterY) - parentHeight;
            }
            else {
                yOffset = (parentCenterY - halfHeight) - parentHeight;
            }

            if (parentCenterX < halfWidth) {
                xOffset = -(halfWidth - parentCenterX);
            }

            if (parentCenterX >= halfWidth) {
                xOffset = parentCenterX - halfWidth;
            }
        }

        toast.setGravity(Gravity.CENTER, xOffset, yOffset);
    }
}
