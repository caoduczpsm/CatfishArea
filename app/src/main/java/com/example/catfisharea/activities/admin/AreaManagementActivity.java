package com.example.catfisharea.activities.admin;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityAreaManagementBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.InfoAreaAdapter;
import com.example.catfisharea.listeners.InfoClicked;
import com.example.catfisharea.models.Area;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AreaManagementActivity extends BaseActivity implements OnMapReadyCallback, InfoClicked {
    private ActivityAreaManagementBinding mBinding;
    private MapboxMap mapboxMap;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private InfoAreaAdapter adapter;
    private List<Object> mItems;
    private String typeActivity;
    private final int zoom = 13;
    private boolean isZoomOut = false;
    private boolean isListened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        mBinding = ActivityAreaManagementBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mBinding.mapView.onCreate(savedInstanceState);
        mBinding.mapView.getMapAsync(this);
        preferenceManager = new PreferenceManager(this);
        database = FirebaseFirestore.getInstance();
        checkPermission();
    }

    private void setListener() {
        isListened = true;
        mItems = new ArrayList<>();
        adapter = new InfoAreaAdapter(this, mItems, this);
        mBinding.recyclerViewAreaManager.setAdapter(adapter);
        Intent mIntent = new Intent(this, ManagementAreaActivity.class);

        mBinding.recyclerViewAreaManager.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mBinding.recyclerViewAreaManager.addItemDecoration(itemDecoration);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mBinding.layoutMaps.getLayoutParams();
        mBinding.zoomOutBtn.setOnClickListener(view -> {
            if (!isZoomOut) {
                mBinding.layoutMaps.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                mBinding.zoomOutBtn.setImageResource(R.drawable.baseline_zoom_in_map_24);
                mBinding.scroolView.setVisibility(View.GONE);
            } else {
                mBinding.layoutMaps.setLayoutParams(params);
                mBinding.zoomOutBtn.setImageResource(R.drawable.ic_zoom_out_map);
                mBinding.scroolView.setVisibility(View.VISIBLE);
            }
            isZoomOut = !isZoomOut;
        });

        mBinding.toolbarManageArea.setNavigationOnClickListener(view -> onBackPressed());
        typeActivity = getIntent().getStringExtra("typeActivity");
        if (typeActivity.equals(Constants.KEY_AREA)) {
            mBinding.toolbarManageArea.setTitle("Quản lý vùng");
            getAreaData();
            mBinding.newBtn.setOnClickListener(view -> {
                mIntent.putExtra("request", "create");
                mIntent.putExtra("typeItem", Constants.KEY_AREA);
                startActivity(mIntent);
            });

        } else if (typeActivity.equals(Constants.KEY_CAMPUS)) {
            mBinding.toolbarManageArea.setTitle("Quản lý khu");
            getCampusData();
            mBinding.newBtn.setOnClickListener(view -> {
                mIntent.putExtra("request", "create");
                mIntent.putExtra("typeItem", Constants.KEY_CAMPUS);
                startActivity(mIntent);
            });
        } else {
            mBinding.toolbarManageArea.setTitle("Quản lý ao");
            getPondData();
            mBinding.newBtn.setOnClickListener(view -> {
                mIntent.putExtra("request", "create");
                mIntent.putExtra("typeItem", Constants.KEY_POND);
                startActivity(mIntent);
            });
        }

        mBinding.deleteBtn.setOnClickListener(view -> {
            adapter.setDeleted(true);
            mBinding.newBtn.setVisibility(View.GONE);
            mBinding.deleteBtn.setVisibility(View.GONE);
            mBinding.doneBtn.setVisibility(View.VISIBLE);
        });

        mBinding.doneBtn.setOnClickListener(view -> deleteItem());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void deleteItem() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        if (typeActivity.equals(Constants.KEY_AREA)) {
            List<Area> mAreas = adapter.getAreasSeleted();
            if (mAreas != null) {
                alertDialog.setTitle("Lưu ý");
                alertDialog.setMessage("Khi xóa vùng sẽ xóa tất cả khu và ao trong vùng. Bạn có chắc chắn muốn xóa?");
                alertDialog.setPositiveButton("Đồng ý", (dialog, which) -> {
                    adapter.setDeleted(false);
                    mBinding.newBtn.setVisibility(View.VISIBLE);
                    mBinding.deleteBtn.setVisibility(View.VISIBLE);
                    mBinding.doneBtn.setVisibility(View.GONE);
                    for (Area area : mAreas) {
                        deleteArea(area.getId());
                        mItems.remove(area);
                    }
                    adapter.notifyDataSetChanged();
                    drawArea();
                    adapter.setDeleted(false);
                    mBinding.newBtn.setVisibility(View.VISIBLE);
                    mBinding.deleteBtn.setVisibility(View.VISIBLE);
                    mBinding.doneBtn.setVisibility(View.GONE);
//                    dialog.dismiss();
                });
                alertDialog.setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                });
                alertDialog.show();
            } else {
                adapter.setDeleted(false);
                adapter.notifyDataSetChanged();
                mBinding.newBtn.setVisibility(View.VISIBLE);
                mBinding.deleteBtn.setVisibility(View.VISIBLE);
                mBinding.doneBtn.setVisibility(View.GONE);
            }
        } else if (typeActivity.equals(Constants.KEY_CAMPUS)) {
            List<Campus> mCampues = adapter.getCampuesSeleted();
            if (mCampues != null && mCampues.size() > 0) {
                alertDialog.setTitle("Lưu ý");
                alertDialog.setMessage("Khi xóa khu sẽ xóa tất cả ao trong khu. Bạn có chắc chắn muốn xóa?");
                alertDialog.setPositiveButton("Đồng ý", (dialog, which) -> {
                    adapter.setDeleted(false);
                    mBinding.newBtn.setVisibility(View.VISIBLE);
                    mBinding.deleteBtn.setVisibility(View.VISIBLE);
                    mBinding.doneBtn.setVisibility(View.GONE);
                    for (Campus campus : mCampues) {
                        deleteCampus(campus.getId());
                        mItems.remove(campus);
                    }
                    adapter.notifyDataSetChanged();
                    drawCampus();
                });

                alertDialog.setNegativeButton("Hủy", (dialog, which) -> {

                });
                alertDialog.show();
            } else {
                adapter.setDeleted(false);
                mBinding.newBtn.setVisibility(View.VISIBLE);
                mBinding.deleteBtn.setVisibility(View.VISIBLE);
                mBinding.doneBtn.setVisibility(View.GONE);
            }

        } else {
            List<Pond> mPonds = adapter.getPondsSeleted();
            if (mPonds != null && mPonds.size() > 0) {
                adapter.setDeleted(false);
                mBinding.newBtn.setVisibility(View.VISIBLE);
                mBinding.deleteBtn.setVisibility(View.VISIBLE);
                mBinding.doneBtn.setVisibility(View.GONE);
                for (Pond pond : mPonds) {
                    deletePond(pond.getId());
                    mItems.remove(pond);
                }
                adapter.notifyDataSetChanged();
            } else {
                adapter.setDeleted(false);
                mBinding.newBtn.setVisibility(View.VISIBLE);
                mBinding.deleteBtn.setVisibility(View.VISIBLE);
                mBinding.doneBtn.setVisibility(View.GONE);
            }

        }
    }

    private void deleteArea(String idArea) {
        database.collection(Constants.KEY_COLLECTION_AREA)
                .document(idArea).delete();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_AREA_ID, idArea)
                .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_REGIONAL_CHIEF).get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                        database.collection(Constants.KEY_COLLECTION_USER).
                                document(queryDocumentSnapshots.getDocuments().get(0).getId())
                                .update(Constants.KEY_AREA_ID, FieldValue.delete()));
        database.collection(Constants.KEY_COLLECTION_CAMPUS)
                .whereEqualTo(Constants.KEY_AREA_ID, idArea)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        deleteCampus(documentSnapshot.getId());
                    }
                });
    }

    private void deletePond(String idPond) {
        database.collection(Constants.KEY_COLLECTION_POND)
                .document(idPond).delete().addOnSuccessListener(deleted -> {
                    database.collection(Constants.KEY_COLLECTION_USER)
                            .whereEqualTo(Constants.KEY_POND_ID, idPond)
                            .get().addOnSuccessListener(queryDocumentSnapshots -> {
                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                    Map<String, Object> mapUser = new HashMap<>();
                                    mapUser.put(Constants.KEY_POND_ID, FieldValue.delete());
                                    mapUser.put(Constants.KEY_CAMPUS_ID, FieldValue.delete());
                                    mapUser.put(Constants.KEY_AREA_ID, FieldValue.delete());
                                    database.collection(Constants.KEY_COLLECTION_USER).document(documentSnapshot.getId())
                                            .update(mapUser);

                                }
                            });
                });

    }

    private void deleteCampus(String idCampus) {
        database.collection(Constants.KEY_COLLECTION_CAMPUS)
                .document(idCampus).delete();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_CAMPUS_ID, idCampus)
                .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_DIRECTOR)
                .get().addOnSuccessListener(userDelete -> {
                    for (DocumentSnapshot documentSnapshot : userDelete.getDocuments()) {
                        Map<String, Object> mapUser = new HashMap<>();
                        mapUser.put(Constants.KEY_CAMPUS_ID, FieldValue.delete());
                        mapUser.put(Constants.KEY_AREA_ID, FieldValue.delete());
                        database.collection(Constants.KEY_COLLECTION_USER).document(documentSnapshot.getId())
                                .update(mapUser);
                    }
                });
        database.collection(Constants.KEY_COLLECTION_POND)
                .whereEqualTo(Constants.KEY_CAMPUS_ID, idCampus)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        deletePond(documentSnapshot.getId());
                    }
                });
    }

    // Lấy dữ liệu ao cá lên recyclerview
    @SuppressLint("NotifyDataSetChanged")
    private void getPondData() {
        drawArea();
        drawCampus();
        IconFactory iconFactory = IconFactory.getInstance(this);
        Icon icon = iconFactory.fromResource(R.drawable.ic_pond_marker);
        mItems.clear();
        database.collection(Constants.KEY_COLLECTION_POND)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String name = doc.getString(Constants.KEY_NAME);
                        String id = doc.getId();
                        GeoPoint geo = (GeoPoint) doc.get(Constants.KEY_MAP);
                        String campusId = doc.getString(Constants.KEY_CAMPUS_ID);
                        String acreage = doc.getString(Constants.KEY_ACREAGE);
                        List<String> numOfFeedingList = (List<String>) doc.get(Constants.KEY_NUM_OF_FEEDING_LIST);
                        List<String> amountFedList = (List<String>) doc.get(Constants.KEY_AMOUNT_FED);
                        List<String> specificationsToMeasureList = (List<String>) doc.get(Constants.KEY_SPECIFICATIONS_TO_MEASURE);
                        HashMap<String, Object> parameters = (HashMap<String, Object>) doc.get(Constants.KEY_SPECIFICATIONS_MEASURED);
                        int numOfFeeding = Integer.parseInt(Objects.requireNonNull(doc.getString(Constants.KEY_NUM_OF_FEEDING)));
//                        thêm điểm ao trên bản đồ
                        MarkerOptions options = new MarkerOptions();
                        options.setIcon(icon);
                        assert geo != null;
                        options.position(new LatLng(Objects.requireNonNull(geo).getLatitude(), geo.getLongitude()));
                        mapboxMap.addMarker(options);
                        Pond pond = new Pond(id, name, geo, campusId, acreage, numOfFeeding, numOfFeedingList, amountFedList, specificationsToMeasureList, parameters);
                        mItems.add(pond);
                        database.collection(Constants.KEY_COLLECTION_USER)
                                .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_WORKER)
                                .whereEqualTo(Constants.KEY_POND_ID, id).get().addOnSuccessListener(count -> {
                                    if (count != null && !count.isEmpty())
                                        pond.setNumberWorker(count.size());
                                    adapter.notifyDataSetChanged();
                                });
                    }
                    if (mItems != null && mItems.size() > 0) {
                        clickPond(((Pond) mItems.get(0)).getLatLng(), ((Pond) mItems.get(0)).getName());
                    }

                });
    }

    //    Lấy dữ liệu khu leen recyclerview
    @SuppressLint({"NewApi", "NotifyDataSetChanged"})
    private void getCampusData() {
        drawArea();
        database.collection(Constants.KEY_COLLECTION_CAMPUS)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String name = doc.getString(Constants.KEY_NAME);
                        String id = doc.getId();
                        ArrayList<GeoPoint> geoList = (ArrayList<GeoPoint>) doc.get(Constants.KEY_MAP);
                        String areaId = doc.getString(Constants.KEY_AREA_ID);
//                        Vẽ khu trên bản đồ
                        List<LatLng> listPoint = new ArrayList<>();
                        assert geoList != null;
                        for (GeoPoint point : geoList) {
                            listPoint.add(new LatLng(point.getLatitude(), point.getLongitude()));
                        }
                        Polygon polygon = mapboxMap.addPolygon(new PolygonOptions()
                                .addAll(listPoint)
                                .fillColor(Color.argb(50, 255, 80, 80)));
                        listPoint.add(listPoint.get(0));
                        PolylineOptions rectOptions = new PolylineOptions()
                                .addAll(listPoint)
                                .color(Color.BLACK)
                                .width(3);
                        Polyline line = mapboxMap.addPolyline(rectOptions);

                        Campus campus = new Campus(id, name, geoList, areaId);
                        mItems.add(campus);
//                        Lấy tên nguời quản lý vùng
                        database.collection(Constants.KEY_COLLECTION_USER)
                                .whereEqualTo(Constants.KEY_CAMPUS_ID, id)
                                .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_DIRECTOR)
                                .get().addOnSuccessListener(command -> {
                                    if (command != null && !command.isEmpty()) {
                                        String nameManager = command.getDocuments().get(0).getString(Constants.KEY_NAME);
                                        String idmanager = command.getDocuments().get(0).getId();
                                        String avt = command.getDocuments().get(0).getString(Constants.KEY_IMAGE);
                                        User manager = new User();
                                        manager.name = nameManager;
                                        manager.id = idmanager;
                                        manager.image = avt;
                                        campus.setManager(manager);
                                    }
                                    adapter.notifyDataSetChanged();
                                });
                        //Lấy số khu của ao
                        database.collection(Constants.KEY_COLLECTION_POND)
                                .whereEqualTo(Constants.KEY_CAMPUS_ID, campus.getId()).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    campus.setNumberPond(queryDocumentSnapshots1.size());
                                    adapter.notifyDataSetChanged();
                                });
                        mItems.sort((o1, o2) -> ((Campus) o1).getName().compareToIgnoreCase(((Campus) o2).getName()));
                        adapter.notifyDataSetChanged();
                    }
                    if (mItems != null && mItems.size() > 0) {
                        clickItem(((Campus) mItems.get(0)).getPolygonCenterPoint(), ((Campus) mItems.get(0)).getListLatLng());
                    }

                });
    }

    // Vẽ vùng trên bản đồ
    private void drawArea() {
        database.collection(Constants.KEY_COLLECTION_AREA)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        ArrayList<GeoPoint> geoList = (ArrayList<GeoPoint>) doc.get(Constants.KEY_MAP);
                        List<LatLng> listPoint = new ArrayList<>();
                        assert geoList != null;
                        for (GeoPoint point : geoList) {
                            listPoint.add(new LatLng(point.getLatitude(), point.getLongitude()));
                        }
//                    Polygon polygon = mapboxMap.addPolygon(new PolygonOptions()
//                            .addAll(listPoint)
//                            .fillColor(Color.argb(50, 255, 80, 80)));
                        listPoint.add(listPoint.get(0));
                        PolylineOptions rectOptions = new PolylineOptions()
                                .addAll(listPoint)
                                .color(Color.BLACK)
                                .width(3);
                        Polyline line = mapboxMap.addPolyline(rectOptions);
                    }

                });
    }

    //    Vẽ khu trên bản đồ
    private void drawCampus() {
        database.collection(Constants.KEY_COLLECTION_CAMPUS)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        ArrayList<GeoPoint> geoList = (ArrayList<GeoPoint>) doc.get(Constants.KEY_MAP);
                        List<LatLng> listPoint = new ArrayList<>();
                        assert geoList != null;
                        for (GeoPoint point : geoList) {
                            listPoint.add(new LatLng(point.getLatitude(), point.getLongitude()));
                        }
//                    Polygon polygon = mapboxMap.addPolygon(new PolygonOptions()
//                            .addAll(listPoint)
//                            .fillColor(Color.argb(50, 255, 80, 80)));
                        listPoint.add(listPoint.get(0));
                        PolylineOptions rectOptions = new PolylineOptions()
                                .addAll(listPoint)
                                .color(Color.BLACK)
                                .width(3);
                        Polyline line = mapboxMap.addPolyline(rectOptions);
                    }

                });
    }

    //    lấy dữ liệu vùng lên recyclerview
    @SuppressLint("NewApi")
    private void getAreaData() {
        loading(true);
//        Lấy vùng có id công ty của user
        database.collection(Constants.KEY_COLLECTION_AREA)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String name = doc.getString(Constants.KEY_NAME);
                        String id = doc.getId();
                        ArrayList<GeoPoint> geoList = (ArrayList<GeoPoint>) doc.get(Constants.KEY_MAP);
                        List<LatLng> listPoint = new ArrayList<>();
                        for (GeoPoint point : geoList) {
                            listPoint.add(new LatLng(point.getLatitude(), point.getLongitude()));
                        }
                        Polygon polygon = mapboxMap.addPolygon(new PolygonOptions()
                                .addAll(listPoint)
                                .fillColor(Color.argb(50, 255, 80, 80)));
                        listPoint.add(listPoint.get(0));
                        PolylineOptions rectOptions = new PolylineOptions()
                                .addAll(listPoint)
                                .color(Color.BLACK)
                                .width(3);
                        Polyline line = mapboxMap.addPolyline(rectOptions);

                        Area area = new Area(id, name, geoList);
                        mItems.add(area);
//                        Lấy tên nguời quản lý vùng
                        database.collection(Constants.KEY_COLLECTION_USER)
                                .whereEqualTo(Constants.KEY_AREA_ID, id)
                                .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_REGIONAL_CHIEF)
                                .get().addOnSuccessListener(command -> {
                                    if (command != null && !command.isEmpty()) {
                                        String nameManager = command.getDocuments().get(0).getString(Constants.KEY_NAME);
                                        String idManager = command.getDocuments().get(0).getId();
                                        String avtManager = command.getDocuments().get(0).getString(Constants.KEY_IMAGE);
                                        User manager = new User();
                                        manager.id = idManager;
                                        manager.name = nameManager;
                                        manager.image = avtManager;
                                        area.setManager(manager);
                                    } else {
                                        User manager = new User();
                                        manager.name = "Chưa có";
                                        area.setManager(manager);
                                    }
                                    adapter.notifyDataSetChanged();
                                });
//                        Lấy số khu của vùng
                        database.collection(Constants.KEY_COLLECTION_CAMPUS)
                                .whereEqualTo(Constants.KEY_AREA_ID, area.getId()).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                                    area.setNumberCumpus(queryDocumentSnapshots1.size());
                                    loading(false);
                                    adapter.notifyDataSetChanged();
                                });
//                        Sắp xếp lại mảng theo thứ tự tên
                        Collections.sort(mItems, new Comparator<Object>() {
                            @Override
                            public int compare(Object o1, Object o2) {
                                return ((Area) o1).getName().compareToIgnoreCase(((Area) o2).getName());
                            }
                        });
                        adapter.notifyDataSetChanged();
                    }
                    if (mItems != null && mItems.size() > 0) {
                        clickItem(((Area) mItems.get(0)).getPolygonCenterPoint(), ((Area) mItems.get(0)).getListLatLng());
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mBinding.mapView.onStart();
        if (mapboxMap != null) {
            setListener();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBinding.mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mBinding.mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mBinding.mapView.onSaveInstanceState(outState);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBinding.mapView.onDestroy();
    }

    //    Set mapbox
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        if (!isListened) {
            setListener();
        }
        mapboxMap.setStyle(Style.SATELLITE_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

            }
        });
    }

    //    Kiểm tra permission
    private void checkPermission() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(AreaManagementActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.create()
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void clickItem(LatLng center, List<LatLng> listLatlng) {
//        Di chuyển camera map đến khu đã chọn
//        LatLng center = area.getPolygonCenterPoint();
        mapboxMap.getPolygons().forEach(it -> {
            if (it.getPoints().contains(listLatlng.get(0))) {
                it.setFillColor(Color.argb(50, 20, 137, 238));
            } else {
                it.setFillColor(Color.argb(50, 255, 80, 80));
            }
        });
        CameraPosition areaPosition = new CameraPosition.Builder()
                .target(center)
                .zoom(zoom).build();
        mapboxMap.setCameraPosition(areaPosition);
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(areaPosition), 500);
    }

    private void loading(boolean isLoading) {
        if (isLoading) {
            mBinding.loadingProgressBar.setVisibility(View.VISIBLE);
        } else {
            mBinding.loadingProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void clickEditItem(Object item) {
        Intent intent = new Intent(this, ManagementAreaActivity.class);
        startActivity(intent);
    }

    @SuppressLint("NewApi")
    @Override
    public void clickPond(LatLng point, String name) {
//        mapboxMap.getPolygons().forEach(it -> {
//            if (it.getPoints().contains(point)) {
//                it.setFillColor(Color.argb(0, 20, 137, 238));
//            } else {
//                it.setFillColor(Color.argb(0, 255, 80, 80));
//            }
//        });
        IconFactory iconFactory = IconFactory.getInstance(this);
        Icon icon = iconFactory.fromResource(R.drawable.ic_location);
        Icon icon2 = iconFactory.fromResource(R.drawable.ic_pond_marker);
        if (mapboxMap.getMarkers() == null || mapboxMap.getMarkers().size() == 0) return;
        mapboxMap.getMarkers().forEach(it-> {
            if (it.getPosition().equals(point)) {
                it.setTitle(name);
                it.setIcon(icon);
            } else {
                it.setIcon(icon2);
            }
        });

        CameraPosition areaPosition = new CameraPosition.Builder()
                .target(point)
                .zoom(15).build();
        mapboxMap.setCameraPosition(areaPosition);
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(areaPosition), 500);
    }
}