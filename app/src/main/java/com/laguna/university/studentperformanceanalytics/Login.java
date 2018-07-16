package com.laguna.university.studentperformanceanalytics;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Login extends AppCompatActivity {

    private EditText username,password;
    Button scan;
    CardView loginBtn;

    SQLiteDBcontroller db  = new SQLiteDBcontroller(Login.this);

    ProgressDialog progress;
    String currentString = "";
    String gg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //setting content view
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        loginBtn = (CardView) findViewById(R.id.loginBtn);
        scan = (Button) findViewById(R.id.scan);

        //username.setText("jerome");
        //password.setText("jerome");

        try{
            String error = getIntent().getExtras().getString("prompt").toString();
            Toast.makeText(Login.this,error,Toast.LENGTH_LONG).show();
        } catch(Exception e){
        }

        db.profList();
        /////////////////////
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(username.getText().length() > 0 && password.getText().length() > 0){
                    db.login(username.getText().toString(), password.getText().toString());
                    if(db.loginID.size() != 0){
                        Intent intent = new Intent(Login.this, MainActivity.class);
                        intent.putExtra("username",username.getText().toString());
                        intent.putExtra("password", password.getText().toString());
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Login.this,"No Account Found please Check username or password",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(Login.this,"Username or Password Cannot be Empty!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Login.this);
                alertDialog.setCancelable(false);
                alertDialog.setMessage("Do you want to Scan Data of server?");
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // update client data
                        try {
                            ReceiveServerData data = new ReceiveServerData();
                            data.execute("");
                        }catch(Exception ex){
                            ex.printStackTrace();
                        }
                    }
                });
                alertDialog.show();
            }
        });
    }

    public class ReceiveServerData extends AsyncTask<String, String, String> {

        String msg;
        String user = username.getText().toString();
        String pass = password.getText().toString();

        //sem and ay
        String sem, ay;
        boolean isSuccess = false;

        @Override
        protected void onPreExecute(){
            progress = ProgressDialog.show(Login.this, null, "Getting server data please wait..");  //show a progress dialog
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                URL url = new URL(mysqlConnectionURI.loginScanData);
                //Opening the URL using HttpURLConnection
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                //StringBuilder object to read the string from the service
                StringBuilder sb = new StringBuilder();
                //We will use a buffered reader to read the string from service
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
                //A simple string to read values from each line
                String json;
                //reading until we don't find null
                while ((json = bufferedReader.readLine()) != null) {
                    //appending it to string builder
                    sb.append(json);
                }

                JSONObject jsonObject = new JSONObject(sb.toString());
                msg = "Retrieving account Failed Check Connection and try again.";
                //sem and ay
                sem = jsonObject.getString("sem");
                ay = jsonObject.getString("ay");
                db.getSemAndAY();
                if(db.sem.size() == 0){
                    db.setSemAndAY(sem, ay);
                } else {
                    db.updateSemAndAY(sem, ay);
                }


                //student information
                JSONArray qs_id = jsonObject.getJSONArray("qs_id");
                JSONArray qs_studentno = jsonObject.getJSONArray("qs_studentno");
                JSONArray qs_name = jsonObject.getJSONArray("qs_name");
                JSONArray qs_gender = jsonObject.getJSONArray("qs_gender");
                JSONArray qs_course = jsonObject.getJSONArray("qs_course");
                JSONArray qs_year = jsonObject.getJSONArray("qs_year");
                JSONArray qs_profid = jsonObject.getJSONArray("qs_profid");
                JSONArray qs_cc = jsonObject.getJSONArray("qs_cc");
                if(qs_id.length() > 0){
                    db.truncateStudent();
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }
                for(int i=0;i<qs_id.length();i++){
                    db.insertStudent(Integer.parseInt(qs_id.get(i).toString()), qs_studentno.get(i).toString(), qs_name.get(i).toString(), qs_gender.get(i).toString(),
                            qs_course.get(i).toString(), qs_year.get(i).toString(), Integer.parseInt(qs_profid.get(i).toString()), qs_cc.get(i).toString());
                }

                //subject data
                JSONArray qsub_id = jsonObject.getJSONArray("qsub_id");
                JSONArray qsub_profid = jsonObject.getJSONArray("qsub_profid");
                JSONArray qsub_sc = jsonObject.getJSONArray("qsub_sc");
                JSONArray qsub_sd = jsonObject.getJSONArray("qsub_sd");
                if(qsub_id.length() > 0){
                    db.truncateClass();
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }
                for(int i=0;i<qsub_id.length();i++){
                    db.insertClass(Integer.parseInt(qsub_id.get(i).toString()), Integer.parseInt(qsub_profid.get(i).toString()),
                            qsub_sc.get(i).toString(), qsub_sd.get(i).toString());
                }

                //teacher information
                JSONArray qt_id = jsonObject.getJSONArray("qt_id");
                JSONArray qt_fn = jsonObject.getJSONArray("qt_fn");
                JSONArray qt_ln = jsonObject.getJSONArray("qt_ln");
                JSONArray qt_user = jsonObject.getJSONArray("qt_user");
                JSONArray qt_pass = jsonObject.getJSONArray("qt_pass");
                if(qt_id.length() > 0 && isSuccess){
                    db.truncateUser();
                }
                for(int i=0;i<qt_id.length();i++){
                    db.insertProf(Integer.parseInt(qt_id.get(i).toString()), qt_fn.get(i).toString(),
                            qt_ln.get(i).toString(), qt_user.get(i).toString(), qt_pass.get(i).toString());
                }
                msg = "Server Data Retrieve Successfully";
            } catch (Exception e) {
                e.printStackTrace();
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg){
            Toast.makeText(Login.this,msg,Toast.LENGTH_SHORT).show();
            progress.dismiss();
            db.login(user,pass);
        }
    }

    private Runnable changeMessage = new Runnable() {
        @Override
        public void run() {
            progress.setMessage(currentString);
        }
    };
}
