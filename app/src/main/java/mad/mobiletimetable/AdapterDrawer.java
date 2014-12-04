package mad.mobiletimetable;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Michael on 04/12/2014.
 */
public class AdapterDrawer extends ArrayAdapter<String> {

    private String[] items;
    private Context context;

    public AdapterDrawer(Context context, String[] items) {
        super(context, R.layout.drawer_list_item, items);
        this.items = items;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.drawer_list_item, parent, false);
        }

        ((TextView)convertView.findViewById(R.id.drawer_item_text)).setText(items[position]);

        Drawable drawable;
        switch(position) {
            case 0:
                ((ImageView)convertView.findViewById(R.id.drawer_item_image))
                        .setImageResource(R.drawable.ic_activity_timetable);
                break;
            case 1:
                ((ImageView)convertView.findViewById(R.id.drawer_item_image))
                        .setImageResource(R.drawable.ic_activity_modules);
                break;
            case 2:
                ((ImageView)convertView.findViewById(R.id.drawer_item_image))
                        .setImageResource(R.drawable.ic_activity_add_to_timetable);
                break;
            default:
                ((ImageView)convertView.findViewById(R.id.drawer_item_image))
                        .setImageResource(R.drawable.ic_activity_settings);
        }

        return convertView;
    }

}
