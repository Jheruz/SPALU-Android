package com.laguna.university.studentperformanceanalytics;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DashboardFragment extends Fragment {

    SQLiteDBcontroller db;

    CardView setting, grades, summary, analysis, archive, help;
    TextView title, updatedDate, semester;

    AlertDialog settingsDialog;
    ProgressDialog progress;

    String currentString = "";

    int swipeIndex = 0;
    String header[];
    int imageResource[];
    String footer[];
    float textSize = 0f;
    int size = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, parent, false);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        db  = new SQLiteDBcontroller(getActivity());
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        setting = (CardView) view.findViewById(R.id.settings);
        grades = (CardView) view.findViewById(R.id.grades);
        summary = (CardView) view.findViewById(R.id.summary);
        analysis = (CardView) view.findViewById(R.id.analysis);
        archive = (CardView) view.findViewById(R.id.archive);
        help = (CardView) view.findViewById(R.id.help);
        title = (TextView) view.findViewById(R.id.title);
        updatedDate = (TextView) view.findViewById(R.id.lastUpdate);
        semester = (TextView) view.findViewById(R.id.semester);

        db.login(((MainActivity)getActivity()).user, ((MainActivity)getActivity()).pass);
        db.getUpdateDate(db.loginID.get(0));
        title.setText(db.loginName.get(0));
        semester.setText(((MainActivity)getActivity()).sem + " Semester - A.Y " + ((MainActivity)getActivity()).completeAY);
        if(db.updateDate.size() > 0){
            updatedDate.setText("Last Update: "+db.updateDate.get(0));
        } else {
            updatedDate.setText("Last Update: None yet");
        }

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSetting();
            }
        });

        grades.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).moveTab();
            }
        });

        summary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), OverallSummary.class);
                intent.putExtra("username", ((MainActivity)getActivity()).user);
                intent.putExtra("password", ((MainActivity)getActivity()).pass);
                startActivity(intent);
            }
        });

        analysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Analysis.class);
                intent.putExtra("username", ((MainActivity)getActivity()).user);
                intent.putExtra("password", ((MainActivity)getActivity()).pass);
                startActivity(intent);
            }
        });

        archive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), Archive.class);
                intent.putExtra("username", ((MainActivity)getActivity()).user);
                intent.putExtra("password", ((MainActivity)getActivity()).pass);
                startActivity(intent);
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHelp();
            }
        });
    }

    public void showHelp(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        final AlertDialog helpDialog = dialog.create();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.top_student, null);
        LinearLayout top10 = (LinearLayout) dialoglayout.findViewById(R.id.top10);
        LinearLayout help = (LinearLayout) dialoglayout.findViewById(R.id.help);
        TextView title = (TextView) dialoglayout.findViewById(R.id.title);
        CardView close = (CardView) dialoglayout.findViewById(R.id.close);
        ListView list = (ListView) dialoglayout.findViewById(R.id.helpList);
        ArrayList<String> helpListData = new ArrayList<>();
        top10.setVisibility(View.GONE);
        help.setVisibility(View.VISIBLE);
        title.setText("Help Section");
        helpListData.add("1. Introduction");
        helpListData.add("2. Getting Started");
        helpListData.add("3. Sync Feature");
        helpListData.add("4. Grade Input");
        helpListData.add("5. Analysis");
        helpListData.add("6. Using Search");
        helpListData.add("7. Adding Categories");
        helpListData.add("8. Editing Categories");
        helpListData.add("9. Adding Major Eaxm Grades");
        helpListData.add("10. Adding Attendance");
        helpListData.add("11. Section Ranking");
        helpListData.add("12. Archive");
        helpListData.add("13. My Account");
        helpListData.add("14. In-App notification Alerts");
        helpListData.add("About");
        list.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.zzz_one_textview, helpListData));
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0){
                    header = new String[size];
                    imageResource = new int[size];
                    footer = new String[size];

                    header[0] = "Introduction";
                    imageResource[0] = R.drawable.sync;
                    footer[0] = "\t\tStudent Performance Analytics Android Application is a mobile application is a modern day tool for professors in encoding, monitoring & analyzing the grades of their students. Grade submission is made easier as it could be submitted through the application.";
                    textSize = 14f;
                } else if(i == 1){
                    header = new String[size];
                    imageResource = new int[size];
                    footer = new String[size];

                    header[0] = "Getting Started";
                    imageResource[0] = R.drawable.sync;
                    footer[0] = "\t\tThe students in the class list viewable in your application are official and from the registrar. Please do not accept students not reflected in the list or else, have them report to the registrar to settle. This app can be used on the android OS and android Version 4.4+ (Kitkat).";
                    textSize = 14f;
                } else if(i == 2){
                    header = new String[size];
                    imageResource = new int[size];
                    footer = new String[size];

                    header[0] = "Sync Feature";
                    imageResource[0] = R.drawable.tut_sync;
                    footer[0] = "\t\tThe sync feature of the app is used to synchronize the mobile app with the desktop app which is used with an emulator. The user may be able to choose whether to set the sync manually or automatically. Once the automatic feature is turned on, the app will automatically save the grade on the server every time the app detects that there are new grades added to it. This may take some time due to the internet connection speed of the device being used. If desired, the user may also turn the sync feature into manual and have it synchronized with the server based on the user’s will. A date and time of when the data is last backed up will appear on the window.";
                    textSize = 12f;
                } else if(i == 3){
                    header = new String[size];
                    imageResource = new int[size];
                    footer = new String[size];

                    header[0] = "Grade Input";
                    imageResource[0] = R.drawable.tut_grade_book;
                    footer[0] = "\t\tBasically, the app was designed in such a way that is very similar to recording the grades manually with a class record book, or electronically through a digital spreadsheet.\n" +
                            "\t\tHowever, the Student Performance Analytics Application is developed as a one stop app.\n" +
                            "\t\tFrom the first process which is receiving the class list, recording the grades and evaluating them according to university standards up to submitting them to the registrar.";
                    textSize = 12f;
                } else if(i == 4){
                    header = new String[size];
                    imageResource = new int[size];
                    footer = new String[size];

                    header[0] = "Analysis";
                    imageResource[0] = R.drawable.tut_statistic;
                    footer[0] = "\t\tAnother best feature of the app is its capability to analyze data and interpret them into meaningful charts.\n" +
                            "\t\tYou will be immediately notified about the grades of students who are not doing well in their academics.\n" +
                            "\t\tNotifications are based from the passing grade standards that you will setup for the first time you will use the app.";
                    textSize = 12f;
                } else if(i == 5){
                    header = new String[size];
                    imageResource = new int[size];
                    footer = new String[size];

                    header[0] = "Using Search";
                    imageResource[0] = R.drawable.tut_search;
                    footer[0] = "\t\tThe Search feature can be used to quickly find the details of a specific student. The queries that can be typed and use are the Name, Section and Student ID.";
                    textSize = 12f;
                } else if(i == 6){
                    header = new String[size];
                    imageResource = new int[size];
                    footer = new String[size];

                    header[0] = "Adding Categories";
                    imageResource[0] = R.drawable.tut_category;
                    footer[0] = "\t\tThis button will only be shown once there are no records inserted in the gradebook as to ensure the regularity of the grading system. This enables the user to include additional categories depending to their liking as well as the percentage to the added criteria." +
                            "\nThe default category of the criteria was based on the student handbook and are as follows: Exam – 40%, Class Standing – 60% which includes Attendance, Decorum, Quizzes & Recitation.";
                    textSize = 12f;
                } else if(i == 7){
                    header = new String[size];
                    imageResource = new int[size];
                    footer = new String[size];

                    header[0] = "Editing Categories";
                    imageResource[0] = R.drawable.tut_editcategory;
                    footer[0] = "\t\tLike the add category, this button will only be displayed once there are no data in the grade book. This feature enables the user to reconfigure the default criteria as well as its percentage as needed by the subject.";
                    textSize = 12f;
                } else if(i == 8){
                    header = new String[size];
                    imageResource = new int[size];
                    footer = new String[size];

                    header[0] = "Adding Major Exam";
                    imageResource[0] = R.drawable.tut_exam;
                    footer[0] = "\t\tTo add grade for the major exam, tap the Add Button. The major exam is the app’s trigger to move the current grading period to the next. For example, once the grades for the prelim exam has been posted, the next grades that will be uploaded will automatically be allotted for the midterm period. However, grades from the previous period could still be edited and modified by the user.";
                    textSize = 12f;
                } else if(i == 9){
                    header = new String[size];
                    imageResource = new int[size];
                    footer = new String[size];

                    header[0] = "Adding Attendance";
                    imageResource[0] = R.drawable.tut_attendance;
                    footer[0] = "\t\tTo add grade for the attendance, tap the Add Button. A pop up screen will show the list of the students’ names as well as a checkbox button. To mark the individuals as present, the checkbox button must be ticked and vice versa to note them as absent. Tap save afterwards to save the record. For excused students, their marks may be notified by the professor afterwards as the data could be edited in the main screen of the attendance.";
                    textSize = 12f;
                } else if(i == 10){
                    header = new String[size];
                    imageResource = new int[size];
                    footer = new String[size];

                    header[0] = "Section Ranking";
                    imageResource[0] = R.drawable.tut_ranking;
                    footer[0] = "\t\tA feature of the app is the top 10 module wherein the app automatically finds the first 10 students who has the highest grade among the class per term. To view the details, tap the ribbon icon on the upper right of the window above the charts to look for the top 10 on the desired section and grading period.";
                    textSize = 12f;
                } else if(i == 11){
                    header = new String[size];
                    imageResource = new int[size];
                    footer = new String[size];

                    header[0] = "Archive";
                    imageResource[0] = R.drawable.tut_archive;
                    footer[0] = "\t\tThe archive feature of the app provides the user with the option to view their previously submitted grades per semester. This module is added as supporting data of the user as well as to provide the user with a copy of their previously submitted grades. Tap on the first drop down button to select the class section or subject, the second to find which grading period to view and the third to pick the semester and academic year.";
                    textSize = 12f;
                } else if(i == 12){
                    header = new String[size];
                    imageResource = new int[size];
                    footer = new String[size];

                    header[0] = "My Account";
                    imageResource[0] = R.drawable.tut_account;
                    footer[0] = "\t\tThis feature enables the user to modify the preset username and password according to desired credentials";
                    textSize = 12f;
                } else if(i == 13){
                    header = new String[size];
                    imageResource = new int[size];
                    footer = new String[size];

                    header[0] = "In-App Notification Alerts";
                    imageResource[0] = R.drawable.tut_notification;
                    footer[0] = "\t\tThe notifications feature of the app is a floating button that is always seen on the app. The number on the icon indicates the number of unread notifications in the app. The notifications can be filtered by Section and Subject. The notifications can be filtered into 3 categories: High Priority (HP), Mid Priority (MP) & Low Priority (LP).  Student entries without grades are also marked in the notification and are identified as No Grade (NG). The categories and their indicators are based on the Student’s Handbook and can further be identified with the following.";
                    textSize = 12f;
                } else if(i == 14){
                    header = new String[size];
                    imageResource = new int[size];
                    footer = new String[size];

                    header[0] = "About";
                    imageResource[0] = R.drawable.sync;
                    footer[0] = "Student Performance Analytics Android Application" +
                            "\nSharmaine Justyne R. Maglapuz" +
                            "\nsjmaglapuz@gmail.com" +
                            "\n09171045229";
                    textSize = 12f;
                }
                showWizard(header.length);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                helpDialog.dismiss();
            }
        });

        helpDialog.setView(dialoglayout);
        helpDialog.show();
    }

    public void showWizard(final int size){
        swipeIndex = 0;
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        final AlertDialog dd = dialog.create();

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View layout = inflater.inflate(R.layout.passing_grade, null);
        final TextView header = (TextView) layout.findViewById(R.id.header);
        final TextView footer = (TextView) layout.findViewById(R.id.footer);
        final ImageView image = (ImageView) layout.findViewById(R.id.image);
        final LinearLayout sliderLayout = (LinearLayout) layout.findViewById(R.id.layoutHolder);
        LinearLayout tut = (LinearLayout) layout.findViewById(R.id.tutorial_popup);
        LinearLayout passingGrade = (LinearLayout) layout.findViewById(R.id.passing_grade_layout);
        CardView done = (CardView) layout.findViewById(R.id.done);
        final CardView next = (CardView) layout.findViewById(R.id.next);
        tut.setVisibility(View.VISIBLE);
        passingGrade.setVisibility(View.GONE);

        setData(swipeIndex, header, image, footer);

        sliderLayout.setOnTouchListener(new OnSwipeTouchListener(getActivity()){
            public void onSwipeRight(){
                if(swipeIndex != 0){
                    swipeIndex--;

                    Animation animation = new TranslateAnimation(0,sliderLayout.getWidth(),0,0);
                    animation.setDuration(500);
                    sliderLayout.startAnimation(animation);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            setData(swipeIndex, header, image, footer);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
                if(swipeIndex == (size - 1)){
                    next.setVisibility(View.INVISIBLE);
                }
            }

            public void onSwipeLeft(){
                if(swipeIndex != (size-1)){
                    swipeIndex++;

                    float gg = sliderLayout.getWidth();
                    gg = -gg;
                    Animation animation = new TranslateAnimation(0,gg,0,0);
                    animation.setDuration(500);
                    sliderLayout.startAnimation(animation);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            setData(swipeIndex, header, image, footer);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
                if(swipeIndex == (size - 1)){
                    next.setVisibility(View.INVISIBLE);
                }
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dd.dismiss();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(swipeIndex != (size-1)){
                    swipeIndex++;

                    float gg = sliderLayout.getWidth();
                    gg = -gg;
                    Animation animation = new TranslateAnimation(0,gg,0,0);
                    animation.setDuration(500);
                    sliderLayout.startAnimation(animation);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            setData(swipeIndex, header, image, footer);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                }
                if(swipeIndex == (size - 1)){
                    next.setVisibility(View.INVISIBLE);
                }
            }
        });

        dd.setView(layout);
        dd.show();
    }

    public void setData(int index, TextView header, ImageView image, TextView footer){
        if(imageResource[index] == R.drawable.tut_search){
            image.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        header.setText(this.header[index]);
        if(imageResource[index] != R.drawable.sync){
            image.setVisibility(View.VISIBLE);
            image.setImageResource(imageResource[index]);
        } else {
            image.setVisibility(View.GONE);
        }
        footer.setText(this.footer[index]);
        footer.setTextSize(textSize);
    }

    public class OnSwipeTouchListener implements View.OnTouchListener {

        private final GestureDetector gestureDetector;

        public OnSwipeTouchListener (Context ctx){
            gestureDetector = new GestureDetector(ctx, new OnSwipeTouchListener.GestureListener());
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

    public void showSetting(){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.settings_layout, null);
        final CardView cancel = (CardView) dialoglayout.findViewById(R.id.close);
        final TextView displayPassingGrade = (TextView) dialoglayout.findViewById(R.id.displayPassingGrade);
        final Switch uploadSwitch = (Switch) dialoglayout.findViewById(R.id.uploadSwitch);
        final LinearLayout uploadLayout = (LinearLayout) dialoglayout.findViewById(R.id.uploadLinear);
        final TextView uploadDate = (TextView) dialoglayout.findViewById(R.id.uploadDate);
        final CardView upload = (CardView) dialoglayout.findViewById(R.id.upload);
        final Switch downloadSwitch = (Switch) dialoglayout.findViewById(R.id.downloadSwitch);
        final LinearLayout downloadLayout = (LinearLayout) dialoglayout.findViewById(R.id.downloadLinear);
        final TextView downloadDate = (TextView) dialoglayout.findViewById(R.id.downloadDate);
        final CardView download = (CardView) dialoglayout.findViewById(R.id.download);
        final CardView save = (CardView) dialoglayout.findViewById(R.id.save);
        final TextView vusername = (TextView) dialoglayout.findViewById(R.id.vusername);
        final TextView vpassword = (TextView) dialoglayout.findViewById(R.id.vpassword);
        final EditText eusername = (EditText) dialoglayout.findViewById(R.id.eusername);
        final EditText epassword = (EditText) dialoglayout.findViewById(R.id.epassword);
        final ImageView openAccount = (ImageView) dialoglayout.findViewById(R.id.openAccount);
        final ImageView closeAccount = (ImageView) dialoglayout.findViewById(R.id.closeAccount);
        final CoordinatorLayout snackBarLayout = (CoordinatorLayout) dialoglayout.findViewById(R.id.snackBarLayout);

        snackBarLayout.bringToFront();
        vusername.setVisibility(View.VISIBLE);
        vpassword.setVisibility(View.VISIBLE);
        eusername.setVisibility(View.GONE);
        epassword.setVisibility(View.GONE);
        closeAccount.setVisibility(View.GONE);
        openAccount.setVisibility(View.VISIBLE);
        db.getLoginAccount(db.loginID.get(0));
        String temp = "";
        for(int i=0;i<db.password.get(0).length();i++){
            temp += "*";
        }
        vusername.setText(db.username.get(0));
        vpassword.setText(temp);
        eusername.setText(db.username.get(0));
        epassword.setText(db.password.get(0));

        db.login(((MainActivity)getActivity()).user, ((MainActivity)getActivity()).pass);
        db.getPassingGrade(db.loginID.get(0));
        db.getSyncDate(db.loginID.get(0));
        db.getSettings(db.loginID.get(0));
        displayPassingGrade.setText(db.passingGradeValue.get(0).toString());
        if(db.settingsAutoUpload.size() > 0){
            if(db.settingsAutoUpload.get(0) == 1){
                uploadSwitch.setChecked(true);
                if(db.uploadDate.size() > 0){
                    if(!db.uploadDate.get(0).equals("")){
                        uploadDate.setTextSize(14);
                        uploadDate.setText("Last upload date: \n"+db.uploadDate.get(0));
                    } else {
                        uploadDate.setTextSize(18);
                        uploadDate.setText("Last upload date: NONE");
                    }
                } else {
                    uploadDate.setTextSize(18);
                    uploadDate.setText("Last upload date: NONE");
                }
            } else {
                uploadSwitch.setChecked(false);
                if(db.uploadDate.size() > 0){
                    if(!db.uploadDate.get(0).equals("")){
                        uploadDate.setTextSize(14);
                        uploadDate.setText("Last upload date: \n"+db.uploadDate.get(0));
                    } else {
                        uploadDate.setTextSize(18);
                        uploadDate.setText("Last upload date: NONE");
                    }
                } else {
                    uploadDate.setTextSize(18);
                    uploadDate.setText("Last upload date: NONE");
                }
                uploadLayout.setVisibility(View.VISIBLE);
            }
        }
        if(db.settingsAutoDownload.size() > 0){
            if(db.settingsAutoDownload.get(0) == 1){
                downloadSwitch.setChecked(true);
                if(db.syncDate.size() > 0){
                    if(!db.syncDate.get(0).equals("")){
                        downloadDate.setTextSize(14);
                        downloadDate.setText("Last sync date: \n"+db.syncDate.get(0));
                    } else {
                        downloadDate.setTextSize(18);
                        downloadDate.setText("Last sync date: NONE");
                    }
                } else {
                    uploadDate.setTextSize(18);
                    uploadDate.setText("Last sync date: NONE");
                }
            } else {
                downloadSwitch.setChecked(false);
                if(db.syncDate.size() > 0){
                    if(!db.syncDate.get(0).equals("")){
                        downloadDate.setTextSize(14);
                        downloadDate.setText("Last sync date: \n"+db.syncDate.get(0));
                    } else {
                        downloadDate.setTextSize(18);
                        downloadDate.setText("Last sync date: NONE");
                    }
                } else {
                    uploadDate.setTextSize(18);
                    uploadDate.setText("Last sync date: NONE");
                }
                downloadLayout.setVisibility(View.VISIBLE);
            }
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingsDialog.dismiss();
            }
        });

        uploadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    db.updateSettings(db.loginID.get(0), "auto_upload", 1);
                    db.getSettings(db.loginID.get(0));
                    uploadLayout.setVisibility(View.GONE);
                } else {
                    db.updateSettings(db.loginID.get(0), "auto_upload", 0);
                    db.getSettings(db.loginID.get(0));
                    uploadLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        downloadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    db.updateSettings(db.loginID.get(0), "auto_download", 1);
                    db.getSettings(db.loginID.get(0));
                    downloadLayout.setVisibility(View.GONE);
                } else {
                    db.updateSettings(db.loginID.get(0), "auto_download", 0);
                    db.getSettings(db.loginID.get(0));
                    downloadLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YesNoOption("upload","Are you sure?","Uploading your data on this app to server will overwrite any data you have in server.");
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YesNoOption("download","Are you sure?","Downloading your data on server will overwrite your data in your app.");
            }
        });

        openAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save.setVisibility(View.VISIBLE);
                epassword.setVisibility(View.VISIBLE);
                vpassword.setVisibility(View.GONE);
                eusername.setVisibility(View.VISIBLE);
                vusername.setVisibility(View.GONE);
                closeAccount.setVisibility(View.VISIBLE);
                openAccount.setVisibility(View.GONE);
            }
        });

        closeAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save.setVisibility(View.GONE);
                vpassword.setVisibility(View.VISIBLE);
                epassword.setVisibility(View.GONE);
                vusername.setVisibility(View.VISIBLE);
                eusername.setVisibility(View.GONE);
                openAccount.setVisibility(View.VISIBLE);
                closeAccount.setVisibility(View.GONE);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(eusername.getText().length() > 0 && epassword.getText().length() > 0){
                    db.updateLoginAccount(db.loginID.get(0), eusername.getText().toString(), epassword.getText().toString());
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle("Save Success");
                    dialog.setMessage("Username and password successfully saved.\nPage is need to reload click Ok to continue.");
                    dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(getActivity(), Login.class);
                            getActivity().finish();
                            startActivity(intent);
                        }
                    });
                    dialog.setCancelable(false);
                    dialog.show();
                } else {
                    showSnackBar(snackBarLayout, "Username and password cannot be empty");
                }
            }
        });

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        settingsDialog = alertDialog.create();
        alertDialog.setView(dialoglayout);
        settingsDialog = alertDialog.show();
    }

    public void showSnackBar(CoordinatorLayout layout, String msg){
        Snackbar snackbar = Snackbar.make(layout, msg, Snackbar.LENGTH_LONG);
        String color = getResources().getString(0+R.color.headerColor);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(Color.parseColor(String.valueOf(color)));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(18);
        snackbar.show();
    }

    public void YesNoOption(final String source, String title, String msg){
        AlertDialog.Builder yesno = new AlertDialog.Builder(getActivity());
        yesno.setTitle(title);
        yesno.setMessage(msg);
        yesno.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (source.equalsIgnoreCase("upload")) {
                    UploadData upload = new UploadData();
                    upload.execute("");
                } else if (source.equalsIgnoreCase("download")) {
                    DownloadData download = new DownloadData();
                    download.execute("");
                }
            }
        });
        yesno.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        yesno.show();
    }

    public class UploadData extends AsyncTask<String, String, String> {

        //0 is failed 1 is success
        int status = 0;
        String msg = "";

        @Override
        protected void onPreExecute(){
            try {
                progress = ProgressDialog.show(getActivity(), null, "Please wait!");  //show a progress dialog
            } catch(Exception e){e.printStackTrace();}
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                //arraylist for postdata
                ArrayList<NameValuePair> PostData = new ArrayList<NameValuePair>();

                //prelim grade
                JSONArray prelim = new JSONArray();
                db.getProfGradeData("Prelim", db.loginID.get(0));
                for (int i = 0; i < db.profDataStudentNo.size(); i++) {
                    JSONObject data = new JSONObject();
                    data.put("p_studentno", db.profDataStudentNo.get(i));
                    data.put("p_subjectcode", db.profDataSubjectCode.get(i));
                    data.put("p_categoryid", String.valueOf(db.profDataCategoryID.get(i)));
                    data.put("p_caption", db.profDataCaption.get(i));
                    data.put("p_date",  db.profDataDate.get(i));
                    data.put("p_grade",  String.valueOf(db.profDataGrade.get(i)));
                    prelim.put(data);
                    System.out.println("Uploading Prelim: "+i+"/"+db.profDataStudentNo.size());
                }
                if(prelim.length() > 0){
                    PostData.add(new BasicNameValuePair("prelim", prelim.toString()));
                }

                //midterm grade
                JSONArray midterm = new JSONArray();
                db.getProfGradeData("Midterm", db.loginID.get(0));
                for (int i = 0; i < db.profDataStudentNo.size(); i++) {
                    JSONObject data = new JSONObject();
                    data.put("m_studentno", db.profDataStudentNo.get(i));
                    data.put("m_subjectcode", db.profDataSubjectCode.get(i));
                    data.put("m_categoryid", String.valueOf(db.profDataCategoryID.get(i)));
                    data.put("m_caption", db.profDataCaption.get(i));
                    data.put("m_date",  db.profDataDate.get(i));
                    data.put("m_grade",  String.valueOf(db.profDataGrade.get(i)));
                    midterm.put(data);
                    System.out.println("Uploading Midterm: "+i+"/"+db.profDataStudentNo.size());
                }
                if(midterm.length() > 0){
                    PostData.add(new BasicNameValuePair("midterm", midterm.toString()));
                }

                //finals grade
                JSONArray finals = new JSONArray();
                db.getProfGradeData("Finals", db.loginID.get(0));
                for (int i = 0; i < db.profDataStudentNo.size(); i++) {
                    JSONObject data = new JSONObject();
                    data.put("f_studentno", db.profDataStudentNo.get(i));
                    data.put("f_subjectcode", db.profDataSubjectCode.get(i));
                    data.put("f_categoryid", String.valueOf(db.profDataCategoryID.get(i)));
                    data.put("f_caption", db.profDataCaption.get(i));
                    data.put("f_date",  db.profDataDate.get(i));
                    data.put("f_grade",  String.valueOf(db.profDataGrade.get(i)));
                    finals.put(data);
                    System.out.println("Uploading Finals: "+i+"/"+db.profDataStudentNo.size());
                }
                if(finals.length() > 0){
                    PostData.add(new BasicNameValuePair("finals", finals.toString()));
                }

                //passing  grade
                JSONArray passingGrade = new JSONArray();
                db.getPassingGrade(db.loginID.get(0));
                JSONObject passingGradeData = new JSONObject();
                passingGradeData.put("passingGrade", String.valueOf(db.passingGradeValue.get(0)));
                passingGrade.put(passingGradeData);
                PostData.add(new BasicNameValuePair("passing", passingGrade.toString()));
                System.out.println("Uploading Passing Grade: 1/1");

                //raw score
                JSONArray rawScore = new JSONArray();
                db.getAllRawGrade(db.loginID.get(0));
                for(int i=0;i<db.rawGradeid.size();i++){
                    JSONObject data = new JSONObject();
                    data.put("raw_gradeid", String.valueOf(db.rawGradeid.get(i)));
                    data.put("raw_rawscore", String.valueOf(db.rawGrade.get(i)));
                    rawScore.put(data);
                    System.out.println("Uploading Raw Score: "+i+"/"+db.rawGradeid.size());
                }
                if(rawScore.length() > 0){
                    PostData.add(new BasicNameValuePair("raw_score", rawScore.toString()));
                }

                //percentage
                JSONArray percentage = new JSONArray();
                db.classList(db.loginID.get(0));
                for (int sub = 0; sub < db.subcode.size(); sub++) {
                    db.getCategory(db.loginID.get(0), db.subcode.get(sub));
                    for (int cat = 0; cat < db.category.size(); cat++) {
                        db.getPercentage(db.loginID.get(0), db.category_id.get(cat));
                        if(db.percentage.size() > 0){
                            JSONObject data = new JSONObject();
                            data.put("percent_categoryid", String.valueOf(db.category_id.get(cat)));
                            data.put("percent_percent", String.valueOf(db.percentage.get(0)));
                            percentage.put(data);
                            System.out.println("Uploading percentage: "+cat+"/"+db.category.size());
                        }
                    }
                }
                if(percentage.length() > 0){
                    PostData.add(new BasicNameValuePair("percentage", percentage.toString()));
                }

                //notification
                JSONArray notification = new JSONArray();
                db.getNotification(db.loginID.get(0));
                for (int i = 0; i < db.notificationId.size(); i++) {
                    JSONObject data = new JSONObject();
                    data.put("notif_studentno", db.notificationStudentId.get(i));
                    data.put("notif_name", db.notificationName.get(i));
                    data.put("notif_subjectcode", db.notificationSubjectCode.get(i));
                    data.put("notif_description", db.notificationDesc.get(i));
                    data.put("notif_type", db.notificationType.get(i));
                    data.put("notif_status", db.notificationStatus.get(i));
                    data.put("notif_tag", db.notificationTag.get(i));
                    notification.put(data);
                    System.out.println("Uploading notification: "+i+"/"+db.notificationId.size());
                }
                if(notification.length() > 0){
                    PostData.add(new BasicNameValuePair("notification", notification.toString()));
                }

                //term
                JSONArray term = new JSONArray();
                db.classList(db.loginID.get(0));
                for (int sub = 0; sub < db.subcode.size(); sub++) {
                    db.getTerm(db.loginID.get(0), db.subcode.get(sub));
                    for (int i = 0; i < db.term.size(); i++) {
                        JSONObject data = new JSONObject();
                        data.put("term_subjectcode", db.subcode.get(sub));
                        data.put("term_currentterm", db.term.get(i));
                        term.put(data);
                        System.out.println("Uploading term: "+i+"/"+db.term.size());
                    }
                }
                if(term.length() > 0){
                    PostData.add(new BasicNameValuePair("term", term.toString()));
                }

                //grade to submit
                JSONArray gts = new JSONArray();
                db.classList(db.loginID.get(0));
                for (int sub = 0; sub < db.subcode.size(); sub++) {
                    db.getStudent(db.loginID.get(0), db.subcode.get(sub));
                    for (int student = 0; student < db.student.size(); student++) {
                        db.getAllGradeBySelectedStudent(db.loginID.get(0), db.subcode.get(sub), db.studentid.get(student));
                        for (int grade = 0; grade < db.all_grade_prelim.size(); grade++) {
                            JSONObject data = new JSONObject();
                            data.put("gts_subjectcode", db.subcode.get(sub));
                            data.put("gts_studentno", db.studentid.get(student));
                            data.put("gts_prelim", String.valueOf(db.all_grade_prelim.get(grade)));
                            data.put("gts_midterm", String.valueOf(db.all_grade_midterm.get(grade)));
                            data.put("gts_finals", String.valueOf(db.all_grade_finals.get(grade)));
                            data.put("gts_average", String.valueOf(db.all_grade_average.get(grade)));
                            data.put("gts_remarks", db.all_grade_remarks.get(grade));
                            gts.put(data);
                            System.out.println("Uploading grade to submit: "+grade+"/"+db.all_grade_prelim.size());
                        }
                    }
                }
                if(gts.length() > 0){
                    PostData.add(new BasicNameValuePair("gts", gts.toString()));
                }

                //grade category
                JSONArray cat = new JSONArray();
                db.classList(db.loginID.get(0));
                for (int sub = 0; sub < db.subcode.size(); sub++) {
                    db.getCategory(db.loginID.get(0), db.subcode.get(sub));
                    for (int i = 0; i < db.category.size(); i++) {
                        JSONObject data = new JSONObject();
                        data.put("cat_id", String.valueOf(db.category_id.get(i)));
                        data.put("cat_subjectcode", db.subcode.get(sub));
                        data.put("cat_name", db.category.get(i));
                        cat.put(data);
                        System.out.println("Uploading Category: "+i+"/"+db.category.size());
                    }
                }
                if(cat.length() > 0){
                    PostData.add(new BasicNameValuePair("category", cat.toString()));
                }

                //last update
                JSONArray lu = new JSONArray();
                db.getUpdateDate(db.loginID.get(0));
                if(db.updateDate.size() > 0) {
                    JSONObject lastUpdate = new JSONObject();
                    lastUpdate.put("lu_date", db.updateDate.get(0));
                    lu.put(lastUpdate);
                    System.out.println("Uploading last update: 1/1");
                }
                if(lu.length() > 0){
                    PostData.add(new BasicNameValuePair("lu", lu.toString()));
                }

                //Archive
                JSONArray archive = new JSONArray();
                db.getAllArchive(db.loginID.get(0));
                for (int i = 0; i < db.archive_student_no.size(); i++) {
                    JSONObject data = new JSONObject();
                    data.put("arc_studentno", db.archive_student_no.get(0));
                    data.put("arc_name", db.archive_student_name.get(0));
                    data.put("arc_year", db.archive_year.get(0));
                    data.put("arc_subjectcode", db.archive_subject.get(0));
                    data.put("arc_subjectdescription", db.archive_subject_description.get(0));
                    data.put("arc_ay", db.archive_ay.get(0));
                    data.put("arc_sem", db.archive_sem.get(0));
                    data.put("arc_prelim", String.valueOf(db.archive_prelim.get(0)));
                    data.put("arc_midterm", String.valueOf(db.archive_midterm.get(0)));
                    data.put("arc_finals", String.valueOf(db.archive_finals.get(0)));
                    data.put("arc_average", String.valueOf(db.archive_average.get(0)));
                    data.put("arc_equivalent", String.valueOf(db.archive_equivalent.get(0)));
                    archive.put(data);
                    System.out.println("Uploading Archive: "+i+"/"+db.archive_student_no.size());
                }
                if(archive.length() > 0){
                    PostData.add(new BasicNameValuePair("archive", archive.toString()));
                }

                //user info
                JSONArray userinfo = new JSONArray();
                db.getLoginAccount(db.loginID.get(0));
                JSONObject userinfoData = new JSONObject();
                userinfoData.put("info_username", db.username.get(0));
                userinfoData.put("info_password", db.password.get(0));
                userinfo.put(userinfoData);
                System.out.println("User info: 1/1");
                PostData.add(new BasicNameValuePair("user_account", userinfo.toString()));

                db.getSettings(db.loginID.get(0));
                if(db.settingsAutoDownload.get(0) == 1){
                    ((MainActivity)getActivity()).isRunning = false;
                }

                //update sync date
                String date = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date());
                JSONArray sync = new JSONArray();
                JSONObject syncData = new JSONObject();
                syncData.put("sync_date", date);
                sync.put(syncData);
                PostData.add(new BasicNameValuePair("sync", sync.toString()));
                System.out.println("Uploading Sync: 1/1");

                //send the data
                PostData.add(new BasicNameValuePair("prof_id", String.valueOf(db.loginID.get(0))));
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(mysqlConnectionURI.UploadData);
                httppost.setEntity(new UrlEncodedFormEntity(PostData));
                HttpResponse response = httpclient.execute(httppost);
                response.getEntity().consumeContent();

                db.updateSyncDate(db.loginID.get(0), "upload_date", date);
                db.updateSyncDate(db.loginID.get(0), "sync_date", date);
                msg = "Success.\nData has been backup successfully.";
                status = 1;
            } catch (Exception ex) {
                msg = "Uploading Grade Failed!.\nCheck Internet Connection and try again.";
                status = 0;
                ex.printStackTrace();
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg){
            progress.dismiss();
            settingsDialog.dismiss();
            if(status == 1){
                Success("Upload Complete", msg, "");
            } else {
                if(progress.isShowing()){
                    progress.dismiss();
                }
                Success("Upload Failed", msg, "");
            }
            if(db.settingsAutoDownload.get(0) == 1){
                ((MainActivity)getActivity()).isRunning = true;
            }
        }
    }

    public class DownloadData extends AsyncTask<String, String, String> {

        //0 is failed 1 is success
        int status = 0;
        String msg = "";

        @Override
        protected void onPreExecute(){
            try {
                progress = ProgressDialog.show(getActivity(), null, "Please wait!");  //show a progress dialog
            } catch(Exception e){e.printStackTrace();}
        }

        @Override
        protected String doInBackground(String... strings) {
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
                            status = 1;
                        } else {
                            status = 1;
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
                        status = 1;
                    }
                }
            } catch (UnsupportedEncodingException e) {
                status = 0;
                e.printStackTrace();
            } catch (IOException e) {
                status = 0;
                e.printStackTrace();
            } catch (JSONException e) {
                status = 0;
                e.printStackTrace();
            } catch (Exception ex){
                status = 0;
                ex.printStackTrace();
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg){
            progress.dismiss();
            settingsDialog.dismiss();
            if(status == 1){
                if(progress.isShowing()){
                    progress.dismiss();
                }
                Success("Download Complete", "Successfully restore your data to the server.", "download");
            } else {
                if(progress.isShowing()){
                    progress.dismiss();
                }
                Success("Download Failed", "Check Internet Connection and try again.", "");
            }
        }
    }

    public void Success(final String title, String msg, final String source){
        android.app.AlertDialog.Builder successDialog = new android.app.AlertDialog.Builder(getActivity());
        successDialog.setCancelable(false);
        successDialog.setTitle(title);
        successDialog.setMessage(msg);
        successDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(source.equalsIgnoreCase("download")){
                    Intent intent = getActivity().getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    getActivity().finish();
                    startActivity(intent);
                }
            }
        });
        successDialog.show();
    }

    private Runnable changeMessage = new Runnable() {
        @Override
        public void run() {
            progress.setMessage(currentString);
        }
    };
}
