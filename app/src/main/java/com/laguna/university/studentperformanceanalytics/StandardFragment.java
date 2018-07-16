package com.laguna.university.studentperformanceanalytics;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class StandardFragment extends Fragment {

    CardView gc;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_standard, parent, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        gc = (CardView) view.findViewById(R.id.gradeCalculator);

        gc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), GradeCalculator.class);
                startActivity(intent);
            }
        });
    }
}
