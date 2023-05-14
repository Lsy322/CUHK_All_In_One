package edu.cuhk.csci3310.cuhk_all_in_one;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.maps.MapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "edu.cuhk.csci3310.cuhk_all_in_one";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NavigationBarView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MapsFragment()).commit();

        createNotificationChannel();
    }

    private final NavigationBarView.OnItemSelectedListener navListener = item ->{
        Fragment selectedFragment = null;
        int itemId = item.getItemId();
        if (itemId == R.id.map){
            selectedFragment = new MapsFragment();
        }else if (itemId == R.id.todo){
            selectedFragment = new TodoFragment();
        }else if (itemId == R.id.course){
            selectedFragment = new CourseFragment();
        }
        if (selectedFragment != null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
        }
        return true;
    };

    private void createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "Channel 1";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription("This is Test channel");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}