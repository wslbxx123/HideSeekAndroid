package dlmj.hideseek.Common.Params;

/**
 * Created by Two on 4/30/16.
 */
public enum SharedPreferenceSettings {
    SESSION_TOKEN("sessionToken", ""),
    USER_INFO("userInfo", ""),
    LEVEL_VALUE("levelValue", -1),
    RACE_GROUP_VERSION("raceGroupVersion", (long)0),
    RACE_GROUP_RECORD_MIN_ID("raceGroupRecordMinId", (long)0),
    FRIEND_VERSION("friendVersion", (long)0),
    RECORD_VERSION("recordVersion", (long)0),
    RECORD_MIN_ID("recordMinId", (long)0),
    SCORE_SUM("scoreSum", 0),
    RACE_GROUP_UPDATE_TIME("raceGroupUpdateTime", ""),
    RECORD_UPDATE_TIME("recordUpdateTime", "");

    private final String mId;
    private final Object mDefaultValue;

    private SharedPreferenceSettings(String id, Object defaultValue) {
        this.mId = id;
        this.mDefaultValue = defaultValue;
    }

    /**
     * Method that returns the unique identifier of the setting.
     * @return the mId.
     */
    public String getId() {
        return this.mId;
    }

    /**
     * Method that returns the default value of the setting.
     * @return Object The default value of the setting.
     */
    public Object getDefaultValue() {
        return this.mDefaultValue;
    }
}
