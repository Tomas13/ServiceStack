package kz.growit.servicestack;

import android.app.ActivityManager;
import android.content.Intent;
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

    ProgressBar mProgressBar;
    Button start, stop;
    EditText fileNameET, urlET;
    String fileName, url;
    Boolean isRunning = false;
    Boolean isPaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlET = (EditText) findViewById(R.id.urlET);
        fileNameET = (EditText) findViewById(R.id.fileNameET);
        start = (Button) findViewById(R.id.startBtn);
        stop = (Button) findViewById(R.id.stopBtn);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setMax(100);
        mProgressBar.setIndeterminate(false);
        mProgressBar.setVisibility(View.INVISIBLE);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // this is how you fire the downloader
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressBar.setProgress(0);

                fileName = fileNameET.getText().toString();
                url = urlET.getText().toString();
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.putExtra("url", url);
                intent.putExtra("fileName", fileName);
                intent.putExtra("receiver", new DownloadReceiver(new Handler()));
//                startService(intent);

                if (!isRunning){
                    isRunning = true;
                    isPaused = false;
                    start.setText("Pause");
                    startService(intent);
                }else{
                    isRunning = false;
                    isPaused = true;
                    start.setText("Start");
                }
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Загрузка остановлена", Toast.LENGTH_SHORT).show();

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

                if(mProgressBar.getVisibility() == View.VISIBLE)
                    mProgressBar.setVisibility(View.INVISIBLE);

                if (!isRunning){
                    isRunning = false;
                    isPaused = false;
                    start.setText("Pause");
//                    startService(intent);
                }else{
                    isRunning = false;
                    isPaused = false;
                    start.setText("Start");
                }
                stopService(new Intent(MainActivity.this, DownloadService.class));
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
                    stopService(new Intent(MainActivity.this, DownloadService.class));
                    Toast.makeText(MainActivity.this, "Загрузка завершена.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}