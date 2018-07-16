package com.laguna.university.studentperformanceanalytics;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class gsExamFragment extends Fragment {

    ListView gradeList;
    ProgressDialog progress;
    CardView add;
    TextView addText;
    TextView displayStudentCount, displaySubject;

    SQLiteDBcontroller db;
    DecimalFormat format = new DecimalFormat("##.00");

    String arrTemp[];
    String selectedSubject = "";
    String selectedStudentNo = "";
    String studentAdapterValue[] = {"No Student Found. Wait for the admin to Add your student"};
    String currentString = "";
    String term;
    int selectedCategoryId;
    boolean isSemDone = false;
    boolean isNoEmptyGrade = false;

    AlertDialog addDialog;

    //for update grade
    int prelimGrade_id = 0;
    float prelimGrade = 0f;
    int midtermGrade_id = 0;
    float midtermGrade = 0f;
    int finalsGrade_id = 0;
    float finalsGrade = 0f;

    String selectedGradeMethod = "";
    int maxScore = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gs_exam, parent, false);
    }
    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        db  = new SQLiteDBcontroller(getActivity());
        selectedCategoryId = ((MainActivity)getActivity()).categoryId;
        selectedSubject = ((MainActivity)getActivity()).selectedSubject;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        add = (CardView) view.findViewById(R.id.add);
        addText = (TextView) view.findViewById(R.id.addText);
        displaySubject = (TextView) view.findViewById(R.id.displaySubject);
        displayStudentCount = (TextView) view.findViewById(R.id.displayStudentCount);
        gradeList = (ListView) view.findViewById(R.id.gradeList);
        db.login(((MainActivity)getActivity()).user, ((MainActivity)getActivity()).pass);
        db.classList(db.loginID.get(0));
        db.getTerm(db.loginID.get(0), db.subcode.get(0));
        checkTerm();
        try {
            db.getStudent(db.loginID.get(0), selectedSubject);
            gradeList.setAdapter(new ViewGradeAdapter());
        }catch(Exception ex){
            gradeList.setAdapter(new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,studentAdapterValue));
            ex.printStackTrace();
        }

        displaySubject.setText("Subject: "+selectedSubject);
        displayStudentCount.setText("Total Student: "+db.student.size());

        arrTemp = new String[db.student.size()];

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isSemDone) {
                    AlertDialog.Builder prompt = new AlertDialog.Builder(getActivity());
                    prompt.setTitle("Note");
                    prompt.setMessage("Data Enter for this grades will close the current term.\nAre you sure you want to add  grade?");
                    prompt.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            LayoutInflater inflater = getActivity().getLayoutInflater();
                            View gs = inflater.inflate(R.layout.grade_selection, null);
                            final RadioButton percentage =  (RadioButton) gs.findViewById(R.id.percent);
                            final RadioButton raw =  (RadioButton) gs.findViewById(R.id.raw);
                            final EditText maxItem = (EditText) gs.findViewById(R.id.totalItem);
                            final LinearLayout rawLinear = (LinearLayout) gs.findViewById(R.id.rawLinear);
                            CardView close = (CardView) gs.findViewById(R.id.close);
                            CardView ok = (CardView) gs.findViewById(R.id.save);

                            selectedGradeMethod = "percentage";
                            raw.setChecked(false);
                            rawLinear.setAlpha(0.5f);
                            maxItem.setEnabled(false);

                            percentage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    if(percentage.isChecked()){
                                        selectedGradeMethod = "percentage";
                                        percentage.setChecked(true);
                                        raw.setChecked(false);
                                        rawLinear.setAlpha(0.5f);
                                        percentage.setAlpha(1f);
                                        maxItem.setEnabled(false);
                                    }
                                }
                            });

                            raw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                                    if(raw.isChecked()){
                                        selectedGradeMethod = "raw";
                                        percentage.setChecked(false);
                                        raw.setChecked(true);
                                        rawLinear.setAlpha(1f);
                                        percentage.setAlpha(0.5f);
                                        maxItem.setEnabled(true);
                                    }
                                }
                            });

                            ok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if(selectedGradeMethod.equals("percentage")){
                                        addDialog.dismiss();
                                        addGrade();
                                    } else {
                                        if(maxItem.getText().length() > 0){
                                            maxScore = Integer.parseInt(maxItem.getText().toString());
                                            addDialog.dismiss();
                                            addGrade();
                                        }
                                    }
                                }
                            });

                            close.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    addDialog.dismiss();
                                }
                            });

                            AlertDialog.Builder addGrade = new AlertDialog.Builder(getActivity());
                            addDialog = addGrade.create();
                            addDialog.setView(gs);
                            addDialog.show();
                        }
                    });
                    prompt.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    prompt.show();
                } else {
                    ((MainActivity)getActivity()).showSnackBar("SEMESTER Already ended.");
                }
            }
        });

        //display grade
        gradeList.setAdapter(new ViewGradeAdapter());
    }

    public void addGrade(){
        Arrays.fill(arrTemp, "0");
        LayoutInflater inf = getActivity().getLayoutInflater();
        View addGradeData = inf.inflate(R.layout.add_grade, null);
        TextView title = (TextView) addGradeData.findViewById(R.id.title);
        ListView gradeList = (ListView) addGradeData.findViewById(R.id.add_grade_lv);
        CardView save = (CardView) addGradeData.findViewById(R.id.save);
        TextView caption = (TextView) addGradeData.findViewById(R.id.addTitle);
        caption.setVisibility(View.GONE);
        if(selectedGradeMethod.equals("percentage")){
            title.setText("Add Grade to Exam");
        } else {
            title.setText("Add Exam score to student max of "+maxScore);
            title.setTextSize(14f);
        }

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = "";
                isNoEmptyGrade = false;
                for(int x=0;x<arrTemp.length;x++){
                    if(arrTemp[x].isEmpty()){
                        isNoEmptyGrade = true;
                    }
                }
                if(isNoEmptyGrade){
                    title = "There still empty grade field.\n Are you sure you want to save?";
                } else {
                    title = "Are you sure you want to save?";
                }
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle(title);
                alertDialog.setPositiveButton("YES, Save It", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SaveGrade save = new SaveGrade();
                        save.execute("");
                    }
                });
                alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertDialog.show();
            }
        });

        gradeList.setAdapter(new AddGradeAdapter());

        AlertDialog.Builder addGrade = new AlertDialog.Builder(getActivity());
        addDialog = addGrade.create();
        addDialog.setView(addGradeData);
        addDialog.show();
        addDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        addDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    //check current term
    public void checkTerm(){
        db.getTerm(db.loginID.get(0), selectedSubject);
        if(db.term.size() == 0){
            db.setTerm(db.loginID.get(0), selectedSubject, 0);
            System.out.println("Current Class set Term to Prelim");
        } else {
            if (db.term.get(0) == 0) {
                term = "Prelim";
            } else if (db.term.get(0) == 1) {
                term = "Midterm";
            } else if (db.term.get(0) == 2) {
                term = "Finals";
            } else if (db.term.get(0) == 3) {
                term = "Submit Grade";
            }
        }

        checkSaveButton();
    }

    //check if save button available
    public void checkSaveButton(){
        if(term.equalsIgnoreCase("Submit Grade")){
            isSemDone = true;
            addText.setText("Add Grade\n(Disable)");
        } else {
            isSemDone = false;
            addText.setText("Add Grade");
        }
    }

    //to add grade
    private class AddGradeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if(db.student != null && db.student.size() != 0){
                return db.student.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return db.student.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View contextView, ViewGroup viewGroup) {
            final AddGradeViewHolder holder;
            if(contextView == null){
                holder = new AddGradeViewHolder();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                contextView = inflater.inflate(R.layout.data_grade, null);
                holder.text = (TextView) contextView.findViewById(R.id.name);
                holder.grade = (EditText) contextView.findViewById(R.id.grade);
                if(selectedGradeMethod.equals("raw")){
                    holder.grade.setFilters(new InputFilter[]{ new InputFilterMinMax(0, maxScore)});
                }
                contextView.setTag(holder);
            }else{
                holder = (AddGradeViewHolder)  contextView.getTag();
            }
            CardView card = (CardView) contextView.findViewById(R.id.card);
            card.setCardBackgroundColor(Color.WHITE);
            holder.ref = position;
            holder.text.setText(db.student.get(position));
            holder.grade.setText(arrTemp[position]);
            holder.grade.setSelection(holder.grade.getText().length());
            holder.grade.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if(selectedGradeMethod.equals("percentage")){
                        String str = holder.grade.getText().toString();
                        String str2 = PerfectDecimal(str, 3, 2);
                        if (!str2.equals(str)) {
                            holder.grade.setText(str2);
                            int pos = holder.grade.getText().length();
                            holder.grade.setSelection(pos);
                        }
                        arrTemp[holder.ref] = holder.grade.getText().toString();
                    } else {
                        if(editable.length() > 0){
                            arrTemp[holder.ref] = editable.toString();
                        }
                    }
                }
            });
            return contextView;
        }
    }
    public class AddGradeViewHolder {
        TextView text;
        EditText grade;
        int ref;
    }

    //filter the grade in edittext of AddGradeAdapter
    public String PerfectDecimal(String str, int MAX_BEFORE_POINT, int MAX_DECIMAL){
        String rFinal = "";
        if(str.equals("")){
        } else {
            if (str.charAt(0) == '.') str = "0" + str;
            int max = str.length();
            boolean after = false;
            int i = 0, up = 0, decimal = 0;
            char t;
            while (i < max) {
                t = str.charAt(i);
                if (t != '.' && after == false) {
                    up++;
                    if (up == MAX_BEFORE_POINT) {
                        return "100";
                    }
                    if (up > MAX_BEFORE_POINT) {
                        return rFinal;
                    }
                } else if (t == '.') {
                    after = true;
                } else {
                    decimal++;
                    if (decimal > MAX_DECIMAL)
                        return rFinal;
                }
                rFinal = rFinal + t;
                i++;
            }
            return rFinal;
        }
        return rFinal;
    }

    public class InputFilterMinMax implements InputFilter {

        private int min, max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) { }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

    //to displaying grade
    private class ViewGradeAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if(db.student != null && db.student.size() != 0){
                return db.student.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return db.student.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View contextView, ViewGroup viewGroup) {
            final ViewGradeViewHolder holder;
            if(contextView == null){
                holder = new ViewGradeViewHolder();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                contextView = inflater.inflate(R.layout.display_grade_detailed, null);
                holder.name = (TextView) contextView.findViewById(R.id.name);
                holder.prelimGrade = (TextView) contextView.findViewById(R.id.examPrelim);
                holder.midtermGrade = (TextView) contextView.findViewById(R.id.examMidterm);
                holder.finalsGrade = (TextView) contextView.findViewById(R.id.examFinals);
                contextView.setTag(holder);
            }else{
                holder = (ViewGradeViewHolder)  contextView.getTag();
            }
            holder.pos = position;
            holder.name.setText(db.student.get(position));
            //get prelim grade
            db.getPassingGrade(db.loginID.get(0));
            db.getGrade("Prelim", db.studentid.get(position), selectedSubject, selectedCategoryId);
            if(db.grade.size() > 0) {
                if(format.format(db.grade.get(0)).equals(".00")) {
                    holder.prelimGrade.setText("0.0");
                } else {
                    holder.prelimGrade.setText(format.format(db.grade.get(0)));
                }
                holder.prelimGrade.setTextColor(Color.parseColor(((MainActivity)getActivity()).EquivalentColor(Math.round(db.grade.get(0)))));
            }
            //get midterm grade
            db.getGrade("Midterm", db.studentid.get(position), selectedSubject, selectedCategoryId);
            if(db.grade.size() > 0) {
                if(format.format(db.grade.get(0)).equals(".00")) {
                    holder.midtermGrade.setText("0.0");
                } else {
                    holder.midtermGrade.setText(format.format(db.grade.get(0)));
                }
                holder.midtermGrade.setTextColor(Color.parseColor(((MainActivity)getActivity()).EquivalentColor(Math.round(db.grade.get(0)))));
            }
            //get finals grade
            db.getGrade("Finals", db.studentid.get(position), selectedSubject, selectedCategoryId);
            if(db.grade.size() > 0) {
                if(format.format(db.grade.get(0)).equals(".00")) {
                    holder.finalsGrade.setText("0.0");
                } else {
                    holder.finalsGrade.setText(format.format(db.grade.get(0)));
                }
                holder.finalsGrade.setTextColor(Color.parseColor(((MainActivity)getActivity()).EquivalentColor(Math.round(db.grade.get(0)))));
            }
            db.getGrade("Prelim", db.studentid.get(position), selectedSubject, selectedCategoryId);
            if(db.grade.size() > 0) {
                CardView card = (CardView) contextView.findViewById(R.id.grade);
                card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedStudentNo = db.studentid.get(position);
                        db.getGrade("Prelim", db.studentid.get(position), selectedSubject, selectedCategoryId);
                        prelimGrade_id = db.grade_id.get(0);
                        prelimGrade = db.grade.get(0);

                        db.getGrade("Midterm", db.studentid.get(position), selectedSubject, selectedCategoryId);
                        if (db.grade.size() > 0) {
                            midtermGrade_id = db.grade_id.get(0);
                            midtermGrade = db.grade.get(0);
                        }

                        db.getGrade("Finals", db.studentid.get(position), selectedSubject, selectedCategoryId);
                        if (db.grade.size() > 0) {
                            finalsGrade_id = db.grade_id.get(0);
                            finalsGrade = db.grade.get(0);
                        }
                        updateExam();
                    }
                });
            }
            return contextView;
        }
    }
    public class ViewGradeViewHolder {
        TextView name, prelimGrade, midtermGrade, finalsGrade;
        int pos;
    }

    public void updateExam(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        final AlertDialog updateDialog = dialog.create();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.grade_info, null);
        CardView save = (CardView) view.findViewById(R.id.update);
        CardView close = (CardView) view.findViewById(R.id.close);
        final EditText prelim = (EditText) view.findViewById(R.id.prelimExam);
        final EditText midterm = (EditText) view.findViewById(R.id.midtermExam);
        final EditText finals = (EditText) view.findViewById(R.id.finalsExam);
        LinearLayout exam = (LinearLayout) view.findViewById(R.id.examOnly);
        LinearLayout termGrade = (LinearLayout) view.findViewById(R.id.termGrade);
        final CoordinatorLayout snackbarLayout = (CoordinatorLayout) view.findViewById(R.id.showSnackBar);
        CardView prelimTooltip = (CardView) view.findViewById(R.id.prelimTooltip);
        CardView midtermTooltip = (CardView) view.findViewById(R.id.midtermTooltip);
        CardView finalsTooltip = (CardView) view.findViewById(R.id.finalsTooltip);
        snackbarLayout.bringToFront();
        exam.setVisibility(View.VISIBLE);
        termGrade.setVisibility(View.GONE);

        prelim.setText(prelimGrade + "");
        if(term.equalsIgnoreCase("Midterm")){
            midterm.setEnabled(false);
            finals.setEnabled(false);
            midtermTooltip.setVisibility(View.GONE);
            finalsTooltip.setVisibility(View.GONE);
        }
        if(term.equalsIgnoreCase("Finals")){
            midterm.setText(midtermGrade + "");
            finals.setEnabled(false);
            finalsTooltip.setVisibility(View.GONE);
        }
        if(term.equalsIgnoreCase("Submit Grade")){
            midterm.setText(midtermGrade + "");
            finals.setText(finalsGrade + "");
        }
        db.getRawGrade(prelimGrade_id);
        if(db.rawGrade.size() == 0){
            prelimTooltip.setVisibility(View.GONE);
        }
        db.getRawGrade(midtermGrade_id);
        if(db.rawGrade.size() == 0){
            midtermTooltip.setVisibility(View.GONE);
        }
        db.getRawGrade(finalsGrade_id);
        if(db.rawGrade.size() == 0){
            finalsTooltip.setVisibility(View.GONE);
        }
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder yesno = new AlertDialog.Builder(getActivity());
                yesno.setTitle("Save edited grade?");
                yesno.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(term.equalsIgnoreCase("Midterm")) {
                            if (prelim.getText().length() > 0) {
                                float prelimValue = Float.parseFloat(prelim.getText().toString());
                                if (Math.round(prelimValue) > 100) {
                                    showSnackBar(snackbarLayout, "Grades cannot exceed 100.");
                                } else {
                                    db.updateGrade("Prelim", prelimGrade_id, prelimValue);
                                    gradeList.setAdapter(new ViewGradeAdapter());
                                    updateDialog.dismiss();
                                    ((MainActivity)getActivity()).showSnackBar("You have successfully updated the data.");
                                    updateGrade("Prelim");
                                    db.getSettings(db.loginID.get(0));
                                    if(db.settingsAutoUpload.get(0) == 1){
                                        progress = ProgressDialog.show(getActivity(), null, "Please wait!");
                                    }
                                    AsyncTask.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(db.settingsAutoUpload.get(0) == 1 && ((MainActivity)getActivity()).isNetworkAvailable()){
                                                UploadData("update");
                                            }
                                        }
                                    });
                                }
                            } else {
                                showSnackBar(snackbarLayout, "Prelim or Midterm fields cannot be empty.");
                            }
                        } else if(term.equalsIgnoreCase("Finals")){
                            if (prelim.getText().length() > 0 && midterm.getText().length() > 0) {
                                float prelimValue = Float.parseFloat(prelim.getText().toString());
                                float midtermValue = Float.parseFloat(midterm.getText().toString());
                                if (Math.round(prelimValue) > 100 || Math.round(midtermValue) > 100) {
                                    showSnackBar(snackbarLayout, "Grades cannot exceed 100.");
                                } else {
                                    db.updateGrade("Prelim", prelimGrade_id, prelimValue);
                                    db.updateGrade("Midterm", midtermGrade_id, midtermValue);
                                    gradeList.setAdapter(new ViewGradeAdapter());
                                    updateDialog.dismiss();
                                    ((MainActivity)getActivity()).showSnackBar("You have successfully updated the data.");
                                    updateGrade("Prelim");
                                    updateGrade("Midterm");
                                    db.getSettings(db.loginID.get(0));
                                    if(db.settingsAutoUpload.get(0) == 1){
                                        progress = ProgressDialog.show(getActivity(), null, "Please wait!");
                                    }
                                    AsyncTask.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(db.settingsAutoUpload.get(0) == 1 && ((MainActivity)getActivity()).isNetworkAvailable()){
                                                UploadData("update");
                                            }
                                        }
                                    });
                                }
                            } else {
                                showSnackBar(snackbarLayout, "Prelim or Midterm fields cannot be empty.");
                            }
                        } else if(term.equalsIgnoreCase("Submit Grade")){
                            if (prelim.getText().length() > 0 && midterm.getText().length() > 0 && finals.getText().length() > 0) {
                                float prelimValue = Float.parseFloat(prelim.getText().toString());
                                float midtermValue = Float.parseFloat(midterm.getText().toString());
                                float finalsValue = Float.parseFloat(finals.getText().toString());
                                if (Math.round(prelimValue) > 100 || Math.round(midtermValue) > 100 || Math.round(finalsValue) > 100) {
                                    showSnackBar(snackbarLayout, "Grades cannot exceed 100.");
                                } else {
                                    db.updateGrade("Prelim", prelimGrade_id, prelimValue);
                                    db.updateGrade("Midterm", midtermGrade_id, midtermValue);
                                    db.updateGrade("Finals", finalsGrade_id, finalsValue);
                                    gradeList.setAdapter(new ViewGradeAdapter());
                                    updateDialog.dismiss();
                                    ((MainActivity)getActivity()).showSnackBar("You have successfully updated the data.");
                                    updateGrade("Prelim");
                                    updateGrade("Midterm");
                                    updateGrade("Finals");
                                    db.getSettings(db.loginID.get(0));
                                    if(db.settingsAutoUpload.get(0) == 1){
                                        progress = ProgressDialog.show(getActivity(), null, "Please wait!");
                                    }
                                    AsyncTask.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(db.settingsAutoUpload.get(0) == 1 && ((MainActivity)getActivity()).isNetworkAvailable()){
                                                UploadData("update");
                                            }
                                        }
                                    });
                                }
                            } else {
                                showSnackBar(snackbarLayout, "All fields cannot be empty.");
                            }
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
        });

        prelimTooltip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.getRawGrade(prelimGrade_id);
                int offsetY = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
                Toast toast = Toast.makeText(view.getContext(), "Raw Score: "+db.rawGrade.get(0), Toast.LENGTH_SHORT);
                MainActivity.Tooltip(view, toast);
                toast.show();
            }
        });

        midtermTooltip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.getRawGrade(midtermGrade_id);
                Toast toast = Toast.makeText(view.getContext(), "Raw Score: "+db.rawGrade.get(0), Toast.LENGTH_SHORT);
                MainActivity.Tooltip(view, toast);
                toast.show();
            }
        });

        finalsTooltip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.getRawGrade(finalsGrade_id);
                Toast toast = Toast.makeText(view.getContext(), "Raw Score: "+db.rawGrade.get(0), Toast.LENGTH_SHORT);
                MainActivity.Tooltip(view, toast);
                toast.show();
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateDialog.dismiss();
            }
        });

        updateDialog.setView(view);
        updateDialog.show();
    }

    public void updateGrade(String term){
        double average = 0.0;
        db.getCategory(db.loginID.get(0), selectedSubject);
        for(int x=0;x<db.category.size();x++) {
            db.getPercentage(db.loginID.get(0), db.category_id.get(x));
            db.getGrade(term, selectedStudentNo, selectedSubject, db.category_id.get(x));
            int sum = 0;
            if(db.grade.size() > 0){
                for(int y=0;y<db.grade.size();y++){
                    sum += db.grade.get(y);
                }
                average += (sum / db.grade.size()) * db.percentage.get(0);
            }
        }

        //update overall grade
        db.updateAllGrade(db.loginID.get(0), selectedStudentNo, selectedSubject, average, term);
        ((MainActivity)getActivity()).startService();
    }

    private Runnable changeMessage = new Runnable() {
        @Override
        public void run() {
            progress.setMessage(currentString);
        }
    };

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    //save grade local class
    public class SaveGrade extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute(){
            progress = ProgressDialog.show(getActivity(), null, "Please wait!");
        }

        @Override
        protected String doInBackground(String... strings) {
            String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
            for (int ii = 0; ii < db.student.size(); ii++) {
                if(arrTemp[ii].isEmpty() || arrTemp[ii].equals("")){
                    String description = "This student not take your exam in "+term+".";
                    db.setNotification(db.loginID.get(0), db.studentid.get(ii), db.student.get(ii), selectedSubject, description, "NG", "UNREAD", "blank");
                    if(selectedGradeMethod.equals("percentage")){
                        db.setGrade(term, db.loginID.get(0), db.studentid.get(ii), selectedSubject, selectedCategoryId, "", date, 0f);
                    } else {
                        db.setGrade(term, db.loginID.get(0), db.studentid.get(ii), selectedSubject, selectedCategoryId, "", date, 0f);
                        db.getGrade(term, db.studentid.get(ii), selectedSubject, selectedCategoryId);
                        db.setRawGrade(db.loginID.get(0), db.grade_id.get((db.grade_id.size() - 1)), arrTemp[ii]+"/"+maxScore);
                    }
                } else {
                    if(selectedGradeMethod.equals("percentage")){
                        db.setGrade(term, db.loginID.get(0), db.studentid.get(ii), selectedSubject, selectedCategoryId, "", date, Double.parseDouble(arrTemp[ii]));
                    } else {
                        float grade = Integer.parseInt(arrTemp[ii]);
                        grade = grade / maxScore * 40 + 60;
                        db.setGrade(term, db.loginID.get(0), db.studentid.get(ii), selectedSubject, selectedCategoryId, "", date, grade);
                        db.getGrade(term, db.studentid.get(ii), selectedSubject, selectedCategoryId);
                        db.setRawGrade(db.loginID.get(0), db.grade_id.get((db.grade_id.size() - 1)), arrTemp[ii]+"/"+maxScore);
                    }
                }
                int size = db.student.size() - 1;
                currentString = "Inserting Grade: " + ii + " / " + size;
                getActivity().runOnUiThread(changeMessage);
            }
            //go to method
            computation(term);
            //to update term
            db.getTerm(db.loginID.get(0), selectedSubject);
            db.updateTerm(db.loginID.get(0) ,selectedSubject, db.term.get(0) + 1);
            db.getTerm(db.loginID.get(0), selectedSubject);
            if (db.term.get(0) > 3) {
                db.updateTerm(db.loginID.get(0), selectedSubject, 0);
            }
            db.getSettings(db.loginID.get(0));
            if(db.settingsAutoUpload.get(0) == 1 && ((MainActivity)getActivity()).isNetworkAvailable()){
                UploadData("add");
            }
            return "Done";
        }

        @Override
        protected void onPostExecute(String msg){
            progress.dismiss();
            addDialog.dismiss();

            //refresh grade
            db.getStudent(db.loginID.get(0), selectedSubject);
            gradeList.setAdapter(new ViewGradeAdapter());

            checkTerm();

            //refreshNotificationText
            ((MainActivity)getActivity()).refreshNotificationText();

            //display snackbar msg
            ((MainActivity)getActivity()).showSnackBar("Saving Grade Success.");
            ((MainActivity)getActivity()).startService();

            db.getSettings(db.loginID.get(0));
            if(db.settingsAutoDownload.get(0) == 1){
                ((MainActivity)getActivity()).isRunning = true;
            }
        }
    }

    public void computation(String term){
        //compute and save overall grade and percentage
        String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
        for(int i=0;i < db.student.size();i++){
            double average = 0.0;
            db.getCategory(db.loginID.get(0), selectedSubject);
            for(int x=0;x<db.category.size();x++) {
                db.getPercentage(db.loginID.get(0), db.category_id.get(x));
                db.getGrade(term, db.studentid.get(i), selectedSubject, db.category_id.get(x));
                int sum = 0;
                if(db.grade.size() > 0){
                    for(int y=0;y<db.grade.size();y++){
                        sum += db.grade.get(y);
                    }
                    average += (sum / db.grade.size()) * db.percentage.get(0);
                } else {
                    if(x == 1){
                        average += 60 * db.percentage.get(0);
                        db.setGrade(term, db.loginID.get(0), db.studentid.get(i), selectedSubject, db.category_id.get(x), "", date, 60f);
                    } else {
                        average += 0 * db.percentage.get(0);
                        db.setGrade(term, db.loginID.get(0), db.studentid.get(i), selectedSubject, db.category_id.get(x), "", date, 0f);
                    }
                    if(x >= 2){
                        String addTitle = db.category.get(x) + " 1";
                        String description = "This student has no grade in your "+addTitle+".";
                        db.setNotification(db.loginID.get(0), db.studentid.get(i), db.student.get(i), selectedSubject, description, "NG", "UNREAD", "blank");
                    }
                }
            }

            //save overall grade
            db.getPassingGrade(db.loginID.get(0));
            if(term.equalsIgnoreCase("Prelim")) {
                db.setAllGrade(db.loginID.get(0), db.studentid.get(i), selectedSubject, average, (average / 3), "Failed!");
            } else {
                db.updateAllGrade(db.loginID.get(0), db.studentid.get(i), selectedSubject, average, term);
            }

            //handle notification
            db.getAllGradeBySelectedStudent(db.loginID.get(0), selectedSubject, db.studentid.get(i));
            if(term.equalsIgnoreCase("Prelim")) {
                if (db.all_grade_prelim.size() > 0) {
                    if (Math.round(db.all_grade_prelim.get(0)) < db.passingGradeValue.get(0)) {
                        //prelim notification //150
                        double gradeNeeded = (db.passingGradeValue.get(0) * 2) - db.all_grade_prelim.get(0);
                        String description;
                        if(Math.round(gradeNeeded) >= 100){
                            description = "This Student grade is below Prelim average requirements.\nRequired grade next quarter to pass subject is above maximum. Please see student immediately.";
                        } else {
                            description = "This Student grade is below Prelim requirements.\nMinimum required grade next quarter to pass subject: "+format.format(gradeNeeded);
                        }
                        db.setNotification(db.loginID.get(0), db.studentid.get(i), db.student.get(i), selectedSubject, description, "HP", "UNREAD", "failed");
                    }
                }
            } else if(term.equalsIgnoreCase("Midterm")){
                if (db.all_grade_midterm.size() > 0) {
                    if (Math.round(db.all_grade_midterm.get(0)) < db.passingGradeValue.get(0)) {
                        //midterm notification //225
                        double gradeNeeded = (db.passingGradeValue.get(0) * 3) - db.all_grade_prelim.get(0) - db.all_grade_midterm.get(0);
                        String description;
                        if(Math.round(gradeNeeded) >= 100){
                            description = "This Student grade is below Midterm average requirements.\nRequired grade next quarter to pass subject is above maximum. Please see student immediately.";
                        } else {
                            description = "This Student grade is below Midterm average requirements.\nMinimum required grade next quarter to pass subject: "+format.format(gradeNeeded);
                        }
                        db.setNotification(db.loginID.get(0), db.studentid.get(i), db.student.get(i), selectedSubject, description, "HP", "UNREAD", "failed");
                    }
                }
            } else if(term.equalsIgnoreCase("Finals")){
                if (db.all_grade_finals.size() > 0) {
                    if (Math.round(db.all_grade_finals.get(0)) < db.passingGradeValue.get(0)) {
                        //finals notification
                        String description = "This Student grade is failed to pass this subject.";
                        db.setNotification(db.loginID.get(0), db.studentid.get(i), db.student.get(i), selectedSubject, description, "HP", "UNREAD", "failed");
                    }
                }
            }
            //update/add updated date
            db.getUpdateDate(db.loginID.get(0));
            if(db.updateDate.size() == 0){
                db.setUpdatedDate(db.loginID.get(0), date);
            } else{
                db.updateUpdatedDate(db.loginID.get(0), date);
            }
            currentString = "Computing & saving each grade of student " + i + " / " + db.student.size();
            getActivity().runOnUiThread(changeMessage);
        }
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

    public void UploadData(String source){
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

            if(source.equalsIgnoreCase("update")){
                progress.dismiss();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
