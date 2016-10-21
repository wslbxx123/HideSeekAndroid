package dlmj.hideseek.UI.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.List;

import dlmj.hideseek.BusinessLogic.Cache.ImageCacheManager;
import dlmj.hideseek.Common.Model.User;
import dlmj.hideseek.Common.Util.PinYinUtil;
import dlmj.hideseek.R;
import dlmj.hideseek.UI.View.CircleNetworkImageView;

/**
 * Created by Two on 6/5/16.
 */
public class FriendListAdapter extends BaseAdapter{
    private final static int VIEW_TYPE_COUNT = 2;
    private Context mContext;
    private List<User> mFriendList;
    private ImageLoader mImageLoader;

    public FriendListAdapter(Context context, List<User> friendList) {
        this.mContext = context;
        this.mFriendList = friendList;
        this.mImageLoader = ImageCacheManager.getInstance(context).getImageLoader();
    }

    @Override
    public int getCount() {
        if(mFriendList != null) {
            return mFriendList.size() + 1;
        }
        return 1;
    }

    @Override
    public Object getItem(int position) {
        if(position >= 1) {
            return mFriendList.get(position - 1);
        }

        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return position < 1 ? position : 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        int viewType = getItemViewType(position);
        LayoutInflater inflater = LayoutInflater.from(mContext);

        switch(viewType) {
            case 0:
                convertView = inflater.inflate(R.layout.new_friend_menu_item, null);
                break;
            case 1:
                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.friend_item, null);
                    viewHolder = new ViewHolder();
                    viewHolder.mAlphaTextView = (TextView) convertView.findViewById(R.id.alphaTextView);
                    viewHolder.mFriendImageView = (CircleNetworkImageView) convertView.findViewById(R.id.friendImageView);
                    viewHolder.mFriendNameTextView = (TextView) convertView.findViewById(R.id.friendNameTextView);
                    viewHolder.mNicknameTextView = (TextView) convertView.findViewById(R.id.nicknameTextView);
                    convertView.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                User user = (User)getItem(position);
                viewHolder.mFriendImageView.setDefaultImageResId(R.drawable.default_photo);
                viewHolder.mFriendImageView.setImageUrl(user.getSmallPhotoUrl(), mImageLoader);

                if(user.getAlias() != null && user.getAlias().isEmpty()) {
                    viewHolder.mFriendNameTextView.setText(user.getAlias());
                    viewHolder.mNicknameTextView.setVisibility(View.VISIBLE);
                    viewHolder.mNicknameTextView.setText(String.format(mContext.getString(R.string.name),
                            user.getNickname()));
                } else {
                    viewHolder.mFriendNameTextView.setText(user.getNickname());
                    viewHolder.mNicknameTextView.setVisibility(View.INVISIBLE);
                }

                String currentStr = PinYinUtil.getAlpha(user.getPinyin());
                String previewStr = (position - 2) >= 0 ? PinYinUtil.getAlpha(mFriendList
                        .get(position - 2).getPinyin()) : " ";
                if (!previewStr.equals(currentStr)) {
                    viewHolder.mAlphaTextView.setVisibility(View.VISIBLE);
                    viewHolder.mAlphaTextView.setText(currentStr);
                } else {
                    viewHolder.mAlphaTextView.setVisibility(View.GONE);
                }
                break;
        }

        return convertView;
    }

    private class ViewHolder {
        CircleNetworkImageView mFriendImageView;
        TextView mFriendNameTextView;
        TextView mNicknameTextView;
        TextView mAlphaTextView;
    }
}
