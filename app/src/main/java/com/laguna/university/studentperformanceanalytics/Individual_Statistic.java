package com.laguna.university.studentperformanceanalytics;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.LargeValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Individual_Statistic extends AppCompatActivity {

    SQLiteDBcontroller db = new SQLiteDBcontroller(this);
    DecimalFormat format = new DecimalFormat("##.0");

    String id = "";
    String name = "";
    String subject = "";
    String username = "";
    String password = "";
    String notifTerm = "";

    TextView bestCategory, weakCategory, bestAverage, weakAverage, categoryText, categoryTextAverage, examAverage, attAverage;
    Spinner chartList;
    TableLayout tableLayout;
    LinearLayout chartView, tableView;
    CoordinatorLayout snackbarLayout;
    LineChart lineChart;
    BarChart barChart;
    HorizontalBarChart hBarChart;
    RadarChart radarChart;
    PieChart pieChart;

    ArrayList<String> chartData = new ArrayList<>();

    AlertDialog termDialog;
    View root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_individual__statistic);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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
        init();
        this.setTitle(name);
        onClick();
        setTextToAnalysis();
    }

    public void init(){
        id = getIntent().getExtras().getString("studentid");
        name = getIntent().getExtras().getString("name");
        subject = getIntent().getExtras().getString("subject");
        username = getIntent().getExtras().getString("username");
        password = getIntent().getExtras().getString("password");
        notifTerm = getIntent().getExtras().getString("term");
        db.login(username, password);
        db.getCategory(db.loginID.get(0), subject);
        if(getIntent().getExtras().getBoolean("isCriticalGrade")){
            showSupport();
        }

        //casting widget
        chartList  = (Spinner) findViewById(R.id.charts);
        tableLayout  = (TableLayout) findViewById(R.id.table_main);
        chartView = (LinearLayout) findViewById(R.id.chartView);
        tableView = (LinearLayout) findViewById(R.id.table);
        bestCategory = (TextView) findViewById(R.id.bestCategory);
        weakCategory = (TextView) findViewById(R.id.weakCategory);
        bestAverage = (TextView) findViewById(R.id.bestAverage);
        weakAverage = (TextView) findViewById(R.id.weakAverage);
        categoryText = (TextView) findViewById(R.id.category);
        categoryTextAverage = (TextView) findViewById(R.id.categoryAverage);
        attAverage = (TextView) findViewById(R.id.attAverage);
        examAverage = (TextView) findViewById(R.id.examAverage);
        snackbarLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        //casting charts
        lineChart = (LineChart) findViewById(R.id.lineChart);
        barChart = (BarChart) findViewById(R.id.barChart);
        hBarChart = (HorizontalBarChart) findViewById(R.id.hBarChart);
        radarChart = (RadarChart) findViewById(R.id.radarChart);
        pieChart = (PieChart) findViewById(R.id.pieChart);

        //chartData
        chartData.add("Summary");
        chartData.add("Charts");
        chartData.add("Line");
        chartData.add("Bar");
        chartData.add("H-Bar");
        chartData.add("Radar");
        chartData.add("Pie");
        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,R.layout.zzz_one_textview,chartData){
            @Override
            public boolean isEnabled(int position){
                if(position == 1) {
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
                if(position == 1) {
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                if(position == 6){
                    root = parent.getRootView();
                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            root.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                            root.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                            showTermChoices();
                        }
                    });
                }
                return view;
            }
        };
        spinnerArrayAdapter.setDropDownViewResource(R.layout.zzz_one_textview);
        chartList.setAdapter(spinnerArrayAdapter);
        chartList.setSelection(2);
    }

    public void onClick(){
        chartList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0) {
                    tableView.setVisibility(View.VISIBLE);
                    chartView.setVisibility(View.GONE);
                    lineChart.setVisibility(View.GONE);
                    barChart.setVisibility(View.GONE);
                    hBarChart.setVisibility(View.GONE);
                    radarChart.setVisibility(View.GONE);
                    pieChart.setVisibility(View.GONE);

                    createSummary();
                } else if(i == 2){
                    tableView.setVisibility(View.GONE);
                    chartView.setVisibility(View.VISIBLE);
                    lineChart.setVisibility(View.VISIBLE);
                    barChart.setVisibility(View.GONE);
                    hBarChart.setVisibility(View.GONE);
                    radarChart.setVisibility(View.GONE);
                    pieChart.setVisibility(View.GONE);

                    lineChart.animateX(1000);
                    setLineData();
                } else if(i == 3){
                    tableView.setVisibility(View.GONE);
                    chartView.setVisibility(View.VISIBLE);
                    lineChart.setVisibility(View.GONE);
                    barChart.setVisibility(View.VISIBLE);
                    hBarChart.setVisibility(View.GONE);
                    radarChart.setVisibility(View.GONE);
                    pieChart.setVisibility(View.GONE);

                    barChart.animateXY(1000,1000);
                    setBarData();
                } else if(i == 4){
                    tableView.setVisibility(View.GONE);
                    chartView.setVisibility(View.VISIBLE);
                    lineChart.setVisibility(View.GONE);
                    barChart.setVisibility(View.GONE);
                    hBarChart.setVisibility(View.VISIBLE);
                    radarChart.setVisibility(View.GONE);
                    pieChart.setVisibility(View.GONE);

                    hBarChart.animateXY(1000,1000);
                    setHBarData();
                } else if(i == 5){
                    tableView.setVisibility(View.GONE);
                    chartView.setVisibility(View.VISIBLE);
                    lineChart.setVisibility(View.GONE);
                    barChart.setVisibility(View.GONE);
                    hBarChart.setVisibility(View.GONE);
                    radarChart.setVisibility(View.VISIBLE);
                    pieChart.setVisibility(View.GONE);

                    radarChart.animateXY(1000,1000);
                    setRadarData();
                } else if(i == 6){
                    chartList.setSelection(6);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        lineChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry entry, Highlight highlight) {
                if(highlight.getDataSetIndex() == 0) {
                    //String fullString = "Category: " + db.category.get(Math.round(entry.getX())) + ", Grade: " + entry.getY();
                    showSnackBar("Target grade of student is greater than the actual grade.\nPlease Advice the student.");
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    public void showSupport(){
        LayoutInflater inflate = getLayoutInflater();
        View layout = inflate.inflate(R.layout.notification_layout, null);
        TextView title = (TextView) layout.findViewById(R.id.title);
        TextView message = (TextView) layout.findViewById(R.id.supportMessage);
        LinearLayout hide = (LinearLayout) layout.findViewById(R.id.hideThis);
        title.setText("Student Analytics Report");
        message.setText(checkMessage());
        hide.setVisibility(View.GONE);

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        AlertDialog support = dialog.create();
        support.setView(layout);
        support.show();
    }

    public String checkMessage(){
        String catCollection = "";
        String dialog = "";
        db.getPassingGrade(db.loginID.get(0));
        for(int i=0;i<db.category.size();i++) {
            if (Math.round(getCategoryAverage(notifTerm, db.category_id.get(i))) < db.passingGradeValue.get(0)) {
                catCollection += "\t\t-" + db.category.get(i)+"\n";
                if(db.category.get(i).equals("Exam")){
                    dialog += "\n●Advice student to improve examination performance next exam.\n";
                } else if(db.category.get(i).equals("Attendance")){
                    dialog += "\n●Frequent absences in class are grounds from being unofficially dropped from the subject. Contact and inform the student.\n";
                } else if(db.category.get(i).equals("Quiz")){
                    dialog += "\n●Require to take a remedial quiz or advice student to improve quiz performance.\n";
                } else if(db.category.get(i).equals("Recitation")){
                    dialog += "\n●Require student participate more in class recitation or advice student to improve recitation performance.\n";
                } else {
                    dialog += "\n●Advice student to improve "+db.category.get(i)+" performance.\n";
                }
            }
        }
        String msg = "Grades in the following category is/are lower than required:\n\n"+catCollection+"\nRecommendations:\n"+dialog;
        return msg;
    }

    public void showSnackBar(String msg){
        Snackbar snackbar = Snackbar.make(snackbarLayout, msg, Snackbar.LENGTH_LONG);
        String color = getResources().getString(0+R.color.headerColor);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(Color.parseColor(String.valueOf(color)));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(14);
        snackbar.show();
    }

    public void setTextToAnalysis(){
        //for category average
        db.getCategory(db.loginID.get(0), subject);
        ArrayList<Float> average = new ArrayList<>();
        for(int i=0;i<db.category.size();i++){
            average.add(getCategoryAverageOverall(db.category_id.get(i)));
        }
        if(format.format(average.get(0)).equals(".0")){
            examAverage.setText("0.0");
            attAverage.setText(format.format(average.get(1)));
        } else if(format.format(average.get(1)).equals(".0")){
            examAverage.setText(format.format(average.get(0)));
            attAverage.setText("0.0");
        } else{
            examAverage.setText(format.format(average.get(0)));
            attAverage.setText(format.format(average.get(1)));
        }
        String cat = "";
        String avg = "";
        for(int i=0;i<db.category.size();i++){
            if(i >= 2){
                if(i != (db.category.size()-1)){
                    cat += db.category.get(i) + "\n";
                    if(average.get(i) != 0.0) {
                        avg += format.format(average.get(i)) + "\n";
                    } else {
                        avg += "0.0\n";
                    }
                } else {
                    cat += db.category.get(i);
                    if(average.get(i) != 0.0) {
                        avg += format.format(average.get(i));
                    } else {
                        avg += "0.0";
                    }
                }
            }
        }
        categoryText.setText(cat);
        if(avg.equals("0.0")){
            categoryTextAverage.setText("0.0");
        } else {
            categoryTextAverage.setText(avg);
        }
        //for performance
        bestCategory.setText(db.category.get(average.indexOf(Collections.max(average))));
        if(Collections.max(average) != 0.0){
            bestAverage.setText(format.format(Collections.max(average)));
        } else {
            bestAverage.setText("0.0");
        }
        weakCategory.setText(db.category.get(average.indexOf(Collections.min(average))));
        if(Collections.min(average) != 0.0){
            weakAverage.setText(format.format(Collections.min(average)));
        } else {
            weakAverage.setText("0.0");
        }
    }

    public void createSummary(){
        tableLayout.removeAllViews(); //clear all views
        createPrelim();
        createMidterm();
        createFinals();
    }

    public void createPrelim(){
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(10,10,10,10);
        TableRow.LayoutParams loopParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        loopParams.setMargins(10,0,10,0);
        TableRow.LayoutParams termParam = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        termParam.setMargins(10,0,10,0);
        termParam.span = 5;

        //term row
        TableRow termRow = new TableRow(this);
        termRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        //prelim Label
        TextView prelimLabel = new TextView(this);
        prelimLabel.setText("Prelim");
        prelimLabel.setTextSize(16);
        prelimLabel.setGravity(Gravity.CENTER_HORIZONTAL);
        prelimLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        termRow.addView(prelimLabel, termParam);
        tableLayout.addView(termRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        //header row
        TableRow header = new TableRow(this);
        header.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        //category Label
        TextView categoryLabel = new TextView(this);
        categoryLabel.setText("Category");
        categoryLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        header.addView(categoryLabel, rowParams);
        //grade Label
        TextView gradeLabel = new TextView(this);
        gradeLabel.setText("Grade");
        gradeLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        header.addView(gradeLabel, rowParams);
        //average Label
        TextView averageLabel = new TextView(this);
        averageLabel.setText("Average");
        averageLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        header.addView(averageLabel, rowParams);
        //percentage Label
        TextView percentLabel = new TextView(this);
        percentLabel.setText("%");
        percentLabel.setGravity(Gravity.CENTER_HORIZONTAL);
        percentLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        header.addView(percentLabel, rowParams);
        tableLayout.addView(header, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        //data
        db.getCategory(db.loginID.get(0), subject);
        double sumAnswer = 0.0;
        for(int i=0;i<db.category.size();i++){
            db.getPercentage(db.loginID.get(0), db.category_id.get(i));
            //data row
            TableRow data = new TableRow(this);
            data.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            if(i%2 == 0){
                data.setBackgroundColor(Color.parseColor("#f1f8e9"));
            } else {
                data.setBackgroundColor(Color.parseColor("#c5e1a5"));
            }
            //categoryValue
            TextView categoryValue = new TextView(this);
            categoryValue.setText(db.category.get(i));
            categoryValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            data.addView(categoryValue, rowParams);
            //grade row
            TableRow gradeRow = new TableRow(this);
            gradeRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            db.getGrade("Prelim", id, subject, db.category_id.get(i));
            double average = 0.0;
            if(db.grade.size() > 0){
                for(int x=0;x<db.grade.size();x++){
                    //grade data
                    TextView gradeValue = new TextView(this);
                    gradeValue.setText(db.grade.get(x) + "");
                    gradeValue.setGravity(Gravity.CENTER_HORIZONTAL);
                    gradeValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    gradeRow.addView(gradeValue, loopParams);
                    average += db.grade.get(x);
                }
                average = average / db.grade.size();
            } else {
                //grade data if empty
                TextView gradeValue = new TextView(this);
                gradeValue.setText("");
                gradeValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                gradeRow.addView(gradeValue, loopParams);
            }
            data.addView(gradeRow, rowParams);
            //average data
            TextView averageValue = new TextView(this);
            if(average != 0.0) {
                averageValue.setText(format.format((average)) + "");
            } else {
                averageValue.setText("");
            }
            averageValue.setGravity(Gravity.CENTER_HORIZONTAL);
            averageValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            data.addView(averageValue, rowParams);
            //percentageValue
            TextView percentValue = new TextView(this);
            percentValue.setText(Math.round(db.percentage.get(0)*100) + "%");
            percentValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            data.addView(percentValue, rowParams);
            //grade each category Value
            TextView gradeEachCategory = new TextView(this);
            if(db.grade.size() > 0){
                double grade = average * db.percentage.get(0);
                sumAnswer += grade;
                if(grade != 0.0){
                    gradeEachCategory.setText(format.format(grade));
                } else {
                    gradeEachCategory.setText("0.0");
                }
            } else {
                gradeEachCategory.setText("");
            }
            gradeEachCategory.setGravity(Gravity.CENTER_HORIZONTAL);
            gradeEachCategory.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            data.addView(gradeEachCategory, rowParams);
            tableLayout.addView(data, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
        //term row
        TableRow averageRow = new TableRow(this);
        averageRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.setBackgroundColor(Color.parseColor("#c5e1a5"));
        //prelim Label
        TextView overAllGradeLabel = new TextView(this);
        overAllGradeLabel.setText("Prelim Grade");
        overAllGradeLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.addView(overAllGradeLabel, rowParams);
        //empty1 Label
        TextView empty1 = new TextView(this);
        empty1.setText("");
        empty1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.addView(empty1, rowParams);
        //empty2 Label
        TextView empty2 = new TextView(this);
        empty2.setText("");
        empty2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.addView(empty2, rowParams);
        //empty3 Label
        TextView empty3 = new TextView(this);
        empty3.setText("");
        empty3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.addView(empty3, rowParams);
        //termGrade Label
        TextView termGrade = new TextView(this);
        if(sumAnswer != 0.0) {
            termGrade.setText(format.format(sumAnswer) + "");
        } else {
            termGrade.setText("");
        }
        termGrade.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.addView(termGrade, rowParams);
        tableLayout.addView(averageRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    public void createMidterm(){
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(10,10,10,10);
        TableRow.LayoutParams loopParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        loopParams.setMargins(10,0,10,0);
        TableRow.LayoutParams termParam = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        termParam.setMargins(10,0,10,0);
        termParam.span = 5;

        //term row
        TableRow termRow = new TableRow(this);
        termRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        //prelim Label
        TextView prelimLabel = new TextView(this);
        prelimLabel.setText("Midterm");
        prelimLabel.setTextSize(16);
        prelimLabel.setGravity(Gravity.CENTER_HORIZONTAL);
        prelimLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        termRow.addView(prelimLabel, termParam);
        tableLayout.addView(termRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        //header row
        TableRow header = new TableRow(this);
        header.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        //category Label
        TextView categoryLabel = new TextView(this);
        categoryLabel.setText("Category");
        categoryLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        header.addView(categoryLabel, rowParams);
        //grade Label
        TextView gradeLabel = new TextView(this);
        gradeLabel.setText("Grade");
        gradeLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        header.addView(gradeLabel, rowParams);
        //average Label
        TextView averageLabel = new TextView(this);
        averageLabel.setText("Average");
        averageLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        header.addView(averageLabel, rowParams);
        //percentage Label
        TextView percentLabel = new TextView(this);
        percentLabel.setText("%");
        percentLabel.setGravity(Gravity.CENTER_HORIZONTAL);
        percentLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        header.addView(percentLabel, rowParams);
        tableLayout.addView(header, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        //data
        db.getCategory(db.loginID.get(0), subject);
        double sumAnswer = 0.0;
        for(int i=0;i<db.category.size();i++){
            db.getPercentage(db.loginID.get(0), db.category_id.get(i));
            //data row
            TableRow data = new TableRow(this);
            data.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            if(i%2 == 0){
                data.setBackgroundColor(Color.parseColor("#f1f8e9"));
            } else {
                data.setBackgroundColor(Color.parseColor("#c5e1a5"));
            }
            //categoryValue
            TextView categoryValue = new TextView(this);
            categoryValue.setText(db.category.get(i));
            categoryValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            data.addView(categoryValue, rowParams);
            //grade row
            TableRow gradeRow = new TableRow(this);
            gradeRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            db.getGrade("Midterm", id, subject, db.category_id.get(i));
            double average = 0.0;
            if(db.grade.size() > 0){
                for(int x=0;x<db.grade.size();x++){
                    //grade data
                    TextView gradeValue = new TextView(this);
                    gradeValue.setText(db.grade.get(x) + "");
                    gradeValue.setGravity(Gravity.CENTER_HORIZONTAL);
                    gradeValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    gradeRow.addView(gradeValue, loopParams);
                    average += db.grade.get(x);
                }
                average = average / db.grade.size();
            } else {
                //grade data if empty
                TextView gradeValue = new TextView(this);
                gradeValue.setText("");
                gradeValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                gradeRow.addView(gradeValue, loopParams);
            }
            data.addView(gradeRow, rowParams);
            //average data
            TextView averageValue = new TextView(this);
            if(average != 0.0) {
                averageValue.setText(format.format((average)) + "");
            } else {
                averageValue.setText("");
            }
            averageValue.setGravity(Gravity.CENTER_HORIZONTAL);
            averageValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            data.addView(averageValue, rowParams);
            //percentageValue
            TextView percentValue = new TextView(this);
            percentValue.setText(Math.round(db.percentage.get(0)*100) + "%");
            percentValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            data.addView(percentValue, rowParams);
            //grade each category Value
            TextView gradeEachCategory = new TextView(this);
            if(db.grade.size() > 0){
                double grade = average * db.percentage.get(0);
                sumAnswer += grade;
                if(grade != 0.0){
                    gradeEachCategory.setText(format.format(grade));
                } else {
                    gradeEachCategory.setText("0.0");
                }
            } else {
                gradeEachCategory.setText("");
            }
            gradeEachCategory.setGravity(Gravity.CENTER_HORIZONTAL);
            gradeEachCategory.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            data.addView(gradeEachCategory, rowParams);
            tableLayout.addView(data, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
        //term row
        TableRow averageRow = new TableRow(this);
        averageRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.setBackgroundColor(Color.parseColor("#c5e1a5"));
        //prelim Label
        TextView overAllGradeLabel = new TextView(this);
        overAllGradeLabel.setText("Midterm Grade");
        overAllGradeLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.addView(overAllGradeLabel, rowParams);
        //empty1 Label
        TextView empty1 = new TextView(this);
        empty1.setText("");
        empty1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.addView(empty1, rowParams);
        //empty2 Label
        TextView empty2 = new TextView(this);
        empty2.setText("");
        empty2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.addView(empty2, rowParams);
        //empty3 Label
        TextView empty3 = new TextView(this);
        empty3.setText("");
        empty3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.addView(empty3, rowParams);
        //termGrade Label
        TextView termGrade = new TextView(this);
        if(sumAnswer != 0.0) {
            termGrade.setText(format.format(sumAnswer) + "");
        } else {
            termGrade.setText("");
        }
        termGrade.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.addView(termGrade, rowParams);
        tableLayout.addView(averageRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    public void createFinals(){
        TableRow.LayoutParams rowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        rowParams.setMargins(10,10,10,10);
        TableRow.LayoutParams loopParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        loopParams.setMargins(10,0,10,0);
        TableRow.LayoutParams termParam = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);
        termParam.setMargins(10,0,10,0);
        termParam.span = 5;

        //term row
        TableRow termRow = new TableRow(this);
        termRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        //prelim Label
        TextView prelimLabel = new TextView(this);
        prelimLabel.setText("Finals");
        prelimLabel.setTextSize(16);
        prelimLabel.setGravity(Gravity.CENTER_HORIZONTAL);
        prelimLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        termRow.addView(prelimLabel, termParam);
        tableLayout.addView(termRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        //header row
        TableRow header = new TableRow(this);
        header.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        //category Label
        TextView categoryLabel = new TextView(this);
        categoryLabel.setText("Category");
        categoryLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        header.addView(categoryLabel, rowParams);
        //grade Label
        TextView gradeLabel = new TextView(this);
        gradeLabel.setText("Grade");
        gradeLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        header.addView(gradeLabel, rowParams);
        //average Label
        TextView averageLabel = new TextView(this);
        averageLabel.setText("Average");
        averageLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        header.addView(averageLabel, rowParams);
        //percentage Label
        TextView percentLabel = new TextView(this);
        percentLabel.setText("%");
        percentLabel.setGravity(Gravity.CENTER_HORIZONTAL);
        percentLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        header.addView(percentLabel, rowParams);
        tableLayout.addView(header, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));

        //data
        db.getCategory(db.loginID.get(0), subject);
        double sumAnswer = 0.0;
        for(int i=0;i<db.category.size();i++){
            db.getPercentage(db.loginID.get(0), db.category_id.get(i));
            //data row
            TableRow data = new TableRow(this);
            data.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            if(i%2 == 0){
                data.setBackgroundColor(Color.parseColor("#f1f8e9"));
            } else {
                data.setBackgroundColor(Color.parseColor("#c5e1a5"));
            }
            //categoryValue
            TextView categoryValue = new TextView(this);
            categoryValue.setText(db.category.get(i));
            categoryValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            data.addView(categoryValue, rowParams);
            //grade row
            TableRow gradeRow = new TableRow(this);
            gradeRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            db.getGrade("Finals", id, subject, db.category_id.get(i));
            double average = 0.0;
            if(db.grade.size() > 0){
                for(int x=0;x<db.grade.size();x++){
                    //grade data
                    TextView gradeValue = new TextView(this);
                    gradeValue.setText(db.grade.get(x) + "");
                    gradeValue.setGravity(Gravity.CENTER_HORIZONTAL);
                    gradeValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    gradeRow.addView(gradeValue, loopParams);
                    average += db.grade.get(x);
                }
                average = average / db.grade.size();
            } else {
                //grade data if empty
                TextView gradeValue = new TextView(this);
                gradeValue.setText("");
                gradeValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                gradeRow.addView(gradeValue, loopParams);
            }
            data.addView(gradeRow, rowParams);
            //average data
            TextView averageValue = new TextView(this);
            if(average != 0.0) {
                averageValue.setText(format.format((average)) + "");
            } else {
                averageValue.setText("");
            }
            averageValue.setGravity(Gravity.CENTER_HORIZONTAL);
            averageValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            data.addView(averageValue, rowParams);
            //percentageValue
            TextView percentValue = new TextView(this);
            percentValue.setText(Math.round(db.percentage.get(0)*100) + "%");
            percentValue.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            data.addView(percentValue, rowParams);
            //grade each category Value
            TextView gradeEachCategory = new TextView(this);
            if(db.grade.size() > 0){
                double grade = average * db.percentage.get(0);
                sumAnswer += grade;
                if(grade != 0.0){
                    gradeEachCategory.setText(format.format(grade));
                } else {
                    gradeEachCategory.setText("0.0");
                }
            } else {
                gradeEachCategory.setText("");
            }
            gradeEachCategory.setGravity(Gravity.CENTER_HORIZONTAL);
            gradeEachCategory.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            data.addView(gradeEachCategory, rowParams);
            tableLayout.addView(data, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
        //term row
        TableRow averageRow = new TableRow(this);
        averageRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.setBackgroundColor(Color.parseColor("#c5e1a5"));
        //prelim Label
        TextView overAllGradeLabel = new TextView(this);
        overAllGradeLabel.setText("Finals Grade");
        overAllGradeLabel.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.addView(overAllGradeLabel, rowParams);
        //empty1 Label
        TextView empty1 = new TextView(this);
        empty1.setText("");
        empty1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.addView(empty1, rowParams);
        //empty2 Label
        TextView empty2 = new TextView(this);
        empty2.setText("");
        empty2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.addView(empty2, rowParams);
        //empty3 Label
        TextView empty3 = new TextView(this);
        empty3.setText("");
        empty3.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.addView(empty3, rowParams);
        //termGrade Label
        TextView termGrade = new TextView(this);
        if(sumAnswer != 0.0) {
            termGrade.setText(format.format(sumAnswer) + "");
        } else {
            termGrade.setText("");
        }
        termGrade.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        averageRow.addView(termGrade, rowParams);
        tableLayout.addView(averageRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
    }

    public void setLineData() {
        String term = "";
        //set data for prelim
        List<Entry> prelimData = new ArrayList<Entry>();
        for(int i=0;i<db.category.size();i++) {
            prelimData.add(new Entry(i, getCategoryAverage("Prelim", db.category_id.get(i))));
            if(getCategoryAverage("Prelim", db.category_id.get(i)) != 0f){
                term = "Prelim";
            }
        }
        LineDataSet setPrelim = new LineDataSet(prelimData, "Prelim");
        setPrelim.setAxisDependency(YAxis.AxisDependency.LEFT);
        setPrelim.setColors(Color.parseColor("#235ab2"));
        setPrelim.setCircleColor(Color.parseColor("#235ab2"));
        setPrelim.setLineWidth(10);

        //set data for midterm
        List<Entry> midtermData = new ArrayList<Entry>();
        for(int i=0;i<db.category.size();i++) {
            midtermData.add(new Entry(i, getCategoryAverage("Midterm", db.category_id.get(i))));
            if(getCategoryAverage("Midterm", db.category_id.get(i)) != 0f){
                term = "Midterm";
            }
        }
        LineDataSet setMidterm = new LineDataSet(midtermData, "Midterm");
        setMidterm.setAxisDependency(YAxis.AxisDependency.LEFT);
        setMidterm.setColors(Color.parseColor("#e50d4a"));
        setMidterm.setCircleColor(Color.parseColor("#e50d4a"));
        setMidterm.setLineWidth(10);

        //set data for finals
        List<Entry> finalsData = new ArrayList<Entry>();
        for(int i=0;i<db.category.size();i++) {
            finalsData.add(new Entry(i, getCategoryAverage("Finals", db.category_id.get(i))));
            if(getCategoryAverage("Finals", db.category_id.get(i)) != 0f){
                term = "Finals";
            }
        }
        LineDataSet setFinals = new LineDataSet(finalsData, "Finals");
        setFinals.setAxisDependency(YAxis.AxisDependency.LEFT);
        setFinals.setColors(Color.parseColor("#caea27"));
        setFinals.setCircleColor(Color.parseColor("#caea27"));
        setFinals.setLineWidth(10);

        //Target Grade
        List<Entry> targetGrade = new ArrayList<Entry>();
        for(int i=0;i<db.category.size();i++) {
            db.getPassingGrade(db.loginID.get(0));
            if(Math.round(getCategoryAverage(term, db.category_id.get(i))) < db.passingGradeValue.get(0)){
                float gradeNeeded = (db.passingGradeValue.get(0) * 2) - getCategoryAverage(term, db.category_id.get(i));
                if(Math.round(gradeNeeded) > 100){
                    gradeNeeded = 100;
                }
                targetGrade.add(new Entry(i, gradeNeeded));
            } else {
                targetGrade.add(new Entry(i, getCategoryAverage(term, db.category_id.get(i))));
            }
        }
        LineDataSet setTargetGrade = new LineDataSet(targetGrade, "Target Grade");
        setTargetGrade.setAxisDependency(YAxis.AxisDependency.LEFT);
        setTargetGrade.setColors(Color.parseColor("#f57c00"));
        setTargetGrade.setCircleColor(Color.parseColor("#f57c00"));
        setTargetGrade.setFillColor(Color.parseColor("#ffcc80"));
        setTargetGrade.setDrawFilled(true);
        setTargetGrade.enableDashedLine(10f, 10f, 0f);
        //setTargetGrade.setValueTextSize(14f);
        //setTargetGrade.setFillAlpha(255); //for maximum color without alpha

        //combine all data
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setPrelim);
        dataSets.add(setMidterm);
        dataSets.add(setFinals);
        dataSets.add(setTargetGrade);

        //set label
        final String[] label = new String[db.category.size()];
        for(int i=0;i<db.category.size();i++){
            label[i] = db.category.get(i);
        }
        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return label[(int) value];
            }
        };

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        LineData data = new LineData(dataSets);
        lineChart.getDescription().setText("Average of each Category");
        lineChart.setData(data);
        lineChart.invalidate();
    }

    public void setBarData(){
        String term = "";
        //add prelim data
        ArrayList PrelimData = new ArrayList();
        for(int i=0;i<db.category.size();i++) {
            PrelimData.add(new BarEntry(i, getCategoryAverage("Prelim", db.category_id.get(i))));
            if(getCategoryAverage("Prelim", db.category_id.get(i)) != 0f){
                term = "Prelim";
            }
        }
        BarDataSet setPrelim = new BarDataSet(PrelimData, "Prelim");
        setPrelim.setColors(Color.parseColor("#235ab2"));

        //add midterm data
        ArrayList MidtermData = new ArrayList();
        for(int i=0;i<db.category.size();i++) {
            MidtermData.add(new BarEntry(i, getCategoryAverage("Midterm", db.category_id.get(i))));
        }
        BarDataSet setMidterm = new BarDataSet(MidtermData, "Midterm");
        setMidterm.setColors(Color.parseColor("#e50d4a"));

        //add finals data
        ArrayList FinalsData = new ArrayList();
        for(int i=0;i<db.category.size();i++) {
            FinalsData.add(new BarEntry(i, getCategoryAverage("Finals", db.category_id.get(i))));
            if(getCategoryAverage("Finals", db.category_id.get(i)) != 0f){
                term = "Finals";
            }
        }
        BarDataSet setFinals = new BarDataSet(FinalsData, "Finals");
        setFinals.setColors(Color.parseColor("#caea27"));

        //initialize need
        int groupCount = db.category.size();
        float barWidth = 0.3f;
        float barSpace = 0f;
        float groupSpace = 0.1f;

        //X-axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setCenterAxisLabels(true);
        xAxis.setAxisMaximum(groupCount);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(db.category));

        //set data
        BarData data = new BarData(setPrelim, setMidterm, setFinals);
        data.setValueFormatter(new LargeValueFormatter());
        barChart.setData(data);
        barChart.getBarData().setBarWidth(barWidth);
        barChart.getXAxis().setAxisMinimum(0);
        barChart.getXAxis().setAxisMaximum(0 + barChart.getBarData().getGroupWidth(groupSpace, barSpace) * groupCount);
        barChart.groupBars(0, groupSpace, barSpace);
        barChart.getData().setHighlightEnabled(false);
        barChart.setDescription(null);
        barChart.invalidate();
    }

    public void setHBarData(){
        //add prelim data
        ArrayList prelimData = new ArrayList();
        for(int i=0;i<db.category.size();i++) {
            prelimData.add(new BarEntry(i, getCategoryAverage("Prelim", db.category_id.get(i))));
        }
        BarDataSet setPrelim = new BarDataSet(prelimData, "Prelim");
        setPrelim.setColors(Color.parseColor("#235ab2"));

        //add midterm data
        ArrayList midtermData = new ArrayList();
        for(int i=0;i<db.category.size();i++) {
            midtermData.add(new BarEntry(i, getCategoryAverage("Midterm", db.category_id.get(i))));
        }
        BarDataSet setMidterm = new BarDataSet(midtermData, "Midterm");
        setMidterm.setColors(Color.parseColor("#e50d4a"));

        //add finals data
        ArrayList finalsData = new ArrayList();
        for(int i=0;i<db.category.size();i++) {
            finalsData.add(new BarEntry(i, getCategoryAverage("Finals", db.category_id.get(i))));
        }
        BarDataSet setFinals = new BarDataSet(finalsData, "Finals");
        setFinals.setColors(Color.parseColor("#caea27"));

        //initialize need
        int groupCount = db.category.size();
        float barWidth = 0.3f;
        float barSpace = 0f;
        float groupSpace = 0.1f;

        //X-axis
        XAxis xAxis = hBarChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setCenterAxisLabels(true);
        xAxis.setAxisMaximum(groupCount);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(db.category));

        //set data
        BarData data = new BarData(setPrelim, setMidterm, setFinals);
        data.setValueFormatter(new LargeValueFormatter());
        hBarChart.setData(data);
        hBarChart.getBarData().setBarWidth(barWidth);
        hBarChart.getXAxis().setAxisMinimum(0);
        hBarChart.getXAxis().setAxisMaximum(0 + hBarChart.getBarData().getGroupWidth(groupSpace, barSpace) * groupCount);
        hBarChart.groupBars(0, groupSpace, barSpace);
        hBarChart.getData().setHighlightEnabled(false);
        hBarChart.getDescription().setText("Data Equivalent to Average");
        hBarChart.invalidate();
    }

    public void setRadarData(){
        System.out.println("Nasa Radar Data na");
        //set data for prelim
        List<RadarEntry> prelimData = new ArrayList<>();
        for(int i=0;i<db.category.size();i++) {
            prelimData.add(new RadarEntry(getCategoryAverage("Prelim", db.category_id.get(i))));
        }
        RadarDataSet setPrelim = new RadarDataSet(prelimData, "Prelim");
        setPrelim.setColors(Color.parseColor("#235ab2"));
        setPrelim.setDrawFilled(true);

        //set data for midterm
        List<RadarEntry> midtermData = new ArrayList<>();
        for(int i=0;i<db.category.size();i++) {
            midtermData.add(new RadarEntry(getCategoryAverage("Midterm", db.category_id.get(i))));
        }
        RadarDataSet setMidterm = new RadarDataSet(midtermData, "Midterm");
        setMidterm.setColors(Color.parseColor("#e50d4a"));
        setMidterm.setDrawFilled(true);

        //set data for finals
        List<RadarEntry> finalsData = new ArrayList<>();
        for(int i=0;i<db.category.size();i++) {
            finalsData.add(new RadarEntry(getCategoryAverage("Finals", db.category_id.get(i))));
        }
        RadarDataSet setFinals = new RadarDataSet(finalsData, "Finals");
        setFinals.setColors(Color.parseColor("#caea27"));
        setFinals.setDrawFilled(true);

        System.out.println("Combining data");
        //combine all data
        List<IRadarDataSet> dataSets = new ArrayList<>();
        dataSets.add(setPrelim);
        dataSets.add(setMidterm);
        dataSets.add(setFinals);

        System.out.println("setting y axis data");
        //X-axis
        XAxis xAxis = radarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(db.category));

        System.out.println("setting all data");
        RadarData data = new RadarData(dataSets);
        radarChart.getDescription().setEnabled(false);
        radarChart.setData(data);
        radarChart.invalidate();
    }

    public void setPiePrelimData() {
        //set data for prelim
        float sum = 0f;
        List<PieEntry> prelimData = new ArrayList<>();
        for(int i=0;i<db.category.size();i++) {
            sum += getCategoryAverage("Prelim", db.category_id.get(i));
        }
        for(int i=0;i<db.category.size();i++) {
            prelimData.add(new PieEntry((getCategoryAverage("Prelim", db.category_id.get(i)) / sum) * 100, db.category.get(i)));
        }
        PieDataSet dataSet = new PieDataSet(prelimData, "Prelim");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        pieChart.getLegend().setEnabled(false);
        pieChart.setCenterText("Prelim");
        pieChart.setCenterTextSize(24);
        pieChart.getDescription().setText("Average of each Category in Prelim");
        pieChart.setData(data);
        pieChart.invalidate();
    }

    public void setPieMidtermData() {
        //set data for prelim
        float sum = 0f;
        List<PieEntry> prelimData = new ArrayList<>();
        for(int i=0;i<db.category.size();i++) {
            sum += getCategoryAverage("Midterm", db.category_id.get(i));
        }
        for(int i=0;i<db.category.size();i++) {
            prelimData.add(new PieEntry((getCategoryAverage("Midterm", db.category_id.get(i)) / sum) * 100, db.category.get(i)));
        }
        PieDataSet dataSet = new PieDataSet(prelimData, "Midterm");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        pieChart.getLegend().setEnabled(false);
        pieChart.setCenterText("Midterm");
        pieChart.setCenterTextSize(24);
        pieChart.getDescription().setText("Average of each Category in Midterm");
        pieChart.setData(data);
        pieChart.invalidate();
    }

    public void setPieFinalsData() {
        //set data for prelim
        float sum = 0f;
        List<PieEntry> prelimData = new ArrayList<>();
        for(int i=0;i<db.category.size();i++) {
            sum += getCategoryAverage("Finals", db.category_id.get(i));
        }
        for(int i=0;i<db.category.size();i++) {
            prelimData.add(new PieEntry((getCategoryAverage("Finals", db.category_id.get(i)) / sum) * 100, db.category.get(i)));
        }
        PieDataSet dataSet = new PieDataSet(prelimData, "Finals");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        pieChart.getLegend().setEnabled(false);
        pieChart.setCenterText("Finals");
        pieChart.setCenterTextSize(24);
        pieChart.getDescription().setText("Average of each Category in Finals");
        pieChart.setData(data);
        pieChart.invalidate();
    }

    public void showTermChoices(){
        LayoutInflater inflate = getLayoutInflater();
        View layout = inflate.inflate(R.layout.term_list, null);
        CardView prelim = (CardView) layout.findViewById(R.id.prelim);
        CardView midterm = (CardView) layout.findViewById(R.id.midterm);
        CardView finals = (CardView) layout.findViewById(R.id.finals);
        CardView close = (CardView) layout.findViewById(R.id.close);

        prelim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tableView.setVisibility(View.GONE);
                chartView.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.GONE);
                barChart.setVisibility(View.GONE);
                hBarChart.setVisibility(View.GONE);
                radarChart.setVisibility(View.GONE);
                pieChart.setVisibility(View.VISIBLE);

                termDialog.dismiss();
                setPiePrelimData();
                pieChart.animateXY(1000, 1000);
            }
        });

        midterm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tableView.setVisibility(View.GONE);
                chartView.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.GONE);
                barChart.setVisibility(View.GONE);
                hBarChart.setVisibility(View.GONE);
                radarChart.setVisibility(View.GONE);
                pieChart.setVisibility(View.VISIBLE);

                termDialog.dismiss();
                setPieMidtermData();
                pieChart.animateXY(1000, 1000);
            }
        });

        finals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tableView.setVisibility(View.GONE);
                chartView.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.GONE);
                barChart.setVisibility(View.GONE);
                hBarChart.setVisibility(View.GONE);
                radarChart.setVisibility(View.GONE);
                pieChart.setVisibility(View.VISIBLE);

                termDialog.dismiss();
                setPieFinalsData();
                pieChart.animateXY(1000, 1000);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                termDialog.dismiss();
            }
        });

        AlertDialog.Builder termList = new AlertDialog.Builder(this);
        termDialog = termList.create();
        termDialog.setView(layout);
        termDialog.show();
    }

    public float getCategoryAverage(String term, int category_id){
        float average;
        db.getIndividualAverageOfCategoryByTerm(term, id, subject, category_id);
        if(db.average.size() > 0){
            average = db.average.get(0);
        } else {
            average = 0f;
        }
        return average;
    }

    public float getCategoryAverageOverall(int category_id){
        float average;
        db.getIndividualAverageOfCategory(id, subject, category_id);
        if(db.tempPrelim.get(0) != 0.0 && db.tempMidterm.get(0) != 0.0 && db.tempFinals.get(0) != 0.0){
            float temp = (db.tempPrelim.get(0) + db.tempMidterm.get(0) + db.tempFinals.get(0)) / 3;
            average = temp;
        } else if(db.tempPrelim.get(0) != 0.0 && db.tempMidterm.get(0) != 0.0){
            float temp = (db.tempPrelim.get(0) + db.tempMidterm.get(0)) / 2;
            average = temp;
        } else {
            average = db.tempPrelim.get(0);
        }
        //System.out.println("Prelim: "+db.tempPrelim.get(0)+" | Midterm: "+db.tempMidterm.get(0)+" | Finals: "+db.tempFinals.get(0));
        return average;
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
            equivalent = 2.00;
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
}
