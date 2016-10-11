package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.baidu.navisdk.adapter.BNRouteGuideManager;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviBaseCallbackModel;
import com.baidu.navisdk.adapter.BaiduNaviCommonModule;
import com.baidu.navisdk.adapter.NaviModuleFactory;
import com.baidu.navisdk.adapter.NaviModuleImpl;

import java.util.ArrayList;
import java.util.List;

import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.Common.Params.MapStateParam;
import dlmj.hideseek.R;

/**
 * Created by Two on 4/2/16.
 */
public class AMapGuideActivity extends Activity {
    private final String TAG = "BaiduGuide Activity";
    private Handler mHandler = null;
    private BaiduNaviCommonModule mNavigationCommonModule = null;
    private BNRouteGuideManager.OnNavigationListener mOnNavigationListener;
    private View mView = null;
    private BNRoutePlanNode mBNRoutePlanNode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListener();
        initData();
    }

    private void setListener() {
        mOnNavigationListener = new BNRouteGuideManager.OnNavigationListener() {

            @Override
            public void onNaviGuideEnd() {

            }

            @Override
            public void notifyOtherAction(int actionType, int arg1, int arg2, Object obj) {
                if (actionType == 0) {
                    Log.i(TAG, "notifyOtherAction actionType = " + actionType + ",导航到达目的地！");
                }

                Log.i(TAG, "actionType:" + actionType + "arg1:" + arg1 + "arg2:" + arg2 +
                        "obj:" + obj.toString());
            }
        };
    }

    private void initData() {
        createHandler();
        mNavigationCommonModule = NaviModuleFactory.getNaviModuleManager().getNaviCommonModule(
                NaviModuleImpl.BNaviCommonModuleConstants.ROUTE_GUIDE_MODULE, this,
                BNaviBaseCallbackModel.BNaviBaseCallbackConstants.CALLBACK_ROUTEGUIDE_TYPE,
                mOnNavigationListener);
        if(mNavigationCommonModule != null) {
            mNavigationCommonModule.onCreate();
            mView = mNavigationCommonModule.getView();
        }

        if(mView != null) {
            setContentView(mView);
        }

        Intent intent = getIntent();
        if(intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                mBNRoutePlanNode = (BNRoutePlanNode) bundle.getSerializable(IntentExtraParam.ROUTE_PLAN_NODE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(mNavigationCommonModule != null) {
            mNavigationCommonModule.onResume();
        }

        if(mHandler != null) {
            mHandler.sendEmptyMessageAtTime(MapStateParam.MSG_SHOW, 2000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mNavigationCommonModule != null) {
            mNavigationCommonModule.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mNavigationCommonModule != null) {
            mNavigationCommonModule.onDestroy();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(mNavigationCommonModule != null) {
            mNavigationCommonModule.onStop();
        }
    }

    @Override
    public void onBackPressed() {
        if(mNavigationCommonModule != null) {
            mNavigationCommonModule.onBackPressed(false);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if(mNavigationCommonModule != null) {
            mNavigationCommonModule.onConfigurationChanged(newConfig);
        }
    }

    private void createHandler() {
        if (mHandler == null) {
            mHandler = new Handler(getMainLooper()) {
                public void handleMessage(android.os.Message msg) {
                    if (msg.what == MapStateParam.MSG_SHOW) {
                        addCustomizedLayerItems();
                    } else if (msg.what == MapStateParam.MSG_HIDE) {
                        BNRouteGuideManager.getInstance().showCustomizedLayer(false);
                    } else if (msg.what == MapStateParam.MSG_RESET_NODE) {
                        BNRouteGuideManager.getInstance().resetEndNodeInNavi(
                                new BNRoutePlanNode(116.21142, 40.85087, "百度大厦11", null, BNRoutePlanNode.CoordinateType.GCJ02));
                    }
                }
            };
        }
    }

    private void addCustomizedLayerItems() {
        List<BNRouteGuideManager.CustomizedLayerItem> items = new ArrayList();
        BNRouteGuideManager.CustomizedLayerItem item = null;
        if (mBNRoutePlanNode != null) {
            item = new BNRouteGuideManager.CustomizedLayerItem(mBNRoutePlanNode.getLongitude(),
                    mBNRoutePlanNode.getLatitude(),
                    mBNRoutePlanNode.getCoordinateType(),
                    getResources().getDrawable(R.drawable.ic_launcher),
                    BNRouteGuideManager.CustomizedLayerItem.ALIGN_CENTER);
            items.add(item);

            BNRouteGuideManager.getInstance().setCustomizedLayerItems(items);
        }
        BNRouteGuideManager.getInstance().showCustomizedLayer(true);
    }
}
