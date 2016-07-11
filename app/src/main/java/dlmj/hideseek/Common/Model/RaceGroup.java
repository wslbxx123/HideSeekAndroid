package dlmj.hideseek.Common.Model;

import dlmj.hideseek.Common.Params.SharedPreferenceSettings;

/**
 * Created by Two on 5/31/16.
 */
public class RaceGroup {
    private long mRecordId;
    private String mNickname;
    private String mPhotoUrl;
    private RecordItem mRecordItem;

    public RaceGroup(long recordId, String nickname, String photoUrl, RecordItem recordItem) {
        this.mRecordId = recordId;
        this.mNickname = nickname;
        this.mPhotoUrl = photoUrl;
        this.mRecordItem = recordItem;
    }

    public long getRecordId() {
        return mRecordId;
    }

    public String getNickname() {
        return mNickname;
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public RecordItem getRecordItem() {
        return mRecordItem;
    }
}
