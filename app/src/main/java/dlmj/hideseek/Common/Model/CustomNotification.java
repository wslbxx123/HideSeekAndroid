package dlmj.hideseek.Common.Model;

/**
 * Created by Two on 19/10/2016.
 */
public class CustomNotification {
    private Integer mId;
    private Long mMessageId;
    private String mTitle;
    private String mContent;
    private String mActivity;
    private int mNotificationActionType;
    private String mUpdateTime;

    public CustomNotification() {

    }

    public CustomNotification(Integer id, Long messageId, String title, String content,
                              String activity, int notificationActionType, String updateTime) {
        this.mId = id;
        this.mMessageId = messageId;
        this.mTitle = title;
        this.mContent = content;
        this.mActivity = activity;
        this.mNotificationActionType = notificationActionType;
        this.mUpdateTime = updateTime;
    }

    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        this.mId = id;
    }

    public Long getMessageId() {
        return mMessageId;
    }

    public void setMessageId(Long messageId) {
        this.mMessageId = messageId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public String getUpdateTime() {
        return mUpdateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.mUpdateTime = updateTime;
    }

    public String getActivity() {
        return mActivity;
    }

    public void setActivity(String activity) {
        this.mActivity = activity;
    }

    public int getNotificationActionType() {
        return mNotificationActionType;
    }

    public void setNotificationActionType(int notificationActionType) {
        this.mNotificationActionType = notificationActionType;
    }
}
