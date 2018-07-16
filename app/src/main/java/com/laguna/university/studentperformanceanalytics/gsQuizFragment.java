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
import android.widget.ImageView;
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

public class gsQuizFragment extends Fragment {

    ListView gradeList;
    ProgressDialog progress;
    CardView add;
    TextView addText, title;

    TextView displayStudentCount, displaySubject;

    String addTitle = "";
    SQLiteDBcontroller db;
    DecimalFormat format = new DecimalFormat("##.00");

    String arrTemp[];
    String selectedSubject = "";
    String studentAdapterValue[] = {"No Student Found. Wait for the admin to Add your student"};
    String currentString = "";
    String term = "";
    String categoryName = "";
    String gradeTemp[];
    String selectedStudentNumber = "";
    int selectedCategoryId;
    boolean isSemDone = false;
    boolean isPrelimActive = false, isMidtermActive = false, isFinalsActive = false;
    boolean isNoEmptyGrade = false;

    AlertDialog addDialog;
    ArrayList<Integer> pGid = new ArrayList<>();
    ArrayList<Integer> mGid = new ArrayList<>();
    ArrayList<Integer> fGid = new ArrayList<>();
    ArrayList<String> pCaption = new ArrayList<>();
    ArrayList<String> mCaption = new ArrayList<>();
    ArrayList<String> fCaption = new ArrayList<>();
    ArrayList<String> pdate = new ArrayList<>();
    ArrayList<String> mdate = new ArrayList<>();
    ArrayList<String> fdate = new ArrayList<>();
    ArrayList<Float> prelimGrade = new ArrayList<>();
    ArrayList<Float> midtermGrade = new ArrayList<>();
    ArrayList<Float> finalsGrade = new ArrayList<>();

    String selectedGradeMethod = "";
    int maxScore = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gs_quiz, parent, false);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        db  = new SQLiteDBcontroller(getActivity());
        selectedCategoryId = ((MainActivity)getActivity()).categoryId;
        categoryName = ((MainActivity)getActivity()).categoryName;
        selectedSubject = ((MainActivity)getActivity()).selectedSubject;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        add = (CardView) view.findViewById(R.id.add);
        addText = (TextView) view.findViewById(R.id.addText);
        displaySubject = (TextView) view.findViewById(R.id.displaySubject);
        displayStudentCount = (TextView) view.findViewById(R.id.displayStudentCount);
        gradeList = (ListView) view.findViewById(R.id.gradeList);
        title = (TextView) view.findViewById(R.id.title);
        title.setText(categoryName+" Grade Average List");
        db.login(((MainActivity)getActivity()).user, ((MainActivity)getActivity()).pass);
        db.classList(db.loginID.get(0));
        db.getTerm(db.loginID.get(0), db.subcode.get(0));
        db.getPassingGrade(db.loginID.get(0));
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
                addTitle = "";
                if(!isSemDone) {
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
                } else {
                    ((MainActivity)getActivity()).showSnackBar("SEMESTER Already ended.");
                }
            }
        });

        //display grade
        ViewGradeAdapter gda = new ViewGradeAdapter();
        gradeList.setAdapter(gda);
    }

    public void addGrade(){
        Arrays.fill(arrTemp, "0");
        LayoutInflater inf = getActivity().getLayoutInflater();
        View addGradeData = inf.inflate(R.layout.add_grade, null);
        TextView title = (TextView) addGradeData.findViewById(R.id.title);
        ListView gradeList = (ListView) addGradeData.findViewById(R.id.add_grade_lv);
        CardView save = (CardView) addGradeData.findViewById(R.id.save);
        final TextView caption = (TextView) addGradeData.findViewById(R.id.addTitle);
        if(selectedGradeMethod.equals("percentage")){
            title.setText("Add Grade to "+categoryName);
        } else {
            title.setText("Add score to student max of "+maxScore);
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
                        if(caption.getText().toString().length() > 0) {
                            addTitle = caption.getText().toString();
                        }
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

        AddGradeAdapter listViewAdapter = new AddGradeAdapter();
        gradeList.setAdapter(listViewAdapter);

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

    public void showSnackBar(String msg, CoordinatorLayout layout){
        layout.bringToFront();
        Snackbar snackbar = Snackbar.make(layout, msg, Snackbar.LENGTH_LONG);
        String color = getResources().getString(0+R.color.headerColor);
        View snackBarView = snackbar.getView();
        snackBarView.setBackgroundColor(Color.parseColor(String.valueOf(color)));
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(18);
        snackbar.show();
    }

    //to view info about the grade
    public void ViewGradeInfo(String name, int prelimCount, int midtermCount, int finalsCount){
        isPrelimActive = false;
        isMidtermActive = false;
        isFinalsActive = false;
        AlertDialog.Builder grade = new AlertDialog.Builder(getActivity());
        final AlertDialog dialogGrade = grade.create();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View viewGrade = inflater.inflate(R.layout.grade_info, null);
        final CoordinatorLayout layout = (CoordinatorLayout) viewGrade.findViewById(R.id.showSnackBar);
        TextView student_name = (TextView) viewGrade.findViewById(R.id.name);
        final ImageView prelimImg = (ImageView) viewGrade.findViewById(R.id.prelimImg);
        final ImageView midtermImg = (ImageView) viewGrade.findViewById(R.id.midtermImg);
        final ImageView finalsImg = (ImageView) viewGrade.findViewById(R.id.finalsImg);
        final ListView prelimList = (ListView) viewGrade.findViewById(R.id.prelimList);
        final ListView midtermList = (ListView) viewGrade.findViewById(R.id.midtermList);
        final ListView finalsList = (ListView) viewGrade.findViewById(R.id.finalsList);
        CardView close = (CardView) viewGrade.findViewById(R.id.close);
        CardView update = (CardView) viewGrade.findViewById(R.id.update);
        CardView prelim = (CardView) viewGrade.findViewById(R.id.prelim);
        CardView midterm = (CardView) viewGrade.findViewById(R.id.midterm);
        CardView finals = (CardView) viewGrade.findViewById(R.id.finals);
        TextView prelimMeetings = (TextView) viewGrade.findViewById(R.id.prelimMeetings);
        TextView midtermMeetings = (TextView) viewGrade.findViewById(R.id.midtermMeetings);
        TextView finalsMeetings = (TextView) viewGrade.findViewById(R.id.finalsMeetings);

        student_name.setText(name);
        prelimMeetings.setText("Total "+categoryName+": "+prelimCount);
        midtermMeetings.setText("Total "+categoryName+": "+midtermCount);
        finalsMeetings.setText("Total "+categoryName+": "+finalsCount);

        prelim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPrelimActive){
                    isPrelimActive = false;
                    isMidtermActive = false;
                    isFinalsActive = false;
                    prelimList.setAdapter(null);
                    midtermList.setAdapter(null);
                    finalsList.setAdapter(null);
                    prelimImg.setImageResource(R.drawable.ic_chevron_right);
                    midtermImg.setImageResource(R.drawable.ic_chevron_right);
                    finalsImg.setImageResource(R.drawable.ic_chevron_right);
                    Arrays.fill(gradeTemp, null);
                } else {
                    isPrelimActive = true;
                    isMidtermActive = false;
                    isFinalsActive = false;
                    midtermList.setAdapter(null);
                    finalsList.setAdapter(null);
                    midtermImg.setImageResource(R.drawable.ic_chevron_right);
                    finalsImg.setImageResource(R.drawable.ic_chevron_right);
                    prelimImg.setImageResource(R.drawable.ic_chevron_down);
                    gradeTemp = new String[prelimGrade.size()];
                    GradeDetails details = new GradeDetails();
                    prelimList.setAdapter(details);
                }
            }
        });

        midterm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMidtermActive){
                    isPrelimActive = false;
                    isMidtermActive = false;
                    isFinalsActive = false;
                    prelimList.setAdapter(null);
                    midtermList.setAdapter(null);
                    finalsList.setAdapter(null);
                    prelimImg.setImageResource(R.drawable.ic_chevron_right);
                    midtermImg.setImageResource(R.drawable.ic_chevron_right);
                    finalsImg.setImageResource(R.drawable.ic_chevron_right);
                    Arrays.fill(gradeTemp, null);
                } else {
                    isPrelimActive = false;
                    isMidtermActive = true;
                    isFinalsActive = false;
                    prelimList.setAdapter(null);
                    finalsList.setAdapter(null);
                    prelimImg.setImageResource(R.drawable.ic_chevron_right);
                    finalsImg.setImageResource(R.drawable.ic_chevron_right);
                    midtermImg.setImageResource(R.drawable.ic_chevron_down);
                    gradeTemp = new String[midtermGrade.size()];
                    GradeDetails details = new GradeDetails();
                    midtermList.setAdapter(details);
                }
            }
        });

        finals.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFinalsActive){
                    isPrelimActive = false;
                    isMidtermActive = false;
                    isFinalsActive = false;
                    prelimList.setAdapter(null);
                    midtermList.setAdapter(null);
                    finalsList.setAdapter(null);
                    prelimImg.setImageResource(R.drawable.ic_chevron_right);
                    midtermImg.setImageResource(R.drawable.ic_chevron_right);
                    finalsImg.setImageResource(R.drawable.ic_chevron_right);
                    Arrays.fill(gradeTemp, null);
                } else {
                    isPrelimActive = false;
                    isMidtermActive = false;
                    isFinalsActive = true;
                    prelimList.setAdapter(null);
                    midtermList.setAdapter(null);
                    prelimImg.setImageResource(R.drawable.ic_chevron_right);
                    midtermImg.setImageResource(R.drawable.ic_chevron_right);
                    finalsImg.setImageResource(R.drawable.ic_chevron_down);
                    gradeTemp = new String[finalsGrade.size()];
                    GradeDetails details = new GradeDetails();
                    finalsList.setAdapter(details);
                }
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isHaveNull = false;
                if(isPrelimActive || isMidtermActive || isFinalsActive) {
                    for (int i = 0; i < gradeTemp.length; i++) {
                        if (gradeTemp[i] == null && gradeTemp[i].isEmpty()) {
                            isHaveNull = true;
                        }
                    }
                    if(!isHaveNull) {
                        AlertDialog.Builder yesno = new AlertDialog.Builder(getActivity());
                        yesno.setTitle("Save Edited Grade?");
                        yesno.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int x) {
                                boolean isHaveNull = false;
                                for(int i=0;i<gradeTemp.length;i++) {
                                    if(gradeTemp[i].isEmpty() || gradeTemp[i] == null){
                                        isHaveNull = true;
                                    }
                                }
                                if(isHaveNull == false) {
                                    if (isPrelimActive) {
                                        for (int i = 0; i < pGid.size(); i++) {
                                            db.updateGrade("Prelim", pGid.get(i), Double.parseDouble(gradeTemp[i]));
                                        }
                                        updateGrade("Prelim");
                                    } else if (isMidtermActive) {
                                        for (int i = 0; i < mGid.size(); i++) {
                                            db.updateGrade("Midterm", mGid.get(i), Double.parseDouble(gradeTemp[i]));
                                        }
                                        updateGrade("Midterm");
                                    } else if (isFinalsActive) {
                                        for (int i = 0; i < fGid.size(); i++) {
                                            db.updateGrade("Finals", fGid.get(i), Double.parseDouble(gradeTemp[i]));
                                        }
                                        updateGrade("Finals");
                                    }
                                    dialogGrade.dismiss();
                                    //refresh grade
                                    gradeList.setAdapter(new ViewGradeAdapter());
                                    ((MainActivity)getActivity()).showSnackBar("You have successfully updated the data.");
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
                                    ((MainActivity)getActivity()).startService();
                                } else {
                                    showSnackBar("All Fields cannot be empty.", layout);
                                }
                            }
                        });
                        yesno.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        yesno.show();
                    } else {
                        showSnackBar("Grades Cannot be empty. Please Complete all grade", layout);
                    }
                }
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogGrade.dismiss();
            }
        });

        dialogGrade.setView(viewGrade);
        dialogGrade.show();
        dialogGrade.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialogGrade.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public void updateGrade(String term){
        double average = 0.0;
        db.getCategory(db.loginID.get(0), selectedSubject);
        for(int x=0;x<db.category.size();x++) {
            db.getPercentage(db.loginID.get(0), db.category_id.get(x));
            db.getGrade(term, selectedStudentNumber, selectedSubject, db.category_id.get(x));
            int sum = 0;
            if(db.grade.size() > 0){
                for(int y=0;y<db.grade.size();y++){
                    sum += db.grade.get(y);
                }
                average += (sum / db.grade.size()) * db.percentage.get(0);
            }
        }

        //update overall grade
        if(db.all_grade_prelim.size() > 0) {
            db.updateAllGrade(db.loginID.get(0), selectedStudentNumber, selectedSubject, average, term);
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
                public void afterTextChanged(final Editable editable) {
                    if(selectedGradeMethod.equals("percentage")){
                        String str = holder.grade.getText().toString();
                        String str2 = PerfectDecimal(str, 3, 2);
                        if (!str2.equals(str)) {
                            holder.grade.setText(str2);
                            int pos = holder.grade.getText().length();
                            holder.grade.setSelection(pos);
                        }
                        if(editable.toString().equals("")){
                            arrTemp[holder.ref] = "";
                        } else {
                            arrTemp[holder.ref] = holder.grade.getText().toString();
                        }
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
                holder.prelim = (TextView) contextView.findViewById(R.id.examPrelim);
                holder.midterm = (TextView) contextView.findViewById(R.id.examMidterm);
                holder.finals = (TextView) contextView.findViewById(R.id.examFinals);
                contextView.setTag(holder);
            }else{
                holder = (ViewGradeViewHolder)  contextView.getTag();
            }
            holder.pos = position;
            holder.name.setText(db.student.get(position));

            //Prelim Grade
            db.getGrade("Prelim", db.studentid.get(position), selectedSubject, selectedCategoryId);
            if(db.grade.size() > 0) {
                double p = 0;
                for (int i = 0; i < db.grade.size(); i++) {
                    p += db.grade.get(i);
                }
                p = p / db.grade.size();
                if(p != 0) {
                    holder.prelim.setText("" + format.format(p));
                } else {
                    holder.prelim.setText("0.0");
                }
                if(Math.round(p) >= db.passingGradeValue.get(0)){
                    holder.prelim.setTextColor(Color.parseColor("#2196F3"));
                } else {
                    holder.prelim.setTextColor(Color.parseColor("#F44336"));
                }
            }
            //Midterm Grade
            db.getGrade("Midterm", db.studentid.get(position), selectedSubject, selectedCategoryId);
            if(db.grade.size() > 0) {
                double m = 0;
                for (int i = 0; i < db.grade.size(); i++) {
                    m += db.grade.get(i);
                }
                m = m / db.grade.size();
                if(m != 0) {
                    holder.midterm.setText("" + format.format(m));
                } else {
                    holder.midterm.setText("0.0");
                }
                if(Math.round(m) >= db.passingGradeValue.get(0)){
                    holder.midterm.setTextColor(Color.parseColor("#2196F3"));
                } else {
                    holder.midterm.setTextColor(Color.parseColor("#F44336"));
                }
            }
            //Finals Grade
            db.getGrade("Finals", db.studentid.get(position), selectedSubject, selectedCategoryId);
            if(db.grade.size() > 0) {
                double f = 0;
                for (int i = 0; i < db.grade.size(); i++) {
                    f += db.grade.get(i);
                }
                f = f / db.grade.size();
                if(f != 0) {
                    holder.finals.setText("" + format.format(f));
                } else {
                    holder.finals.setText("0.0");
                }
                if(Math.round(f) >= db.passingGradeValue.get(0)){
                    holder.finals.setTextColor(Color.parseColor("#2196F3"));
                } else {
                    holder.finals.setTextColor(Color.parseColor("#F44336"));
                }
            }

            //perform onClick listener
            CardView gradeInfo = (CardView) contextView.findViewById(R.id.grade);
            gradeInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String name = holder.name.getText().toString();
                    selectedStudentNumber = db.studentid.get(position);
                    pGid.clear(); mGid.clear(); fGid.clear();
                    pCaption.clear(); mCaption.clear(); fCaption.clear();
                    pdate.clear(); mdate.clear(); fdate.clear();
                    prelimGrade.clear(); midtermGrade.clear(); finalsGrade.clear();
                    db.getGrade("Prelim", db.studentid.get(position), selectedSubject, selectedCategoryId);
                    pGid.addAll(db.grade_id);
                    pCaption.addAll(db.caption);
                    pdate.addAll(db.date);
                    prelimGrade.addAll(db.grade);
                    db.getGrade("Midterm", db.studentid.get(position), selectedSubject, selectedCategoryId);
                    mGid.addAll(db.grade_id);
                    mCaption.addAll(db.caption);
                    mdate.addAll(db.date);
                    midtermGrade.addAll(db.grade);
                    db.getGrade("Finals", db.studentid.get(position), selectedSubject, selectedCategoryId);
                    fGid.addAll(db.grade_id);
                    fCaption.addAll(db.caption);
                    fdate.addAll(db.date);
                    finalsGrade.addAll(db.grade);
                    ViewGradeInfo(name, prelimGrade.size(), midtermGrade.size(), finalsGrade.size());
                }
            });
            return contextView;
        }
    }
    public class ViewGradeViewHolder {
        TextView name, prelim, midterm, finals;
        int pos;
    }

    //to view grade in details
    private class GradeDetails extends BaseAdapter {

        @Override
        public int getCount() {
            if(isPrelimActive){
                if(prelimGrade != null && prelimGrade.size() != 0){
                    return prelimGrade.size();
                }
            } else if(isMidtermActive){
                if(midtermGrade != null && midtermGrade.size() != 0){
                    return midtermGrade.size();
                }
            } else if(isFinalsActive){
                if(finalsGrade != null && finalsGrade.size() != 0){
                    return finalsGrade.size();
                }
            }
            return 0;
        }

        @Override
        public Object getItem(int i) {
            if(isPrelimActive){
                return prelimGrade.get(i);
            } else if(isMidtermActive){
                return midtermGrade.get(i);
            } else if(isFinalsActive){
                return finalsGrade.get(i);
            }

            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View contextView, ViewGroup viewGroup) {
            final GradeDetailsHolder holder;
            if(contextView == null){
                holder = new GradeDetailsHolder();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                contextView = inflater.inflate(R.layout.data_grade_info, null);
                holder.caption = (TextView) contextView.findViewById(R.id.caption);
                holder.dateLabel = (TextView) contextView.findViewById(R.id.date);
                holder.gradeText = (EditText) contextView.findViewById(R.id.gradeEditText);
                contextView.setTag(holder);
            }else{
                holder = (GradeDetailsHolder)  contextView.getTag();
            }
            holder.pos = position;
            if(isPrelimActive){
                holder.caption.setText(pCaption.get(position));
                holder.dateLabel.setText(pdate.get(position));
                if(gradeTemp[position] == null){
                    holder.gradeText.setText(prelimGrade.get(position).toString());
                }else{
                    holder.gradeText.setText(""+gradeTemp[position]);
                }
            } else if(isMidtermActive){
                holder.caption.setText(mCaption.get(position));
                holder.dateLabel.setText(mdate.get(position));
                if(gradeTemp[position] == null){
                    holder.gradeText.setText(midtermGrade.get(position).toString());
                }else{
                    holder.gradeText.setText(""+gradeTemp[position]);
                }
            } else if(isFinalsActive){
                holder.caption.setText(fCaption.get(position));
                holder.dateLabel.setText(fdate.get(position));
                if(gradeTemp[position] == null){
                    holder.gradeText.setText(finalsGrade.get(position).toString());
                }else{
                    holder.gradeText.setText(""+gradeTemp[position]);
                }
            }
            holder.gradeText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    String str = holder.gradeText.getText().toString();
                    String str2 = PerfectDecimal(str, 3, 2);
                    if (!str2.equals(str)) {
                        holder.gradeText.setText(str2);
                        int pos = holder.gradeText.getText().length();
                        holder.gradeText.setSelection(pos);
                    }
                    gradeTemp[holder.pos] = holder.gradeText.getText().toString();
                }
            });
            final CardView tooltip = (CardView) contextView.findViewById(R.id.showTooltip);
            tooltip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(isPrelimActive){
                        db.getRawGrade(pGid.get(position));
                    } else if(isMidtermActive){
                        db.getRawGrade(mGid.get(position));
                    } else if(isFinalsActive){
                        db.getRawGrade(fGid.get(position));
                    }
                    Toast toast = Toast.makeText(view.getContext(), "Raw Score: "+db.rawGrade.get(0), Toast.LENGTH_SHORT);
                    MainActivity.Tooltip(view, toast);
                    toast.show();
                }
            });


            if(isPrelimActive){
                db.getRawGrade(pGid.get(position));
            } else if(isMidtermActive){
                db.getRawGrade(mGid.get(position));
            } else if(isFinalsActive){
                db.getRawGrade(fGid.get(position));
            }
            if(db.rawGrade.size() > 0){
                tooltip.setVisibility(View.VISIBLE);
            } else {
                tooltip.setVisibility(View.GONE);
            }
            return contextView;
        }
    }
    public class GradeDetailsHolder {
        TextView dateLabel, caption;
        EditText gradeText;
        int pos;
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

    //save grade
    public class SaveGrade extends AsyncTask<String, String, String> {

        String msg="";

        @Override
        protected void onPreExecute(){
            progress = ProgressDialog.show(getActivity(), null, "Please wait!");
        }

        @Override
        protected String doInBackground(String... strings) {
            String date = new SimpleDateFormat("MM-dd-yyyy").format(new Date());
            if(addTitle.equals("")){
                db.getGrade(term, db.studentid.get(0), selectedSubject, selectedCategoryId);
                int i = db.grade.size() + 1;
                addTitle = categoryName + " " + i;
            }
            for (int ii = 0; ii < db.student.size(); ii++) {
                if(arrTemp[ii].isEmpty()){
                    System.out.println("Empty: "+arrTemp[ii]);
                } else {
                    System.out.println("Not Empty: "+arrTemp[ii]);
                }
                if(arrTemp[ii].isEmpty() || arrTemp[ii].equals("")){
                    String description = "This student has no grade in your "+addTitle+".";
                    db.setNotification(db.loginID.get(0), db.studentid.get(ii), db.student.get(ii), selectedSubject, description, "NG", "UNREAD", "blank");
                    if(selectedGradeMethod.equals("percentage")){
                        db.setGrade(term, db.loginID.get(0), db.studentid.get(ii), selectedSubject, selectedCategoryId, addTitle, date, 0f);
                    } else {
                        db.setGrade(term, db.loginID.get(0), db.studentid.get(ii), selectedSubject, selectedCategoryId, addTitle, date, 0f);
                        db.getGrade(term, db.studentid.get(ii), selectedSubject, selectedCategoryId);
                        db.setRawGrade(db.loginID.get(0), db.grade_id.get((db.grade_id.size() - 1)), arrTemp[ii]+"/"+maxScore);
                    }
                } else {
                    if(selectedGradeMethod.equals("percentage")){
                        db.setGrade(term, db.loginID.get(0), db.studentid.get(ii), selectedSubject, selectedCategoryId, addTitle, date, Double.parseDouble(arrTemp[ii]));
                    } else {
                        float grade = Integer.parseInt(arrTemp[ii]);
                        grade = grade / maxScore * 40 + 60;
                        db.setGrade(term, db.loginID.get(0), db.studentid.get(ii), selectedSubject, selectedCategoryId, addTitle, date, grade);
                        db.getGrade(term, db.studentid.get(ii), selectedSubject, selectedCategoryId);
                        db.setRawGrade(db.loginID.get(0), db.grade_id.get((db.grade_id.size() - 1)), arrTemp[ii]+"/"+maxScore);
                    }
                }
                int size = db.student.size() - 1;
                currentString = "Inserting Grade: " + ii + " / " + size;
                getActivity().runOnUiThread(changeMessage);
            }
            //update/add updated date
            db.getUpdateDate(db.loginID.get(0));
            if(db.updateDate.size() == 0){
                db.setUpdatedDate(db.loginID.get(0), date);
            } else{
                db.updateUpdatedDate(db.loginID.get(0), date);
            }
            db.getSettings(db.loginID.get(0));
            if(db.settingsAutoUpload.get(0) == 1 && ((MainActivity)getActivity()).isNetworkAvailable()){
                UploadData("add");
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg){
            isNoEmptyGrade = false;
            progress.dismiss();
            addDialog.dismiss();

            //refresh grade
            db.getStudent(db.loginID.get(0), selectedSubject);
            gradeList.setAdapter(new ViewGradeAdapter());

            //refreshNotificationText
            ((MainActivity)getActivity()).refreshNotificationText();

            //display snackbar msg
            ((MainActivity)getActivity()).showSnackBar("Done Saving Grade!");
            ((MainActivity)getActivity()).startService();

            db.getSettings(db.loginID.get(0));
            if(db.settingsAutoDownload.get(0) == 1){
                ((MainActivity)getActivity()).isRunning = true;
            }
        }
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