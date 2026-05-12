package com.example.mapdistanceapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MapView map;
    private GeoPoint pointA, pointB;
    private TextView txtDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Configuration.getInstance().load(
                getApplicationContext(),
                getSharedPreferences("osm", MODE_PRIVATE)
        );

        setContentView(R.layout.activity_main);

        map = findViewById(R.id.map);
        txtDistance = findViewById(R.id.txtDistance);
        Button btnClear = findViewById(R.id.btnClear);

        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);

        checkPermission();

        btnClear.setOnClickListener(v -> {
            map.getOverlays().clear();
            pointA = null;
            pointB = null;
            txtDistance.setText("Distance:");
            map.invalidate();
        });

        // 👉 Correct click handling for OSMDROID
        map.setOnClickListener(v -> {
            // not reliable for geo tap, so we use map projection below
        });

        map.setOnTouchListener((v, event) -> {

            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {

                GeoPoint tappedPoint = (GeoPoint)
                        map.getProjection().fromPixels(
                                (int) event.getX(),
                                (int) event.getY()
                        );

                if (pointA == null) {
                    pointA = tappedPoint;
                    addMarker(pointA, "Point A");

                } else if (pointB == null) {
                    pointB = tappedPoint;
                    addMarker(pointB, "Point B");

                    drawLine();
                    calculateDistance();
                }

                v.performClick();
            }

            return true;
        });
    }

    private void addMarker(GeoPoint point, String title) {
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setTitle(title);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        map.getOverlays().add(marker);
        map.invalidate();
    }

    private void drawLine() {
        Polyline line = new Polyline();

        ArrayList<GeoPoint> points = new ArrayList<>();
        points.add(pointA);
        points.add(pointB);

        line.setPoints(points);

        map.getOverlays().add(line);
        map.invalidate();
    }

    private void calculateDistance() {

        float[] results = new float[1];

        Location.distanceBetween(
                pointA.getLatitude(), pointA.getLongitude(),
                pointB.getLatitude(), pointB.getLongitude(),
                results
        );

        float distance = results[0];

        String text;

        if (distance > 1000) {
            text = "Distance: " + (distance / 1000f) + " km";
        } else {
            text = "Distance: " + distance + " meters";
        }

        txtDistance.setText(text);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }
}