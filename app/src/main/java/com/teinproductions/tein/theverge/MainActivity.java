package com.teinproductions.tein.theverge;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.DownloadListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements DownloadAsyncTask.OnLoadedListener {

    DownloadAsyncTask asyncTask;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        asyncTask = new DownloadAsyncTask(this, this);
        textView = (TextView) findViewById(R.id.textView);

        asyncTask.execute("http://www.theverge.com/");
    }

    @Override
    public void onLoaded(String s) {
        if (check400Number(s) || getString(R.string.no_network).equals(s)) {
            Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
        }

        textView.setText(s);
    }

    private boolean check400Number(String s) {
        try {
            int check = Integer.parseInt(s);
            return check >= 400;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
