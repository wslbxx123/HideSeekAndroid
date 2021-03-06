package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.List;

import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Util.PinYinUtil;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.View.CircleNetworkImageView;

/**
 * Created by Two on 6/6/16.
 */
public class FriendResultListAdapter extends BaseAdapter{
    private Context mContext;
    private List<User> mFriendList;
    private ImageLoader mImageLoader;

    public FriendResultListAdapter(Context context, List<User> friendList) {
        this.mContext = context;
        this.mFriendList = friendList;
        this.mImageLoader = ImageCacheManager.getInstance(context).getImageLoader();
    }

    @Override
    public int getCount() {
        if(mFriendList != null) {
            return mFriendList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return mFriendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.friend_item, null);
            viewHolder = new ViewHolder();
            viewHolder.mFriendImageView = (CircleNetworkImageView) convertView.findViewById(R.id.friendImageView);
            viewHolder.mFriendNameTextView = (TextView) convertView.findViewById(R.id.friendNameTextView);
            viewHolder.mRoleImageView = (ImageView) convertView.findViewById(R.id.roleImageView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        User user = (User)getItem(position);
        viewHolder.mFriendImageView.setImageUrl(user.getSmallPhotoUrl(), mImageLoader);
        viewHolder.mFriendNameTextView.setText(user.getNickname());
        viewHolder.mRoleImageView.setImageResource(user.getRoleImageDrawableId());

        return convertView;
    }

    private class ViewHolder {
        CircleNetworkImageView mFriendImageView;
        TextView mFriendNameTextView;
        ImageView mRoleImageView;
    }
}
