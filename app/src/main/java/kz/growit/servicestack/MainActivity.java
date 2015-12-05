package kz.growit.servicestack;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    // declare the dialog as a member field of your activity
    ProgressDialog mProgressDialog;

    Button start, stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = (Button) findViewById(R.id.startBtn);
        stop = (Button) findViewById(R.id.stopBtn);


// instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setMessage("A message");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // this is how you fire the downloader
                mProgressDialog.show();
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.putExtra("url", "https://www.jetbrains.com/webstorm/documentation/WebStorm_ReferenceCard.pdf");
                intent.putExtra("receiver", new DownloadReceiver(new Handler()));
                startService(intent);

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Загрузка остановлена", Toast.LENGTH_SHORT).show();
                if (mProgressDialog.isShowing()){
                    mProgressDialog.dismiss();
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
                mProgressDialog.setProgress(progress);
                if (progress == 100) {
                    mProgressDialog.dismiss();

                    stopService(new Intent(MainActivity.this, DownloadService.class));

                    Toast.makeText(MainActivity.this,
                            "Загрузка завершена.",
                            Toast.LENGTH_LONG).show();
                }
            }



        }
    }
}
