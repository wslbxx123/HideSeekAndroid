package dlmj.hideseek.Common.Interfaces;

/**
 * Created by Two on 4/30/16.
 */
public interface UIDataListener<T> {
    public void onDataChanged(T data);
    public void onErrorHappened(int errorCode, String errorMessage);
}
