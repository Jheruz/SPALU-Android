package com.laguna.university.studentperformanceanalytics;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Analysis extends AppCompatActivity {

    SQLiteDBcontroller db = new SQLiteDBcontroller(this);
    DecimalFormat format = new DecimalFormat("##.0");
    ArrayList<String> chartList = new ArrayList<>();

    String username = "";
    String password = "";
    String selectedSubject = "";

    TextView bestCategory, weakCategory, bestAverage, weakAverage, categoryText, categoryTextAverage, examAverage, attAverage;
    Spinner section, charts;
    ImageView topStudent;
    LineChart lineChart;
    BarChart barChart;
    HorizontalBarChart hBarChart;

    boolean isLineChartActive = false;
    boolean isBarChartActive = false;
    boolean isHBarChartActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_analysis);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(android.graphics.Color.WHITE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        init();
        onClick();
    }

    public void init(){
        this.setTitle("Section Analysis");
        username = getIntent().getExtras().getString("username");
        password = getIntent().getExtras().getString("password");
        db.login(username, password);
        db.classList(db.loginID.get(0));
        db.getCategory(db.loginID.get(0), db.subcode.get(0));
        selectedSubject = db.subcode.get(0);

        //widget
        section = (Spinner) findViewById(R.id.section);
        charts = (Spinner) findViewById(R.id.charts);
        bestCategory = (TextView) findViewById(R.id.bestCategory);
        weakCategory = (TextView) findViewById(R.id.weakCategory);
        bestAverage = (TextView) findViewById(R.id.bestAverage);
        weakAverage = (TextView) findViewById(R.id.weakAverage);
        categoryText = (TextView) findViewById(R.id.category);
        categoryTextAverage = (TextView) findViewById(R.id.categoryAverage);
        attAverage = (TextView) findViewById(R.id.attAverage);
        examAverage = (TextView) findViewById(R.id.examAverage);
        topStudent = (ImageView) findViewById(R.id.topStudent);

        //charts
        lineChart = (LineChart) findViewById(R.id.lineChart);
        barChart = (BarChart) findViewById(R.id.barChart);
        hBarChart = (HorizontalBarChart) findViewById(R.id.hBarChart);

        chartList.add("Line Chart");
        chartList.add("Bar Chart");
        chartList.add("H-Bar Chart");

        section.setAdapter(new ArrayAdapter<>(this, R.layout.zzz_one_textview, db.subcode));
        charts.setAdapter(new ArrayAdapter<>(this, R.layout.zzz_one_textview, chartList));
    }

    public void onClick(){
        section.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSubject = (String) section.getItemAtPosition(i);
                db.getStudent(db.loginID.get(0), selectedSubject);
                db.getCategory(db.loginID.get(0), selectedSubject);
                if(db.category.size() > 0) {
                    if (isLineChartActive) {
                        lineChart.animateX(1000);
                        setLineData();
                    } else if (isBarChartActive) {
                        barChart.animateXY(1000, 1000);
                        setBarData();
                    } else if (isHBarChartActive) {
                        hBarChart.animateXY(1000, 1000);
                        setHBarData();
                    }
                } else {
                    db.setCategory(db.loginID.get(0), selectedSubject, "Exam");
                    db.setCategory(db.loginID.get(0), selectedSubject, "Attendance");
                    db.setCategory(db.loginID.get(0), selectedSubject, "Class Standing");
                    db.setCategory(db.loginID.get(0), selectedSubject, "Recitation");
                    db.setCategory(db.loginID.get(0), selectedSubject, "Quiz");
                    db.getCategory(db.loginID.get(0), selectedSubject);
                    String array[] = {".40", ".10", ".10", ".20", ".20"};
                    for (int x = 0; x < db.category.size(); x++) {
                        db.setPercentage(db.loginID.get(0), db.category_id.get(x), Float.parseFloat(array[x]));
                    }
                }
                setTextToAnalysis();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        charts.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                isLineChartActive = false;
                isBarChartActive = false;
                isHBarChartActive = false;
                if(i == 0) {
                    lineChart.setVisibility(View.VISIBLE);
                    barChart.setVisibility(View.GONE);
                    hBarChart.setVisibility(View.GONE);
                    isLineChartActive = true;

                    lineChart.animateX(1000);
                    setLineData();
                }else if(i == 1){
                    lineChart.setVisibility(View.GONE);
                    barChart.setVisibility(View.VISIBLE);
                    hBarChart.setVisibility(View.GONE);
                    isBarChartActive = true;

                    barChart.animateXY(1000,1000);
                    setBarData();
                } else if(i == 2){
                    lineChart.setVisibility(View.GONE);
                    barChart.setVisibility(View.GONE);
                    hBarChart.setVisibility(View.VISIBLE);
                    isHBarChartActive = true;

                    hBarChart.animateXY(1000,1000);
                    setHBarData();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        topStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTopStudent();
            }
        });
    }

    public float sectionAverage(String term){
        db.getAverageOfAllCategory(term, selectedSubject);
        return db.average.get(0);
    }

    public void setTextToAnalysis(){
        ArrayList<Float> average = new ArrayList<>();
        for(int i=0;i<db.category.size();i++){
            average.add(getAverageOfAllCategoryById(db.category_id.get(i)));
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

    public float getAverageOfAllCategoryById(int category_id){
        float average;
        db.getAverageOfAllCategoryById(selectedSubject, category_id);
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

    public void setLineData() {
        //set data for prelim
        List<Entry> prelimData = new ArrayList<Entry>();
        prelimData.add(new Entry(0f, 0f));
        prelimData.add(new Entry(1f, sectionAverage("Prelim")));
        prelimData.add(new Entry(2f, sectionAverage("Midterm")));
        prelimData.add(new Entry(3f, sectionAverage("Finals")));
        prelimData.add(new Entry(4f, 0f));
        LineDataSet setPrelim = new LineDataSet(prelimData, "");
        setPrelim.setAxisDependency(YAxis.AxisDependency.LEFT);
        setPrelim.setColors(ColorTemplate.JOYFUL_COLORS);
        //setPrelim.setCircleColor(Color.parseColor("#235ab2"));
        setPrelim.setLineWidth(10);

        //set label
        final String[] label = {"","Prelim", "Midterm", "Finals",""};
        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return label[(int) value];
            }
        };

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        LineData data = new LineData(setPrelim);
        lineChart.getLegend().setEnabled(false);
        lineChart.getDescription().setText("Grade Average of student in "+selectedSubject);
        lineChart.setData(data);
        lineChart.invalidate();
    }

    public void setBarData(){
        //add prelim data
        ArrayList barData = new ArrayList();
        barData.add(new BarEntry(0f, sectionAverage("Prelim")));
        barData.add(new BarEntry(1f, sectionAverage("Midterm")));
        barData.add(new BarEntry(2f, sectionAverage("Finals")));
        BarDataSet dataSet = new BarDataSet(barData, null);
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);

        //set label
        final String[] label = {"Prelim", "Midterm", "Finals"};
        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return label[(int) value];
            }
        };

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        //set data
        BarData data = new BarData(dataSet);
        barChart.getLegend().setEnabled(false);
        barChart.setData(data);
        barChart.setDescription(null);
        barChart.invalidate();
    }

    public void setHBarData(){
        //add prelim data
        ArrayList barData = new ArrayList();
        barData.add(new BarEntry(0f, sectionAverage("Prelim")));
        barData.add(new BarEntry(1f, sectionAverage("Midterm")));
        barData.add(new BarEntry(2f, sectionAverage("Finals")));
        BarDataSet dataSet = new BarDataSet(barData, null);
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        //parseColor("#235ab2")
        //set label
        final String[] label = {"Prelim", "Midterm", "Finals"};
        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return label[(int) value];
            }
        };

        XAxis xAxis = hBarChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        //set data
        BarData data = new BarData(dataSet);
        hBarChart.getLegend().setEnabled(false);
        hBarChart.setData(data);
        hBarChart.setDescription(null);
        hBarChart.invalidate();
    }

    String selectedTerm = "";
    public void showTopStudent(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final AlertDialog topDialog = dialog.create();

        LayoutInflater inflate = getLayoutInflater();
        View layout = inflate.inflate(R.layout.top_student, null);
        CardView close = (CardView) layout.findViewById(R.id.close);
        final ListView studentList = (ListView) layout.findViewById(R.id.studentList);
        final Spinner termList = (Spinner) layout.findViewById(R.id.termList);
        String term[] = {"Prelim","Midterm","Finals"};
        termList.setAdapter(new ArrayAdapter<>(this, R.layout.zzz_one_textview, term));

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                topDialog.dismiss();
            }
        });

        termList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedTerm = (String) termList.getItemAtPosition(i);
                db.getAllGrade(db.loginID.get(0), selectedSubject, "Grade", 0);
                ArrayList<String> noData = new ArrayList<String>();
                noData.add("No Honor Student Yet");
                if(db.all_grade_student_name.size() == 0){
                    studentList.setAdapter(new ArrayAdapter<>(Analysis.this, android.R.layout.simple_list_item_1, noData));
                } else {
                    if(selectedTerm.equalsIgnoreCase("Prelim")){
                        if(db.all_grade_prelim.get(0) != 0f){
                            studentList.setAdapter(new TopStudentAdapter());
                        } else {
                            studentList.setAdapter(new ArrayAdapter<>(Analysis.this, android.R.layout.simple_list_item_1, noData));
                        }
                    } else if(selectedTerm.equalsIgnoreCase("Midterm")){
                        if(db.all_grade_midterm.get(0) != 0f){
                            studentList.setAdapter(new TopStudentAdapter());
                        } else {
                            studentList.setAdapter(new ArrayAdapter<>(Analysis.this, android.R.layout.simple_list_item_1, noData));
                        }
                    } else if(selectedTerm.equalsIgnoreCase("Finals")){
                        if(db.all_grade_finals.get(0) != 0f){
                            studentList.setAdapter(new TopStudentAdapter());
                        } else {
                            studentList.setAdapter(new ArrayAdapter<>(Analysis.this, android.R.layout.simple_list_item_1, noData));
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        topDialog.setView(layout);
        topDialog.show();
    }

    private class TopStudentAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if(db.all_grade_student_name != null && db.all_grade_student_name.size() != 0){
                if(db.all_grade_student_name.size() > 10){
                    return 10;
                } else {
                    return db.all_grade_student_name.size();
                }
            }
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return db.all_grade_student_name.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View contextView, ViewGroup viewGroup) {
            final TopStudentViewHolder holder;
            if(contextView == null){
                holder = new TopStudentViewHolder();
                LayoutInflater inflater = getLayoutInflater();
                contextView = inflater.inflate(R.layout.data_top_student, null);
                holder.number = (TextView) contextView.findViewById(R.id.count);
                holder.name = (TextView) contextView.findViewById(R.id.name);
                holder.grade = (TextView) contextView.findViewById(R.id.grade);
                contextView.setTag(holder);
            }else{
                holder = (TopStudentViewHolder)  contextView.getTag();
            }

            if(selectedTerm.equalsIgnoreCase("Prelim")){
                if(db.all_grade_prelim.get(position) != 0f){
                    holder.number.setText((position + 1) +"");
                    holder.name.setText(db.all_grade_student_name.get(position));
                    holder.grade.setText(Math.round(db.all_grade_prelim.get(position)) +"");
                }
            } else if(selectedTerm.equalsIgnoreCase("Midterm")){
                if(db.all_grade_midterm.get(position) != 0f){
                    holder.number.setText((position + 1) +"");
                    holder.name.setText(db.all_grade_student_name.get(position));
                    holder.grade.setText(Math.round(db.all_grade_midterm.get(position)) +"");
                }
            } else if(selectedTerm.equalsIgnoreCase("Finals")){
                if(db.all_grade_finals.get(position) != 0f){
                    holder.number.setText((position + 1) +"");
                    holder.name.setText(db.all_grade_student_name.get(position));
                    holder.grade.setText(Math.round(db.all_grade_finals.get(position)) +"");
                }
            }
            return contextView;
        }
    }
    public class TopStudentViewHolder {
        TextView number, name, grade;
    }
}
