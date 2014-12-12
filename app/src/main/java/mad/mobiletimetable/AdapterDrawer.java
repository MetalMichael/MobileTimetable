package mad.mobiletimetable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;

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

    private Bitmap getDisplayPicture(String auth) {
        Bitmap b = null;
        try {
            File f = new File(context.getFilesDir(), "user-" + auth + "-pic");
            if(f.exists()) {
                b = BitmapFactory.decodeStream(new FileInputStream(f));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return b;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = ((LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.drawer_list_item, parent, false);
        }
        Log.d("Drawer","Updated");
        ((TextView) convertView.findViewById(R.id.drawer_item_text)).setText(items[position]);
        Drawable drawable;
        switch(position) {
            case 0:
                String auth = context.getSharedPreferences("MyAuthFile", 0).getString("Auth","");
                Bitmap displayPicture = null;
                if(!auth.equals("")) {
                    Log.d("Drawer","Getting display picture");
                    displayPicture = getDisplayPicture(auth);
                }
                if(displayPicture != null){
                    ((ImageView) convertView.findViewById(R.id.drawer_item_image))
                            .setImageBitmap(displayPicture);
                } else {
                    ((ImageView) convertView.findViewById(R.id.drawer_item_image))
                            .setImageResource(R.drawable.ic_contact_picture);
                }
                ((ImageView) convertView.findViewById(R.id.drawer_item_image)).getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
                break;
            //Timetable
            case 1:
                ((ImageView)convertView.findViewById(R.id.drawer_item_image))
                        .setImageResource(R.drawable.ic_activity_timetable);
                break;
            //Modules
            case 2:
                ((ImageView)convertView.findViewById(R.id.drawer_item_image))
                        .setImageResource(R.drawable.ic_activity_modules);
                break;
            //Add To Timetable
            case 3:
                ((ImageView)convertView.findViewById(R.id.drawer_item_image))
                        .setImageResource(R.drawable.ic_activity_add_to_timetable);
                break;
            //Settings
            default:
                ((ImageView)convertView.findViewById(R.id.drawer_item_image))
                        .setImageResource(R.drawable.ic_activity_settings);
        }


        return convertView;
    }

}
