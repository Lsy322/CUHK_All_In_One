package edu.cuhk.csci3310.cuhk_all_in_one;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
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
import android.widget.ImageView;

import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class TodoFragment extends Fragment implements DateListener{

    // Todo Data Array
    private JSONArray todoList = new JSONArray();

    // Popup Views
    TextInputEditText titleInput;
    TextInputEditText descriptionInput;
    CheckBox notificationFlag;
    Button datePickerTrigger;

    TodoListAdaptor adaptor;

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
        // Init Recycler View
        RecyclerView recyclerView = v.findViewById(R.id.todoListView);
        JSONObject todo = new JSONObject();
        try{
            todo.put("Title", "Test");
            todo.put("Deadline", "2023-01-01");
            todoList.put(todo);
        }catch (JSONException e){
            e.printStackTrace();
        }
        adaptor = new TodoListAdaptor(todoList);
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
                // Add new todo to the todo list
                JSONObject todo = new JSONObject();
                try{
                    todo.put("Title", titleInput.getText());
                    todo.put("Deadline", datePickerTrigger.getText());
                    todoList.put(todo);
                }catch (JSONException e){
                    e.printStackTrace();
                }
                // Update the recycler view
                adaptor.UpdateTodoList(todoList);
                Log.v("Count", Integer.toString(todoList.length()));
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
}