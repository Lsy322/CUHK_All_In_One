package edu.cuhk.csci3310.cuhk_all_in_one;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TodoReceiver extends BroadcastReceiver {
    public TodoReceiver(){
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent1 = new Intent(context, TodoNotificationService.class);
        intent1.putExtra("Title", intent.getStringExtra("Title"));
        context.startService(intent1);
    }

    ;


}
