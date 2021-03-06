package dlmj.hideseek.Common.Model;

import java.util.List;

/**
 * Created by Two on 11/10/2016.
 */
public class Reward {
    private long mPkId;
    private String mName;
    private String mImageUrl;
    private int mRecord;
    private int mExchangeCount;
    private String mIntroduction;
    private long mVersion;

    public Reward(long pkId, String name, String imageUrl, int record,
                  int exchangeCount, String introduction, long version) {
        this.mPkId = pkId;
        this.mName = name;
        this.mImageUrl = imageUrl;
        this.mRecord = record;
        this.mExchangeCount = exchangeCount;
        this.mIntroduction = introduction;
        this.mVersion = version;
    }

    public String getName() {
        return this.mName;
    }

    public String getImageUrl() {
        return this.mImageUrl;
    }

    public int getRecord() {
        return this.mRecord;
    }

    public int getExchangeCount() {
        return this.mExchangeCount;
    }

    public String getIntroduction() {
        return this.mIntroduction;
    }

    public long getVersion() {
        return this.mVersion;
    }

    public long getPkId() {
        return mPkId;
    }
}
