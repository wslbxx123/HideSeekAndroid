package dlmj.hideseek.BusinessLogic.Network;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Two on 4/30/16.
 */
public class VolleyQueueController {
    private RequestQueue mRequestQueue;
    private static VolleyQueueController mInstance;

    public static VolleyQueueController getInstance(Context context){
        if(mInstance == null){
            synchronized (VolleyQueueController.class){
                if(mInstance == null){
                    mInstance = new VolleyQueueController(context);
                }
            }
        }
        return mInstance;
    }

    public VolleyQueueController(Context context){
        mRequestQueue = Volley.newRequestQueue(context);
    }

    public RequestQueue getRequestQueue(){
        return mRequestQueue;
    }
}
