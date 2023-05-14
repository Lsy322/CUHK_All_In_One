package edu.cuhk.csci3310.cuhk_all_in_one;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TodoListAdaptor extends RecyclerView.Adapter<TodoListAdaptor.ViewHolder> {

    JSONArray localDataSet;
    TodoListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView deadlineView;
        private final CardView cardView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            titleView = view.findViewById(R.id.todoItemTitle);
            deadlineView = view.findViewById(R.id.todoItemDeadline);
            cardView = view.findViewById(R.id.todoCardView);
        }

        public TextView getTitleView() {
            return titleView;
        }

        public TextView getDeadlineView(){
            return deadlineView;
        }

        public CardView getCardView(){return  cardView;}
    }

    public TodoListAdaptor(JSONArray dataSet, TodoListener listener){
        localDataSet = dataSet;
        this.listener = listener;
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
            holder.getTitleView().setTypeface(null, Typeface.BOLD);
            holder.getDeadlineView().setText(localDataSet.getJSONObject(position).getString("Deadline"));
            holder.getCardView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onTodoClick(holder.getBindingAdapterPosition());
                }
            });
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    public void UpdateTodoList(long id, JSONArray todoList){
        this.localDataSet = todoList;
        // Find the newly added todo item in the sorted array
        int newIndex = -1;
        for (int i = 0; i < todoList.length(); i++){
            try{
                JSONObject curr = todoList.getJSONObject(i);
                if (curr.getLong("ID") == id){
                    newIndex = i;
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        if (newIndex != -1){
            notifyItemInserted(newIndex);
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet.length();
    }
}
