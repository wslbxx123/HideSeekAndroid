package dlmj.hideseek.Common.Model;

/**
 * Created by Two on 11/10/2016.
 */
public class PurchaseOrder {
    private long mOrderId;
    private int mStatus;
    private String mCreateTime;
    private String mUpdateTime;
    private int mCount;
    private String mTradeNo;
    private long mProductId;
    private String mProductName;
    private String mImageUrl;
    private double mPrice;
    private int mPurchaseCount;
    private String mIntroduction;
    private long mVersion;

    public PurchaseOrder(long orderId, int status, String createTime, String updateTime,
                         int count, String tradeNo, long productId, String productName,
                         String imageUrl, double price, int purchaseCount, String introduction,
                         long version) {
        this.mOrderId = orderId;
        this.mStatus = status;
        this.mCreateTime = createTime;
        this.mUpdateTime = updateTime;
        this.mCount = count;
        this.mTradeNo = tradeNo;
        this.mProductId = productId;
        this.mProductName = productName;
        this.mImageUrl = imageUrl;
        this.mPrice = price;
        this.mPurchaseCount = purchaseCount;
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

    public String getTradeNo() {
        return this.mTradeNo;
    }

    public long getProductId() {
        return this.mProductId;
    }

    public String getProductName() {
        return this.mProductName;
    }

    public String getImageUrl() {
        return this.mImageUrl;
    }

    public double getPrice() {
        return this.mPrice;
    }

    public int getPurchaseCount() {
        return this.mPurchaseCount;
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
