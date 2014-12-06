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
        ((TextView) rowView.findViewById(R.id.event_title)).setText(modelEvent.getModule().getCode());
        ((TextView) rowView.findViewById(R.id.event_location)).setText(modelEvent.getLocation());

        if(position %2 == 0) {
            rowView.setBackgroundColor(context.getResources().getColor(R.color.module_row_even));
        } else {
            rowView.setBackgroundColor(context.getResources().getColor(R.color.module_row_odd));
        }

        return rowView;
    }

    @Override
    public int getCount() {
        return events.size();
    }
}
