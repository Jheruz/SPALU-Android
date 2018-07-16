package com.laguna.university.studentperformanceanalytics;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import java.text.DecimalFormat;

public class GradeCalculator extends AppCompatActivity {

    DecimalFormat format = new DecimalFormat("##.00");

    CardView calculate;
    EditText conditionGrade, targetGrade;
    TextView output;
    CoordinatorLayout snackbarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_grade_calculator);
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

        this.setTitle("Grade Calculator");
        init();
        onClick();
    }

    public void init(){
        conditionGrade = (EditText) findViewById(R.id.termGrade);
        targetGrade = (EditText) findViewById(R.id.targetGrade);
        output = (TextView) findViewById(R.id.output);
        calculate = (CardView) findViewById(R.id.calculate);
        snackbarLayout = (CoordinatorLayout) findViewById(R.id.snackbar);
    }

    public void onClick(){
        calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                computation();
            }
        });
    }

    public void computation(){
        String outputText = " Needed to attain target grade.";
        if(conditionGrade.getText().length() > 0 && targetGrade.getText().length() > 0){
            float grade = Float.parseFloat(conditionGrade.getText().toString());
            float target = Float.parseFloat(targetGrade.getText().toString());
            if(Math.round(grade) <= 100){
                float temp = (target * 2) - grade;
                output.setText(format.format(temp) + outputText);
            } else {
                showSnackBar("All fields must be below 100");
            }
        } else {
            showSnackBar("Please Complete All fields.");
        }
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
}
