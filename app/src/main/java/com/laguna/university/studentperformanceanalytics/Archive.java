package com.laguna.university.studentperformanceanalytics;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Archive extends AppCompatActivity {

    SQLiteDBcontroller db  = new SQLiteDBcontroller(this);
    DecimalFormat format = new DecimalFormat("##.00");
    ArrayList<String> ayData = new ArrayList<>();
    ArrayList<String> semData = new ArrayList<>();
    ArrayList<String> subjectData = new ArrayList<>();

    Toolbar toolbar;
    Spinner ayList, semList, subjectList;
    TableLayout tableLayout;

    String selectedAY, selectedSem, selectedSubject, selectedSubjectDescription;
    String username, password;

    int temp = 0;
    int temp1 = 0;
    boolean isSemSelected = false, isAYSelected = false, isSubjectSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_archive);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        init();
        handleToolbar();
        onClick();
        refreshData();
    }

    public void handleToolbar(){
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void init(){
        this.setTitle("Archive");
        username = getIntent().getExtras().getString("username");
        password = getIntent().getExtras().getString("password");
        db.login(username, password);

        //widget
        ayList = (Spinner) findViewById(R.id.ay);
        semList = (Spinner) findViewById(R.id.sem);
        subjectList = (Spinner) findViewById(R.id.subject);
        tableLayout = (TableLayout) findViewById(R.id.table_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        db.getAYandSubject(db.loginID.get(0));
        semData.add("No Data");
        subjectData.add("No Data");
        ayData.add("No Data");
        if(db.archive_subject.size() > 0){
            semData.clear(); subjectData.clear(); ayData.clear();
            semData.add("1st");
            semData.add("2nd");
            subjectData.addAll(db.archive_subject);
            ayData.addAll(db.archive_ay);
        }

        semList.setAdapter(new ArrayAdapter<>(this, R.layout.zzz_one_textview, semData));
        ayList.setAdapter(new ArrayAdapter<>(this, R.layout.zzz_one_textview, ayData));
        subjectList.setAdapter(new ArrayAdapter<>(this, R.layout.zzz_one_textview, subjectData));
    }

    public void onClick(){
        semList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSem = (String) semList.getItemAtPosition(i);

                if(!selectedSem.equals("No Data")) {
                    isSemSelected = true;
                    refreshData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ayList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedAY = (String) ayList.getItemAtPosition(i);
                if(!selectedAY.equals("No Data")) {
                    isAYSelected = true;
                    refreshData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        subjectList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSubject = (String) subjectList.getItemAtPosition(i);
                if(!selectedSubject.equals("No Data")) {
                    selectedSubjectDescription = db.archive_subject_description.get(i);
                    int lastIndex = selectedSubjectDescription.length() - 1;
                    if (selectedSubjectDescription.charAt(lastIndex) == '\n') {
                        selectedSubjectDescription = selectedSubjectDescription.substring(0, lastIndex);
                    }
                    isSubjectSelected = true;
                    refreshData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void refreshData(){
        if(isSemSelected && isAYSelected && isSubjectSelected) {
            createSummary();
        }
    }

    public void createSummary(){
        tableLayout.removeAllViews(); //clear all views
        db.getArchive(db.loginID.get(0), selectedAY, selectedSem, selectedSubject);
        TypedValue outValue = new TypedValue();
        this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(10,10,10,10);
        TableRow.LayoutParams paramSpan = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        paramSpan.span = 8;
        paramSpan.setMargins(10,10,10,10);

        TableRow trTitle = new TableRow(this); // title
        trTitle.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        //Subject Description
        TextView SubjectDescription = new TextView(this);
        SubjectDescription.setText(selectedSubjectDescription);
        SubjectDescription.setTextSize(16);
        SubjectDescription.setGravity(Gravity.CENTER_HORIZONTAL);
        SubjectDescription.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        trTitle.addView(SubjectDescription, paramSpan);
        tableLayout.addView(trTitle, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        TableRow trHeader = new TableRow(this);
        trHeader.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        //no name column
        TextView nothing = new TextView(this);
        nothing.setText("No.");
        nothing.setTextSize(16);
        nothing.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        trHeader.addView(nothing, rowParams);
        //student number column
        TextView studentNumber = new TextView(this);
        studentNumber.setText("Student No.");
        studentNumber.setTextSize(16);
        studentNumber.setClickable(true);
        studentNumber.setBackgroundResource(outValue.resourceId);
        studentNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(temp1 == 0){
                    db.sortArchiveStudentNo(db.loginID.get(0), selectedAY, selectedSem, selectedSubject, temp1);
                    createSummary();
                    temp1 = 1;
                } else {
                    db.sortArchiveStudentNo(db.loginID.get(0), selectedAY, selectedSem, selectedSubject, temp1);
                    createSummary();
                    temp1 = 0;
                }
            }
        });
        studentNumber.setGravity(Gravity.CENTER_HORIZONTAL);
        studentNumber.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        trHeader.addView(studentNumber, rowParams);
        //name column
        TextView name = new TextView(this);
        name.setText("Name");
        name.setTextSize(16);
        name.setClickable(true);
        name.setBackgroundResource(outValue.resourceId);
        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(temp == 0){
                    db.sortArchiveStudentName(db.loginID.get(0), selectedAY, selectedSem, selectedSubject, temp1);
                    createSummary();
                    temp = 1;
                } else {
                    db.sortStudentName(db.loginID.get(0), selectedSubject, 0);
                    db.sortArchiveStudentName(db.loginID.get(0), selectedAY, selectedSem, selectedSubject, temp1);
                    createSummary();
                    temp = 0;
                }
            }
        });
        name.setGravity(Gravity.CENTER_HORIZONTAL);
        name.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        trHeader.addView(name, rowParams);
        //prelim column
        TextView prelimLabel = new TextView(this);
        prelimLabel.setText("Prelim");
        prelimLabel.setTextSize(16);
        prelimLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        trHeader.addView(prelimLabel, rowParams);
        //midterm column
        TextView midtermLabel = new TextView(this);
        midtermLabel.setText("Midterm");
        midtermLabel.setTextSize(16);
        midtermLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        trHeader.addView(midtermLabel, rowParams);
        //finals column
        TextView finalsLabel = new TextView(this);
        finalsLabel.setText("Finals");
        finalsLabel.setTextSize(16);
        finalsLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        trHeader.addView(finalsLabel, rowParams);
        //average column
        TextView averageLabel = new TextView(this);
        averageLabel.setText("Average");
        averageLabel.setTextSize(16);
        averageLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        trHeader.addView(averageLabel, rowParams);
        //equivalent column
        TextView equivalentLabel = new TextView(this);
        equivalentLabel.setText("Equivalent");
        equivalentLabel.setTextSize(16);
        equivalentLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        trHeader.addView(equivalentLabel, rowParams);
        tableLayout.addView(trHeader, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        //set data each column
        for(int i=0;i<db.archive_student_no.size();i++){
            TableRow row = new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            if(i%2 == 0){
                row.setBackgroundColor(Color.parseColor("#E0E0E0"));
            } else {
                row.setBackgroundColor(Color.parseColor("#9E9E9E"));
            }
            //id data
            TextView id = new TextView(this);
            id.setText((i+1)+"");
            id.setGravity(Gravity.CENTER_HORIZONTAL);
            id.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(id, rowParams);
            //student number data
            TextView studentNumberValue = new TextView(this);
            studentNumberValue.setText(db.archive_student_no.get(i));
            studentNumberValue.setGravity(Gravity.CENTER_HORIZONTAL);
            studentNumberValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(studentNumberValue, rowParams);
            //name data
            TextView nameValue = new TextView(this);
            nameValue.setText(db.archive_student_name.get(i));
            nameValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(nameValue, rowParams);
            //prelim data
            TextView prelimValue = new TextView(this);
            prelimValue.setText(db.archive_prelim.get(i).toString());
            prelimValue.setGravity(Gravity.CENTER_HORIZONTAL);
            prelimValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(prelimValue, rowParams);
            //midterm data
            TextView midtermValue = new TextView(this);
            midtermValue.setText(db.archive_midterm.get(i).toString());
            midtermValue.setGravity(Gravity.CENTER_HORIZONTAL);
            midtermValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(midtermValue, rowParams);
            //finals data
            TextView finalsValue = new TextView(this);
            finalsValue.setText(db.archive_finals.get(i).toString());
            finalsValue.setGravity(Gravity.CENTER_HORIZONTAL);
            finalsValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(finalsValue, rowParams);
            //average data
            TextView averageValue = new TextView(this);
            averageValue.setText(db.archive_average.get(i).toString());
            averageValue.setGravity(Gravity.CENTER_HORIZONTAL);
            averageValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(averageValue, rowParams);
            //equivalent data
            TextView equivalentValue = new TextView(this);
            equivalentValue.setText(db.archive_equivalent.get(i).toString());
            equivalentValue.setGravity(Gravity.CENTER_HORIZONTAL);
            equivalentValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(equivalentValue, rowParams);
            tableLayout.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
        if(db.archive_student_no.size() == 0){
            createNoDataView();
        }
    }

    public void createNoDataView(){
        tableLayout.removeAllViews(); //clear all views
        TableRow.LayoutParams noDataParam = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        noDataParam.span = 8;
        noDataParam.setMargins(10,10,10,10);

        TableRow noDataRow = new TableRow(this); // title
        noDataRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        noDataRow.setBackgroundColor(Color.parseColor("#E0E0E0"));
        //Subject Description
        TextView noData = new TextView(this);
        noData.setText("No Data to Load");
        noData.setTextSize(16);
        noData.setGravity(Gravity.CENTER_HORIZONTAL);
        noData.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        noDataRow.addView(noData, noDataParam);
        tableLayout.addView(noDataRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }
}
