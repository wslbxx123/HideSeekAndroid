package dlmj.hideseek.BusinessLogic.Cache;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Two on 5/2/16.
 */
public class BaseCache<T> {
    protected List<T> mList;

    public BaseCache() {
        mList = new LinkedList<>();
    }

    public List<T> getList(){
        return mList;
    }

    public void setList(List<T> list) {
        mList = list;
    }

    public void addItem(T item) {
        mList.add(item);
    }

    public void removeItem(T item) {
        mList.remove(item);
    }

    public void clearList() {
        mList.clear();
    }

}