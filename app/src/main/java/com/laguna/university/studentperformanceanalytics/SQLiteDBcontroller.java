package com.laguna.university.studentperformanceanalytics;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class SQLiteDBcontroller extends SQLiteOpenHelper{

    //login info
    ArrayList<String> username = new ArrayList<>();
    ArrayList<String> password = new ArrayList<>();

    //for search data
    ArrayList<String> searchStudentNo = new ArrayList<>();
    ArrayList<String> searchStudentName = new ArrayList<>();
    ArrayList<String> searchStudentSubject = new ArrayList<>();
    //notification
    ArrayList<Integer> notificationId = new ArrayList<>();
    ArrayList<String> notificationStudentId = new ArrayList<>();
    ArrayList<String> notificationName = new ArrayList<>();
    ArrayList<String> notificationSubjectCode = new ArrayList<>();
    ArrayList<String> notificationDesc = new ArrayList<>();
    ArrayList<String> notificationType = new ArrayList<>();
    ArrayList<String> notificationStatus = new ArrayList<>();
    ArrayList<String> notificationTag = new ArrayList<>();
    //term
    ArrayList<Integer> term = new ArrayList<>();
    //get all grade
    ArrayList<String> all_grade_student_no = new ArrayList<>();
    ArrayList<String> all_grade_student_name = new ArrayList<>();
    ArrayList<Float> all_grade_prelim = new ArrayList<>();
    ArrayList<Float> all_grade_midterm = new ArrayList<>();
    ArrayList<Float> all_grade_finals = new ArrayList<>();
    ArrayList<Float> all_grade_average = new ArrayList<>();
    ArrayList<String> all_grade_remarks = new ArrayList<>();
    //settigs
    ArrayList<Integer> settingsAutoUpload = new ArrayList<>();
    ArrayList<Integer> settingsAutoDownload = new ArrayList<>();
    //passing grade storage
    ArrayList<Integer> passingGradeValue = new ArrayList<>();
    //list for grade value handler
    ArrayList<Integer> category_id = new ArrayList<>();
    ArrayList<String> category = new ArrayList<>();
    ArrayList<Integer> grade_id = new ArrayList<>();
    ArrayList<String> date = new ArrayList<>();
    ArrayList<String> caption = new ArrayList<>();
    ArrayList<Float> grade = new ArrayList<>();
    ArrayList<Integer> rawGradeid = new ArrayList<>();
    ArrayList<String> rawGrade = new ArrayList<>();
    //list for student
    ArrayList<String> studentid = new ArrayList<>();
    ArrayList<String> student = new ArrayList<>();
    ArrayList<String> year = new ArrayList<>();
    //list for prof account
    ArrayList<String> prof = new ArrayList<>();
    ArrayList<Integer> loginID = new ArrayList<>();
    ArrayList<String> loginName = new ArrayList<>();
    //list for class
    ArrayList<String> subcode = new ArrayList<>();
    ArrayList<String> subDescription = new ArrayList<>();
    //list for grade percentage
    ArrayList<Float> percentage = new ArrayList<>();
    //average
    ArrayList<Float> average = new ArrayList<>();
    ArrayList<Float> tempPrelim = new ArrayList<>();
    ArrayList<Float> tempMidterm = new ArrayList<>();
    ArrayList<Float> tempFinals = new ArrayList<>();
    //update date
    ArrayList<String> updateDate = new ArrayList<>();
    //archive variable
    ArrayList<String> archive_student_no = new ArrayList<>();
    ArrayList<String> archive_student_name = new ArrayList<>();
    ArrayList<Float> archive_prelim = new ArrayList<>();
    ArrayList<Float> archive_midterm = new ArrayList<>();
    ArrayList<Float> archive_finals= new ArrayList<>();
    ArrayList<Float> archive_average= new ArrayList<>();
    ArrayList<Float> archive_equivalent= new ArrayList<>();
    ArrayList<String> archive_sem = new ArrayList<>();
    ArrayList<String> archive_year = new ArrayList<>();
    ArrayList<String> archive_ay = new ArrayList<>();
    ArrayList<String> archive_subject = new ArrayList<>();
    ArrayList<String> archive_subject_description = new ArrayList<>();
    //sync date
    ArrayList<String> syncDate = new ArrayList<>();
    ArrayList<String> uploadDate = new ArrayList<>();
    //prof data
    ArrayList<String> profDataStudentNo = new ArrayList<>();
    ArrayList<String> profDataSubjectCode = new ArrayList<>();
    ArrayList<Integer> profDataCategoryID= new ArrayList<>();
    ArrayList<String> profDataCaption= new ArrayList<>();
    ArrayList<String> profDataDate = new ArrayList<>();
    ArrayList<Float> profDataGrade = new ArrayList<>();
    //sem and ay
    ArrayList<String> sem = new ArrayList<>();
    ArrayList<String> ay = new ArrayList<>();

    public SQLiteDBcontroller(Context context) {
        super(context, "SPALU.db", null, 1);
    }

    //create necessary table
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE PROF_INFO( PID INTEGER PRIMARY KEY AUTOINCREMENT, FIRST_NAME TEXT, LAST_NAME TEXT, USERNAME TEXT, PASSWORD TEXT );");
        sqLiteDatabase.execSQL("CREATE TABLE STUDENT_INFO( SID INTEGER PRIMARY KEY AUTOINCREMENT, STUDENT_NO TEXT, NAME TEXT, GENDER TEXT, COURSE TEXT, YEAR TEXT, PROF_ID INTEGER, SUBJECT_CODE TEXT );");
        sqLiteDatabase.execSQL("CREATE TABLE CLASS( CID INTEGER PRIMARY KEY AUTOINCREMENT, PROF_ID INTEGER, SUBJECT_CODE TEXT, SUBJECT_DESCRIPTION TEXT );");
        //for percentage
        sqLiteDatabase.execSQL("CREATE TABLE Grade_Percentage( ID INTEGER PRIMARY KEY AUTOINCREMENT, prof_id INTEGER, category_id INTEGER, percent FLOAT );");
        //for category
        sqLiteDatabase.execSQL("CREATE TABLE Grade_Category( id INTEGER PRIMARY KEY AUTOINCREMENT, prof_id INTEGER, subject_code TEXT, name TEXT );");
        //for raw grade
        sqLiteDatabase.execSQL("CREATE TABLE RawGrade( prof_id INTEGER, grade_id INTEGER, raw_grade TEXT);");
        //for prelim
        sqLiteDatabase.execSQL("CREATE TABLE Grade_Prelim( id INTEGER PRIMARY KEY AUTOINCREMENT, prof_id INTEGER, student_no TEXT, subject_code TEXT, category_id INTEGER, caption TEXT, date TEXT, grade FLOAT );");
        //for midterm
        sqLiteDatabase.execSQL("CREATE TABLE Grade_Midterm( id INTEGER PRIMARY KEY AUTOINCREMENT, prof_id INTEGER, student_no TEXT, subject_code TEXT, category_id INTEGER, caption TEXT, date TEXT, grade FLOAT );");
        //for finals
        sqLiteDatabase.execSQL("CREATE TABLE Grade_Finals( id INTEGER PRIMARY KEY AUTOINCREMENT, prof_id INTEGER, student_no TEXT, subject_code TEXT, category_id INTEGER, caption TEXT, date TEXT, grade FLOAT );");
        //for overall
        sqLiteDatabase.execSQL("CREATE TABLE GradeToSubmit(id INTEGER PRIMARY KEY AUTOINCREMENT,  prof_id INTEGER, subject_code TEXT, student_no TEXT, prelim FLOAT, midterm FLOAT, finals FLOAT, average FLOAT, remarks TEXT );");
        //to verify term
        sqLiteDatabase.execSQL("CREATE TABLE Term( prof_id INTEGER, subject_code TEXT, current_term INTEGER );");
        //settings
        sqLiteDatabase.execSQL("CREATE TABLE settings( prof_id INTEGER, auto_upload INTEGER, auto_download INTEGER );");
        //ceiling grade
        sqLiteDatabase.execSQL("CREATE TABLE passing_grade( prof_id INTEGER, grade INTEGER );");
        //notification
        sqLiteDatabase.execSQL("CREATE TABLE notification(id INTEGER PRIMARY KEY AUTOINCREMENT, prof_id INTEGER, student_no TEXT, name TEXT, subject_code TEXT, description TEXT, type TEXT, status TEXT, tag TEXT );");
        //last update
        sqLiteDatabase.execSQL("CREATE TABLE Last_update( prof_id INTEGER, date TEXT );");
        //archive
        sqLiteDatabase.execSQL("CREATE TABLE Archive( id INTEGER PRIMARY KEY AUTOINCREMENT, student_no TEXT, name TEXT, year TEXT, prof_id INTEGER, subject_code TEXT, subject_description TEXT, ay TEXT, sem TEXT, prelim FLOAT, midterm FLOAT, finals FLOAT, average FLOAT, equivalent FLOAT );");
        //import export table
        sqLiteDatabase.execSQL("CREATE TABLE ExportImportDate( prof_id INTEGER, sync_date TEXT, upload_date TEXT )");
        //currentAY and Sem table
        sqLiteDatabase.execSQL("CREATE TABLE SemAndAY( Sem TEXT, AY TEXT )");
    }
    //drop if exist the necessary table
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST PROF_INFO;");
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST STUDENT_INFO;");
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST CLASS;");
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST GRADE_PERCENTAGE;");
        //for category
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST Grade_Category;");
        //for raw grade
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST RawGrade;");
        //for prelim
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST Grade_Prelim;");
        //for midterm
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST Grade_Midterm;");
        //for finals
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST Grade_Finals;");
        //for overall grade
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST GradeToSubmit;");
        //to verify term
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST Term;");
        //settings
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST settings;");
        //ceiling grade
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST passing_grade;");
        //notification table
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST notification;");
        //last update
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST Last_update;");
        //archive
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST Archive;");
        //import export table
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST ExportImportDate;");
        //sem and ay
        sqLiteDatabase.execSQL("DROP TABLE IF EXIST SemAndAY;");
        onCreate(sqLiteDatabase);
    }

    public void setSemAndAY(String sem, String ay){
        ContentValues contentValues = new ContentValues();
        contentValues.put("Sem",sem);
        contentValues.put("AY",ay);
        this.getWritableDatabase().insertOrThrow("SemAndAY","",contentValues);
    }
    public void updateSemAndAY(String sem, String ay){
        Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE SemAndAY set Sem = '"+sem+"', AY = '"+ay+"'",null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void getSemAndAY(){
        sem.clear(); ay.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM SemAndAY",null);
        while (cursor.moveToNext()) {
            sem.add(cursor.getString(0));
            ay.add(cursor.getString(1));
        }
        cursor.close();
    }

    //class table
    public void insertClass(int id, int profid, String code, String description){
        ContentValues contentValues = new ContentValues();
        contentValues.put("CID",id);
        contentValues.put("PROF_ID",profid);
        contentValues.put("SUBJECT_CODE",code);
        contentValues.put("SUBJECT_DESCRIPTION",description);
        this.getWritableDatabase().insertOrThrow("CLASS","",contentValues);
    }
    public void classList(int prof_id){
        subcode.clear(); subDescription.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM CLASS WHERE PROF_ID = "+prof_id,null);
        while (cursor.moveToNext()) {
            subcode.add(cursor.getString(2));
            subDescription.add(cursor.getString(3));
        }
        cursor.close();
    }
    public void truncateClass(){
        Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM CLASS",null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }

    //PROF TABLE
    public void login(String username, String password){
        loginID.clear(); loginName.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM PROF_INFO WHERE username = '"+username+"' AND password = '"+password+"'",null);
        while (cursor.moveToNext()) {
            loginID.add(cursor.getInt(0));
            loginName.add(cursor.getString(1) + " " + cursor.getString(2));
        }
        cursor.close();
    }
    public String loginFirstName(int id){
        String name = "";
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM PROF_INFO WHERE PID = "+id,null);
        while (cursor.moveToNext()) {
            name = cursor.getString(1);
        }
        cursor.close();

        return name;
    }
    public void getLoginAccount(int id){
        username.clear(); password.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM PROF_INFO WHERE PID = "+id,null);
        while (cursor.moveToNext()) {
            username.add(cursor.getString(3));
            password.add(cursor.getString(4));
        }
        cursor.close();
    }
    public void updateLoginAccount(int id, String username, String password){
        Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE PROF_INFO set USERNAME = '"+username+"', PASSWORD ='"+password+"' WHERE PID = "+id,null);
        while (cursor.moveToNext()) {
        }
        cursor.close();
    }
    public void insertProf(int id, String fn, String ln, String username, String password){
        ContentValues contentValues = new ContentValues();
        contentValues.put("PID",id);
        contentValues.put("FIRST_NAME",fn);
        contentValues.put("LAST_NAME",ln);
        contentValues.put("USERNAME",username);
        contentValues.put("PASSWORD",password);
        this.getWritableDatabase().insertOrThrow("PROF_INFO","",contentValues);
    }
    public void profList(){
        prof.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM PROF_INFO",null);
        while (cursor.moveToNext()) {
            prof.add(cursor.getString(0));
        }
        cursor.close();
    }
    public void truncateUser(){
        Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM PROF_INFO",null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }


    public String capitalizeWord(String str){
        String words[]=str.split("\\s");
        String capitalizeWord="";
        for(String w:words){
            String first=w.substring(0,1);
            String afterfirst=w.substring(1);
            System.out.println("First: "+first+" | afterfirst: "+afterfirst);
            capitalizeWord+=first.toUpperCase()+afterfirst+" ";
            System.out.println(capitalizeWord);
        }
        return capitalizeWord.trim();
    }

    //STUDENT TABLE
    public void insertStudent(int id, String student_no, String name, String gender, String course, String year, int profid, String code){
        //System.out.println(id + " | " + student_no + " | " + name + " | " + gender + " | " + course + " | " + year + " | " + profid + " | " + code);
        //name = capitalizeWord(name.toLowerCase());
        ContentValues contentValues = new ContentValues();
        contentValues.put("SID",id);
        contentValues.put("STUDENT_NO",student_no);
        contentValues.put("NAME",name);
        contentValues.put("GENDER",gender);
        contentValues.put("COURSE",course);
        contentValues.put("YEAR",year);
        contentValues.put("PROF_ID",profid);
        contentValues.put("SUBJECT_CODE",code);
        this.getWritableDatabase().insertOrThrow("STUDENT_INFO","",contentValues);
    }
    public void getStudent(int prof_id, String code){
        studentid.clear();
        student.clear();
        year.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM STUDENT_INFO WHERE PROF_ID = "+prof_id+" AND SUBJECT_CODE = '"+code+"'",null);
        while (cursor.moveToNext()) {
            studentid.add(cursor.getString(1));
            student.add(cursor.getString(2));
            year.add(cursor.getString(5));
        }
        cursor.close();
    }
    public void sortStudentName(int prof_id, String code, int value){
        if(value == 1) {
            studentid.clear();
            student.clear();
            Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM STUDENT_INFO WHERE PROF_ID = " + prof_id + " AND SUBJECT_CODE = '" + code + "' ORDER BY NAME DESC", null);
            while (cursor.moveToNext()) {
                studentid.add(cursor.getString(1));
                student.add(cursor.getString(2));
            }
            cursor.close();
        } else {
            studentid.clear();
            student.clear();
            Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM STUDENT_INFO WHERE PROF_ID = " + prof_id + " AND SUBJECT_CODE = '" + code + "' ORDER BY NAME ASC", null);
            while (cursor.moveToNext()) {
                studentid.add(cursor.getString(1));
                student.add(cursor.getString(2));
            }
            cursor.close();
        }
    }
    public void sortStudentNo(int prof_id, String code, int value){
        if(value == 1) {
            studentid.clear();
            student.clear();
            Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM STUDENT_INFO WHERE PROF_ID = " + prof_id + " AND SUBJECT_CODE = '" + code + "' ORDER BY STUDENT_NO DESC", null);
            while (cursor.moveToNext()) {
                studentid.add(cursor.getString(1));
                student.add(cursor.getString(2));
            }
            cursor.close();
        } else {
            studentid.clear();
            student.clear();
            Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM STUDENT_INFO WHERE PROF_ID = " + prof_id + " AND SUBJECT_CODE = '" + code + "' ORDER BY STUDENT_NO ASC", null);
            while (cursor.moveToNext()) {
                studentid.add(cursor.getString(1));
                student.add(cursor.getString(2));
            }
            cursor.close();
        }
    }
    public void getAllStudent(int prof_id){
        searchStudentNo.clear(); searchStudentName.clear(); searchStudentSubject.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT STUDENT_INFO.STUDENT_NO, STUDENT_INFO.NAME, STUDENT_INFO.SUBJECT_CODE FROM STUDENT_INFO, CLASS WHERE STUDENT_INFO.PROF_ID = "+prof_id+" AND STUDENT_INFO.SUBJECT_CODE = CLASS.SUBJECT_CODE",null);
        while (cursor.moveToNext()) {
            searchStudentNo.add(cursor.getString(0));
            searchStudentName.add(cursor.getString(1));
            searchStudentSubject.add(cursor.getString(2));
        }
        cursor.close();
    }
    public void deleteAllStudentInClass(int profid, String subject){
        Cursor student = this.getWritableDatabase().rawQuery("DELETE FROM STUDENT_INFO WHERE PROF_ID = "+profid+" AND SUBJECT_CODE = '"+subject+"'",null);
        while(student.moveToNext()){
        }
        student.close();
        Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM CLASS WHERE PROF_ID = "+profid+" AND SUBJECT_CODE = '"+subject+"'",null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void truncateStudent(){
        Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM STUDENT_INFO",null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }

    //term settings handler
    public void setTerm(int profid, String subcode, int termValue){
        ContentValues contentValues = new ContentValues();
        contentValues.put("prof_id",profid);
        contentValues.put("subject_code",subcode);
        contentValues.put("current_term",termValue);
        this.getWritableDatabase().insertOrThrow("Term","",contentValues);
    }
    public void updateTerm(int profid, String subcode, int termValue){
        Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE Term set current_term = " + termValue + " WHERE prof_id = "+profid+" AND subject_code = '"+subcode+"'", null);
        while (cursor.moveToNext()) {
        }
        cursor.close();
    }
    public void getTerm(int profid, String subcode){
        term.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM Term WHERE prof_id = "+profid+" AND subject_code = '"+subcode+"'",null);
        while(cursor.moveToNext()){
            term.add(cursor.getInt(2));
        }
        cursor.close();
    }
    public void truncateTerm(int profid){
        Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM Term WHERE prof_id = "+profid,null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }

    //SETTINGS TABLE
    public void setSettings(int profid, int uploadValue, int downloadValue){
        ContentValues contentValues = new ContentValues();
        contentValues.put("prof_id",profid);
        contentValues.put("auto_upload",uploadValue);
        contentValues.put("auto_download",downloadValue);
        this.getWritableDatabase().insertOrThrow("settings","",contentValues);
    }
    public void updateSettings(int profid, String tableName, int value){
        Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE settings set '"+tableName+"' = "+value+" WHERE PROF_ID = "+profid,null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void getSettings(int profid){
        settingsAutoUpload.clear(); settingsAutoDownload.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM settings WHERE prof_id ="+profid,null);
        while(cursor.moveToNext()){
            settingsAutoUpload.add(cursor.getInt(1));
            settingsAutoDownload.add(cursor.getInt(2));
        }
        cursor.close();
    }

    //PASSING GRADE TABLE or CEILING GRADE
    public void insertPassingGrade(int profid, float value){
        ContentValues contentValues = new ContentValues();
        contentValues.put("prof_id",profid);
        contentValues.put("grade",value);
        this.getWritableDatabase().insertOrThrow("passing_grade","",contentValues);
    }
    public void getPassingGrade(int profid){
        passingGradeValue.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM passing_grade WHERE prof_id ="+profid,null);
        while(cursor.moveToNext()){
            passingGradeValue.add(cursor.getInt(1));
        }
        cursor.close();
    }
    public void truncatePassingGrade(int profid){
        Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM passing_grade WHERE prof_id ="+profid,null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }

    //notification table
    public void setNotification(int profid, String student_no, String name, String subject, String desc, String type, String status, String tag){
        ContentValues contentValues = new ContentValues();
        contentValues.put("prof_id",profid);
        contentValues.put("student_no",student_no);
        contentValues.put("name",name);
        contentValues.put("subject_code",subject);
        contentValues.put("description",desc);
        contentValues.put("type",type);
        contentValues.put("status",status);
        contentValues.put("tag",tag);
        this.getWritableDatabase().insertOrThrow("notification","",contentValues);
    }
    public void updateNotification(int profid, int notifid){
        Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE notification set status = 'READ' WHERE id = "+notifid+" AND prof_id = "+profid,null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void getNotification(int profid){
        notificationId.clear(); notificationStudentId.clear(); notificationName.clear(); notificationSubjectCode.clear(); notificationDesc.clear(); notificationType.clear(); notificationStatus.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM notification WHERE prof_id = "+profid+"",null);
        while(cursor.moveToNext()){
            notificationId.add(cursor.getInt(0));
            notificationStudentId.add(cursor.getString(2));
            notificationName.add(cursor.getString(3));
            notificationSubjectCode.add(cursor.getString(4));
            notificationDesc.add(cursor.getString(5));
            notificationType.add(cursor.getString(6));
            notificationStatus.add(cursor.getString(7));
            notificationTag.add(cursor.getString(8));
        }
        cursor.close();
    }
    public void getAllNotification(){
        notificationId.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM notification",null);
        while(cursor.moveToNext()){
            notificationId.add(cursor.getInt(0));
        }
        cursor.close();
    }
    public void sortNotificationByPriority(int profid, String priority, String subject){
        notificationId.clear(); notificationStudentId.clear(); notificationName.clear(); notificationSubjectCode.clear(); notificationDesc.clear(); notificationType.clear(); notificationStatus.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM notification WHERE prof_id = "+profid+" AND type = '"+priority+"' AND subject_code = '"+subject+"'",null);
        while(cursor.moveToNext()){
            notificationId.add(cursor.getInt(0));
            notificationStudentId.add(cursor.getString(2));
            notificationName.add(cursor.getString(3));
            notificationSubjectCode.add(cursor.getString(4));
            notificationDesc.add(cursor.getString(5));
            notificationType.add(cursor.getString(6));
            notificationStatus.add(cursor.getString(7));
        }
        cursor.close();
    }
    public void sortNotificationByGrade(int profid, String tag, String subject){
        notificationId.clear(); notificationStudentId.clear(); notificationName.clear(); notificationSubjectCode.clear(); notificationDesc.clear(); notificationType.clear(); notificationStatus.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM notification WHERE prof_id = "+profid+" AND tag = '"+tag+"' AND subject_code = '"+subject+"'",null);
        while(cursor.moveToNext()){
            notificationId.add(cursor.getInt(0));
            notificationStudentId.add(cursor.getString(2));
            notificationName.add(cursor.getString(3));
            notificationSubjectCode.add(cursor.getString(4));
            notificationDesc.add(cursor.getString(5));
            notificationType.add(cursor.getString(6));
            notificationStatus.add(cursor.getString(7));
        }
        cursor.close();
    }
    public void deleteClassNotification(int profid, String subject){
        Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM notification WHERE prof_id = "+profid+" AND subject_code = '"+subject+"'",null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void deleteNotification(int notifid){
        Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM notification WHERE id = "+notifid,null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void truncateNotification(int profid){
        Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM notification WHERE prof_id = "+profid,null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }

    //grade before submit
    public void restoreAllGrade(int profid, String student_no, String code, float prelim, float midterm, float finals, float average, String remarks){
        ContentValues contentValues = new ContentValues();
        contentValues.put("prof_id",profid);
        contentValues.put("subject_code",code);
        contentValues.put("student_no",student_no);
        contentValues.put("prelim",prelim);
        contentValues.put("midterm",midterm);
        contentValues.put("finals",finals);
        contentValues.put("average",average);
        contentValues.put("remarks",remarks);
        this.getWritableDatabase().insertOrThrow("GradeToSubmit","",contentValues);
    }
    public void setAllGrade(int profid, String student_no, String code, double prelim, double average, String remarks){
        ContentValues contentValues = new ContentValues();
        contentValues.put("prof_id",profid);
        contentValues.put("subject_code",code);
        contentValues.put("student_no",student_no);
        contentValues.put("prelim",prelim);
        contentValues.put("average",average);
        contentValues.put("remarks",remarks);
        this.getWritableDatabase().insertOrThrow("GradeToSubmit","",contentValues);
    }
    public void updateAllGrade(int profid, String student_no, String code, double grade, String term){
        term.toLowerCase();
        Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE GradeToSubmit set '"+term+"' = "+grade+" WHERE prof_id = "+profid+" AND subject_code = '"+code+"' AND student_no = '"+student_no+"'",null);
        while(cursor.moveToNext()){
        }
        cursor.close();
        ArrayList<Float> temp = new ArrayList<>();
        Cursor calculateAverage = this.getWritableDatabase().rawQuery("SELECT prelim, midterm, finals FROM GradeToSubmit WHERE prof_id = "+profid+" AND subject_code = '"+code+"' AND student_no = '"+student_no+"'",null);
        while(calculateAverage.moveToNext()){
            temp.add(calculateAverage.getFloat(0));
            temp.add(calculateAverage.getFloat(1));
            temp.add(calculateAverage.getFloat(2));
        }
        calculateAverage.close();
        String remarks;
        float average = (temp.get(0) + temp.get(1) + temp.get(2))/3;
        if(Math.round(average) >= passingGradeValue.get(0)){
            remarks = "Passed!";
        } else {
            remarks = "Failed!";
        }
        Cursor saveUpdated = this.getWritableDatabase().rawQuery("UPDATE GradeToSubmit set average = "+average+", remarks = '"+remarks+"'  WHERE prof_id = "+profid+" AND subject_code = '"+code+"' AND student_no = '"+student_no+"'",null);
        while(saveUpdated.moveToNext()){
            temp.add(saveUpdated.getFloat(0));
        }
        saveUpdated.close();
    }
    public void getAllGradeBySelectedStudent(int profid, String code, String student_no){
        all_grade_prelim.clear(); all_grade_midterm.clear(); all_grade_finals.clear(); all_grade_average.clear(); all_grade_remarks.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM GradeToSubmit WHERE prof_id = "+profid+" AND subject_code = '"+code+"' AND student_no = '"+student_no+"'",null);
        while (cursor.moveToNext()) {
            all_grade_prelim.add(cursor.getFloat(4));
            all_grade_midterm.add(cursor.getFloat(5));
            all_grade_finals.add(cursor.getFloat(6));
            all_grade_average.add(cursor.getFloat(7));
            all_grade_remarks.add(cursor.getString(8));
        }
        cursor.close();
    }
    public void getAllGrade(int profid, String subject, String sort, int sortId){
        all_grade_student_no.clear(); all_grade_student_name.clear(); all_grade_prelim.clear(); all_grade_midterm.clear(); all_grade_finals.clear(); all_grade_average.clear(); all_grade_remarks.clear();
        if(sort.equalsIgnoreCase("Grade")) {
            Cursor cursor;
            if(sortId == 1){
                cursor = this.getWritableDatabase().rawQuery("SELECT st.STUDENT_NO, st.NAME, gs.prelim, gs.midterm, gs.finals, gs.average, gs.remarks, gs.prof_id FROM STUDENT_INFO st JOIN GradeToSubmit gs ON st.STUDENT_NO = gs.student_no WHERE gs.prof_id = " + profid + " AND st.SUBJECT_CODE = '" + subject + "' ORDER BY gs.average ASC", null);
            } else {
                cursor = this.getWritableDatabase().rawQuery("SELECT st.STUDENT_NO, st.NAME, gs.prelim, gs.midterm, gs.finals, gs.average, gs.remarks, gs.prof_id FROM STUDENT_INFO st JOIN GradeToSubmit gs ON st.STUDENT_NO = gs.student_no WHERE gs.prof_id = " + profid + " AND st.SUBJECT_CODE = '" + subject + "' ORDER BY gs.average DESC", null);
            }
            while (cursor.moveToNext()) {
                all_grade_student_no.add(cursor.getString(0));
                all_grade_student_name.add(cursor.getString(1));
                all_grade_prelim.add(cursor.getFloat(2));
                all_grade_midterm.add(cursor.getFloat(3));
                all_grade_finals.add(cursor.getFloat(4));
                all_grade_average.add(cursor.getFloat(5));
                all_grade_remarks.add(cursor.getString(6));
            }
            cursor.close();
        } else if(sort.equalsIgnoreCase("Name")){
            Cursor cursor;
            if(sortId == 1){
                cursor = this.getWritableDatabase().rawQuery("SELECT st.STUDENT_NO, st.NAME, gs.prelim, gs.midterm, gs.finals, gs.average, gs.remarks, gs.prof_id FROM STUDENT_INFO st JOIN GradeToSubmit gs ON st.STUDENT_NO = gs.student_no WHERE gs.prof_id = " + profid + " AND st.SUBJECT_CODE = '" + subject + "' ORDER BY st.Name ASC", null);
            } else {
                cursor = this.getWritableDatabase().rawQuery("SELECT st.STUDENT_NO, st.NAME, gs.prelim, gs.midterm, gs.finals, gs.average, gs.remarks, gs.prof_id FROM STUDENT_INFO st JOIN GradeToSubmit gs ON st.STUDENT_NO = gs.student_no WHERE gs.prof_id = " + profid + " AND st.SUBJECT_CODE = '" + subject + "' ORDER BY st.Name DESC", null);
            }
            while (cursor.moveToNext()) {
                all_grade_student_no.add(cursor.getString(0));
                all_grade_student_name.add(cursor.getString(1));
                all_grade_prelim.add(cursor.getFloat(2));
                all_grade_midterm.add(cursor.getFloat(3));
                all_grade_finals.add(cursor.getFloat(4));
                all_grade_average.add(cursor.getFloat(5));
                all_grade_remarks.add(cursor.getString(6));
            }
            cursor.close();
        }
    }
    public void truncateGrateToSubmit(int profid){
        Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM GradeToSubmit WHERE prof_id = "+profid,null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }

    //GRADE PERCENTAGE TABLE
    public void setPercentage(int profid, int cat_id, float percent){
        ContentValues contentValues = new ContentValues();
        contentValues.put("prof_id",profid);
        contentValues.put("category_id",cat_id);
        contentValues.put("percent",percent);
        this.getWritableDatabase().insertOrThrow("Grade_Percentage","",contentValues);
    }
    public void updatePercentage(int profid, int cat_id, float percent){
        Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE Grade_Percentage set percent = "+percent+" WHERE prof_id ="+profid+" AND category_id = "+cat_id,null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void getPercentage(int profid, int cat_id){
        percentage.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT percent FROM Grade_Percentage WHERE prof_id ="+profid+" AND category_id = "+cat_id,null);
        while (cursor.moveToNext()) {
            percentage.add(cursor.getFloat(0));
        }
        cursor.close();
    }
    public void truncatePercentage(int profid){
        Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM Grade_Percentage WHERE prof_id ="+profid,null);
        while (cursor.moveToNext()) {
        }
        cursor.close();
    }

    //for category
    public void setCategory(int profid, String subject, String category){
        ContentValues contentValues = new ContentValues();
        contentValues.put("prof_id",profid);
        contentValues.put("subject_code",subject);
        contentValues.put("name",category);
        this.getWritableDatabase().insertOrThrow("Grade_Category","",contentValues);
    }
    public void restoreCategory(int profid, int id, String subject, String category){
        System.out.println("id: "+id+" | profid: "+profid+" | subject code: "+subject+" | name: "+category);
        Cursor cursor = this.getWritableDatabase().rawQuery("INSERT INTO Grade_Category (id, prof_id, subject_code, name) VALUES ("+id+", "+profid+", '"+subject+"', '"+category+"')",null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void updateCategory(int profid, String category, int category_id){
        Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE Grade_Category set name = '"+category+"' WHERE prof_id = "+profid+" AND id = "+category_id,null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void getCategory(int profid, String subject){
        category_id.clear(); category.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT id, name FROM Grade_Category WHERE subject_code = '"+subject+"' AND prof_id = "+profid,null);
        while(cursor.moveToNext()){
            category_id.add(cursor.getInt(0));
            category.add(cursor.getString(1));
        }
        cursor.close();
    }
    public void truncateCategory(int profid){
        Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM Grade_Category WHERE prof_id = "+profid,null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }

    //for grade
    public void setRawGrade(int profid, int gradeid, String raw){
        ContentValues contentValues = new ContentValues();
        contentValues.put("prof_id", profid);
        contentValues.put("grade_id", gradeid);
        contentValues.put("raw_grade", raw);
        this.getWritableDatabase().insertOrThrow("RawGrade", "", contentValues);
    }
    public void getRawGrade(int gradeid){
        rawGrade.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM RawGrade WHERE grade_id = "+gradeid,null);
        while(cursor.moveToNext()){
            rawGrade.add(cursor.getString(2));
        }
        cursor.close();
    }
    public void getAllRawGrade(int profid){
        rawGrade.clear();
        rawGradeid.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM RawGrade WHERE prof_id = "+profid,null);
        while(cursor.moveToNext()){
            rawGradeid.add(cursor.getInt(1));
            rawGrade.add(cursor.getString(2));
        }
        cursor.close();
    }
    public void truncateRawGrade(int profid){
        Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM RawGrade WHERE prof_id = "+profid,null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void setGrade(String term, int profid, String student_no, String subject, int cat_id, String caption, String date, double grade){
        ContentValues contentValues = new ContentValues();
        contentValues.put("prof_id", profid);
        contentValues.put("student_no", student_no);
        contentValues.put("subject_code", subject);
        contentValues.put("category_id", cat_id);
        contentValues.put("caption", caption);
        contentValues.put("date", date);
        contentValues.put("grade", grade);
        if(term.equals("Prelim")) {
            this.getWritableDatabase().insertOrThrow("Grade_Prelim", "", contentValues);
        } else if(term.equals("Midterm")){
            this.getWritableDatabase().insertOrThrow("Grade_Midterm", "", contentValues);
        } else if(term.equals("Finals")){
            this.getWritableDatabase().insertOrThrow("Grade_Finals", "", contentValues);
        }
    }
    public void updateGrade(String term, int grade_id, double grade){
        if(term.equals("Prelim")){
            Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE Grade_Prelim set grade = '"+grade+"' WHERE id = "+grade_id,null);
            while(cursor.moveToNext()){
            }
            cursor.close();
        } else if(term.equals("Midterm")){
            Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE Grade_Midterm set grade = '"+grade+"' WHERE id = "+grade_id,null);
            while(cursor.moveToNext()){
            }
            cursor.close();
        } else if(term.equals("Finals")){
            Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE Grade_Finals set grade = '"+grade+"' WHERE id = "+grade_id,null);
            while(cursor.moveToNext()){
            }
            cursor.close();
        }
    }
    public void getGrade(String term, String student_no, String subject, int cat_id){
        grade_id.clear(); date.clear(); caption.clear(); grade.clear();
        if(term.equals("Prelim")){
            Cursor cursor = this.getWritableDatabase().rawQuery("SELECT id, date, caption, grade FROM Grade_Prelim WHERE category_id  = "+cat_id+" AND student_no = '"+student_no+"' AND subject_code = '"+subject+"'",null);
            while(cursor.moveToNext()){
                grade_id.add(cursor.getInt(0));
                date.add(cursor.getString(1));
                caption.add(cursor.getString(2));
                grade.add(cursor.getFloat(3));
            }
            cursor.close();
        } else if(term.equals("Midterm")){
            Cursor cursor = this.getWritableDatabase().rawQuery("SELECT id, date, caption, grade FROM Grade_Midterm WHERE category_id  = "+cat_id+" AND student_no = '"+student_no+"' AND subject_code = '"+subject+"'",null);
            while(cursor.moveToNext()){
                grade_id.add(cursor.getInt(0));
                date.add(cursor.getString(1));
                caption.add(cursor.getString(2));
                grade.add(cursor.getFloat(3));
            }
            cursor.close();
        } else if(term.equals("Finals")){
            Cursor cursor = this.getWritableDatabase().rawQuery("SELECT id, date, caption, grade FROM Grade_Finals WHERE category_id  = "+cat_id+" AND student_no = '"+student_no+"' AND subject_code = '"+subject+"'",null);
            while(cursor.moveToNext()){
                grade_id.add(cursor.getInt(0));
                date.add(cursor.getString(1));
                caption.add(cursor.getString(2));
                grade.add(cursor.getFloat(3));
            }
            cursor.close();
        }
    }

    public void truncateGrade(String term, int profid){
        if(term.equalsIgnoreCase("Prelim")){
            Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM Grade_Prelim WHERE prof_id = "+profid,null);
            while(cursor.moveToNext()){
            }
            cursor.close();
        } else if(term.equalsIgnoreCase("Midterm")){
            Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM Grade_Midterm WHERE prof_id = "+profid,null);
            while(cursor.moveToNext()){
            }
            cursor.close();
        } else if(term.equalsIgnoreCase("Finals")){
            Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM Grade_Finals WHERE prof_id = "+profid,null);
            while(cursor.moveToNext()){
            }
            cursor.close();
        }
    }

    //grade of all in professor
    public void getProfGradeData(String term, int profid){
        profDataStudentNo.clear(); profDataSubjectCode.clear(); profDataCategoryID.clear(); profDataCaption.clear();
        profDataDate.clear(); profDataGrade.clear();
        if(term.equals("Prelim")){
            Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM Grade_Prelim WHERE prof_id = "+profid,null);
            while(cursor.moveToNext()){
                profDataStudentNo.add(cursor.getString(2));
                profDataSubjectCode.add(cursor.getString(3));
                profDataCategoryID.add(cursor.getInt(4));
                profDataCaption.add(cursor.getString(5));
                profDataDate.add(cursor.getString(6));
                profDataGrade.add(cursor.getFloat(7));
            }
            cursor.close();
        } else if(term.equals("Midterm")){
            Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM Grade_Midterm WHERE prof_id = "+profid,null);
            while(cursor.moveToNext()){
                profDataStudentNo.add(cursor.getString(2));
                profDataSubjectCode.add(cursor.getString(3));
                profDataCategoryID.add(cursor.getInt(4));
                profDataCaption.add(cursor.getString(5));
                profDataDate.add(cursor.getString(6));
                profDataGrade.add(cursor.getFloat(7));
            }
            cursor.close();
        } else if(term.equals("Finals")){
            Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM Grade_Finals WHERE prof_id = "+profid,null);
            while(cursor.moveToNext()){
                profDataStudentNo.add(cursor.getString(2));
                profDataSubjectCode.add(cursor.getString(3));
                profDataCategoryID.add(cursor.getInt(4));
                profDataCaption.add(cursor.getString(5));
                profDataDate.add(cursor.getString(6));
                profDataGrade.add(cursor.getFloat(7));
            }
            cursor.close();
        }
    }
    //for overall
    public void getAverageOfAllCategory(String term, String subject){
        average.clear();
        if(term.equalsIgnoreCase("Prelim")) {
            Cursor cursor = this.getWritableDatabase().rawQuery("SELECT AVG(grade) FROM Grade_Prelim WHERE subject_code = '" + subject + "'", null);
            while (cursor.moveToNext()) {
                average.add(cursor.getFloat(0));
            }
            cursor.close();
        } else if(term.equalsIgnoreCase("Midterm")){
            Cursor cursor = this.getWritableDatabase().rawQuery("SELECT AVG(grade) FROM Grade_Midterm WHERE subject_code = '" + subject + "'", null);
            while (cursor.moveToNext()) {
                average.add(cursor.getFloat(0));
            }
            cursor.close();
        } else if(term.equalsIgnoreCase("Finals")){
            Cursor cursor = this.getWritableDatabase().rawQuery("SELECT AVG(grade) FROM Grade_Finals WHERE subject_code = '" + subject + "'", null);
            while (cursor.moveToNext()) {
                average.add(cursor.getFloat(0));
            }
            cursor.close();
        }
    }
    public void getAverageOfAllCategoryById(String subject, int cat_id){
        tempPrelim.clear(); tempMidterm.clear(); tempFinals.clear();
        Cursor cursor1 = this.getWritableDatabase().rawQuery("SELECT AVG(grade) FROM Grade_Prelim WHERE  category_id  = "+cat_id+" AND subject_code = '"+subject+"'",null);
        while(cursor1.moveToNext()){
            tempPrelim.add(cursor1.getFloat(0));
        }
        cursor1.close();
        Cursor cursor2 = this.getWritableDatabase().rawQuery("SELECT AVG(grade) FROM Grade_Midterm WHERE  category_id  = "+cat_id+" AND subject_code = '"+subject+"'",null);
        while(cursor2.moveToNext()){
            tempMidterm.add(cursor2.getFloat(0));
        }
        cursor2.close();
        Cursor cursor3 = this.getWritableDatabase().rawQuery("SELECT AVG(grade) FROM Grade_Finals WHERE  category_id  = "+cat_id+" AND subject_code = '"+subject+"'",null);
        while(cursor3.moveToNext()){
            tempFinals.add(cursor3.getFloat(0));
        }
        cursor3.close();
    }
    public void getIndividualAverageOfCategory(String student_no, String subject, int cat_id){
        tempPrelim.clear(); tempMidterm.clear(); tempFinals.clear();
        Cursor cursor1 = this.getWritableDatabase().rawQuery("SELECT AVG(grade) FROM Grade_Prelim WHERE  category_id  = "+cat_id+" AND student_no = '"+student_no+"' AND subject_code = '"+subject+"'",null);
        while(cursor1.moveToNext()){
            tempPrelim.add(cursor1.getFloat(0));
        }
        cursor1.close();
        Cursor cursor2 = this.getWritableDatabase().rawQuery("SELECT AVG(grade) FROM Grade_Midterm WHERE  category_id  = "+cat_id+" AND student_no = '"+student_no+"' AND subject_code = '"+subject+"'",null);
        while(cursor2.moveToNext()){
            tempMidterm.add(cursor2.getFloat(0));
        }
        cursor2.close();
        Cursor cursor3 = this.getWritableDatabase().rawQuery("SELECT AVG(grade) FROM Grade_Finals WHERE  category_id  = "+cat_id+" AND student_no = '"+student_no+"' AND subject_code = '"+subject+"'",null);
        while(cursor3.moveToNext()){
            tempFinals.add(cursor3.getFloat(0));
        }
        cursor3.close();
    }
    public void getIndividualAverageOfCategoryByTerm(String term, String student_no, String subject, int cat_id){
        if(term.equalsIgnoreCase("Prelim")) {
            average.clear();
            Cursor cursor = this.getWritableDatabase().rawQuery("SELECT AVG(grade) FROM Grade_Prelim WHERE  category_id  = " + cat_id + " AND student_no = '" + student_no + "' AND subject_code = '" + subject + "'", null);
            while (cursor.moveToNext()) {
                average.add(cursor.getFloat(0));
            }
            cursor.close();
        } else if(term.equalsIgnoreCase("Midterm")){
            average.clear();
            Cursor cursor = this.getWritableDatabase().rawQuery("SELECT AVG(grade) FROM Grade_Midterm WHERE  category_id  = " + cat_id + " AND student_no = '" + student_no + "' AND subject_code = '" + subject + "'", null);
            while (cursor.moveToNext()) {
                average.add(cursor.getFloat(0));
            }
            cursor.close();
        } else if(term.equalsIgnoreCase("Finals")){
            average.clear();
            Cursor cursor = this.getWritableDatabase().rawQuery("SELECT AVG(grade) FROM Grade_Finals WHERE  category_id  = " + cat_id + " AND student_no = '" + student_no + "' AND subject_code = '" + subject + "'", null);
            while (cursor.moveToNext()) {
                average.add(cursor.getFloat(0));
            }
            cursor.close();
        }
    }

    //Last update table
    public void setUpdatedDate(int profid, String date){
        ContentValues contentValues = new ContentValues();
        contentValues.put("prof_id", profid);
        contentValues.put("date", date);
        this.getWritableDatabase().insertOrThrow("Last_update", "", contentValues);
        this.getWritableDatabase().close();
    }
    public void updateUpdatedDate(int profid, String date){
        Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE Last_update set date = '"+date+"' WHERE prof_id = "+profid,null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void getUpdateDate(int profid){
        updateDate.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT date FROM Last_update WHERE prof_id = "+profid,null);
        while(cursor.moveToNext()){
            updateDate.add(cursor.getString(0));
        }
        cursor.close();
    }
    public void truncateUpdateDate(int profid){
        Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM Last_update WHERE prof_id = "+profid,null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }

    //archive table
    public void setArchive(String student_no, String name, String year, int prof_id, String subject, String description, String ay, String sem, float prelim, float midterm, float finals, float average, double equivalent){
        System.out.println(student_no + " | " + name + " | " + year + " | " + prof_id + " | " + subject + " | " + description + " | " + ay + " | " + sem + " | " + prelim + " | " + midterm + " | " + finals + " | " + average + " | " + equivalent);
        ContentValues contentValues = new ContentValues();
        contentValues.put("student_no", student_no);
        contentValues.put("name", name);
        contentValues.put("year", year);
        contentValues.put("prof_id", prof_id);
        contentValues.put("subject_code", subject);
        contentValues.put("subject_description", description);
        contentValues.put("ay", ay);
        contentValues.put("sem", sem);
        contentValues.put("prelim", prelim);
        contentValues.put("midterm", midterm);
        contentValues.put("finals", finals);
        contentValues.put("average", average);
        contentValues.put("equivalent", equivalent);
        this.getWritableDatabase().insertOrThrow("Archive", "", contentValues);
    }
    public void getArchive(int profid, String ay, String sem, String subject){
        archive_student_no.clear(); archive_student_name.clear(); archive_prelim.clear();
        archive_midterm.clear(); archive_finals.clear(); archive_average.clear(); archive_equivalent.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM Archive WHERE prof_id = "+profid+" AND ay = '"+ay+"' AND sem = '"+sem+"' AND subject_code = '"+subject+"'",null);
        while(cursor.moveToNext()){
            archive_student_no.add(cursor.getString(1));
            archive_student_name.add(cursor.getString(2));
            archive_prelim.add(cursor.getFloat(9));
            archive_midterm.add(cursor.getFloat(10));
            archive_finals.add(cursor.getFloat(11));
            archive_average.add(cursor.getFloat(12));
            archive_equivalent.add(cursor.getFloat(13));
        }
        cursor.close();
    }
    //( id INTEGER PRIMARY KEY AUTOINCREMENT, student_no TEXT, name TEXT, year TEXT, prof_id INTEGER, subject_code TEXT, subject_description TEXT, ay TEXT,
    // sem TEXT, prelim FLOAT, midterm FLOAT, finals FLOAT, average FLOAT, equivalent FLOAT );")
    public void getAllArchive(int profid){
        archive_student_no.clear(); archive_student_name.clear();  archive_year.clear(); archive_subject.clear(); archive_subject_description.clear();
        archive_ay.clear(); archive_sem.clear();
        archive_prelim.clear(); archive_midterm.clear(); archive_finals.clear(); archive_average.clear(); archive_equivalent.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM Archive WHERE prof_id = "+profid,null);
        while(cursor.moveToNext()){
            archive_student_no.add(cursor.getString(1));
            archive_student_name.add(cursor.getString(2));
            archive_year.add(cursor.getString(3));
            archive_subject.add(cursor.getString(5));
            archive_subject_description.add(cursor.getString(6));
            archive_ay.add(cursor.getString(7));
            archive_sem.add(cursor.getString(8));
            archive_prelim.add(cursor.getFloat(9));
            archive_midterm.add(cursor.getFloat(10));
            archive_finals.add(cursor.getFloat(11));
            archive_average.add(cursor.getFloat(12));
            archive_equivalent.add(cursor.getFloat(13));
        }
        cursor.close();
    }
    public void truncateArchive(int profid, String subject){
        Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM Archive WHERE prof_id = "+profid+" AND subject_code = '"+subject+"'",null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void truncateAllArchive(int profid){
        Cursor cursor = this.getWritableDatabase().rawQuery("DELETE FROM Archive WHERE prof_id = "+profid,null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void sortArchiveStudentNo(int profid, String ay, String sem, String subject, int sort){
        archive_student_no.clear(); archive_student_name.clear(); archive_prelim.clear();
        archive_midterm.clear(); archive_finals.clear(); archive_average.clear(); archive_equivalent.clear();
        Cursor cursor;
        if(sort == 1){
            cursor = this.getWritableDatabase().rawQuery("SELECT * FROM Archive WHERE prof_id = "+profid+" AND ay = '"+ay+"' AND sem = '"+sem+"' AND subject_code = '"+subject+"' ORDER BY student_no ASC",null);
        } else {
            cursor = this.getWritableDatabase().rawQuery("SELECT * FROM Archive WHERE prof_id = "+profid+" AND ay = '"+ay+"' AND sem = '"+sem+"' AND subject_code = '"+subject+"' ORDER BY student_no DESC",null);
        }
        while(cursor.moveToNext()){
            archive_student_no.add(cursor.getString(1));
            archive_student_name.add(cursor.getString(2));
            archive_prelim.add(cursor.getFloat(9));
            archive_midterm.add(cursor.getFloat(10));
            archive_finals.add(cursor.getFloat(11));
            archive_average.add(cursor.getFloat(12));
            archive_equivalent.add(cursor.getFloat(13));
        }
        cursor.close();
    }
    public void sortArchiveStudentName(int profid, String ay, String sem, String subject, int sort){
        archive_student_no.clear(); archive_student_name.clear(); archive_prelim.clear();
        archive_midterm.clear(); archive_finals.clear(); archive_average.clear(); archive_equivalent.clear();
        Cursor cursor;
        if(sort == 1){
            cursor = this.getWritableDatabase().rawQuery("SELECT * FROM Archive WHERE prof_id = "+profid+" AND ay = '"+ay+"' AND sem = '"+sem+"' AND subject_code = '"+subject+"' ORDER BY name ASC",null);
        } else {
            cursor = this.getWritableDatabase().rawQuery("SELECT * FROM Archive WHERE prof_id = "+profid+" AND ay = '"+ay+"' AND sem = '"+sem+"' AND subject_code = '"+subject+"' ORDER BY name DESC",null);
        }
        while(cursor.moveToNext()){
            archive_student_no.add(cursor.getString(1));
            archive_student_name.add(cursor.getString(2));
            archive_prelim.add(cursor.getFloat(9));
            archive_midterm.add(cursor.getFloat(10));
            archive_finals.add(cursor.getFloat(11));
            archive_average.add(cursor.getFloat(12));
            archive_equivalent.add(cursor.getFloat(13));
        }
        cursor.close();
    }
    public void getAYandSubject(int profid){
        archive_ay.clear(); archive_subject.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT ay, subject_code, subject_description FROM Archive WHERE prof_id = "+profid+" GROUP BY ay",null);
        while(cursor.moveToNext()){
            archive_ay.add(cursor.getString(0));
            archive_subject.add(cursor.getString(1));
            archive_subject_description.add(cursor.getString(2));
        }
        cursor.close();
    }

    //import export
    public void setSyncDate(int profid, String syncDate, String uploadDate){
        ContentValues contentValues = new ContentValues();
        contentValues.put("prof_id", profid);
        contentValues.put("sync_date", syncDate);
        contentValues.put("upload_date", uploadDate);
        this.getWritableDatabase().insertOrThrow("ExportImportDate", "", contentValues);
    }
    public void updateSyncDate(int profid, String tableName, String value){
        Cursor cursor = this.getWritableDatabase().rawQuery("UPDATE ExportImportDate set '"+tableName+"' = '"+value+"' WHERE prof_id = "+profid,null);
        while(cursor.moveToNext()){
        }
        cursor.close();
    }
    public void getSyncDate(int profid){
        syncDate.clear(); uploadDate.clear();
        Cursor cursor = this.getWritableDatabase().rawQuery("SELECT * FROM ExportImportDate WHERE prof_id = "+profid,null);
        while(cursor.moveToNext()){
            syncDate.add(cursor.getString(1));
            uploadDate.add(cursor.getString(2));
        }
        cursor.close();
    }
}