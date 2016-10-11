package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.RaceGroupCache;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Cache.WarningCache;
import dlmj.hideseek.BusinessLogic.Network.NetworkHelper;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.Warning;
import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.Adapter.WarningAdapter;

public class WarningActivity extends Activity implements UIDataListener<Bean>{
    private final static String TAG = "WarningActivity";
    private final static int MSG_REFRESH_LIST = 1;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm:ss");
    private PullToRefreshListView mWarningListView;
    private TextView mLeftTimeTextView;
    private WarningAdapter mWarningAdapter;
    private List<Warning> mWarningList = new LinkedList<>();
    private NetworkHelper mNetworkHelper;
    private CountDownTimer mCountDownTimer;
    private Date mLeftTime;
    private Handler mUiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_LIST:
                    mWarningAdapter.notifyDataSetChanged();
                    mWarningListView.onRefreshComplete();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.warning);

        initData();
        findView();
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mWarningList.clear();

        if(UserCache.getInstance().ifLogin()) {
            mWarningListView.setRefreshing(true);
        }
    }

    @Override
    protected void onDestroy() {
        if(mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }

        super.onDestroy();
    }

    private void initData() {
        mWarningAdapter = new WarningAdapter(this, mWarningList);
        mNetworkHelper = new NetworkHelper(this);
    }

    private void findView() {
        mWarningListView = (PullToRefreshListView) findViewById(R.id.warningListView);
        mWarningListView.setAdapter(mWarningAdapter);
        mWarningListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);

        ILoadingLayout startLabels = mWarningListView.getLoadingLayoutProxy(true, false);
        startLabels.setPullLabel("");
        startLabels.setRefreshingLabel("");
        startLabels.setReleaseLabel("");
        mLeftTimeTextView = (TextView) findViewById(R.id.leftTimeTextView);
    }

    private void setListener() {
        mNetworkHelper.setUiDataListener(this);

        mWarningAdapter.setGetBtnOnClickedListener(new WarningAdapter.GetBtnOnClickedListener() {
            @Override
            public void getBtnOnClicked(long goalId) {
                Intent intent = WarningActivity.this.getIntent();
                intent.putExtra(IntentExtraParam.GOAL_ID, goalId);
                WarningActivity.this.setResult(Activity.RESULT_OK);
                WarningActivity.this.finish();
            }
        });

        mWarningListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                Map<String, String> params = new HashMap<>();
                mNetworkHelper.sendPostRequest(UrlParams.GET_DANGER_WARNING_URL, params);
                RaceGroupCache.getInstance(WarningActivity.this).clearList();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
    }

    @Override
    public void onDataChanged(Bean data) {
        LogUtil.d(TAG, data.getResult());

        WarningCache.getInstance().setWarnings(data.getResult());

        try {
            Date serverTime = WarningCache.getInstance().getServerTime();
            Calendar serverCalendar = Calendar.getInstance();
            serverCalendar.setTime(serverTime);
            String noonTime = mDateFormat.format(serverTime);
            Calendar noonCalendar = Calendar.getInstance();
            noonCalendar.setTime(mDateFormat.parse(noonTime));
            noonCalendar.add(Calendar.DATE, 1);

            long interval = noonCalendar.getTimeInMillis() - serverCalendar.getTimeInMillis();

            setCountDownTimer(interval);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mWarningList.clear();
        mWarningList.addAll(WarningCache.getInstance().getList());
        Message.obtain(mUiHandler, MSG_REFRESH_LIST).sendToTarget();
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        LogUtil.d(TAG, errorMessage);
    }

    private void setCountDownTimer(long interval) {
        if(mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }

        mCountDownTimer = new CountDownTimer(interval, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mLeftTime = new Date(millisUntilFinished);

                long secondSum = millisUntilFinished / 1000;
                long hour = secondSum / (60 * 60);
                secondSum = secondSum - hour * (60 * 60);
                long minute = secondSum / 60;
                secondSum = secondSum - minute * 60;
                long second = secondSum;
                mLeftTimeTextView.setText((hour < 10 ? "0" + hour : hour)
                        + ":" + (minute < 10 ? "0" + minute : minute)
                        + ":" + (second < 10 ? "0" + second : second));
            }

            @Override
            public void onFinish() {

            }
        };

        mCountDownTimer.start();
    }
}
