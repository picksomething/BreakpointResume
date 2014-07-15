package cn.picksomething.breakpointresume;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import cn.picksomething.network.DownloadProgressListener;
import cn.picksomething.network.FileDownloader;


public class MyActivity extends Activity implements View.OnClickListener{
    private EditText downloadurl;
    private Button startDownload;
    private ProgressBar progressBar;
    private TextView resultView;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    progressBar.setProgress(msg.getData().getInt("size"));
                    float num = (float)progressBar.getProgress()/(float)progressBar.getMax();
                    int result = (int)(num*100);
                    resultView.setText(result+"%");
                    if(progressBar.getProgress() == progressBar.getMax()){
                        Toast.makeText(MyActivity.this, R.string.downcomplete, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case -1:
                    Toast.makeText(MyActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        downloadurl = (EditText)findViewById(R.id.downloadurl);
        startDownload = (Button)findViewById(R.id.startdownload);
        progressBar = (ProgressBar)findViewById(R.id.progress);
        resultView = (TextView)findViewById(R.id.result);
        startDownload.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        String path = downloadurl.getText().toString();
        System.out.println(Environment.getExternalStorageState()+"------"+Environment.MEDIA_MOUNTED);
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            download(path,Environment.getExternalStorageDirectory());
        }else{
            Toast.makeText(MyActivity.this, R.string.nosdcard, Toast.LENGTH_SHORT).show();
        }
    }

    private void download(final String pathurl, final File savedir){
        Thread downloadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                FileDownloader loader = new FileDownloader(MyActivity.this, pathurl, savedir, 5);
                progressBar.setMax(loader.getFileSize());

                try{
                    loader.download(new DownloadProgressListener() {
                        @Override
                        public void onDownloadSize(int size) {
                            Message msg = new Message();
                            msg.what = 1;
                            msg.getData().putInt("size",size);
                            handler.sendMessage(msg);
                        }
                    });
                }catch (Exception e){
                    handler.obtainMessage(-1).sendToTarget();
                }
            }
        });
        downloadThread.start();
    }
}
