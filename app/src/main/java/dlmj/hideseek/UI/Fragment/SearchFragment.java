package dlmj.hideseek.UI.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.navi.model.NaviLatLng;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.GoalCache;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Factory.ErrorMessageFactory;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.Goal;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.DisplayUtil;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.Common.Util.MathUtil;
import dlmj.hideseek.Hardware.CameraInterface;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Activity.MapActivity;
import dlmj.hideseek.UI.Activity.NavigationActivity;
import dlmj.hideseek.UI.Activity.StoreActivity;
import dlmj.hideseek.UI.Thread.OverlayThread;
import dlmj.hideseek.UI.View.CameraSurfaceView;
import dlmj.hideseek.UI.View.CustomSuperToast;
import dlmj.hideseek.UI.View.GameView;

/**
 * Created by Two on 4/29/16.
 * 主页fragment
 */
public class SearchFragment extends Fragment implements CameraInterface.CamOpenOverCallback,
        CameraSurfaceView.OnCreateListener, LocationSource, AMapLocationListener,
        UIDataListener<Bean>, SensorEventListener, AMap.OnMapLoadedListener {
    private final String TAG = "Search Fragment";
    private float mPreviewRate = -1f;
    private CameraSurfaceView mSurfaceView = null;
    private MapView mMapView;
    private AMap mMap;
    private LocationSource.OnLocationChangedListener mLocationListener;
    private AMapLocationClient mLocationClient = null;
    private AMapLocationClientOption mLocationOption = null;
    private View rootView;
    private NetworkHelper mNetworkHelper;
    private NetworkHelper mGetGoalNetworkHelper;
    private NetworkHelper mHitMonsterNetworkHelper;
    private Button mNavigateButton;
    private double mLatitude;
    private double mLongitude;
    private NaviLatLng mStartLatLng = new NaviLatLng();
    private NaviLatLng mEndLatLng = new NaviLatLng();
    private Goal mEndGoal;
    private TextView mDistanceTextView;
    private int mOrientation;
    private float mDistance;
    private SensorManager mSensorManager;
    private boolean mIfRegisteredSensor;
    private Button mGetButton;
    private Hashtable<Long, Marker> mMarkerHashTable = new Hashtable<>();
    private LinearLayout mDistanceLayout;
    private boolean mIsExploding = false;
    private CustomSuperToast mErrorSuperToast;
    private ErrorMessageFactory mErrorMessageFactory;
    private TextView mOverlayTextVew;
    private WindowManager mWindowManager;
    private OverlayThread mOverlayThread;
    private GameView mGameView;
    private Handler mUiHandler = new Handler();
    private Handler mRefreshMapHandler = new Handler();
    private boolean mLocationFlag = false;
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            refreshMap();
        }
    };
    private ImageView mMonsterHandbook;
    private ImageView mBombThrow;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        initOverlay();

        if(rootView == null) {
            rootView = inflater.inflate(R.layout.search, null);
            initData();
            findView(rootView);
            setListener();
            mMapView.onCreate(savedInstanceState);
            setLayer("MAP_TYPE_NORMAL");
        }

        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    private void initOverlay() {
        //获取xml布局文件
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        mOverlayTextVew = (TextView) inflater.inflate(R.layout.score_overlay, null);
        //设置字体类型
        Typeface fontFace = Typeface.createFromAsset(getActivity().getAssets(), "fonts/score_font.ttf");
        mOverlayTextVew.setTypeface(fontFace);
        mOverlayTextVew.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                0, -150,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        mWindowManager = (WindowManager) getActivity()
                .getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(mOverlayTextVew, layoutParams);
        mOverlayThread = new OverlayThread(mOverlayTextVew, mUiHandler);
    }

    @Override
    public void onResume() {
        super.onResume();
        openCamera();
        mMapView.onResume();
        LogUtil.d(TAG, "Map View is resumed");

        List<Goal> goals = GoalCache.getInstance().getList();

        setGoalsOnMap(goals);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);

        if (sensors.size() > 0){
            Sensor sensor = sensors.get(0);
            mIfRegisteredSensor = mSensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        } else {
            mOrientation = -1;
        }
        setEndGoal();
    }

    private void refreshMap() {
        if(mLongitude == 0 || mLatitude == 0) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("latitude", mLatitude + "");
        params.put("longitude", mLongitude + "");

        mStartLatLng.setLatitude(mLatitude);
        mStartLatLng.setLongitude(mLongitude);

        String updateTime = GoalCache.getInstance().getUpdateTime();
        LogUtil.d(TAG, updateTime);
        if(updateTime != null && !updateTime.equals("null")) {
            params.put("update_time", updateTime);
        }

        User user = UserCache.getInstance().getUser();
        if(user != null) {
            params.put("account_role", user.getRole().getValue() + "");
            mNetworkHelper.sendPostRequest(UrlParams.REFRESH_MAP_URL, params);
        } else {
            mNetworkHelper.sendPostRequestWithoutSid(UrlParams.REFRESH_MAP_URL, params);
        }
    }

    //设置目标
    private void setEndGoal() {
        mEndGoal = GoalCache.getInstance().getSelectedGoal();

        if(mEndGoal != null) {
            mGameView.setGoal(mEndGoal);
            mEndLatLng.setLatitude(mEndGoal.getLatitude());
            mEndLatLng.setLongitude(mEndGoal.getLongitude());
        }

        mDistance = AMapUtils.calculateLineDistance(
                new LatLng(mStartLatLng.getLatitude(), mStartLatLng.getLongitude()),
                new LatLng(mEndLatLng.getLatitude(), mEndLatLng.getLongitude()));
        mDistanceTextView.setText(MathUtil.round(mDistance) + "m");
        LogUtil.d(TAG, "distance: " + MathUtil.round(mDistance) + "m");
    }

    @Override
    public void onDestroyView() {
        mWindowManager.removeView(mOverlayTextVew);
        mRefreshMapHandler.removeCallbacks(mRunnable);

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mMapView.onDestroy();
        LogUtil.d(TAG, "Map View is destroyed");
        mLocationClient.stopLocation();
        mLocationClient.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        LogUtil.d(TAG, "Map View is paused");
        CameraInterface.getInstance(getActivity()).doStopCamera();

        if (mIfRegisteredSensor) {
            mSensorManager.unregisterListener(this);
            mIfRegisteredSensor = false;
        }
    }

    public void openCamera() {
        Thread openThread = new Thread() {
            @Override
            public void run() {
                try{
                    CameraInterface.getInstance(getActivity()).doOpenCamera(SearchFragment.this);
                } catch(RuntimeException ex) {
                    LogUtil.e(TAG, ex.getMessage());
                }
            }
        };
        openThread.start();
    }

    public void initData() {
        mPreviewRate = DisplayUtil.getScreenRate(getActivity());
        mNetworkHelper = new NetworkHelper(getActivity());
        mGetGoalNetworkHelper = new NetworkHelper(getActivity());
        mHitMonsterNetworkHelper = new NetworkHelper(getActivity());
        //传感器
        mSensorManager =  (SensorManager) getActivity().
                getSystemService(Context.SENSOR_SERVICE);

        mErrorSuperToast = new CustomSuperToast(getActivity());
        mErrorMessageFactory = new ErrorMessageFactory(getActivity());
    }

    private void findView(View view) {
        mSurfaceView = (CameraSurfaceView) view.findViewById(R.id.cameraSurfaceView);
        mSurfaceView.setOnCreateListener(this);

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMap = mMapView.getMap();

        mNavigateButton = (Button) view.findViewById(R.id.navigateButton);
        mDistanceTextView = (TextView) view.findViewById(R.id.distanceTextView);
        mGetButton = (Button) view.findViewById(R.id.getButton);
        mDistanceLayout = (LinearLayout) view.findViewById(R.id.distanceLayout);
        mGameView = (GameView) view.findViewById(R.id.gameView);

        mMonsterHandbook = (ImageView) view.findViewById(R.id.monster_handbook);
        mBombThrow = (ImageView)view.findViewById(R.id.bomb_throw);
    }

    private void setLayer(String layerName) {
        if (layerName.equals("MAP_TYPE_NORMAL")) {
            mMapView.getMap().setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
        } else if (layerName.equals("MAP_TYPE_SATELLITE")) {
            mMapView.getMap().setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式
        }
    }

    private void setListener() {
        mNetworkHelper.setUiDataListener(this);
        mMonsterHandbook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), StoreActivity.class);
                startActivity(intent);
            }
        });
        mBombThrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), StoreActivity.class);
                startActivity(intent);
            }
        });
        UIDataListener<Bean> getGoalUIDataListener = new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                LogUtil.d(TAG, data.getMessage());
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {

            }
        };

        mGetGoalNetworkHelper.setUiDataListener(getGoalUIDataListener);

        UIDataListener<Bean> hitMonsterUIDataListener = new UIDataListener<Bean>() {
            @Override
            public void onDataChanged(Bean data) {
                LogUtil.d(TAG, "Hit monster: " + data.getResult());
                updateEndGoal();

                mOverlayTextVew.setText(getString(R.string.add_one_score));
                mOverlayTextVew.setVisibility(View.VISIBLE);
                mUiHandler.removeCallbacks(mOverlayThread);
                mUiHandler.postDelayed(mOverlayThread, 1000);
            }

            @Override
            public void onErrorHappened(int errorCode, String errorMessage) {
                String message = mErrorMessageFactory.get(errorCode);
                mErrorSuperToast.show(message);

                if(errorCode == CodeParams.ERROR_GOAL_DISAPPEAR) {
                    updateEndGoal();
                }
            }
        };

        mHitMonsterNetworkHelper.setUiDataListener(hitMonsterUIDataListener);

        mNavigateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), NavigationActivity.class);
                intent.putExtra(IntentExtraParam.START_POINT, mStartLatLng);
                intent.putExtra(IntentExtraParam.END_POINT, mEndLatLng);

                startActivity(intent);
            }
        });

        mMap.setOnMapLoadedListener(this);


        mDistanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.d(TAG, "Map view is clicked");

                Intent intent = new Intent(getActivity(), MapActivity.class);
                startActivity(intent);
            }
        });

        mGetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mGameView.getGoalDisplayed()) {
                    switch(mEndGoal.getType()) {
                        case mushroom:
                            if(GoalCache.getInstance().getHasSelectMushroom() && !mEndGoal.isEnabled()) {
                                mErrorSuperToast.show(getActivity().getString(R.string.warning_not_enabled));
                            } else {
                                mGameView.hideGoal();

                                Map<String, String> params = new HashMap<>();
                                params.put("goal_id", mEndGoal.getPkId() + "");
                                params.put("goal_type", mEndGoal.getType().getValue() + "");
                                mGetGoalNetworkHelper.sendPostRequest(UrlParams.GET_GOAL_URL, params);
                                updateEndGoal();
                                mOverlayTextVew.setText(getString(R.string.add_one_score));
                                mOverlayTextVew.setVisibility(View.VISIBLE);
                                mUiHandler.removeCallbacks(mOverlayThread);
                                mUiHandler.postDelayed(mOverlayThread, 1000);
                            }
                            break;
                        case monster:
                            mGameView.hitMonster();
//                            Map<String, String> params = new HashMap<>();
//                            params.put("goal_id", mEndGoal.getPkId() + "");
//                            User user = UserCache.getInstance().getUser();
//                            params.put("account_role", user.getRole().getValue() + "");
//                            mHitMonsterNetworkHelper.sendPostRequest(
//                                    UrlParams.HIT_MONSTER_URL, params);
                            break;
                    }
                }
            }
        });
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

                if(!mLocationFlag) {
                    refreshMap();
                    mLocationFlag = true;
                }
                LogUtil.d(TAG, "Latitude: " + mLatitude +
                        ";Longitude: " + mLongitude);

                Map<String, String> params = new HashMap<>();
                params.put("latitude", mLatitude + "");
                params.put("longitude", mLongitude + "");

                mStartLatLng.setLatitude(mLatitude);
                mStartLatLng.setLongitude(mLongitude);

                String updateTime = GoalCache.getInstance().getUpdateTime();
                LogUtil.d(TAG, updateTime);
                if(updateTime != null && !updateTime.equals("null")) {
                    params.put("update_time", updateTime);
                }

                User user = UserCache.getInstance().getUser();
                if(user != null) {
                    params.put("account_role", user.getRole().getValue() + "");
                }

                mNetworkHelper.sendPostRequest(UrlParams.REFRESH_MAP_URL, params);
                mNetworkHelper.openLock();

            } else{
                String errText = "定位失败，" + aMapLocation.getErrorCode() + ": " +
                        aMapLocation.getErrorInfo();
                LogUtil.d(TAG, errText);
            }
        }
    }

    @Override
    public void cameraHasOpened() {
        LogUtil.d(TAG, "Camera has been opened.");
        checkCamera();
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mLocationListener = onLocationChangedListener;

        if(mLocationClient == null) {
            mLocationClient = new AMapLocationClient(getActivity());
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
    public void onSurfaceViewCreated() {
        LogUtil.d(TAG, "Surface view has been created.");
        checkCamera();
    }

    private void checkCamera() {
        final SurfaceHolder holder = mSurfaceView.getSurfaceHolder();
        if(holder != null) {
            CameraInterface.getInstance(getActivity()).doStartPreview(holder, mPreviewRate);
            mMapView.onResume();
        }
    }

    @Override
    public void onDataChanged(Bean data) {
        LogUtil.d(TAG, data.getResult());
        GoalCache.getInstance().setGoals(data.getResult(), mLatitude, mLongitude);

        List<Goal> updateGoals = GoalCache.getInstance().getUpdateList();

        if(updateGoals.size() > 0) {
            setEndGoal();
        }

        setGoalsOnMap(updateGoals);

        CheckIfGoalDisplayed();
        mNetworkHelper.closeLock();
    }

    @Override
    public void onMapLoaded() {
        setUpMap();

    }

    private void setGoalsOnMap(List<Goal> goals) {
        if(GoalCache.getInstance().getIfNeedClearMap()) {
            mMap.clear();
        }

        for(Goal goal : goals) {
            MarkerOptions markerOptions;
            if(goal.getIsSelected()) {
                markerOptions = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.box_selected_marker))
                        .draggable(true);
            } else{
                markerOptions = new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.box_marker))
                        .draggable(true);
            }

            Marker marker;
            if(mMarkerHashTable.containsKey(goal.getPkId())) {
                marker = mMarkerHashTable.get(goal.getPkId());
                marker.setIcon(markerOptions.getIcon());
            } else {
                marker = mMap.addMarker(markerOptions);
                mMarkerHashTable.put(goal.getPkId(), marker);
            }

            if(goal.getValid()) {
                marker.setVisible(true);
            } else{
                marker.setVisible(false);
            }

            marker.setPosition(new LatLng(goal.getLatitude(), goal.getLongitude()));

        }
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {

    }

    private void CheckIfGoalDisplayed() {
        if(mEndGoal != null) {
            LogUtil.d(TAG, "End Goal: Latitude=" + mEndGoal.getLatitude() + ", " +
                    "Longitude=" + mEndGoal.getLongitude());
            if((mEndGoal.getOrientation() == mOrientation || mOrientation == -1) &&
                    mDistance < 20 && mEndGoal.getValid()) {
                LogUtil.d(TAG, "Goal is displayed");

                mGameView.showGoal();

//                switch(mEndGoal.getType()) {
//                    case bomb:
//                        if(mGoalImageView.getBackground() instanceof AnimationDrawable &&
//                                !mIsExploding && mEndGoal.isEnabled()) {
//                            LogUtil.d(TAG, "Animation Start!");
//                            mIfGoalVisible = true;
//                            AnimationDrawable animationDrawable = (AnimationDrawable)
//                                    mGoalImageView.getBackground();
//                            animationDrawable.start();
//                            mIsExploding = true;
//                            mGetButton.setEnabled(false);
//
//                            mHandler = new Handler();
//                            mHandler.postDelayed(new Runnable() {
//                                public void run() {
//                                    LogUtil.d(TAG, "Animation finished");
//
//                                    Map<String, String> params = new HashMap<>();
//                                    params.put("goal_id", mEndGoal.getPkId() + "");
//                                    params.put("goal_type", mEndGoal.getType().getValue() + "");
//                                    mGetGoalNetworkHelper.sendPostRequest(UrlParams.GET_GOAL_URL, params);
//                                    updateEndGoal();
//                                    mOverlayTextVew.setText(getString(R.string.minus_one_score));
//                                    mOverlayTextVew.setVisibility(View.VISIBLE);
//                                    mUiHandler.removeCallbacks(mOverlayThread);
//                                    mUiHandler.postDelayed(mOverlayThread, 1000);
//                                    mGetButton.setEnabled(true);
//                                    mIsExploding = false;
//                                }
//                            }, EXPLODE_DURATION);
//                        }
//                        break;
//                    case monster:
//                        if(!mIfAnimFinished && !mIfHittingMonster) {
//                            mIfGoalVisible = true;
//                            mGoalImageView.startAnimation(mAnim);
//                        }
//                        break;
//                    case mushroom:
//                    default:
//                        mIfGoalVisible = true;
////                        mGoalImageView.startAnimation(mAnim);
//                }
            } else if(!mIsExploding){
                LogUtil.d(TAG, "Goal is hidden");
                mGameView.hideGoal();
            }
        }
    }

    private void updateEndGoal() {
        mEndGoal.setValid(false);
        GoalCache goalCache = GoalCache.getInstance();
        goalCache.setSelectedGoal(null);
        goalCache.refreshClosestGoal(mLatitude, mLongitude);
        List<Goal> list = new LinkedList<>();
        list.add(mEndGoal);
        list.add(goalCache.getSelectedGoal());
        setEndGoal();
        setGoalsOnMap(list);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float x = sensorEvent.values[0];

        mOrientation = (((int)x + 45) / 90 * 90) % 360;
        LogUtil.d("TAG", "Orientation: " + mOrientation);

        CheckIfGoalDisplayed();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
