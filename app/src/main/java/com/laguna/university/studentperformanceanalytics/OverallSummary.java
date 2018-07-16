package com.laguna.university.studentperformanceanalytics;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class OverallSummary extends AppCompatActivity {

    SQLiteDBcontroller db  = new SQLiteDBcontroller(this);
    DecimalFormat format = new DecimalFormat("##.00");
    ArrayList<String> termData = new ArrayList<>();

    Toolbar toolbar;
    Spinner sectionList, termList;
    TableLayout tableLayout;

    String username = "", password = "";
    String selectedTerm = "";
    String selectedSubject = "";
    String selectedSubjectDescription = "";

    int temp = 0;
    int temp1 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_overall_summary);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        initialize();
        handleToolbar();
        termData.add("Prelim");
        termData.add("Midterm");
        termData.add("Finals");
        termData.add("Average");
        sectionList.setAdapter(new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, db.subcode));
        sectionList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSubject = (String) sectionList.getItemAtPosition(i);
                selectedSubjectDescription = db.subDescription.get(i);
                int lastIndex = selectedSubjectDescription.length() - 1;
                if(selectedSubjectDescription.charAt(lastIndex) == '\n'){
                    selectedSubjectDescription = selectedSubjectDescription.substring(0, lastIndex + 1);
                }
                db.getStudent(db.loginID.get(0), selectedSubject);
                if(selectedTerm.equalsIgnoreCase("Average")){
                    createAverage();
                } else {
                    createSummary();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        termList.setAdapter(new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, termData));
        termList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTerm  = (String) termList.getItemAtPosition(i);
                db.getStudent(db.loginID.get(0), selectedSubject);
                if(selectedTerm.equalsIgnoreCase("Average")){
                    createAverage();
                } else {
                    createSummary();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
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

    public void initialize(){
        this.setTitle("Overall Summary");
        username = getIntent().getExtras().getString("username");
        password = getIntent().getExtras().getString("password");
        db.login(username, password);
        db.classList(db.loginID.get(0));

        //widget
        sectionList = (Spinner) findViewById(R.id.section);
        termList = (Spinner) findViewById(R.id.term);
        tableLayout = (TableLayout) findViewById(R.id.table_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
    }

    public void createSummary(){
        tableLayout.removeAllViews(); //clear all views
        db.getCategory(db.loginID.get(0), selectedSubject);
        TypedValue outValue = new TypedValue();
        this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(10,10,10,10);
        TableRow.LayoutParams paramSpan = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        paramSpan.span = db.category.size() + 3;
        paramSpan.setMargins(10,10,10,10);
        TableRow.LayoutParams loopParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        loopParams.setMargins(10,0,10,0);

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
                    db.sortStudentNo(db.loginID.get(0), selectedSubject, 1);
                    createSummary();
                    temp1 = 1;
                } else {
                    db.sortStudentNo(db.loginID.get(0), selectedSubject, 0);
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
                    db.sortStudentName(db.loginID.get(0), selectedSubject, 1);
                    createSummary();
                    temp = 1;
                } else {
                    db.sortStudentName(db.loginID.get(0), selectedSubject, 0);
                    createSummary();
                    temp = 0;
                }
            }
        });
        name.setGravity(Gravity.CENTER_HORIZONTAL);
        name.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        trHeader.addView(name, rowParams);
        //loop through category
        boolean isHaveCategoryName = false;
        for(int i=0;i<db.category.size();i++){
            isHaveCategoryName = true;
            //category column
            TextView category = new TextView(this);
            category.setText(db.category.get(i));
            category.setTextSize(16);
            category.setGravity(Gravity.CENTER_HORIZONTAL);
            category.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            trHeader.addView(category, rowParams);
        }
        if(isHaveCategoryName) {
            //average column
            TextView averageCol = new TextView(this);
            averageCol.setText("Average");
            averageCol.setTextSize(16);
            averageCol.setGravity(Gravity.CENTER_HORIZONTAL);
            averageCol.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            trHeader.addView(averageCol, rowParams);
            //equivalent column
            TextView equivalent = new TextView(this);
            equivalent.setText("Equivalent");
            equivalent.setTextSize(16);
            equivalent.setGravity(Gravity.CENTER_HORIZONTAL);
            equivalent.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            trHeader.addView(equivalent, rowParams);
        }
        tableLayout.addView(trHeader, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        //set data each column
        for(int i=0;i<db.student.size();i++){
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
            id.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(id, rowParams);
            //student number data
            TextView studentNumberValue = new TextView(this);
            studentNumberValue.setText(db.studentid.get(i));
            studentNumberValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(studentNumberValue, rowParams);
            //name data
            TextView nameValue = new TextView(this);
            nameValue.setText(db.student.get(i));
            nameValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(nameValue, rowParams);
            //loop through grade of every category
            boolean isHaveCategory = false, isHaveGrade = false;
            db.getCategory(db.loginID.get(0), selectedSubject);
            for(int ii=0;ii<db.category.size();ii++) {
                isHaveCategory = true;
                db.getGrade(selectedTerm, db.studentid.get(i), selectedSubject, db.category_id.get(ii));
                double average = 0.0;
                for (int x=0;x<db.grade.size();x++){
                    average += db.grade.get(x);
                }
                average = average / db.grade.size();
                //grade data
                if(db.grade.size() > 0) {
                    isHaveGrade = true;
                    TextView gradeValue = new TextView(this);
                    if(format.format(average).equals(".00")) {
                        gradeValue.setText("0.0");
                    } else {
                        gradeValue.setText(format.format(average) + "");
                    }
                    gradeValue.setGravity(Gravity.CENTER_HORIZONTAL);
                    gradeValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    row.addView(gradeValue, rowParams);
                } else {
                    TextView emptyValue = new TextView(this);
                    emptyValue.setText("");
                    emptyValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    row.addView(emptyValue, rowParams);
                }
            }
            if(isHaveCategory && isHaveGrade){
                //average data
                TextView averageValue = new TextView(this);
                averageValue.setText(format.format(GradeAverage(db.studentid.get(i))) + "");
                averageValue.setGravity(Gravity.CENTER_HORIZONTAL);
                averageValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                row.addView(averageValue, rowParams);
                //equivalent data
                TextView equivalentValue = new TextView(this);
                equivalentValue.setText(GradeEquivalent((int)Math.round(GradeAverage(db.studentid.get(i)))) + "");
                equivalentValue.setGravity(Gravity.CENTER_HORIZONTAL);
                equivalentValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                row.addView(equivalentValue, rowParams);
            }
            tableLayout.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    public void createAverage() {
        tableLayout.removeAllViews(); //clear all views
        db.getCategory(db.loginID.get(0), selectedSubject);
        TypedValue outValue = new TypedValue();
        this.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(10,10,10,10);
        TableRow.LayoutParams paramSpan = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        paramSpan.span = 8;
        paramSpan.setMargins(10,10,10,10);
        TableRow.LayoutParams loopParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        loopParams.setMargins(10,0,10,0);

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
                    db.sortStudentNo(db.loginID.get(0), selectedSubject, 1);
                    createAverage();
                    temp1 = 1;
                } else {
                    db.sortStudentNo(db.loginID.get(0), selectedSubject, 0);
                    createAverage();
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
                    db.sortStudentName(db.loginID.get(0), selectedSubject, 1);
                    createAverage();
                    temp = 1;
                } else {
                    db.sortStudentName(db.loginID.get(0), selectedSubject, 0);
                    createAverage();
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
        //add to row
        tableLayout.addView(trHeader, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        //set data each column
        for(int i=0;i<db.student.size();i++){
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
            id.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(id, rowParams);
            //student number data
            TextView studentNumberValue = new TextView(this);
            studentNumberValue.setText(db.studentid.get(i));
            studentNumberValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(studentNumberValue, rowParams);
            //name data
            TextView nameValue = new TextView(this);
            nameValue.setText(db.student.get(i));
            nameValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(nameValue, rowParams);
            int totalAverage;
            //prelim data
            totalAverage = (int) Math.round(GradeAveragePerTerm("Prelim", db.studentid.get(i)));
            TextView prelimValue = new TextView(this);
            prelimValue.setText(Math.round(GradeAveragePerTerm("Prelim", db.studentid.get(i))) + "");
            prelimValue.setGravity(Gravity.CENTER_HORIZONTAL);
            prelimValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(prelimValue, rowParams);
            //midterm data
            totalAverage += (int) Math.round(GradeAveragePerTerm("Midterm", db.studentid.get(i)));
            TextView midtermValue = new TextView(this);
            midtermValue.setText(Math.round(GradeAveragePerTerm("Midterm", db.studentid.get(i))) + "");
            midtermValue.setGravity(Gravity.CENTER_HORIZONTAL);
            midtermValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(midtermValue, rowParams);
            //finals data
            totalAverage += (int) Math.round(GradeAveragePerTerm("Finals", db.studentid.get(i)));
            TextView finalsValue = new TextView(this);
            finalsValue.setText(Math.round(GradeAveragePerTerm("Finals", db.studentid.get(i))) + "");
            finalsValue.setGravity(Gravity.CENTER_HORIZONTAL);
            finalsValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(finalsValue, rowParams);
            //finals data
            TextView averageValue = new TextView(this);
            averageValue.setText((totalAverage/3) + "");
            averageValue.setGravity(Gravity.CENTER_HORIZONTAL);
            averageValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(averageValue, rowParams);
            //finals data
            TextView equivalentValue = new TextView(this);
            equivalentValue.setText(GradeEquivalent((totalAverage/3)) + "");
            equivalentValue.setGravity(Gravity.CENTER_HORIZONTAL);
            equivalentValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            row.addView(equivalentValue, rowParams);

            tableLayout.addView(row, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    public double GradeEquivalent(int grade){
        double equivalent = 0.0;
        if(grade >= 98 && grade <= 100){
            equivalent = 1.0;
        } else if(grade >= 94 && grade <= 97){
            equivalent = 1.25;
        } else if(grade >= 90 && grade <= 93){
            equivalent = 1.5;
        } else if(grade >= 86 && grade <= 89){
            equivalent = 1.75;
        } else if(grade == 85){
            equivalent = 2.0;
        } else if(grade >= 82 && grade <= 84){
            equivalent = 2.25;
        } else if(grade >= 80 && grade <= 81){
            equivalent = 2.5;
        } else if(grade >= 76 && grade <= 79){
            equivalent = 2.75;
        } else if(grade == 75){
            equivalent = 3.0;
        } else if(grade <= 74){
            equivalent = 5.0;
        }
        return equivalent;
    }

    public double GradeAverage(String selectedStudentNumber){
        double average = 0.0;
        db.getCategory(db.loginID.get(0), selectedSubject);
        for(int x=0;x<db.category.size();x++) {
            db.getPercentage(db.loginID.get(0), db.category_id.get(x));
            db.getGrade(selectedTerm, selectedStudentNumber, selectedSubject, db.category_id.get(x));
            int sum = 0;
            if(db.grade.size() > 0){
                for(int y=0;y<db.grade.size();y++){
                    sum += db.grade.get(y);
                    System.out.println(db.grade.get(y));
                }
                average += (sum / db.grade.size()) * db.percentage.get(0);
                System.out.println(average);
            }
        }
        System.out.println(average);
        System.out.println("term: "+selectedTerm+" | subject: "+selectedSubject+" | studentid: "+selectedStudentNumber);
        return average;
    }

    public double GradeAveragePerTerm(String term, String selectedStudentNumber){
        double average = 0.0;
        db.getCategory(db.loginID.get(0), selectedSubject);
        for(int x=0;x<db.category.size();x++) {
            db.getPercentage(db.loginID.get(0), db.category_id.get(x));
            db.getGrade(term, selectedStudentNumber, selectedSubject, db.category_id.get(x));
            int sum = 0;
            if(db.grade.size() > 0){
                for(int y=0;y<db.grade.size();y++){
                    sum += db.grade.get(y);
                    System.out.println(db.grade.get(y));
                }
                average += (sum / db.grade.size()) * db.percentage.get(0);
                System.out.println(average);
            }
        }
        System.out.println(average);
        System.out.println("term: "+selectedTerm+" | subject: "+selectedSubject+" | studentid: "+selectedStudentNumber);
        return average;
    }
}
