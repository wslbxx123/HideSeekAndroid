package dlmj.hideseek.UI.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import dlmj.hideseek.UI.Thread.OverlayThread;
import dlmj.hideseek.UI.View.CameraSurfaceView;
import dlmj.hideseek.UI.View.CustomSuperToast;

/**
 * Created by Two on 4/29/16.
 */
public class SearchFragment extends Fragment implements CameraInterface.CamOpenOverCallback,
        CameraSurfaceView.OnCreateListener, LocationSource, AMapLocationListener,
        UIDataListener<Bean>, SensorEventListener, AMap.OnMapLoadedListener {
    private final String TAG = "Search Fragment";
    private final long EXPLODE_DURATION = 600;
    private final long HIT_MONSTER_DURATION = 450;
    private final int MSG_GOAL_VISIBLE = 3;
    private final int MSG_GOAL_INVISIBLE = 2;
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
    private Animation mAnim;
    private ImageView mGoalImageView;
    private boolean mIsExploding = false;
    private Handler mHandler;
    private boolean mIfHittingMonster = false;
    private CustomSuperToast mErrorSuperToast;
    private boolean mIfGoalVisible = false;
    private boolean mIfAnimFinished = false;
    private ErrorMessageFactory mErrorMessageFactory;
    private TextView mOverlayTextVew;
    private WindowManager mWindowManager;
    private OverlayThread mOverlayThread;
    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GOAL_VISIBLE:
                    mGoalImageView.setVisibility(View.VISIBLE);
                    mIfGoalVisible = true;
                    break;
                case MSG_GOAL_INVISIBLE:
                    mGoalImageView.clearAnimation();
                    mGoalImageView.setVisibility(View.INVISIBLE);
                    mIfGoalVisible = false;
                    break;
                case OverlayThread.HIDE_OVERLAY:
                    mOverlayTextVew.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };

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
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        mOverlayTextVew = (TextView) inflater.inflate(R.layout.score_overlay, null);
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
        mIfAnimFinished = false;

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

    private void setEndGoal() {
        mEndGoal = GoalCache.getInstance().getSelectedGoal();

        if(mEndGoal != null) {
            mEndLatLng.setLatitude(mEndGoal.getLatitude());
            mEndLatLng.setLongitude(mEndGoal.getLongitude());

            if(mEndGoal.getType() == Goal.GoalTypeEnum.bomb) {
                mGoalImageView.setBackgroundResource(mEndGoal.getType().toDrawable());
                mGoalImageView.setImageResource(0);
            } else {
                mGoalImageView.setImageResource(mEndGoal.getType().toDrawable());
                mGoalImageView.setBackgroundResource(0);
            }
        }
        mGoalImageView.setVisibility(View.INVISIBLE);

        mDistance = AMapUtils.calculateLineDistance(
                new LatLng(mStartLatLng.getLatitude(), mStartLatLng.getLongitude()),
                new LatLng(mEndLatLng.getLatitude(), mEndLatLng.getLongitude()));
        mDistanceTextView.setText(MathUtil.round(mDistance) + "m");
        LogUtil.d(TAG, "distance: " + MathUtil.round(mDistance) + "m");
    }

    @Override
    public void onDestroyView() {
        mWindowManager.removeView(mOverlayTextVew);

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

        mSensorManager =  (SensorManager) getActivity().
                getSystemService(Context.SENSOR_SERVICE);

        mAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.bling);
        mErrorSuperToast = new CustomSuperToast(getActivity());
        mErrorMessageFactory = new ErrorMessageFactory(getActivity());
    }

    private void findView(View view) {
        mSurfaceView = (CameraSurfaceView) view.findViewById(R.id.cameraSurfaceView);
        mSurfaceView.setOnCreateListener(this);

        mGoalImageView = (ImageView) view.findViewById(R.id.goalImageView);
        mGoalImageView.setImageResource(R.drawable.mushroom);

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMap = mMapView.getMap();

        mNavigateButton = (Button) view.findViewById(R.id.navigateButton);
        mDistanceTextView = (TextView) view.findViewById(R.id.distanceTextView);
        mGetButton = (Button) view.findViewById(R.id.getButton);
        mDistanceLayout = (LinearLayout) view.findViewById(R.id.distanceLayout);
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

        mAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mEndGoal.getType() == Goal.GoalTypeEnum.monster){
                    mIfAnimFinished = true;
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mGetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.d(TAG, "VISIBLE: " + mGoalImageView.getVisibility());
                if(mIfGoalVisible) {
                    switch(mEndGoal.getType()) {
                        case mushroom:
                            if(GoalCache.getInstance().getHasSelectMushroom() && !mEndGoal.isEnabled()) {
                                mErrorSuperToast.show(getActivity().getString(R.string.warning_not_enabled));
                            } else {
                                mGoalImageView.setVisibility(View.INVISIBLE);

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
                            startMonsterAnimation();
                            mIfHittingMonster = true;
                            mHandler = new Handler();
                            mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    LogUtil.d(TAG, "Animation of hitting monster finished");
                                    mGoalImageView.setImageResource(mEndGoal.getType().toDrawable());
                                    mGoalImageView.setBackgroundResource(0);

                                    Map<String, String> params = new HashMap<>();
                                    params.put("goal_id", mEndGoal.getPkId() + "");
                                    User user = UserCache.getInstance().getUser();
                                    params.put("account_role", user.getRole().getValue() + "");
                                    mHitMonsterNetworkHelper.sendPostRequest(
                                            UrlParams.HIT_MONSTER_URL, params);
                                    mIfHittingMonster = false;
                                }
                            }, HIT_MONSTER_DURATION);
                            break;
                    }
                }
            }
        });
    }

    private void startMonsterAnimation() {
        mGoalImageView.clearAnimation();
        mGoalImageView.setImageResource(0);
        mGoalImageView.setBackgroundResource(R.drawable.hit_monster);

        AnimationDrawable animationDrawable = (AnimationDrawable)
                mGoalImageView.getBackground();
        animationDrawable.start();
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

                switch(mEndGoal.getType()) {
                    case bomb:
                        if(mGoalImageView.getBackground() instanceof AnimationDrawable &&
                                !mIsExploding && mEndGoal.isEnabled()) {
                            LogUtil.d(TAG, "Animation Start!");
                            Message.obtain(mUiHandler, MSG_GOAL_VISIBLE).sendToTarget();
                            AnimationDrawable animationDrawable = (AnimationDrawable)
                                    mGoalImageView.getBackground();
                            animationDrawable.start();
                            mIsExploding = true;
                            mGetButton.setEnabled(false);

                            mHandler = new Handler();
                            mHandler.postDelayed(new Runnable() {
                                public void run() {
                                    LogUtil.d(TAG, "Animation finished");

                                    Map<String, String> params = new HashMap<>();
                                    params.put("goal_id", mEndGoal.getPkId() + "");
                                    params.put("goal_type", mEndGoal.getType().getValue() + "");
                                    mGetGoalNetworkHelper.sendPostRequest(UrlParams.GET_GOAL_URL, params);
                                    updateEndGoal();
                                    mOverlayTextVew.setText(getString(R.string.minus_one_score));
                                    mOverlayTextVew.setVisibility(View.VISIBLE);
                                    mUiHandler.removeCallbacks(mOverlayThread);
                                    mUiHandler.postDelayed(mOverlayThread, 1000);
                                    mGetButton.setEnabled(true);
                                    mIsExploding = false;
                                }
                            }, EXPLODE_DURATION);
                        }
                        break;
                    case monster:
                        if(!mIfAnimFinished && !mIfHittingMonster) {
                            Message.obtain(mUiHandler, MSG_GOAL_VISIBLE).sendToTarget();
                            mGoalImageView.startAnimation(mAnim);
                        }
                        break;
                    case mushroom:
                    default:
                        Message.obtain(mUiHandler, MSG_GOAL_VISIBLE).sendToTarget();
//                        mGoalImageView.startAnimation(mAnim);
                }
            } else if(!mIsExploding){
                LogUtil.d(TAG, "Goal is hidden");
                Message.obtain(mUiHandler, MSG_GOAL_INVISIBLE).sendToTarget();
                mIfAnimFinished = false;
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
