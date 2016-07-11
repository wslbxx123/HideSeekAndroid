package dlmj.hideseek.BusinessLogic.Network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import dlmj.hideseek.Common.Interfaces.UIDataListener;
import dlmj.hideseek.Common.Model.Bean;
import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Params.SharedPreferenceSettings;
import dlmj.hideseek.Common.Util.SharedPreferenceUtil;

/**
 * Created by Two on 4/30/16.
 */
public class NetworkHelper implements Response.Listener<JSONObject>,
        Response.ErrorListener{
    private static String TAG = "Network Helper";
    private Context mContext;
    private boolean mIsLocked = false;

    public NetworkHelper(Context context) {
        this.mContext = context;
    }

    protected CustomJsonRequest getRequestForGet(String url) {
        return new CustomJsonRequest(url, null, this, this);
    }

    protected CustomJsonRequest getRequestForPost(String url, Map<String, String> params) {
        return new CustomJsonRequest(Request.Method.POST, url, params, this, this);
    }

    public void sendGetRequest(String url, Map<String, String> params) {
        if(!mIsLocked) {
            SharedPreferenceSettings sessionToken = SharedPreferenceSettings.SESSION_TOKEN;
            SharedPreferences sharedPreferences = SharedPreferenceUtil.getSharedPreferences();
            String sessionTokenStr = sharedPreferences.getString(
                    sessionToken.getId(),
                    (String)sessionToken.getDefaultValue());
            params.put("session_id", sessionTokenStr);
            url = convertParamsToUrl(url, params);
            VolleyQueueController.getInstance(mContext)
                    .getRequestQueue().add(getRequestForGet(url));
        }
    }

    public void sendPostRequestWithoutSid(String url, Map<String, String> params) {
        if(!mIsLocked) {
            VolleyQueueController.getInstance(mContext)
                    .getRequestQueue().add(getRequestForPost(url, params));
        }
    }

    public void sendPostRequest(String url, Map<String, String> params) {
        if(!mIsLocked) {
            SharedPreferenceSettings sessionToken = SharedPreferenceSettings.SESSION_TOKEN;
            SharedPreferences sharedPreferences = SharedPreferenceUtil.getSharedPreferences();
            String sessionTokenStr = sharedPreferences.getString(
                    sessionToken.getId(),
                    (String)sessionToken.getDefaultValue());
            params.put("session_id", sessionTokenStr);
            VolleyQueueController.getInstance(mContext)
                    .getRequestQueue().add(getRequestForPost(url, params));
        }
    }

    public void openLock() {
        mIsLocked = true;
    }

    public void closeLock() {
        mIsLocked = false;
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.d(TAG, error.toString());
        disposeVolleyError(error);
    }

    @Override
    public void onResponse(JSONObject response) {
        Log.d(TAG, response.toString());
        disposeResponse(response);
    }

    protected void disposeVolleyError(VolleyError error) {
        notifyErrorHappened(CodeParams.ERROR_VOLLEY_CODE,
                error == null ? "NULL" : error.getMessage());
    }

    private String convertParamsToUrl(String url, Map<String, String> params){
        StringBuilder urlStr = new StringBuilder(url);
        urlStr.append('?');
        for (Map.Entry<String, String> entry : params.entrySet()) {
            try {
                urlStr.append(URLEncoder.encode(entry.getKey(), "utf-8"));
                urlStr.append('=');
                urlStr.append(URLEncoder.encode(entry.getValue(), "utf-8"));
                urlStr.append('&');
                return urlStr.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    protected void disposeResponse(JSONObject response) {
        Bean bean = null;

        if (response != null) {
            try {
                String result = response.getString("result");
                String message = response.getString("message");
                int flag = response.getInt("code");

                if (flag == CodeParams.SUCCESS) {
                    bean = new Bean();
                    bean.setFlag(flag);
                    bean.setMessage(message);
                    bean.setResult(result);

                    notifyDataChanged(bean);
                } else {
                    notifyErrorHappened(flag, message);
                }
            } catch (Exception e) {
                notifyErrorHappened(CodeParams.ERROR_RESPONSE_FORMAT,
                        "Response format error");
            }
        }
    }

    private UIDataListener uiDataListener;

    public void setUiDataListener(UIDataListener uiDataListener) {
        this.uiDataListener = uiDataListener;
    }

    protected void notifyDataChanged(Bean bean) {
        if (uiDataListener != null) {
            uiDataListener.onDataChanged(bean);
        }
    }

    protected void notifyErrorHappened(int errorCode, String errorMessage) {
        if (uiDataListener != null) {
            uiDataListener.onErrorHappened(errorCode, errorMessage);
        }
    }
}
