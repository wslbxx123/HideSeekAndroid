package dlmj.hideseek.BusinessLogic.Network;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.Common.Util.LogUtil;

/**
 * Created by Two on 5/9/16.
 */
public class MultiPartJsonRequest extends Request<String> {
    private static String TAG = "MultiPartRequest";
    private MultipartEntity entity = new MultipartEntity();

    private Response.Listener<String> mListener;
    private Response.ErrorListener mErrorListener;

    private List<File> mFileParts;
    private String mFilePartName;
    private Map<String, String> mParams;

    /**
     * 单个文件＋参数 上传
     * @param url
     * @param listener
     * @param errorListener
     * @param filePartName
     * @param file
     * @param params
     */
    public MultiPartJsonRequest(String url, Response.Listener<String> listener,
                                Response.ErrorListener errorListener, String filePartName,
                                File file, Map<String, String> params){
        super(Method.POST, url, errorListener);
        mFileParts = new ArrayList<>();
        if(file != null && file.exists()){
            mFileParts.add(file);
        }else{
            VolleyLog.e("MultiPartRequest---file not found");
        }
        mFilePartName = filePartName;
        mListener = listener;
        mErrorListener = errorListener;
        mParams = params;
        buildMultiPartEntity();
    }

    /**
     * 多个文件＋参数上传
     * @param url
     * @param listener
     * @param errorListener
     * @param filePartName
     * @param files
     * @param params
     */
    public MultiPartJsonRequest(String url, Response.Listener<String> listener,
                                Response.ErrorListener errorListener,
                                String filePartName, List<File> files, Map<String, String> params) {
        super(Method.POST, url, errorListener);
        mFilePartName = filePartName;
        mListener = listener;
        mErrorListener = errorListener;
        mFileParts = files;
        mParams = params;
        buildMultiPartEntity();
    }



    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed,
                HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
        JSONObject result = null;
        try {
            LogUtil.d(TAG, response);
            result = new JSONObject(response);
            int code = result.getInt("code");

            if (code == CodeParams.SUCCESS) {
                mListener.onResponse(response);
            } else {
                mErrorListener.onErrorResponse(new VolleyError(response));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = super.getHeaders();

        if (headers == null || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<>();
        }
        return headers;
    }

    @Override
    public String getBodyContentType() {
        return entity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try{
            entity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    private void buildMultiPartEntity() {
        if (mFileParts != null && mFileParts.size() > 0) {
            for (File file : mFileParts) {
                entity.addPart(mFilePartName, new FileBody(file));
            }
            long l = entity.getContentLength();
            Log.i("YanZi-volley", mFileParts.size() + "个，长度：" + l);
        }

        try {
            if (mParams != null && mParams.size() > 0) {
                for (Map.Entry<String, String> entry : mParams.entrySet()) {
                    entity.addPart(
                            entry.getKey(),
                            new StringBody(entry.getValue(), Charset
                                    .forName("UTF-8")));
                }
            }
        } catch (UnsupportedEncodingException e) {
            VolleyLog.e("UnsupportedEncodingException");
        }
    }
}
