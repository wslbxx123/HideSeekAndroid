package dlmj.hideseek.Common.Model;

/**
 * Created by Two on 22/10/2016.
 */
public class Product {
    private long mPkId;
    private String mName;
    private String mImageUrl;
    private double mPrice;
    private int mPurchaseCount;
    private String mIntroduction;
    private long mVersion;

    public Product(long pkId, String name, String imageUrl, double price,
                   int purchaseCount, String introduction, long version) {
        this.mPkId = pkId;
        this.mName = name;
        this.mImageUrl = imageUrl;
        this.mPrice = price;
        this.mPurchaseCount = purchaseCount;
        this.mIntroduction = introduction;
        this.mVersion = version;
    }

    public String getName() {
        return this.mName;
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

    public long getPkId() {
        return mPkId;
    }
}
