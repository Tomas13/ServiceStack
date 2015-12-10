package kz.growit.servicestack;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.*;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final String mBroadcastStringAction = "kz.growit.servicestack";
    int ProgressIntent;
    ProgressBar mProgressBar;
    Button start, stop;
    EditText fileNameET, urlET;
    String fileName, url;

    Boolean isRunning = false;
    Boolean isPaused = false;


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(mBroadcastStringAction)) {
                ProgressIntent = (Integer) intent.getExtras().get("Data");
                mProgressBar.setProgress(ProgressIntent);
            }
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putInt("ProgressActivity", ProgressIntent);

        if (isRunning) {
            outState.putBoolean("isRunning", true);
        }
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(mBroadcastStringAction);


        urlET = (EditText) findViewById(R.id.urlET);
        fileNameET = (EditText) findViewById(R.id.fileNameET);
        start = (Button) findViewById(R.id.startBtn);
        stop = (Button) findViewById(R.id.stopBtn);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setMax(100);
        mProgressBar.setIndeterminate(false);
        mProgressBar.setVisibility(View.VISIBLE);

        // Check whether we're recreating a previously destroyed instance
        //But it doesn't seem to save data when user force quits
        if (savedInstanceState != null) {
            // Restore value of members from saved state
            ProgressIntent = savedInstanceState.getInt("ProgressActivity");
//            mProgressBar.setProgress(ProgressIntent);
            isRunning = savedInstanceState.getBoolean("isRunning");
        } else {
//            mProgressBar.setProgress(ProgressIntent);
        }



        registerReceiver(mReceiver, mIntentFilter);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(0);

                fileName = fileNameET.getText().toString();
                url = urlET.getText().toString();
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.putExtra("url", url);
                intent.putExtra("fileName", fileName);
                intent.putExtra("receiver", new DownloadReceiver(new Handler()));
//                startService(intent);

                if (!isRunning) {
                    isRunning = true;
                    isPaused = false;
                    start.setText("Pause");

                    startService(intent);
                } else {
                    isRunning = false;
                    isPaused = true;
                    start.setText("Start");
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //finding a service's process and killing it
                ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
                Iterator<ActivityManager.RunningAppProcessInfo> iter = runningAppProcesses.iterator();
                while(iter.hasNext()){
                    ActivityManager.RunningAppProcessInfo next = iter.next();
                    String pricessName = getPackageName() + ":downloadService";
                    if(next.processName.equals(pricessName)){
                        Process.killProcess(next.pid);
                        break;
                    }
                }

                stopService(new Intent(MainActivity.this, DownloadService.class));

                if (mProgressBar.getVisibility() == View.VISIBLE) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }

                if (!isRunning) {
                    isRunning = false;
                    isPaused = false;
                    start.setText("Start");
//                    Toast.makeText(MainActivity.this, "Загрузка остановлена", Toast.LENGTH_SHORT).show();
//                    startService(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Загрузка остановлена", Toast.LENGTH_SHORT).show();
                    isRunning = false;
                    isPaused = false;
                    start.setText("Start");
                }


            }
        });
    }

    private class DownloadReceiver extends ResultReceiver {
        public DownloadReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == DownloadService.UPDATE_PROGRESS) {
                int progress = resultData.getInt("progress");
                mProgressBar.setProgress(progress);
                if (progress == 100) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}