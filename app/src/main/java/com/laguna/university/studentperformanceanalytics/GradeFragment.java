package com.laguna.university.studentperformanceanalytics;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

public class GradeFragment extends Fragment {

    SQLiteDBcontroller db  = new SQLiteDBcontroller(getActivity());

    DrawerLayout drawer;
    ListView categoryList;
    ArrayAdapter<String> sectionAdapter;
    FloatingActionButton openDrawer;
    CardView closeDrawer,addCategory,editCategory;
    Spinner section;

    String term = "";
    String categoryName = "";
    String arrTemp[];
    String nameTemp[];
    String percentTemp[];
    String noClass[] = {"No Subject"};
    String selectedSubject = "";
    String selectedSubjectDescription = "";
    int idTemp[];
    int categoryPercent;
    int selectedPosition = 0;

    AlertDialog addDialog, editDialog;
    ArrayList<String> listCat = new ArrayList<>();
    ArrayList<String> listPercent = new ArrayList<>();
    ArrayList<Integer> deleteCategory = new ArrayList<>();

    @Override
    public void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grade, parent, false);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        db  = new SQLiteDBcontroller(getActivity());
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        openDrawer = (FloatingActionButton)  view.findViewById(R.id.openDrawer);
        closeDrawer = (CardView)  view.findViewById(R.id.closeDrawer);
        addCategory = (CardView)  view.findViewById(R.id.add);
        editCategory = (CardView)  view.findViewById(R.id.edit);
        drawer = (DrawerLayout) view.findViewById(R.id.drawer_Layout);
        categoryList = (ListView) view.findViewById(R.id.categoryList);
        section = (Spinner) view.findViewById(R.id.subject);
        openDrawer.bringToFront();
        drawer.bringToFront();

        db.login(((MainActivity)getActivity()).user,((MainActivity)getActivity()).pass);
        db.classList(db.loginID.get(0));
        checkTerm();
        try{
            db.classList(db.loginID.get(0));
            sectionAdapter = new ArrayAdapter<>(getActivity(),R.layout.support_simple_spinner_dropdown_item,db.subcode);
            section.setAdapter(sectionAdapter);
        } catch (Exception e){
            sectionAdapter = new ArrayAdapter<>(getActivity(),R.layout.support_simple_spinner_dropdown_item,noClass);
            section.setAdapter(sectionAdapter);
            e.printStackTrace();
        }

        section.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSubject = (String) section.getItemAtPosition(i);
                selectedSubjectDescription = db.subDescription.get(i);
                ((MainActivity)getActivity()).selectedSubject = selectedSubject;
                ((MainActivity)getActivity()).setDefaultCategory(selectedSubject);
                db.getStudent(db.loginID.get(0), selectedSubject);

                if(checkDatabaseIfHasValue(selectedSubject)){
                    addCategory.setVisibility(View.GONE);
                    editCategory.setVisibility(View.GONE);
                } else{
                    addCategory.setVisibility(View.VISIBLE);
                    editCategory.setVisibility(View.VISIBLE);
                }

                db.getTerm(db.loginID.get(0), selectedSubject);
                if(db.term.size() == 0){
                    db.setTerm(db.loginID.get(0), selectedSubject, 0);
                }
                refreshCategoryAndPercentage();
                checkTerm();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        //drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        openDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                section.setSelection(sectionAdapter.getPosition(((MainActivity)getActivity()).selectedSubject));
                drawer.openDrawer(Gravity.LEFT);

                if(checkDatabaseIfHasValue(selectedSubject)){
                    addCategory.setVisibility(View.GONE);
                    editCategory.setVisibility(View.GONE);
                } else{
                    addCategory.setVisibility(View.VISIBLE);
                    editCategory.setVisibility(View.VISIBLE);
                }
            }
        });


        closeDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.closeDrawer(GravityCompat.START);
            }
        });

        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.getCategory(db.loginID.get(0), selectedSubject);
                addCategory();
            }
        });

        editCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.getCategory(db.loginID.get(0), selectedSubject);
                editCategory();
            }
        });
        
        ((MainActivity)getActivity()).displayView("allgrade");
    }

    public boolean checkDatabaseIfHasValue(String subject){
        return ((MainActivity)getActivity()).hideShowAddEditCategory(subject);
    }

    public void checkTerm(){
        try {
            db.getTerm(db.loginID.get(0), db.subcode.get(0));
            if (db.term.get(0) == 0) {
                term = "Prelim";
            } else if (db.term.get(0) == 1) {
                term = "Midterm";
            } else if (db.term.get(0) == 2) {
                term = "Finals";
            } else if (db.term.get(0) == 3) {
                term = "Submit Grade";
            }
        } catch(Exception e){

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

    public void refreshCategoryAndPercentage(){
        listCat.clear(); listPercent.clear();
        listCat.add("All Grade");       listPercent.add("");
        listCat.add("Major Exam");      listPercent.add("40%");
        db.getCategory(db.loginID.get(0), selectedSubject);
        for(int i=0;i<db.category.size();i++){
            db.getPercentage(db.loginID.get(0), db.category_id.get(i));
            listCat.add("\t\t"+db.category.get(i));
            listPercent.add(Math.round((db.percentage.get(0)*100))+"%");
        }
        categoryList.setAdapter(new ViewCategoryAdapter());
    }

    public void addCategory(){
        arrTemp = new String[db.category.size()];
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View addLayout = inflater.inflate(R.layout.category_add,null);
        final CoordinatorLayout layout = (CoordinatorLayout) addLayout.findViewById(R.id.showSnackBar);
        final EditText catName = (EditText) addLayout.findViewById(R.id.name);
        final EditText percent = (EditText) addLayout.findViewById(R.id.percent);
        CardView add = (CardView) addLayout.findViewById(R.id.add);
        CardView save = (CardView) addLayout.findViewById(R.id.save);
        final TextView prompt = (TextView) addLayout.findViewById(R.id.titlePrompt);
        final ListView categoryData = (ListView) addLayout.findViewById(R.id.categoryList);
        final LinearLayout addLinear = (LinearLayout) addLayout.findViewById(R.id.hideWhenAdd);
        final LinearLayout saveLinear = (LinearLayout) addLayout.findViewById(R.id.displayWhenAdd);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(catName.getText().toString().length()>0 && percent.getText().toString().length()>0){
                    categoryName = catName.getText().toString();
                    categoryPercent = Integer.parseInt(percent.getText().toString());
                    addLinear.setVisibility(View.GONE);
                    saveLinear.setVisibility(View.VISIBLE);
                    int remaining = 100 - categoryPercent;
                    prompt.setText("You want to add "+categoryName.toUpperCase()+" "+categoryPercent+"% please complete the remaining "+remaining+"% for the rest of your category.");
                    categoryData.setAdapter(new AddCategory());
                } else {
                    showSnackBar("Please Complete the form.", layout);
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isProceed = false;
                int percent = 0;
                for(int x=0;x<arrTemp.length;x++){
                    if(arrTemp[x] != null && !arrTemp[x].isEmpty()){
                        percent += Integer.parseInt(arrTemp[x]);
                    }
                }
                int totalAverage = categoryPercent + percent;
                if(totalAverage == 100){
                    isProceed = true;
                } else {
                    showSnackBar("Percentage: "+totalAverage+"%", layout);
                }
                if(isProceed) {
                    AlertDialog.Builder yesno = new AlertDialog.Builder(getActivity());
                    yesno.setTitle("You want to save?");
                    yesno.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            for (int x = 0; x < db.category.size(); x++) {
                                db.updatePercentage(db.loginID.get(0), db.category_id.get(x), Float.parseFloat(arrTemp[x]) / 100);
                            }
                            db.setCategory(db.loginID.get(0), selectedSubject, categoryName);
                            db.getCategory(db.loginID.get(0), selectedSubject);
                            int address = db.category_id.size() - 1;
                            float custom_percent = Float.parseFloat(String.valueOf(categoryPercent)) / 100;
                            db.setPercentage(db.loginID.get(0), db.category_id.get(address), custom_percent);
                            refreshCategoryAndPercentage();
                            ((MainActivity) getActivity()).showSnackBar("Category and percentage Save.");
                            addDialog.dismiss();
                        }
                    });
                    yesno.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    yesno.show();
                }
            }
        });

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        addDialog = dialog.create();
        addDialog.setView(addLayout);
        addDialog.show();
    }

    public void editCategory(){
        deleteCategory.clear();
        nameTemp = new String[db.category.size()];
        percentTemp = new String[db.category.size()];
        idTemp = new int[db.category_id.size()];
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View editLayout = inflater.inflate(R.layout.category_edit,null);
        final CoordinatorLayout layout = (CoordinatorLayout) editLayout.findViewById(R.id.showSnackBar);
        CardView save = (CardView) editLayout.findViewById(R.id.save);
        ListView category = (ListView) editLayout.findViewById(R.id.catList);

        db.getCategory(db.loginID.get(0), selectedSubject);
        category.setAdapter(new EditCategoryAdapter());

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isProceed = false;
                int totalPercent = 0;
                for(int x=0;x<percentTemp.length;x++){
                    if(percentTemp[x] != null && !percentTemp[x].isEmpty()) {
                        totalPercent += Integer.parseInt(percentTemp[x]);
                    }
                }
                if(totalPercent == 100){
                    isProceed = true;
                } else {
                    showSnackBar("Percentage: "+totalPercent+"%", layout);
                }
                if(isProceed) {
                    AlertDialog.Builder yesno = new AlertDialog.Builder(getActivity());
                    yesno.setTitle("Save Category?");
                    yesno.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            for (int x = 0; x < deleteCategory.size(); x++) {
                                db.truncateCategory(deleteCategory.get(x));
                                db.truncatePercentage(deleteCategory.get(x));
                            }
                            db.getCategory(db.loginID.get(0), selectedSubject);
                            for (int x = 0; x < nameTemp.length; x++) {
                                if (nameTemp[x] != null && !nameTemp[x].isEmpty()) {
                                    db.updateCategory(db.loginID.get(0), nameTemp[x], idTemp[x]);
                                    db.updatePercentage(db.loginID.get(0), idTemp[x], Float.parseFloat(percentTemp[x]) / 100);
                                }
                            }
                            refreshCategoryAndPercentage();
                            editDialog.dismiss();
                        }
                    });
                    yesno.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    yesno.show();
                }
            }
        });

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        editDialog = dialog.create();
        editDialog.setView(editLayout);
        editDialog.show();

        editDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE  | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        editDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    //view category list
    private class ViewCategoryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if(listCat != null && listCat.size() != 0){
                return listCat.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return listCat.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View contextView, ViewGroup viewGroup) {
            final ViewCategoryViewHolder holder;
            if(contextView == null){
                holder = new ViewCategoryViewHolder();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                contextView = inflater.inflate(R.layout.data_grade, null);
                holder.text = (TextView) contextView.findViewById(R.id.name);
                holder.text.setTextSize(14);
                holder.grade = (EditText) contextView.findViewById(R.id.grade);
                holder.grade.setTextSize(14);
                holder.grade.setEnabled(false);
                holder.card = (CardView) contextView.findViewById(R.id.card);
                holder.line = (TextView) contextView.findViewById(R.id.selected);
                contextView.setTag(holder);
            }else{
                holder = (ViewCategoryViewHolder)  contextView.getTag();
            }
            holder.ref = position;
            if(position == 0){
                holder.grade.setVisibility(View.INVISIBLE);
            }
            if(position == 2) {
                holder.text.setText("Class Standing");
                holder.text.setTextColor(Color.DKGRAY);
                holder.grade.setText("60%");
                holder.grade.setTextColor(Color.DKGRAY);
            } else {
                holder.text.setText(listCat.get(position));
                holder.grade.setText(listPercent.get(position));
            }
            if(position == selectedPosition){
                holder.card.setBackgroundColor(Color.parseColor("#9ccc65"));
                holder.line.setVisibility(View.VISIBLE);
            } else {
                holder.card.setBackgroundColor(Color.WHITE);
                holder.line.setVisibility(View.GONE);
            }
            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    categoryList.setAdapter(new ViewCategoryAdapter());
                    db.getCategory(db.loginID.get(0), selectedSubject);
                    if(position == 0){
                        ((MainActivity)getActivity()).displayView("allgrade");
                        drawer.closeDrawer(GravityCompat.START);
                    } else if(position == 1){
                        setData(0, 0);
                        ((MainActivity)getActivity()).displayView("exam");
                    }  else if(position == 2){

                    } else if(position == 3){
                        setData(1, 0);
                        ((MainActivity)getActivity()).displayView("attendance");
                    } else if(position == 4){
                        setData(2, 0);
                        ((MainActivity)getActivity()).displayView("quiz");
                    } else if(position == 5){
                        setData(3, 0);
                        ((MainActivity)getActivity()).displayView("quiz");
                    } else {
                        setData(position, 2);
                        ((MainActivity)getActivity()).displayView("quiz");
                    }
                    if(position != 2){
                        selectedPosition = position;
                    }
                }
            });
            return contextView;
        }
    }
    public class ViewCategoryViewHolder {
        TextView text, line;
        EditText grade;
        CardView card;
        int ref;
    }

    public void setData(int position, int deduc){
        ((MainActivity)getActivity()).selectedSubject = selectedSubject;
        ((MainActivity)getActivity()).selectedSubjectDescription = selectedSubjectDescription;
        ((MainActivity) getActivity()).categoryId = db.category_id.get(position - deduc);
        ((MainActivity) getActivity()).categoryName = db.category.get(position - deduc);
        drawer.closeDrawer(GravityCompat.START);
    }

    //to add category
    private class AddCategory extends BaseAdapter {

        @Override
        public int getCount() {
            if(db.category != null && db.category.size() != 0){
                return db.category.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return db.category.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View contextView, ViewGroup viewGroup) {
            final AddCategoryViewHolder holder;
            if(contextView == null){
                holder = new AddCategoryViewHolder();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                contextView = inflater.inflate(R.layout.data_grade, null);
                holder.text = (TextView) contextView.findViewById(R.id.name);
                holder.grade = (EditText) contextView.findViewById(R.id.grade);
                holder.grade.setInputType(InputType.TYPE_CLASS_NUMBER);
                holder.grade.setFilters( new InputFilter[] { new InputFilter.LengthFilter(2)});
                contextView.setTag(holder);
            }else{
                holder = (AddCategoryViewHolder)  contextView.getTag();
            }
            holder.ref = position;
            holder.text.setText(db.category.get(position));
            db.getPercentage(db.loginID.get(0), db.category_id.get(position));
            if(position == 0){
                holder.grade.setEnabled(false);
            }
            if(arrTemp[position] == null) {
                holder.grade.setText(String.valueOf(Math.round(db.percentage.get(0)*100)));
            }
            else{
                holder.grade.setText(arrTemp[position]);
            }
            holder.grade.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    arrTemp[holder.ref] = editable.toString();
                }
            });
            return contextView;
        }
    }
    public class AddCategoryViewHolder {
        TextView text;
        EditText grade;
        int ref;
    }

    //to edit category
    private class EditCategoryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            if(db.category != null && db.category.size() != 0){
                return db.category.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return db.category.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(final int position, View contextView, ViewGroup viewGroup) {
            final EditCategoryViewHolder holder;
            if(contextView == null){
                holder = new EditCategoryViewHolder();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                contextView = inflater.inflate(R.layout.category_edit_data, null);
                holder.name = (EditText) contextView.findViewById(R.id.catName);
                holder.percent = (EditText) contextView.findViewById(R.id.catPercent);
                holder.delete = (CardView) contextView.findViewById(R.id.delete);
                contextView.setTag(holder);
            }else{
                holder = (EditCategoryViewHolder)  contextView.getTag();
            }
            holder.ref = position;
            idTemp[position] = db.category_id.get(position);
            db.getPercentage(db.loginID.get(0), db.category_id.get(position));
            if(position >= 0 && position <= 4){
                holder.delete.setVisibility(View.INVISIBLE);
            }
            if(position == 0){
                holder.name.setEnabled(false);
                holder.percent.setEnabled(false);
            }
            if(position == 1){
                holder.name.setEnabled(false);
            }
            if(percentTemp[holder.ref] == null) {
                holder.name.setText(db.category.get(position));
                holder.percent.setText(String.valueOf(Math.round(db.percentage.get(0)*100)));
            }
            else{
                holder.name.setText(nameTemp[position]);
                holder.percent.setText(percentTemp[position]);
            }

            holder.name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    nameTemp[holder.ref] = editable.toString();
                }
            });

            holder.percent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    percentTemp[holder.ref] = editable.toString();
                }
            });

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder yesno = new AlertDialog.Builder(getActivity());
                    yesno.setTitle("Delete "+db.category.get(position)+"?");
                    yesno.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            holder.name.setText("");
                            holder.percent.setText("");
                            holder.name.setVisibility(View.GONE);
                            holder.percent.setVisibility(View.GONE);
                            holder.delete.setVisibility(View.GONE);

                            deleteCategory.add(db.category_id.get(position));
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

            return contextView;
        }
    }
    public class EditCategoryViewHolder {
        EditText name, percent;
        CardView delete;
        int ref;
    }
}
