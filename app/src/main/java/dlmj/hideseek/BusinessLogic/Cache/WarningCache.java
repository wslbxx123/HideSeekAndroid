package dlmj.hideseek.BusinessLogic.Cache;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import dlmj.hideseek.Common.Model.Goal;
import dlmj.hideseek.Common.Model.Warning;

/**
 * Created by Two on 11/10/2016.
 */
public class WarningCache extends BaseCache<Warning> {
    private static WarningCache mInstance;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Date mServerTime;

    public static WarningCache getInstance(){
        synchronized (WarningCache.class){
            if(mInstance == null){
                mInstance = new WarningCache();
            }
        }
        return mInstance;
    }

    public void setWarnings(String warningStr) {
        try {
            JSONObject result = new JSONObject(warningStr);
            String warnings = result.getString("warnings");

            saveRaceGroup(warnings);

            mServerTime = mDateFormat.parse(result.getString("server_time"));
        } catch (JSONException ex) {
            ex.printStackTrace();
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
    }

    public void saveRaceGroup(String warnings) {
        List<Warning> list = new LinkedList<>();

        try {
            JSONArray warningList = new JSONArray(warnings);
            String warningStr;

            for (int i = 0; i < warningList.length(); i++) {
                warningStr = warningList.getString(i);
                JSONObject warning = new JSONObject(warningStr);

                Goal goal = new Goal(warning.getLong("pk_id"),
                        warning.getDouble("latitude"),
                        warning.getDouble("longitude"),
                        warning.getInt("orientation"),
                        warning.getInt("valid") == 1,
                        Goal.GoalTypeEnum.valueOf(warning.getInt("type")),
                        warning.getString("show_type_name"),
                        warning.getLong("create_by"),
                        warning.getString("introduction"),
                        warning.getInt("score"),
                        warning.getInt("union_type"));

                list.add(new Warning(goal,
                        warning.getString("create_time")));
            }

            mList = list;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Date getServerTime() {
        return this.mServerTime;
    }
}
