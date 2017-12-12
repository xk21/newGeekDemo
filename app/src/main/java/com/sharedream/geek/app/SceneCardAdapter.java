package com.sharedream.geek.app;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by young on 2017/6/16.
 */

public class SceneCardAdapter extends BaseAdapter {
    private List<SceneNode> sceneNodeList;
    private Context context;

    public SceneCardAdapter(Context context, List<SceneNode> newSceneNodeList) {
        this.sceneNodeList = newSceneNodeList;
        this.context = context;
    }

    public void setData(List<SceneNode> nodeList) {
        this.sceneNodeList = nodeList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (sceneNodeList == null) {
            return 0;
        }

        return sceneNodeList.size() > 6 ? 6 : sceneNodeList.size();
    }

    @Override
    public Object getItem(int position) {
        return sceneNodeList == null ? null : sceneNodeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_node_service, null);
            viewHolder = new ViewHolder();
            viewHolder.ivLogo = (ImageView) convertView.findViewById(R.id.iv_service_logo);
            viewHolder.tvServiceName = (TextView) convertView.findViewById(R.id.tv_service_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        SceneNode sceneNode = sceneNodeList.get(position);
        String serviceName = sceneNode.serviceName;
        String iconFileUrl = sceneNode.iconFileUrl;
        String iconHttpUrl = sceneNode.iconHttpUrl;

        viewHolder.tvServiceName.setText(serviceName);
        try {
            if (TextUtils.isEmpty(iconFileUrl)) {
                Picasso.with(context).load(iconHttpUrl)
                        .placeholder(R.drawable.icon_geek_app_logo).into(viewHolder.ivLogo);
            } else {
                Picasso.with(context).load(iconFileUrl)
                        .placeholder(R.drawable.icon_geek_app_logo).into(viewHolder.ivLogo);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return convertView;
    }

    private class ViewHolder {
        public ImageView ivLogo;
        public TextView tvServiceName;
    }
}
