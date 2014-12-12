package mad.mobiletimetable;

import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


/**
 * Created by Michael on 25/11/2014.
 */
public class AdapterModules extends BaseAdapter {
    private final Context context;
    private ArrayList<ModelModule> modulesArrayList;

    public AdapterModules(Context context, ArrayList<ModelModule> modulesArrayList) {
        //Store the context and modules info
        this.context = context;
        this.modulesArrayList = modulesArrayList;
    }

    //Getter Function
    public ArrayList<ModelModule> getModulesArrayList(){
        return modulesArrayList;
    }

    //Empty the list
    public void clear() {
        modulesArrayList = new ArrayList<ModelModule>();
        notifyDataSetChanged();
    }

    //Update the dataset
    public void addAll(ArrayList<ModelModule> items) {
        modulesArrayList = items;
        notifyDataSetChanged();
    }

    //Remove an item from the list
    public void remove(int position) {
        modulesArrayList.remove(position);
        notifyDataSetChanged();
    }

    //Add an item to the list
    public void insert(int position, ModelModule item) {
        modulesArrayList.add(position, item);
        notifyDataSetChanged();
    }

    //Get one item from the list
    @Override
    public ModelModule getItem(int position) {
        return modulesArrayList.get(position);
    }

    //Get the view for an item
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Get the inflater
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Either recycle view or inflate a new one
        View rowView;
        if(convertView == null) {
            rowView = inflater.inflate(R.layout.fragment_modules_item, parent, false);
        } else {
            rowView = convertView;
        }

        //Get info about the current module this item is for
        ModelModule module = modulesArrayList.get(position);

        //Set the Title
        ((TextView) rowView.findViewById(R.id.module_title)).setText(module.getTitle());
        //Set the Code
        ((TextView) rowView.findViewById(R.id.module_code)).setText(module.getCode());

        //Set background parity
        if(position %2 == 0) {
            rowView.setBackgroundColor(context.getResources().getColor(R.color.module_row_even));
        } else {
            rowView.setBackgroundColor(context.getResources().getColor(R.color.module_row_odd));
        }

        //Store the ID so we can extract it for editing later
        rowView.setTag(Integer.toString(module.getId()));

        return rowView;
    }

    //Get a total of all of how many items there are in the list
    @Override
    public int getCount() {
        return modulesArrayList.size();
    }

    //Get a unique "ID" for each item. We just use the position
    @Override
    public long getItemId(int position) {
        return position;
    }
}
