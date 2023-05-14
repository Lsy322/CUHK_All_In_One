package edu.cuhk.csci3310.cuhk_all_in_one;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CourseFragment extends Fragment implements CourseListener{
    JSONArray comments = new JSONArray();
    JSONArray courseList = new JSONArray();

    CourseAdaptor adaptor;

    RequestQueue mRequestQueue;

    public CourseFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestQueue = Volley.newRequestQueue(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_course, container, false);

        adaptor = new CourseAdaptor(courseList, this);
        RecyclerView recyclerView = v.findViewById(R.id.courseListView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adaptor);

        // Get Dataset from API
        // Get Courses from API
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, "http://10.0.2.2:3000/api/course/", null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.v("Course Response", response.toString());
                courseList = response;
                // Calculate Course Ratings by comments
                for (int i = 0; i < courseList.length(); i++){
                    try {
                        JSONObject currCourse = courseList.getJSONObject(i);
                        currCourse.put("Rating", CalculateCourseRating(currCourse));
                        courseList.put(i, currCourse);
                    }catch(JSONException e){
                        e.printStackTrace();
                    }
                }
                adaptor.UpdateDataSet(courseList);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error);
            }
        });
        mRequestQueue.add(request);

        SearchView mSearchView = v.findViewById(R.id.courseSearchView);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.length() > 0){
                    filter(s);
                }
                return false;
            }
        });
        // Inflate the layout for this fragment
        return v;
    }

    public void filter(String s){
        JSONArray filtered = new JSONArray();
        for (int i = 0; i < courseList.length(); i++){
            try {
                JSONObject curr = courseList.getJSONObject(i);
                if (curr.getString("abbrev").toLowerCase().contains(s.toLowerCase()) || curr.getString("fullname").toLowerCase().contains(s.toLowerCase())){
                    filtered.put(curr);
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        adaptor.UpdateDataSet(filtered);
    }

    @Override
    public void onClick(int index) {
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.course_detail);
        dialog.setCanceledOnTouchOutside(false);

        // Get components in Dialogs
        TextView courseAbbrev = dialog.findViewById(R.id.courseDetailAbbrev);
        TextView courseTitle = dialog.findViewById(R.id.courseDetailTitle);
        RatingBar courseRating = dialog.findViewById(R.id.courseDetailRating);
        Button addNewButton = dialog.findViewById(R.id.courseAddNewCommentButton);
        courseRating.setIsIndicator(true);
        RecyclerView recyclerView = dialog.findViewById(R.id.courseCommentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        JSONArray commentArray = new JSONArray();
        try {
            commentArray = courseList.getJSONObject(index).getJSONArray("Comment");
        }catch (JSONException e){
            e.printStackTrace();
        }
        CommentAdaptor mCommentAdaptor = new CommentAdaptor(commentArray);

        // Show empty view if no comments
        TextView emptyView;
        emptyView = dialog.findViewById(R.id.empty_course_comment_view);
        if (commentArray.length() == 0){
            emptyView.setVisibility(View.VISIBLE);
        }
        recyclerView.setAdapter(mCommentAdaptor);
        try{
            JSONObject curr = courseList.getJSONObject(index);
            courseAbbrev.setText(curr.getString("abbrev"));
            courseTitle.setText(curr.getString("fullname"));
            courseRating.setRating((float)curr.getDouble("Rating"));
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            addNewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog addCommentDialog = new Dialog(getContext());
                    addCommentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    addCommentDialog.setContentView(R.layout.comment_form);
                    addCommentDialog.setCanceledOnTouchOutside(false);
                    // Get components in Dialog
                    EditText commentInput = addCommentDialog.findViewById(R.id.commentInput);
                    RatingBar ratingInput = addCommentDialog.findViewById(R.id.commentRatingInput);
                    Button submitButton = addCommentDialog.findViewById(R.id.newCommentSubmit);
                    // Set the submit button onclick handler
                    submitButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Add new comment to list
                            JSONObject comment = new JSONObject();
                            try{
                                String commentString = commentInput.getText().toString();
                                if (commentString.equals("")){
                                    Toast t = Toast.makeText(getContext(), "Comment can't be empty.", Toast.LENGTH_SHORT);
                                    t.show();
                                    return;
                                }
                                comment.put("Content", commentString);
                                comment.put("Rating", ratingInput.getRating());
                                // Create the date
                                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                                Date date = new Date();
                                comment.put("PostedDate", formatter.format(date));
                                JSONArray newCommentsList = new JSONArray();
                                newCommentsList.put(comment);
                                JSONArray commentArray = courseList.getJSONObject(index).getJSONArray("Comment");
                                for (int i = 0; i < commentArray.length(); i++){
                                    newCommentsList.put(commentArray.getJSONObject(i));
                                }
                                curr.put("Comment", newCommentsList);
                                // Update the comments list
                                mCommentAdaptor.AddNewComment(newCommentsList);
                                // Update course Rating
                                float newRating = CalculateCourseRating(curr);
                                curr.put("Rating", newRating);
                                courseRating.setRating(newRating);
                                courseList.put(index, curr);
                                // Display new Rating
                                adaptor.UpdateDataSet(courseList);
                                courseRating.setRating((float)curr.getDouble("Rating"));

                                // Send the new comment to API
                                // put abbrev to comment
                                comment.put("abbrev", curr.getString("abbrev"));
                                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "http://10.0.2.2:3000/api/course/review/", comment, new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.v("Review Send", response.toString());
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        System.out.println(error);
                                    }
                                });
                                mRequestQueue.add(request);
                            }catch (JSONException e){
                                e.printStackTrace();
                            }
                            emptyView.setVisibility(View.GONE);
                            addCommentDialog.dismiss();
                        }
                    });
                    // Display the add new comment dialogue
                    DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
                    int dialogWidth = (int)(displayMetrics.widthPixels * 0.95);
                    int dialogHeight = (int)(displayMetrics.heightPixels * 0.4);
                    addCommentDialog.getWindow().setLayout(dialogWidth, dialogHeight);
                    addCommentDialog.show();
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

    public float CalculateCourseRating(JSONObject courseObject){
        try {
            JSONArray courseComments = courseObject.getJSONArray("Comment");
            Log.v("Length", Integer.toString(courseComments.length()));
            if (courseComments.length() <= 0){
                return 0;
            }
            float sumRating = 0;
            for (int i = 0; i < courseComments.length(); i++){
                sumRating += courseComments.getJSONObject(i).getDouble("Rating");
            }
            return (sumRating / courseComments.length());
        }catch (JSONException e){
            e.printStackTrace();
        }
        return 0;
    }
}