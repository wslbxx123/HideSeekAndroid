package dlmj.hideseek.BusinessLogic.Network;

import android.content.Context;
import android.util.Log;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;

import java.util.HashMap;
import java.util.Map;

import dlmj.hideseek.BusinessLogic.Cache.UserCache;
import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Params.UrlParams;
import dlmj.hideseek.Common.Util.LogUtil;

/**
 * Created by Two on 19/10/2016.
 */
public class PushManager implements UIDataListener<Bean> {
    private static String TAG = "PushManager";
    private static PushManager mInstance;
    private NetworkHelper mNetworkHelper;
    private int mPostChannelId = 0;
    private String mChannelId;
    private Context mContext;

    public static PushManager getInstance(Context context){
        synchronized (PushManager.class){
            if(mInstance == null){
                mInstance = new PushManager(context);
            }
        }
        return mInstance;
    }

    public PushManager(Context context) {
        mNetworkHelper = new NetworkHelper(context);
        mContext = context;
    }

    public void register() {
        User user = UserCache.getInstance().getUser();
        XGPushManager.registerPush(mContext, user.getPhone(), new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                Log.d("TPush", "注册成功，设备token为：" + data);
                mPostChannelId = 0;
                postChannelId(data.toString());
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {
                Log.d("TPush", "注册失败，错误码：" + errCode + ",错误信息：" + msg);
            }
        });
    }

    public void postChannelId(String channelId) {
        mChannelId = channelId;
        mPostChannelId++;

        if(mPostChannelId > 5) {
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("channel_id", channelId);
        mNetworkHelper.sendPostRequest(UrlParams.UPDATE_CHANNEL_URL, params);
    }

    @Override
    public void onDataChanged(Bean data) {
        LogUtil.d(TAG, data.getResult());
    }

    @Override
    public void onErrorHappened(int errorCode, String errorMessage) {
        LogUtil.d(TAG, errorMessage);
        this.postChannelId(mChannelId);
    }
}
