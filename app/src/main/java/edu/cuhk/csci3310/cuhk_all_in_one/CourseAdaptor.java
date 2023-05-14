package edu.cuhk.csci3310.cuhk_all_in_one;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CourseAdaptor extends RecyclerView.Adapter<CourseAdaptor.ViewHolder>{

    JSONArray localdataset;

    CourseListener listener;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView abbrevView;
        private final TextView nameView;
        private final RatingBar ratingView;
        private final TextView ratingText;
        private final CardView cardView;

        public ViewHolder(View view) {
            super(view);
            abbrevView = view.findViewById(R.id.courseIdView);
            nameView = view.findViewById(R.id.courseNameView);
            ratingView = view.findViewById(R.id.courseRatingView);
            ratingText = view.findViewById(R.id.courseRatingText);
            cardView = view.findViewById(R.id.courseCardView);
        }

        public TextView getAbbrevView(){return abbrevView;}
        public TextView getNameView(){return nameView;}
        public RatingBar getRatingView(){return  ratingView;}
        public TextView getRatingText(){return ratingText;}

        public CardView getCardView() {
            return cardView;
        }
    }

    public CourseAdaptor(JSONArray data, CourseListener listener){
        localdataset = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.course_item, parent, false);
        return new CourseAdaptor.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try{
            holder.getAbbrevView().setText(localdataset.getJSONObject(position).getString("abbrev"));
            holder.getNameView().setText(localdataset.getJSONObject(position).getString("fullname"));
            float rating = (float)localdataset.getJSONObject(position).getDouble("Rating");
            holder.getRatingView().setRating(rating);
            holder.getRatingView().setIsIndicator(true);
            holder.getRatingText().setText(String.format("%.1f", rating));
            holder.getCardView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onClick(holder.getBindingAdapterPosition());
                }
            });
        }catch(JSONException e){
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return localdataset.length();
    }

    public void UpdateDataSet(JSONArray data){
        localdataset = data;
        notifyDataSetChanged();
    }
}
