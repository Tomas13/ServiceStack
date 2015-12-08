package kz.growit.servicestack;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by jean on 12/5/2015.
 */

public class DownloadService extends IntentService {
    public static final int UPDATE_PROGRESS = 8344;
    final static int myID = 1234;
    Notification notification;

    public DownloadService() {
        super("DownloadService");
    }



    Handler HN = new Handler();

    private class DisplayToast implements Runnable {
        String TM = "";

        public DisplayToast(String toast){
            TM = toast;
        }

        public void run(){
            Toast.makeText(getApplicationContext(), TM, Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        Intent i=new Intent(this, MainActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi=PendingIntent.getActivity(this, 0,
                i, 0);

        Notification.Builder builder = new Notification.Builder(getApplicationContext());

        builder.setAutoCancel(false);
        builder.setTicker("this is ticker text");
        builder.setContentTitle("WhatsApp Notification");
        builder.setContentText("You have a new message");
        builder.setSmallIcon(R.drawable.tassta_logo);
        builder.setContentIntent(pi);
        builder.setOngoing(true);
        builder.setNumber(100);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.build();
        }

        Notification myNotication = builder.getNotification();

//        note.setLatestEventInfo(this, "Fake Player", "Now Playing: \"Ummmm, Nothing\"", pi);
//        note.flags|=Notification.FLAG_NO_CLEAR;

        startForeground(1337, myNotication);

        String urlToDownload = intent.getStringExtra("url");
        String fileName = intent.getStringExtra("fileName");
        ResultReceiver receiver = (ResultReceiver) intent.getParcelableExtra("receiver");
        try {
            URL url = new URL(urlToDownload);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();

            File output2 = new File(Environment.getExternalStorageDirectory(), fileName);

            // download the file
            InputStream input = new BufferedInputStream(connection.getInputStream());
            OutputStream output = new FileOutputStream(output2);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                Bundle resultData = new Bundle();
                resultData.putInt("progress", (int) (total * 100 / fileLength));
                receiver.send(UPDATE_PROGRESS, resultData);
                if((int) (total * 100 / fileLength) == 100){
                    HN.post(new DisplayToast("FINISH DOWNLOAD"));
//                    stopForeground(true);
                }
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bundle resultData = new Bundle();
        resultData.putInt("progress", 100);
        receiver.send(UPDATE_PROGRESS, resultData);

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainActivity.mBroadcastStringAction);
        broadcastIntent.putExtra("Data", resultData.getInt("progress"));
        sendBroadcast(broadcastIntent);

    }
}
