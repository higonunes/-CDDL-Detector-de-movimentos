package com.example.detectordemovimentos;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    private Intent i;
    private TextView tvCapturaStatus;
    private Button buttonStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvCapturaStatus = findViewById(R.id.tvCapturaStatus);
        buttonStart = findViewById(R.id.startButton);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            }
            return;
        }
    }

    public void onButtonStart(View view) {
        tvCapturaStatus.setText("Captura Iniciada");

        buttonStart.setBackgroundResource(R.drawable.borda_arredondada_cinza);
        buttonStart.setEnabled(false);

        i = new Intent(this, BackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            this.startForegroundService(i);
        else
            this.startService(i);
    }

    public void onButtonStop(View view) {
        tvCapturaStatus.setText("Captura Parada");

        buttonStart.setEnabled(true);
        buttonStart.setBackgroundResource(R.drawable.borda_arredondada);

        this.stopService(i);
    }

}