package com.example.a4laba_plotnikova;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TextView statusTextView;
    private ListView trackListView;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        statusTextView = findViewById(R.id.statusTextView);
        trackListView = findViewById(R.id.trackListView);

        // Проверка подключения к интернету
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Нет подключения к интернету. Оффлайн режим.", Toast.LENGTH_SHORT).show();
        } else {
            // Запуск асинхронной задачи с интервалом 20 секунд
            handler.postDelayed(fetchTrackRunnable, 20000);
        }

        // Загрузка данных из БД и отображение в ListView
        loadTracksFromDatabase();
    }

    private Runnable fetchTrackRunnable = new Runnable() {
        @Override
        public void run() {
            new FetchTrackTask(dbHelper, statusTextView).execute();
            handler.postDelayed(this, 20000); // повторы каждые 20 секунд
        }
    };

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void loadTracksFromDatabase() {
        Cursor cursor = dbHelper.getAllTracks();
        String[] fromColumns = new String[]{DatabaseHelper.COLUMN_ARTIST, DatabaseHelper.COLUMN_TITLE, DatabaseHelper.COLUMN_TIMESTAMP};
        int[] toViews = new int[]{R.id.artistTextView, R.id.titleTextView, R.id.timestampTextView};

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.track_list_item,
                cursor,
                fromColumns,
                toViews,
                0
        );

        trackListView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(fetchTrackRunnable);
        dbHelper.close();
        super.onDestroy();
    }
}