package mad.mobiletimetable;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by Michael on 25/11/2014.
 */
public class AdapterModules extends ArrayAdapter<ModelModule> {
    private final Context context;
    private final ArrayList<ModelModule> modulesArrayList;

    public AdapterModules(Context context, ArrayList<ModelModule> modulesArrayList) {
        super(context, R.layout.fragment_modules_item, modulesArrayList);

        this.context = context;
        this.modulesArrayList = modulesArrayList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView;
        if(convertView == null) {
            rowView = inflater.inflate(R.layout.fragment_modules_item, parent, false);
        } else {
            rowView = convertView;
        }

        ModelModule module = modulesArrayList.get(position);

        ((TextView) rowView.findViewById(R.id.module_title)).setText(module.getTitle());
        ((TextView) rowView.findViewById(R.id.module_code)).setText(module.getCode());

        if(position %2 == 0) {
            rowView.setBackgroundColor(context.getResources().getColor(R.color.module_row_even));
        } else {
            rowView.setBackgroundColor(context.getResources().getColor(R.color.module_row_odd));
        }

        return rowView;
    }

    @Override
    public int getCount() {
        return modulesArrayList.size();
    }
}
