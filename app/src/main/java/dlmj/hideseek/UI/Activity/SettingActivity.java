package dlmj.hideseek.UI.Activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import dlmj.hideseek.BusinessLogic.Cache.GoalCache;
import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.BusinessLogic.Helper.UserInfoManager;
import dlmj.hideseek.Common.Params.IntentExtraParam;
import dlmj.hideseek.Common.Util.BaseInfoUtil;
import dlmj.hideseek.Common.Util.MathUtil;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.View.CustomSuperToast;

/**
 * Created by Two on 5/3/16.
 */
public class SettingActivity extends Activity implements View.OnClickListener {
    private String mLastTitle;
    private View mLogoutLayout;
    private TextView mCacheSizeTextView;
    private AsyncTask<Void,Void,Long> mGetFileSizeTask;
    private AsyncTask<Void,Void,Void> mDeleteFileTask;
    private CustomSuperToast mToast;
    private Dialog mDeleteDialog;
    private TextView mLastTitleTextView;
    private LinearLayout mBackLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        initData();
        findView();
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFileSize();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null!= mGetFileSizeTask)
        {
            mGetFileSizeTask.cancel(true);
            mGetFileSizeTask =null;
        }
    }

    private void initData() {
        mLastTitle = getIntent().getStringExtra(IntentExtraParam.LAST_TITLE);
    }

    private void findView() {
        mLogoutLayout =  findViewById(R.id.logoutLayout);
        mCacheSizeTextView = (TextView) findViewById(R.id.cacheSizeTextView);
        if(UserCache.getInstance().ifLogin()) {
            mLogoutLayout.setVisibility(View.VISIBLE);
        } else{
            mLogoutLayout.setVisibility(View.GONE);
        }
        mToast = new CustomSuperToast(this);

        mLastTitleTextView = (TextView) findViewById(R.id.lastTitleTextView);
        mLastTitleTextView.setText(mLastTitle);
        mBackLayout = (LinearLayout) findViewById(R.id.backLayout);
    }

    private void setListener() {
        findViewById(R.id.clearCacheLayout).setOnClickListener(this);
        findViewById(R.id.feedbackLayout).setOnClickListener(this);
        findViewById(R.id.rateLayout).setOnClickListener(this);
        findViewById(R.id.useGuideLayout).setOnClickListener(this);
        findViewById(R.id.aboutLayout).setOnClickListener(this);
        mLogoutLayout.setOnClickListener(this);

        mBackLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.clearCacheLayout:
                //清空缓存
                showDeleteDialog();
                break;
            case R.id.rateLayout:
                //评价
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" +getPackageName()));
                startActivity(intent);
                break;
            case R.id.feedbackLayout:
                //意见
                startActivity(new Intent(this,FeedBackActivity.class));
                break;
            case R.id.useGuideLayout:
                //指南
                BrowserActivity.toWeb(this,R.string.use_guide,R.string.use_guide_url);
                break;
            case R.id.aboutLayout:
                //关于
                startActivity(new Intent(this,AboutActivity.class));
                break;
            case R.id.logoutLayout:
                //退出登陆
                UserInfoManager.getInstance().clearData(getApplicationContext());
                GoalCache.getInstance().setIfNeedClearMap(true);
                finish();
                break;
        }
    }

    /**
     * 获取文件大小
     */
    private void getFileSize()
    {
        mGetFileSizeTask =new AsyncTask<Void, Void, Long>() {
            @Override
            protected Long doInBackground(Void... params) {
                return BaseInfoUtil.getCacheSize(SettingActivity.this);
            }

            @Override
            protected void onPostExecute(Long result) {
                if (!isFinishing())
                {
                    String disPlay= MathUtil.getPrintSize(result);
                    mCacheSizeTextView.setText(disPlay);
                }
            }
        };
        mGetFileSizeTask.execute();
    }

    /**
     * 执行删除缓存
     */
    private void deleteFileCache()
    {
        mDeleteFileTask =new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                BaseInfoUtil.clearImageCache(SettingActivity.this);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (!isFinishing())
                {
                    mToast.show(getString(R.string.clear_finish), CustomSuperToast.MessageType.success);
                    getFileSize();
                }
            }
        };
        mDeleteFileTask.execute();
    }

    /**
     * 弹出删除弹窗
     */
    private void showDeleteDialog()
    {
        if (null==mDeleteDialog)
        {
            mDeleteDialog=new AlertDialog.Builder(this)
                    .setTitle(android.R.string.dialog_alert_title)
                    .setMessage(R.string.clear_dialog_content)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            deleteFileCache();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mDeleteDialog.dismiss();
                        }
                    })
                    .create();
        }
        mDeleteDialog.show();
    }
}
