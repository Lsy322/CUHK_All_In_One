package edu.cuhk.csci3310.cuhk_all_in_one;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

public class MapsFragment extends Fragment implements SelectListener, GoogleMap.OnInfoWindowClickListener{
    JSONArray buildings = new JSONArray();
    JSONArray canteens = new JSONArray();
    double minLat = 0;
    double minLng = 0;
    double maxLat = 0;
    double maxLng = 0;
    int selectedMode = 0; // 0 is building, 1 is canteen
    GoogleMap mMap;
    SearchView mSearchView;
    RecyclerView mRecyclerView;
    MapSearchAdaptor adaptor;
    String filterkey = "";

    private RequestQueue mRequestQueue;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {

        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        @Override
        public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;

            // Buildings
            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, "http://10.0.2.2:3000/api/building/", null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Log.v("Building Response", response.toString());
                    buildings = response;
                    for (int i = 0; i < buildings.length(); i++){
                        try{
                            JSONObject curr = buildings.getJSONObject(i);
                            curr.put("Rating",CalculateMapRating(curr));
                            buildings.put(i, curr);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                    pinArray(buildings);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println(error);
                }
            });
            mRequestQueue.add(request);

            JsonArrayRequest canteenRequest = new JsonArrayRequest(Request.Method.GET, "http://10.0.2.2:3000/api/canteen/", null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Log.v("Canteen Response", response.toString());
                    canteens = response;
                    for (int i = 0; i < canteens.length(); i++){
                        try{
                            JSONObject curr = canteens.getJSONObject(i);
                            curr.put("Rating",CalculateMapRating(curr));
                            canteens.put(i, curr);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println(error);
                }
            });
            mRequestQueue.add(canteenRequest);

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.419625, 114.2045719), 15));

            // Search View
            mSearchView = getView().findViewById(R.id.mapSearchView);
            mRecyclerView = getView().findViewById(R.id.mapSearch);
            mSearchView.setOnSearchClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    showRecyclerView();
                }
            });
            mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    filterkey = "";
                    clearRecyclerView();
                    return false;
                }
            });
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    // Save the key in case of mode switch
                    filterkey = s;
                    filter(s);
                    return false;
                }
            });
            initRecyclerView();
        }
    };

    public void initRecyclerView(){
        adaptor = new MapSearchAdaptor(new JSONArray(), this);
        mRecyclerView.setAdapter(adaptor);
        // Add linear layout for the search items
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void pinArray(JSONArray array){
        for (int i = 0; i < array.length(); i++){
            try{
                JSONObject curr = array.getJSONObject(i);
                double lat = curr.getDouble("Lat");
                double lng = curr.getDouble("Lng");
                if (i == 0){
                    minLat = lat;
                    maxLat = lat;
                    minLng = lng;
                    maxLng = lng;
                }else{
                    if (lat > maxLat){
                        maxLat = lat;
                    }else if (lat < minLat){
                        minLat = lat;
                    }
                    if (lng > maxLng){
                        maxLng = lng;
                    }else if (lng < minLng){
                        minLng = lng;
                    }
                }
                LatLng loc = new LatLng(lat, lng);
                Marker marker = mMap.addMarker(new MarkerOptions().position(loc).title(curr.getString("fullname")).snippet(curr.getString("abbrev")));
                marker.setTag(curr);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        // Set the info window click listener
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestQueue = Volley.newRequestQueue(getContext());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.map_pin_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Return to full map view
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(22.419625, 114.2045719), 15));
        switch (item.getItemId()){
            case R.id.buildings:
                selectedMode = 0;
                mMap.clear();
                pinArray(buildings);
                filter(filterkey);
                return true;
            case R.id.canteens:
                selectedMode = 1;
                mMap.clear();
                pinArray(canteens);
                filter(filterkey);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    public void showRecyclerView(){
        mRecyclerView.setVisibility(View.VISIBLE);
        if (selectedMode == 0){
            adaptor.filterList(buildings);
        }else if (selectedMode == 1){
            adaptor.filterList(canteens);
        }
    }

    public void clearRecyclerView(){
        mRecyclerView.setVisibility(View.GONE);
    }

    public void filter(String key){
        JSONArray filteredArray = new JSONArray();
        JSONArray target = new JSONArray();
        if (selectedMode == 0){
            target = buildings;
        }else if (selectedMode == 1){
            target = canteens;
        }
        for (int i = 0; i < target.length(); i++){
            try{
                JSONObject curr = target.getJSONObject(i);
                String lowerKey = key.toLowerCase();
                if (curr.getString("abbrev").toLowerCase().contains(lowerKey) || curr.getString("fullname").toLowerCase().contains(lowerKey)){
                    filteredArray.put(curr);
                }
                if (filteredArray.length() > 0){
                    adaptor.filterList(filteredArray);
                }else{
                    adaptor.filterList(new JSONArray());
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSearchItemClick(JSONObject clickedObject) {
        try{
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(clickedObject.getDouble( "Lat"), clickedObject.getDouble("Lng")), 18));
            mSearchView.setQuery("",false);
            mSearchView.setIconified(true);
            clearRecyclerView();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        JSONObject currItem = (JSONObject) marker.getTag();
        // Show map detail dialogue
        final Dialog dialog = new Dialog(getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.map_detail);
        dialog.setCanceledOnTouchOutside(false);

        // Get components in Dialogs
        TextView mapAbbrev = dialog.findViewById(R.id.mapItemAbbrev);
        TextView mapTitle = dialog.findViewById(R.id.mapItemName);
        RatingBar mapRating = dialog.findViewById(R.id.mapItemRating);
        Button addNewButton = dialog.findViewById(R.id.mapAddNewCommentButton);
        // Set the rating display as indicator
        mapRating.setIsIndicator(true);
        // Get the comment recyclerView
        RecyclerView recyclerView = dialog.findViewById(R.id.mapCommentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        JSONArray commentArray = new JSONArray();
        try {
            commentArray = currItem.getJSONArray("Comment");
        }catch (JSONException e){
            e.printStackTrace();
        }
        CommentAdaptor mCommentAdaptor = new CommentAdaptor(commentArray);

        // Show empty view if no comments
        TextView emptyView;
        emptyView = dialog.findViewById(R.id.empty_map_comment_view);
        if (commentArray.length() == 0){
            emptyView.setVisibility(View.VISIBLE);
        }
        // Set the comment adaptor
        recyclerView.setAdapter(mCommentAdaptor);
        // Set map dialogue view content
        try{
            mapAbbrev.setText(currItem.getString("abbrev"));
            mapTitle.setText(currItem.getString("fullname"));
            mapRating.setRating((float)currItem.getDouble("Rating"));
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
                                JSONArray commentArray = currItem.getJSONArray("Comment");
                                for (int i = 0; i < commentArray.length(); i++){
                                    newCommentsList.put(commentArray.getJSONObject(i));
                                }
                                currItem.put("Comment", newCommentsList);
                                // Update the comments list
                                mCommentAdaptor.AddNewComment(newCommentsList);
                                // Update course Rating
                                float newRating = CalculateMapRating(currItem);
                                currItem.put("Rating", newRating);
                                mapRating.setRating(newRating);
                                UpdateMapItem(currItem);
                                // Send the new comment to API
                                // put abbrev to comment
                                comment.put("fullname", currItem.getString("fullname"));
                                String URL = "";
                                if (selectedMode == 0){
                                    URL = "http://10.0.2.2:3000/api/building/review/";
                                }else if (selectedMode == 1){
                                    URL = "http://10.0.2.2:3000/api/canteen/review/";
                                }
                                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, comment, new Response.Listener<JSONObject>() {
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

    public float CalculateMapRating(JSONObject courseObject){
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

    public void UpdateMapItem(JSONObject mapItem){
        try{
            String mapItemName = mapItem.getString("fullname");
            if (selectedMode == 0){
                for (int i = 0; i < buildings.length(); i++){
                    String currName = buildings.getJSONObject(i).getString("fullname");
                    if (currName.equals(mapItemName)){
                        buildings.put(i, mapItem);
                        break;
                    }
                }
            }else if (selectedMode == 1){
                for (int i = 0; i < canteens.length(); i++){
                    String currName = canteens.getJSONObject(i).getString("fullname");
                    if (currName.equals(mapItemName)){
                        canteens.put(i, mapItem);
                        break;
                    }
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}