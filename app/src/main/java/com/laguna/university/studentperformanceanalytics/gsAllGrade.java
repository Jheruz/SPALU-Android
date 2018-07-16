package com.laguna.university.studentperformanceanalytics;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.Date;

public class gsAllGrade extends Fragment {

    SQLiteDBcontroller db;
    DecimalFormat format = new DecimalFormat("##.00");

    String studentAdapterValue[] = {"No Student Found. Wait for the admin to Add your student"};

    String selectedSubject = "";
    String selectedSubjectDescription = "";
    String[] gradeTemp;
    String term;
    boolean isSemDone = false;

    ProgressDialog progress;
    String currentString = "";

    TextView submitText, displayStudentCount, displaySubject;
    CardView sortName, sortGrade;
    CardView submit;
    ListView data;

    int sortIdName = 1;
    int sortIdGrade = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.fragment_gs_all_grade, parent, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        db = new SQLiteDBcontroller(getActivity());
        selectedSubject = ((MainActivity)getActivity()).selectedSubject;
        selectedSubjectDescription = ((MainActivity)getActivity()).selectedSubjectDescription;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        submit = (CardView) view.findViewById(R.id.add);
        submitText = (TextView) view.findViewById(R.id.addText);
        displaySubject = (TextView) view.findViewById(R.id.displaySubject);
        displayStudentCount = (TextView) view.findViewById(R.id.displayStudentCount);
        sortName = (CardView) view.findViewById(R.id.sortName);
        sortGrade = (CardView) view.findViewById(R.id.sortGrade);
        data = (ListView) view.findViewById(R.id.studentList);
        db.login(((MainActivity)getActivity()).user, ((MainActivity)getActivity()).pass);
        gradeTemp = new String[db.student.size()];
        db.classList(db.loginID.get(0));
        db.getTerm(db.loginID.get(0), db.subcode.get(0));
        try {
            if(selectedSubject.isEmpty()){
                selectedSubject = db.subcode.get(0);
                selectedSubjectDescription = db.subDescription.get(0);
            }
            db.getStudent(db.loginID.get(0), selectedSubject);
            db.getAllGrade(db.loginID.get(0), selectedSubject, "Grade", 0);
            data.setAdapter(new GradeListAdapter());
        }catch(Exception ex){
            data.setAdapter(new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,studentAdapterValue));
            ex.printStackTrace();
        }

        displaySubject.setText("Subject: "+selectedSubject);
        displayStudentCount.setText("Total Student: "+db.student.size());

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSemDone){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle("Upload?");
                    dialog.setMessage("Are you sure you want to upload grade to server?");
                    dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            uploadToServer upload = new uploadToServer();
                            upload.execute("");
                        }
                    });
                    dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    dialog.show();
                } else {
                    ((MainActivity)getActivity()).showSnackBar("You can only submit grade after final exam.");
                }
            }
        });

        sortName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sortIdName == 0){
                    db.getAllGrade(db.loginID.get(0), selectedSubject, "Name", sortIdName);
                    sortIdName = 1;
                } else {
                    db.getAllGrade(db.loginID.get(0), selectedSubject, "Name", sortIdName);
                    sortIdName = 0;
                }
                data.setAdapter(new GradeListAdapter());
                sortIdGrade = 0;
            }
        });

        sortGrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(sortIdGrade == 1){
                    db.getAllGrade(db.loginID.get(0), selectedSubject, "Grade", sortIdGrade);
                    sortIdGrade = 0;
                } else {
                    db.getAllGrade(db.loginID.get(0), selectedSubject, "Grade", sortIdGrade);
                    sortIdGrade = 1;
                }
                data.setAdapter(new GradeListAdapter());
                sortIdName = 0;
            }
        });
        checkTerm();
    }

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
        checkSubmitButton();
    }

    public void checkSubmitButton(){
        if(term.equalsIgnoreCase("Submit Grade")){
            isSemDone = true;
            submitText.setText("Submit");
        } else {
            isSemDone = false;
            submitText.setText("Submit (Disable)");
        }
    }

    private Runnable changeMessage = new Runnable() {
        @Override
        public void run() {
            progress.setMessage(currentString);
        }
    };

    public class GradeListAdapter extends BaseAdapter{

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
        public View getView(final int position, View view, ViewGroup viewGroup) {
            final GradeViewHolder holder;
            if(view == null){
                holder = new GradeViewHolder();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                view = inflater.inflate(R.layout.display_all_grade_handler, null);
                holder.nameHolder = (TextView) view.findViewById(R.id.name);
                holder.prelimHolder = (TextView) view.findViewById(R.id.prelim);
                holder.prelimEquivalentHolder = (TextView) view.findViewById(R.id.prelimEquivalent);
                holder.midtermHolder = (TextView) view.findViewById(R.id.midterm);
                holder.midtermEquivalentHolder = (TextView) view.findViewById(R.id.midtermEquivalent);
                holder.finalHolder = (TextView) view.findViewById(R.id.finalterm);
                holder.finalsEquivalentHolder = (TextView) view.findViewById(R.id.finalsEquivalent);
                holder.averageHolder = (TextView) view.findViewById(R.id.average);
                holder.remarksHolder = (TextView) view.findViewById(R.id.remarks);
                view.setTag(holder);
            }else{
                holder = (GradeViewHolder)  view.getTag();
            }
            holder.pos = position;
            db.getPassingGrade(db.loginID.get(0));
            if(db.all_grade_prelim.size() > 0){
                holder.nameHolder.setText(db.all_grade_student_name.get(position));
                if(db.all_grade_prelim.get(position) == 0){
                    holder.prelimHolder.setText("0.00");
                } else {
                    holder.prelimHolder.setText("" + format.format(db.all_grade_prelim.get(position)));
                }
                holder.prelimHolder.setTextColor(Color.parseColor(((MainActivity)getActivity()).EquivalentColor(Math.round(db.all_grade_prelim.get(position)))));
                holder.prelimEquivalentHolder.setText("" + GradeEquivalent(Math.round(db.all_grade_prelim.get(position))));
                holder.prelimEquivalentHolder.setTextColor(Color.parseColor(((MainActivity)getActivity()).EquivalentColor(Math.round(db.all_grade_prelim.get(position)))));
                if(db.all_grade_midterm.size() > 0){
                    if(db.all_grade_midterm.get(position) == 0){
                        holder.midtermHolder.setText("0.00");
                    } else {
                        holder.midtermHolder.setText("" + format.format(db.all_grade_midterm.get(position)));
                    }
                    holder.midtermHolder.setTextColor(Color.parseColor(((MainActivity)getActivity()).EquivalentColor(Math.round(db.all_grade_midterm.get(position)))));
                    holder.midtermEquivalentHolder.setText("" + GradeEquivalent(Math.round(db.all_grade_midterm.get(position))));
                    holder.midtermEquivalentHolder.setTextColor(Color.parseColor(((MainActivity)getActivity()).EquivalentColor(Math.round(db.all_grade_midterm.get(position)))));
                }
                if(db.all_grade_finals.size() > 0){
                    if(db.all_grade_finals.get(position) == 0){
                        holder.finalHolder.setText("0.00");
                    } else {
                        holder.finalHolder.setText("" + format.format(db.all_grade_finals.get(position)));
                    }
                    holder.finalHolder.setTextColor(Color.parseColor(((MainActivity)getActivity()).EquivalentColor(Math.round(db.all_grade_finals.get(position)))));
                    holder.finalsEquivalentHolder.setText("" + GradeEquivalent(Math.round(db.all_grade_finals.get(position))));
                    holder.finalsEquivalentHolder.setTextColor(Color.parseColor(((MainActivity)getActivity()).EquivalentColor(Math.round(db.all_grade_finals.get(position)))));
                }
                if(db.all_grade_average.get(position) == 0){
                    holder.averageHolder.setText("0.00");
                } else {
                    holder.averageHolder.setText(""+format.format(db.all_grade_average.get(position)));
                }
                holder.averageHolder.setTextColor(Color.parseColor(((MainActivity)getActivity()).EquivalentColor(Math.round(db.all_grade_average.get(position)))));
                holder.remarksHolder.setText(db.all_grade_remarks.get(position));
                if(db.all_grade_remarks.get(position).equalsIgnoreCase("Passed!")){
                    holder.remarksHolder.setTextColor(Color.parseColor(((MainActivity)getActivity()).EquivalentColor(100)));
                } else {
                    holder.remarksHolder.setTextColor(Color.parseColor(((MainActivity)getActivity()).EquivalentColor(74)));
                }
            } else {
                holder.nameHolder.setText(db.student.get(position));
            }

            CardView cv = (CardView) view.findViewById(R.id.stats);
            cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), Individual_Statistic.class);
                    if(db.all_grade_prelim.size() > 0){
                        intent.putExtra("studentid", db.all_grade_student_no.get(position));
                    } else {
                        intent.putExtra("studentid", db.studentid.get(position));
                    }
                    intent.putExtra("name",holder.nameHolder.getText().toString());
                    intent.putExtra("subject",selectedSubject);
                    intent.putExtra("username", ((MainActivity)getActivity()).user);
                    intent.putExtra("password", ((MainActivity)getActivity()).pass);
                    startActivity(intent);
                }
            });
            return view;
        }
    }
    public class GradeViewHolder {
        TextView nameHolder;
        TextView prelimHolder, prelimEquivalentHolder;
        TextView midtermHolder, midtermEquivalentHolder;
        TextView finalHolder, finalsEquivalentHolder;
        TextView averageHolder;
        TextView remarksHolder;
        int pos;
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

    public class uploadToServer extends AsyncTask<String, String, String> {

        //0 is failed 1 is success
        int status = 0;
        String msg="";

        mysqlConnectionURI mysqlconnect = new mysqlConnectionURI();

        @Override
        protected void onPreExecute(){
            progress = ProgressDialog.show(getActivity(), null, "Please wait!");  //show a progress dialog
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                //arraylist for postdata
                ArrayList<NameValuePair> PostData = new ArrayList<>();

                //student grade
                JSONArray studentGrade = new JSONArray();
                for(int i=0;i<db.student.size();i++) {
                    db.getAllGradeBySelectedStudent(db.loginID.get(0), selectedSubject, db.studentid.get(i));
                    JSONObject data = new JSONObject();
                    data.put("g_studentno", db.studentid.get(i));
                    data.put("g_prelim",  String.valueOf(db.all_grade_prelim.get(0)));
                    data.put("g_midterm",  String.valueOf(db.all_grade_midterm.get(0)));
                    data.put("g_finals",  String.valueOf(db.all_grade_finals.get(0)));
                    data.put("g_average",  String.valueOf(db.all_grade_average.get(0)));
                    data.put("g_remarks",  db.all_grade_remarks.get(0));
                    studentGrade.put(data);
                    System.out.println(db.loginID.get(0));
                    System.out.println("Submit Grade: "+i+"/"+studentGrade);
                }
                if(studentGrade.length() > 0){
                    PostData.add(new BasicNameValuePair("student_grade", studentGrade.toString()));
                }

                //prof data
                JSONArray profData = new JSONArray();
                JSONObject profObject = new JSONObject();
                profObject.put("prof_name", db.loginName.get(0));
                profData.put(profObject);
                PostData.add(new BasicNameValuePair("prof_data", profData.toString()));
                System.out.println("Prof Data: 1/1");

                //send the data
                String ay = ((MainActivity)getActivity()).completeAY;
                String sem = ((MainActivity)getActivity()).sem;
                String date = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
                PostData.add(new BasicNameValuePair("prof_id", String.valueOf(db.loginID.get(0))));
                PostData.add(new BasicNameValuePair("sem", sem));
                PostData.add(new BasicNameValuePair("ay", ay));
                PostData.add(new BasicNameValuePair("date", date));
                PostData.add(new BasicNameValuePair("subject", selectedSubject));
                System.out.println(sem + " | " + ay + " | " + date + " | " + selectedSubject);
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(mysqlConnectionURI.UploadGrade);
                httppost.setEntity(new UrlEncodedFormEntity(PostData));
                HttpResponse response = httpclient.execute(httppost);
                response.getEntity().consumeContent();

                db.truncateArchive(db.loginID.get(0), selectedSubject);
                for(int i=0;i<db.student.size();i++) {
                    db.getAllGradeBySelectedStudent(db.loginID.get(0), selectedSubject, db.studentid.get(i));
                    db.setArchive(db.studentid.get(i), db.student.get(i), db.year.get(i), db.loginID.get(0), selectedSubject, selectedSubjectDescription, ay, sem, Math.round(db.all_grade_prelim.get(0)), Math.round(db.all_grade_midterm.get(0)), Math.round(db.all_grade_finals.get(0)), Math.round(db.all_grade_average.get(0)), GradeEquivalent(Math.round(db.all_grade_average.get(0))));
                }

                db.deleteAllStudentInClass(db.loginID.get(0), selectedSubject);
                db.deleteClassNotification(db.loginID.get(0), selectedSubject);
                db.truncateGrade("Prelim", db.loginID.get(0));
                db.truncateGrade("Midterm", db.loginID.get(0));
                db.truncateGrade("Finals", db.loginID.get(0));
                db.truncateRawGrade(db.loginID.get(0));
                db.truncateGrateToSubmit(db.loginID.get(0));

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
            //dito lagy ung move to archive
            progress.dismiss();
            if(status == 1){
                Success("Upload Complete", "This class is move to archive because it successfully uploaded to server.");
            } else {
                Success("Upload Failed", msg);
            }
            checkSubmitButton();
        }
    }

    public void Success(final String title, String msg){
        AlertDialog.Builder successDialog = new AlertDialog.Builder(getActivity());
        successDialog.setTitle(title);
        successDialog.setMessage(msg);
        successDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(title.equals("Upload Complete")){
                    Intent intent = getActivity().getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    getActivity().finish();
                    startActivity(intent);
                }
            }
        });
        successDialog.show();
    }
}
