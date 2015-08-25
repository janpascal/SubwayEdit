package org.vanbest.subwayedit;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Map;

/**
 * Created by Jan-Pascal on 23-8-2015.
 */
public class SubwayAdapter extends BaseAdapter {

    private Context context;
    private SubwayFile f;

    public SubwayAdapter(Context context, SubwayFile f) {
        this.context = context;
        this.f = f;
    }

    @Override
    public int getCount() {
        return 2 * f.getSize();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public View getView (int position, View convertView, ViewGroup parent) {
        View gridView;

        if (convertView == null) {
            //gridView = new View(context);
            if (position % 2 == 0) {
                TextView view = new TextView(context);
                view.setText(f.getKey(position / 2));
                gridView = view;
            } else {
                TextView view = new EditText(context);
                view.setText(f.getValue(position / 2));
                view.setLines(1);
                view.setSingleLine();
                gridView = view;
            }
        } else {
            gridView = convertView;
        }
        return gridView;


    }

}
