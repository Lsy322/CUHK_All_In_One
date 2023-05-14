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

public class CommentAdaptor extends RecyclerView.Adapter<CommentAdaptor.ViewHolder> {

    JSONArray localdataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView commentView;
        private final TextView commentDate;
        private final RatingBar commentRating;

        public ViewHolder(View view) {
            super(view);
            commentView = view.findViewById(R.id.commentContentView);
            commentDate = view.findViewById(R.id.commentDate);
            commentRating = view.findViewById(R.id.commentRating);
        }

        public RatingBar getCommentRating() {
            return commentRating;
        }

        public TextView getCommentDate() {
            return commentDate;
        }

        public TextView getCommentView() {
            return commentView;
        }
    }

    public CommentAdaptor(JSONArray data) {
        localdataset = data;
    }

    @NonNull
    @Override
    public CommentAdaptor.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item, parent, false);
        return new CommentAdaptor.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentAdaptor.ViewHolder holder, int position) {
        try {
            holder.getCommentView().setText(localdataset.getJSONObject(position).getString("Content"));
            holder.getCommentDate().setText(localdataset.getJSONObject(position).getString("PostedDate"));
            holder.getCommentRating().setRating((float)localdataset.getJSONObject(position).getDouble("Rating"));
            holder.getCommentRating().setIsIndicator(true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void AddNewComment(JSONArray data){
        // Default new comment is added add index 0
        localdataset = data;
        notifyItemInserted(0);
    }

    @Override
    public int getItemCount() {
        return localdataset.length();
    }
}
