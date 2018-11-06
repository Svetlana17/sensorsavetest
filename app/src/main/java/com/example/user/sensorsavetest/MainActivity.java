package com.example.user.sensorsavetest;



import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorEventListener2;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    SensorManager manager;
    Button buttonStart;
    Button buttonStop;
    boolean isRunning;
    final String TAG = "SensorLog";
    FileWriter writer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isRunning = false;
        manager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        buttonStart = (Button)findViewById(R.id.buttonStart);
        buttonStop = (Button)findViewById(R.id.buttonStop);
        buttonStart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);
                Log.d(TAG, "Writing to " + getStorageDir());
                try {
                    writer = new FileWriter(new File(getStorageDir(), "sensors_" + System.currentTimeMillis() + ".csv"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                manager.registerListener(MainActivity.this, manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 0);
                manager.registerListener(MainActivity.this, manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 0);
//                manager.registerListener(MainActivity.this, manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED), 0);
//                manager.registerListener(MainActivity.this, manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), 0);
//                manager.registerListener(MainActivity.this, manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED), 0);
//                manager.registerListener(MainActivity.this, manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), 0);
//                manager.registerListener(MainActivity.this, manager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR), 0);

                isRunning = true;
                return true;
            }
        });
        buttonStop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                isRunning = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    manager.flush(MainActivity.this);
                }
                manager.unregisterListener(MainActivity.this);
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        Button shareButton = (Button) findViewById(R.id.share);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });

    //}

}
    private String getStorageDir() {
        return this.getExternalFilesDir(null).getAbsolutePath();
        //  return "/storage/emulated/0/Android/data/com.iam360.sensorlog/";
    }
    //@Override
    public void onFlushCompleted(Sensor sensor) {

    }
    @Override
    public void onSensorChanged(SensorEvent evt) {
        if(isRunning) {
            try {
                switch(evt.sensor.getType()) {
                    case Sensor.TYPE_ACCELEROMETER:
                   ///  ---оригинал  writer.write(String.format("%d;  %f; %f; %f; %f; %f; %f\n", evt.timestamp, evt.values[0], evt.values[1], evt.values[2], 0.f, 0.f, 0.f));
                  writer.write(String.format("%d;  %f; %f; %f; %f; %f; %f\n", evt.timestamp, evt.values[0], evt.values[1], evt.values[2],  0.f, 0.f, 0.f)

                  );

                        break;
//                  \
                    case Sensor.TYPE_GYROSCOPE:
                        writer.write(String.format("%d;  %f; %f; %f; %f; %f; %f\n", evt.timestamp, evt.values[0], evt.values[1], evt.values[2], 0.f, 0.f, 0.f));
                        break;
//
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
    private void share() {
        File dir = getExternalFilesDir(null);
        File zipFile = new File(dir, "accel.zip");
        if (zipFile.exists()) {
            zipFile.delete();
        }
        File[] fileList = dir.listFiles();
        try {
            zipFile.createNewFile();
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
            out.setLevel(Deflater.DEFAULT_COMPRESSION);
            for (File file : fileList) {
                zipFile(out, file);
            }
            out.close();
            sendBundleInfo(zipFile);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Can't send file!", Toast.LENGTH_LONG).show();
        }
    }
    private static void zipFile(ZipOutputStream zos, File file) throws IOException {
        zos.putNextEntry(new ZipEntry(file.getName()));
        FileInputStream fis = new FileInputStream(file);
        //byte[] buffer = new byte[4092];
        byte[] buffer = new byte[10000];
        int byteCount = 0;
        try {
            while ((byteCount = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, byteCount);
            }
        } finally {
            safeClose(fis);
        }
        zos.closeEntry();
    }
    private static void safeClose(FileInputStream fis) {
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void sendBundleInfo(File file) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file));
        startActivity(Intent.createChooser(emailIntent, "Send data"));
    }
}
