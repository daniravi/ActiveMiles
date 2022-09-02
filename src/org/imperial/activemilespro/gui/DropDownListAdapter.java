package org.imperial.activemilespro.gui;

import java.util.ArrayList;

import org.imperial.activemilespro.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.view.View.OnClickListener;

class DropDownListAdapter extends BaseAdapter {

    private final ArrayList<String> mListItems;
    private final LayoutInflater mInflater;


    public DropDownListAdapter(Context context, ArrayList<String> items) {
        mListItems = new ArrayList<>();
        mListItems.addAll(items);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mListItems.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pop_up_list_row, parent, false);
            holder = new ViewHolder();
            holder.tv = (TextView) convertView.findViewById(R.id.DropDownSelectOption);
            holder.chkbox = (CheckBox) convertView.findViewById(R.id.DropDownCheckbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tv.setText(mListItems.get(position));

        final int position1 = position;

        // whenever the checkbox is clicked the selected values textview is
        // updated with new selected values
        holder.chkbox.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setText(position1);
            }
        });

        if (ActiveMilesGUI.ActivitiesSelected[position + 1])
            holder.chkbox.setChecked(true);
        else
            holder.chkbox.setChecked(false);
        return convertView;
    }

    /*
     * Function which updates the selected values display and information(checkSelected[])
     * */
    private void setText(int position1) {
        ActiveMilesGUI.ActivitiesSelected[position1 + 1] = !ActiveMilesGUI.ActivitiesSelected[position1 + 1];
    }

    private class ViewHolder {
        TextView tv;
        CheckBox chkbox;
    }
}
