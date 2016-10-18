package dlmj.hideseek.Common.Factory;

import android.content.Context;
import android.content.res.Resources;

import dlmj.hideseek.Common.Params.CodeParams;
import dlmj.hideseek.R;

/**
 * Created by Two on 5/1/16.
 */
public class ErrorMessageFactory {
    Resources mResources;

    public ErrorMessageFactory(Context context){
        mResources = context.getResources();
    }
    public String get(int errorCode){
        switch(errorCode){
            case CodeParams.ERROR_VOLLEY_CODE:
                return mResources.getString(R.string.error_connect_network_failed);
            case CodeParams.ERROR_SESSION_INVALID:
                return mResources.getString(R.string.error_session_invalid);
            case CodeParams.ERROR_LOGIN_FAILED:
                return mResources.getString(R.string.error_login_failed);
            case CodeParams.ERROR_USER_ALREADY_EXIST:
                return mResources.getString(R.string.error_user_already_exist);
            case CodeParams.ERROR_RESPONSE_FORMAT:
                return mResources.getString(R.string.error_response_format);
            case CodeParams.ERROR_GOAL_DISAPPEAR:
                return mResources.getString(R.string.error_goal_disappear);
            case CodeParams.ERROR_SEARCH_MYSELF:
                return mResources.getString(R.string.error_search_myself);
            default:
                return "";
        }
    }
}
