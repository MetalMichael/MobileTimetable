package mad.mobiletimetable;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by Michael on 25/11/2014.
 */
public class AdapterModules extends BaseAdapter {
    private final Context context;
    private ArrayList<ModelModule> modulesArrayList;

    public AdapterModules(Context context, ArrayList<ModelModule> modulesArrayList) {
        this.context = context;
        this.modulesArrayList = modulesArrayList;
    }

    public void clear() {
        modulesArrayList = new ArrayList<ModelModule>();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<ModelModule> items) {
        modulesArrayList = items;
        notifyDataSetChanged();
    }

    public void remove(int position) {
        modulesArrayList.remove(position);
        notifyDataSetChanged();
    }

    public void insert(int position, ModelModule item) {
        modulesArrayList.add(position, item);
        notifyDataSetChanged();
    }

    @Override
    public ModelModule getItem(int position) {
        return modulesArrayList.get(position);
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

    @Override
    public long getItemId(int position) {
        return position;
    }
}
