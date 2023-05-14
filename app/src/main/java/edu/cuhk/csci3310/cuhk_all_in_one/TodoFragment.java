package edu.cuhk.csci3310.cuhk_all_in_one;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class TodoFragment extends Fragment implements TodoListener {

    // Todo Data Array
    private JSONArray todoList = new JSONArray();
    TextView emptyView;

    // Popup Views
    TextInputEditText titleInput;
    TextInputEditText descriptionInput;
    CheckBox notificationFlag;
    Button datePickerTrigger;

    TodoListAdaptor adaptor;

    // Shared Preference File
    private final String SharedPrefFile = "edu.cuhk.csci3310.cuhk_all_in_one";

    public TodoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.todo_option_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.addTodo:
                showToDoPopup(getContext());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_todo, container, false);
        // Inflate the option menu on top right
        setHasOptionsMenu(true);

        // Read todos from shared preference
        SharedPreferences mPreferences = getActivity().getSharedPreferences(SharedPrefFile, Context.MODE_PRIVATE);
        try {
            todoList = new JSONArray(mPreferences.getString("todoData", ""));
        }catch(JSONException e){
            e.printStackTrace();
        }

        emptyView = v.findViewById(R.id.empty_todo_view);
        if (todoList.length() == 0){
           emptyView.setVisibility(View.VISIBLE);
        }
        // Init Recycler View
        RecyclerView recyclerView = v.findViewById(R.id.todoListView);
        adaptor = new TodoListAdaptor(todoList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adaptor);
        super.onCreate(savedInstanceState);
        // Inflate the layout for this fragment
        return v;
    }

    // Implementation of the add todo popup
    private void showToDoPopup(Context context){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.todo_popup);
        dialog.setCanceledOnTouchOutside(false);

        titleInput = dialog.findViewById(R.id.titleInput);
        descriptionInput = dialog.findViewById(R.id.descriptionInput);
        notificationFlag = dialog.findViewById(R.id.notiCheckBox);
        Button submitButton = dialog.findViewById(R.id.submitNewTodo);
        datePickerTrigger = dialog.findViewById(R.id.datepicker);
        ImageButton exitButton = dialog.findViewById(R.id.exitTodo);

        // Set the default date of the display date picker to current date
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        String fString = String.format("%02d-%02d-%d", year , month + 1, day);
        datePickerTrigger.setText(fString);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Pre-submit check
                if (titleInput.getText().toString().equals("")){
                    Toast t = Toast.makeText(getContext(), "Title can not be null!", Toast.LENGTH_SHORT);
                    t.show();
                    return;
                }
                // Add new todo to the todo list
                JSONObject todo = new JSONObject();
                long savedID = -1;
                try{
                    // Add new todo to list
                    savedID = System.currentTimeMillis();
                    todo.put("ID", savedID);
                    todo.put("Title", titleInput.getText().toString());
                    todo.put("Deadline", datePickerTrigger.getText());
                    todo.put("Description", descriptionInput.getText().toString());
                    todoList.put(todo);
                    List<JSONObject> jsonValues = new ArrayList<JSONObject>();
                    for (int i = 0; i < todoList.length(); i++) {
                        jsonValues.add(todoList.getJSONObject(i));
                    }
                    // Sort the todo array
                    Collections.sort(jsonValues, new Comparator<JSONObject>(){
                        private static final String KEY_NAME = "Deadline";

                        @Override
                        public int compare(JSONObject a, JSONObject b){
                            String valA = new String();
                            String valB = new String();

                            try{
                                valA = a.getString(KEY_NAME);
                                valB = b.getString(KEY_NAME);
                            }catch (JSONException e){
                                e.printStackTrace();
                            }

                            return valA.compareTo(valB);
                        }
                    });
                    todoList = new JSONArray();
                    for (int i = 0; i < jsonValues.size(); i++){
                        todoList.put(jsonValues.get(i));
                    }

                    // Save new todo to shared preference
                    SharedPreferences mPreferences = getActivity().getSharedPreferences(SharedPrefFile, Context.MODE_PRIVATE);
                    SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                    preferencesEditor.putString("todoData", todoList.toString()).apply();

                    // Set the notification
                    if (notificationFlag.isChecked()){
                        // Convert String date to Mills
                        try{
                            String myDate = todo.getString("Deadline");
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
                            Date date = sdf.parse(myDate);
                            long mills = date.getTime();
                            Intent notifyIntent = new Intent(getContext(), TodoReceiver.class);
                            notifyIntent.putExtra("Title", titleInput.getText().toString());
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int)todo.getLong("ID"), notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, mills, pendingIntent);
                        }catch (ParseException e){
                            e.printStackTrace();
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
                if (todoList.length() > 0){
                    emptyView.setVisibility(View.GONE);
                }
                // Update the recycler view
                adaptor.UpdateTodoList(savedID, todoList);
                dialog.dismiss();
            }
        });
        datePickerTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dialogWidth = (int)(displayMetrics.widthPixels * 0.85);
        int dialogHeight = (int)(displayMetrics.heightPixels * 0.85);
        dialog.getWindow().setLayout(dialogWidth, dialogHeight);
        dialog.show();
    }

    // Show the date picker
    public void showDatePickerDialog(){
        DialogFragment datepickerFrag = new DatePickerFragment(this);
        datepickerFrag.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    // Implement listener to listen for result for date picker
    @Override
    public void onDateSet(int i, int i1, int i2) {
        String fString = String.format("%d-%02d-%02d", i , i1 + 1, i2);
        datePickerTrigger.setText(fString);
    }

    // Implement listener for card deletion
    public void onTodoCardDeleted(int index) {
        try{
            // Remove Notification
            AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(getContext().ALARM_SERVICE);
            Intent intent = new Intent(getContext(), TodoReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), (int)todoList.getJSONObject(index).getLong("ID"), intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }catch (JSONException e){
            e.printStackTrace();
        }
        // remove target item
        todoList.remove(index);
        if (todoList.length() == 0){
            emptyView.setVisibility(View.VISIBLE);
        }
        // Save the updated list
        SharedPreferences mPreferences = getActivity().getSharedPreferences(SharedPrefFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putString("todoData", todoList.toString()).apply();
    }

    // Implement todo long click, show detail of the to do
    @Override
    public void onTodoClick(int index) {
        try{
            showDetailTodo(index, todoList.getJSONObject(index));
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void showDetailTodo(int index, JSONObject todo){
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.todo_detail);
        dialog.setCanceledOnTouchOutside(false);

        TextView detailTitle = dialog.findViewById(R.id.detailTitle);
        TextView detailDesc = dialog.findViewById(R.id.detailDescription);
        TextView detailDeadline = dialog.findViewById(R.id.detailDeadline);
        Button removeButton = dialog.findViewById(R.id.detailRemoveButton);

        try{
            detailTitle.setText(todo.getString("Title"));
            detailDesc.setText(todo.getString("Description"));
            detailDeadline.setText(todo.getString("Deadline"));
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onTodoCardDeleted(index);
                    adaptor.notifyItemRemoved(index);
                    dialog.dismiss();
                }
            });
        }catch(JSONException e){
            e.printStackTrace();
        }
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int dialogWidth = (int)(displayMetrics.widthPixels * 0.95);
        int dialogHeight = (int)(displayMetrics.heightPixels * 0.95);
        dialog.getWindow().setLayout(dialogWidth, dialogHeight);
        dialog.show();
    }
}