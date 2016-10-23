package dlmj.hideseek.Common.Model;

/**
 * Created by Two on 11/10/2016.
 */
public class ExchangeOrder {
    private long mOrderId;
    private int mStatus;
    private String mCreateTime;
    private String mUpdateTime;
    private int mCount;
    private long mRewardId;
    private String mRewardName;
    private String mImageUrl;
    private int mRecord;
    private int mExchangeCount;
    private String mIntroduction;
    private long mVersion;

    public ExchangeOrder (long orderId, int status, String createTime, String updateTime,
                         int count, long rewardId, String rewardName, String imageUrl,
                         int record, int exchangeCount, String introduction,
                         long version) {
        this.mOrderId = orderId;
        this.mStatus = status;
        this.mCreateTime = createTime;
        this.mUpdateTime = updateTime;
        this.mCount = count;
        this.mRewardId = rewardId;
        this.mRewardName = rewardName;
        this.mImageUrl = imageUrl;
        this.mRecord = record;
        this.mExchangeCount = exchangeCount;
        this.mIntroduction = introduction;
        this.mVersion = version;
    }

    public int getStatus() {
        return this.mStatus;
    }

    public String getCreateTime() {
        return this.mCreateTime;
    }

    public String getUpdateTime() {
        return this.mUpdateTime;
    }

    public int getCount() {
        return this.mCount;
    }

    public long getRewardId() {
        return this.mRewardId;
    }

    public String getRewardName() {
        return this.mRewardName;
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

    public long getOrderId() {
        return this.mOrderId;
    }
}
