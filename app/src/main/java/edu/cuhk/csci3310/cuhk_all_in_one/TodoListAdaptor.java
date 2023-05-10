package edu.cuhk.csci3310.cuhk_all_in_one;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

public class TodoListAdaptor extends RecyclerView.Adapter<TodoListAdaptor.ViewHolder> {

    JSONArray localDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView deadlineView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            titleView = view.findViewById(R.id.todoItemTitle);
            deadlineView = view.findViewById(R.id.todoItemDeadline);
        }

        public TextView getTitleView() {
            return titleView;
        }

        public TextView getDeadlineView(){
            return deadlineView;
        }

    }

    public TodoListAdaptor(JSONArray dataSet){
        localDataSet = dataSet;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.todo_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try{
            holder.getTitleView().setText(localDataSet.getJSONObject(position).getString("Title"));
            holder.getDeadlineView().setText(localDataSet.getJSONObject(position).getString("Deadline"));
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public void UpdateTodoList(JSONArray todoList){
        this.localDataSet = todoList;
        notifyItemInserted(todoList.length());
    }

    @Override
    public int getItemCount() {
        return localDataSet.length();
    }
}
