package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.os.Bundle;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;

import java.util.Hashtable;
import java.util.List;

import dlmj.hideseek.BusinessLogic.Cache.GoalCache;
import dlmj.hideseek.Common.Model.Goal;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.R;

/**
 * Created by Two on 5/20/16.
 */
public class MapActivity extends Activity implements LocationSource, AMapLocationListener,
        AMap.OnMapLoadedListener, AMap.OnMarkerClickListener {
    private final String TAG = "MapActivity";
    private MapView mMapView;
    private AMap mMap;
    private OnLocationChangedListener mLocationListener;
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private double mLatitude;
    private double mLongitude;
    private Hashtable<Long, Marker> mMarkerHashTable = new Hashtable<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(true);
        setContentView(R.layout.map);
        initData();
        findView();
        setListener();
        mMapView.onCreate(savedInstanceState);
        mMap.setMapType(AMap.MAP_TYPE_NORMAL);
    }

    private void initData() {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mLocationClient.stopLocation();
        mLocationClient.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    private void findView() {
        mMapView = (MapView) findViewById(R.id.mapView);
        mMap = mMapView.getMap();
    }

    private void setListener() {
        mMap.setOnMapLoadedListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    private void setUpMap() {
        MyLocationStyle locationStyle = new MyLocationStyle();
        locationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.location));
        mMap.setMyLocationStyle(locationStyle);
        mMap.setLocationSource(this);
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(19));
    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if(mLocationListener != null) {
            if(aMapLocation.getErrorCode() == 0) {
                mLocationListener.onLocationChanged(aMapLocation);

                mLatitude = aMapLocation.getLatitude();
                mLongitude = aMapLocation.getLongitude();

                LogUtil.d(TAG, "Latitude: " + mLatitude +
                        ";Longitude: " + mLongitude);

                List<Goal> goals = GoalCache.getInstance().getList();

                for(Goal goal : goals) {
                    MarkerOptions markerOptions;
                    if(goal.getIsSelected()) {
                        markerOptions = new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.big_box_selected_marker))
                                .draggable(true);
                    } else{
                        markerOptions = new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.big_box_marker))
                                .draggable(true);
                    }

                    Marker marker;
                    if(mMarkerHashTable.containsKey(goal.getPkId())) {
                        marker = mMarkerHashTable.get(goal.getPkId());
                        marker.setIcon(markerOptions.getIcon());
                    } else {
                        marker = mMap.addMarker(markerOptions);
                        mMarkerHashTable.put(goal.getPkId(), marker);
                        marker.setObject(goal);
                    }

                    if(goal.getValid()) {
                        marker.setVisible(true);
                    } else{
                        marker.setVisible(false);
                    }

                    marker.setPosition(new LatLng(goal.getLatitude(), goal.getLongitude()));

                }
            } else{
                String errText = "定位失败，" + aMapLocation.getErrorCode() + ": " +
                        aMapLocation.getErrorInfo();
                LogUtil.d(TAG, errText);
            }
        }
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mLocationListener = onLocationChangedListener;

        if(mLocationClient == null) {
            mLocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            mLocationClient.setLocationListener(this);
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationClient.setLocationOption(mLocationOption);

            mLocationClient.startLocation();
        }
    }

    @Override
    public void deactivate() {

    }

    @Override
    public void onMapLoaded() {
        setUpMap();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.getObject() instanceof Goal) {
            Goal currentGoal = GoalCache.getInstance().getSelectedGoal();
            currentGoal.setIsSelected(false);
            Goal goal = (Goal)marker.getObject();
            goal.setIsSelected(true);
            GoalCache.getInstance().setSelectedGoal(goal);
        }

        return false;
    }
}
