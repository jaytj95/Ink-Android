package com.paladin.ink;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.TwoLineListItem;

/**
 * Created by jason on 1/19/17.
 */

public class UserAdapter extends ArrayAdapter<User> {
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rootView = convertView;

        User user = getItem(position);

        TwoLineListItem twoLineListItem;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            twoLineListItem = (TwoLineListItem) inflater.inflate(
                    android.R.layout.simple_list_item_2, null);
        } else {
            twoLineListItem = (TwoLineListItem) convertView;
        }

        TextView text1 = twoLineListItem.getText1();
        TextView text2 = twoLineListItem.getText2();
        text1.setTextColor(ContextCompat.getColor(getContext(),android.R.color.white));
        text2.setTextColor(ContextCompat.getColor(getContext(),android.R.color.white));

        text1.setText(user.getName());
        text2.setText(user.getId());

        return twoLineListItem;

//            return rootView;
    }

    public UserAdapter(Context context, int resource) {
        super(context, resource);
    }
}