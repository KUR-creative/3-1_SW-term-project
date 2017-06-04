package com.example.kouram.usetmap;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapPOIItem;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    TMapView mapView;

    LocationManager mLM;
    String mProvider = LocationManager.NETWORK_PROVIDER;

    EditText keywordView;
    ListView listView;
    ArrayAdapter<POI> mAdapter;

    TMapPoint start, end;
    RadioGroup typeView;

    //mine
    DBHelper helper;
    SQLiteDatabase db;

    Button insert_btn;
    Button select_btn;
    Button delete_btn;
    Button update_btn;
    Button delete_all_btn;

    MediaPlayer mediaPlayer;
    Button music_btn;
    Button ticks_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = new DBHelper(this, "person.db", null, 1); // version은 내 맘대로 함.
        db = helper.getWritableDatabase();
        helper.onCreate(db);

        //blob data
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(100);
        list.add(10000);
        //serialize data (bos and oos do that jobs!)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(bos);
            oos.writeObject(list);
            oos.flush();
            oos.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // get blob data
        byte[] blobData = bos.toByteArray();

        // DB buttons
        insert_btn = (Button)findViewById(R.id.insert_btn);
        insert_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {               System.out.println("call insert click listener");
                ContentValues values = new ContentValues();
                values.put("name", "asdf");
                values.put("age", 26);
                values.put("address", "ButSSan");
                db.insert("data", null, values);

                ContentValues values2 = new ContentValues();
                values2.put("name", "지워");
                values2.put("age", 26);
                values2.put("address", "스울");
                db.insert("data", null, values2);
            }
        });

        select_btn = (Button)findViewById(R.id.select_btn);
        select_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {               System.out.println("call select btn click listener");
                Cursor c = db.query("data", null, null, null, null, null, null, null);
                while(c.moveToNext()){
                    int id = c.getInt(c.getColumnIndex("_id"));
                    String name = c.getString(c.getColumnIndex("name"));
                    int age = c.getInt(c.getColumnIndex("age"));
                    String address = c.getString(c.getColumnIndex("address"));

                    System.out.println("result: " + "_id = " + id
                                                + ", name = " + name
                                                + ", age = " + age
                                                + ", address = " + address);
                }
            }
        });

        delete_btn = (Button)findViewById(R.id.delete_btn);
        delete_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                System.out.println("call delete click listener");
                db.delete("data", "_id=?", new String[]{"1"}); // _id = 1 인 거 지워짐.
                db.delete("data", "name=?", new String[]{"지워"}); // name = 지워 인 거 전부 지워짐.
            }
        });

        update_btn = (Button)findViewById(R.id.update_btn);
        update_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                ContentValues new_values = new ContentValues();
                new_values.put("address", "UPDATED DATA");
                db.update("data", new_values, "name=?", new String[] {"지워"});
            }
        });

        delete_all_btn = (Button)findViewById(R.id.delete_all_btn);
        delete_all_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                db.delete("data", "name=?", new String[]{"지워"}); // String[] 이거 문자열 하나만 가능
                db.delete("data", "name=?", new String[]{"asdf"});
            }
        });



        final Context ctx = this;

        mediaPlayer = MediaPlayer.create(this, R.raw.title);
        music_btn = (Button)findViewById(R.id.music_btn);
        music_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null; // garbage!
                }else {
                    mediaPlayer.start();
                }
            }

            private boolean isPlaying(){
                if(mediaPlayer == null){
                    mediaPlayer = MediaPlayer.create(ctx, R.raw.title); //for replaying!
                    return false;
                }

                if(mediaPlayer.isPlaying()){
                    return true;
                }else{
                    return false;
                }
            }
        });
        ticks_btn = (Button)findViewById(R.id.ticks_btn);

        typeView = (RadioGroup)findViewById(R.id.group_type);
        keywordView = (EditText) findViewById(R.id.edit_keyword);
        listView = (ListView) findViewById(R.id.listView);
        mAdapter = new ArrayAdapter<POI>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(mAdapter);

        mLM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mapView = (TMapView) findViewById(R.id.map_view);
        mapView.setOnApiKeyListener(new TMapView.OnApiKeyListenerCallback() {
            @Override
            public void SKPMapApikeySucceed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupMap();
                    }
                });
            }

            @Override
            public void SKPMapApikeyFailed(String s) {

            }
        });
        mapView.setSKPMapApiKey("9e719482-ed73-3153-9f91-5bcb75e149d5");
        mapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        Button btn = (Button) findViewById(R.id.btn_add_marker);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TMapPoint point = mapView.getCenterPoint();
                addMarker(point.getLatitude(), point.getLongitude(), "My Marker");
            }
        });

        btn = (Button) findViewById(R.id.btn_search);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchPOI();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                POI poi = (POI) listView.getItemAtPosition(position);
                moveMap(poi.item.getPOIPoint().getLatitude(), poi.item.getPOIPoint().getLongitude());
            }
        });

        btn = (Button)findViewById(R.id.btn_route);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (start != null && end != null) {
                    searchRoute(start, end);
                    start = end = null;
                }else{
                    Toast.makeText(MainActivity.this,"start or end is null", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void searchRoute(TMapPoint start, TMapPoint end){
        TMapData data = new TMapData();
        data.findPathData(start, end, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(final TMapPolyLine path) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        path.setLineWidth(5);
                        path.setLineColor(Color.RED);
                        mapView.addTMapPath(path);
                        Bitmap s = ((BitmapDrawable)ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_input_delete)).getBitmap();
                        Bitmap e = ((BitmapDrawable)ContextCompat.getDrawable(MainActivity.this, android.R.drawable.ic_input_get)).getBitmap();
                        mapView.setTMapPathIcon(s, e);

                    }
                });
            }
        });
    }

    private void searchPOI() {
        TMapData data = new TMapData();
        String keyword = keywordView.getText().toString();
        if (!TextUtils.isEmpty(keyword)) {
            data.findAllPOI(keyword, new TMapData.FindAllPOIListenerCallback() {
                @Override
                public void onFindAllPOI(final ArrayList<TMapPOIItem> arrayList) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapView.removeAllMarkerItem();
                            mAdapter.clear();

                            for (TMapPOIItem poi : arrayList) {
                                addMarker(poi);
                                mAdapter.add(new POI(poi));
                            }

                            if (arrayList.size() > 0) {
                                TMapPOIItem poi = arrayList.get(0);
                                moveMap(poi.getPOIPoint().getLatitude(), poi.getPOIPoint().getLongitude());
                            }
                        }
                    });
                }
            });
        }
    }

    public void addMarker(TMapPOIItem poi) {
        TMapMarkerItem item = new TMapMarkerItem();
        item.setTMapPoint(poi.getPOIPoint());
        Bitmap icon = ((BitmapDrawable) ContextCompat.getDrawable(this, android.R.drawable.ic_input_add)).getBitmap();
        item.setIcon(icon);
        item.setPosition(0.5f, 1);
        item.setCalloutTitle(poi.getPOIName());
        item.setCalloutSubTitle(poi.getPOIContent());
        Bitmap left = ((BitmapDrawable) ContextCompat.getDrawable(this, android.R.drawable.ic_dialog_alert)).getBitmap();
        item.setCalloutLeftImage(left);
        Bitmap right = ((BitmapDrawable) ContextCompat.getDrawable(this, android.R.drawable.ic_input_get)).getBitmap();
        item.setCalloutRightButtonImage(right);
        item.setCanShowCallout(true);
        mapView.addMarkerItem(poi.getPOIID(), item);
    }

    private void addMarker(double lat, double lng, String title) {
        TMapMarkerItem item = new TMapMarkerItem();
        TMapPoint point = new TMapPoint(lat, lng);
        item.setTMapPoint(point);
        Bitmap icon = ((BitmapDrawable) ContextCompat.getDrawable(this, android.R.drawable.ic_input_add)).getBitmap();
        item.setIcon(icon);
        item.setPosition(0.5f, 1);
        item.setCalloutTitle(title);
        item.setCalloutSubTitle("sub " + title);
        Bitmap left = ((BitmapDrawable) ContextCompat.getDrawable(this, android.R.drawable.ic_dialog_alert)).getBitmap();
        item.setCalloutLeftImage(left);
        Bitmap right = ((BitmapDrawable) ContextCompat.getDrawable(this, android.R.drawable.ic_input_get)).getBitmap();
        item.setCalloutRightButtonImage(right);
        item.setCanShowCallout(true);
        mapView.addMarkerItem("m" + id, item);
        id++;
    }

    int id = 0;

    boolean isInitialized = false;

    private void setupMap() {
        isInitialized = true;
        mapView.setMapType(TMapView.MAPTYPE_STANDARD);
        //        mapView.setSightVisible(true);
        //        mapView.setCompassMode(true);
        //        mapView.setTrafficInfo(true);
        //        mapView.setTrackingMode(true);
        if (cacheLocation != null) {
            moveMap(cacheLocation.getLatitude(), cacheLocation.getLongitude());
            setMyLocation(cacheLocation.getLatitude(), cacheLocation.getLongitude());
        }
        mapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(TMapMarkerItem tMapMarkerItem) {
                String message = null;
                switch (typeView.getCheckedRadioButtonId()){
                    case R.id.radio_start:
                        start = tMapMarkerItem.getTMapPoint();
                        message = "start";
                        break;
                    case R.id.radio_end:
                        end = tMapMarkerItem.getTMapPoint();
                        message = "end";
                        break;
                }
                Toast.makeText(MainActivity.this,message + " setting",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location location = mLM.getLastKnownLocation(mProvider);
        if (location != null) {
            mListener.onLocationChanged(location);
        }
        mLM.requestSingleUpdate(mProvider, mListener, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLM.removeUpdates(mListener);
    }

    Location cacheLocation = null;

    private void moveMap(double lat, double lng) {
        mapView.setCenterPoint(lng, lat);
    }

    private void setMyLocation(double lat, double lng) {
        Bitmap icon = ((BitmapDrawable) ContextCompat.getDrawable(this, android.R.drawable.ic_dialog_map)).getBitmap();
        mapView.setIcon(icon);
        mapView.setLocationPoint(lng, lat);
        mapView.setIconVisibility(true);
    }

    LocationListener mListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (isInitialized) {
                moveMap(location.getLatitude(), location.getLongitude());
                setMyLocation(location.getLatitude(), location.getLongitude());
            } else {
                cacheLocation = location;
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
}

/*
package com.example.kouram.usetmap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RelativeLayout;

import com.skp.Tmap.TMapView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RelativeLayout relativeLayout = new RelativeLayout(this);
        TMapView tmapview = new TMapView(this);
        tmapview.setSKPMapApiKey("9553cc22-8104-3088-a882-b90ef2a051d7");
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);
        tmapview.setIconVisibility(true);
        tmapview.setZoomLevel(10);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setCompassMode(true);
        tmapview.setTrackingMode(true);
        relativeLayout.addView(tmapview);
        setContentView(relativeLayout);
    }
}
*/
