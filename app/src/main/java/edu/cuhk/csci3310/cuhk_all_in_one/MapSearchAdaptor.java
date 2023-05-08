package edu.cuhk.csci3310.cuhk_all_in_one;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

public class MapSearchAdaptor extends RecyclerView.Adapter<MapSearchAdaptor.ViewHolder> {

    JSONArray localDataSet;
    private SelectListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final CardView cardView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            textView = (TextView) view.findViewById(R.id.searchItemText);
            cardView = view.findViewById(R.id.searchItemContainer);
        }

        public TextView getTextView() {
            return textView;
        }

        public CardView getCardView(){
            return cardView;
        }
    }

    public MapSearchAdaptor(JSONArray dataSet, SelectListener listener){
        localDataSet = dataSet;
        this.listener = listener;
    }

    public void filterList(JSONArray filteredArray){
        localDataSet = filteredArray;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_row_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try{
            holder.getTextView().setText(localDataSet.getJSONObject(position).getString("fullname"));
            holder.getCardView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{
                        listener.onSearchItemClick(localDataSet.getJSONObject(holder.getBindingAdapterPosition()));
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            });
        }catch(JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet.length();
    }
}
