package mad.mobiletimetable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Benjy on 06/12/2014.
 */
public class AdapterTimetable extends ArrayAdapter<ModelEvent>{
    private ArrayList<ModelEvent> events;
    private Context context;

    public AdapterTimetable(Context context, ArrayList<ModelEvent> events) {
        super(context, R.layout.fragment_timetable_event_item, events);
        this.events = events;
        this.context = context;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView;
        if(convertView == null) {
            rowView = inflater.inflate(R.layout.fragment_timetable_event_item, parent, false);
        } else {
            rowView = convertView;
        }

        ModelEvent modelEvent = events.get(position);
        ((TextView) rowView.findViewById(R.id.event_time)).setText(modelEvent.getDate());
        
        //If isn't a dummy
        if(modelEvent.getId() != -1) {
            ((TextView) rowView.findViewById(R.id.event_title)).setText(modelEvent.getModule().getTitle());
            ((TextView) rowView.findViewById(R.id.event_location)).setText(modelEvent.getLocation());
            rowView.setTag(Integer.toString(modelEvent.getId()));
            ((View) rowView.findViewById(R.id.event_item_info)).setVisibility(View.VISIBLE);
        } else {
            ((TextView) rowView.findViewById(R.id.event_title)).setText("");
            ((TextView) rowView.findViewById(R.id.event_location)).setText("");
            rowView.setTag(modelEvent.getDate()+"-"+modelEvent.getDay());
            ((View) rowView.findViewById(R.id.event_item_info)).setVisibility(View.INVISIBLE);
        }
        

        if(position %2 == 0) {
            rowView.setBackgroundColor(context.getResources().getColor(R.color.event_row_even));
        } else {
            rowView.setBackgroundColor(context.getResources().getColor(R.color.event_row_odd));
        }

        return rowView;
    }

    @Override
    public int getCount() {
        return events.size();
    }
}
