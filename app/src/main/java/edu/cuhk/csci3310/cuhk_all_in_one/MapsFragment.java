package edu.cuhk.csci3310.cuhk_all_in_one;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.logging.Logger;

public class MapsFragment extends Fragment implements SelectListener{
    JSONArray buildings = new JSONArray();
    double minLat = 0;
    double minLng = 0;
    double maxLat = 0;
    double maxLng = 0;
    GoogleMap mMap;
    SearchView mSearchView;
    RecyclerView mRecyclerView;
    MapSearchAdaptor adaptor;

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
            try{
                JSONObject building1 = new JSONObject();
                building1.put("abbrev", "AB1");
                building1.put("fullname", "Academic Building No.1");
                building1.put("Lat",22.4175314);
                building1.put("Lng", 114.2073637);

                JSONObject building2 = new JSONObject();
                building2.put("abbrev", "AMEW");
                building2.put("fullname", "Art Museum East Wing");
                building2.put("Lat",22.4193142);
                building2.put("Lng", 114.206023);

                buildings.put(building1);
                buildings.put(building2);
            }catch (JSONException e){
                e.printStackTrace();
            }


            for (int i = 0; i < buildings.length(); i++){
                try{
                    JSONObject curr = buildings.getJSONObject(i);
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
                    googleMap.addMarker(new MarkerOptions().position(loc).title(curr.getString("fullname")).snippet(curr.getString("abbrev")));
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
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
                    filter(s);
                    return false;
                }
            });
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adaptor = new MapSearchAdaptor(buildings, this);
        mRecyclerView.setAdapter(adaptor);
    }

    public void clearRecyclerView(){
        mRecyclerView.setLayoutManager(null);
        mRecyclerView.setAdapter(null);
    }

    public void filter(String key){
        JSONArray filteredBuilding = new JSONArray();
        for (int i = 0; i < buildings.length(); i++){
            try{
                JSONObject currBuilding = buildings.getJSONObject(i);
                String lowerKey = key.toLowerCase();
                if (currBuilding.getString("abbrev").toLowerCase().contains(lowerKey) || currBuilding.getString("fullname").toLowerCase().contains(lowerKey)){
                    filteredBuilding.put(currBuilding);
                }
                if (filteredBuilding.length() > 0){
                    adaptor.filterList(filteredBuilding);
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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(clickedObject.getDouble("Lat"), clickedObject.getDouble("Lng")), 18));
            mSearchView.setQuery("",false);
            mSearchView.setIconified(true);
            clearRecyclerView();
        }catch (JSONException e){
            e.printStackTrace();
        }
    }
}