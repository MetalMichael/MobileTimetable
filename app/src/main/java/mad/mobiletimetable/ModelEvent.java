package mad.mobiletimetable;

import org.apache.http.impl.cookie.DateParseException;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Michael on 5/12/2014.
 */
public class ModelEvent {
    private int id;
    private int moduleId;
    private String day;
    private Date time;
    private ModelModule module;

    public ModelEvent(JSONObject event) {
        try {
            id = event.getInt("ID");
            moduleId = event.getInt("ModuleID");
            day = event.getString("ModuleCode");

            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            time = df.parse(event.getString("Lecturer"));

            if(event.has("module") && !event.isNull("module")) {
                module = new ModelModule(event.getJSONObject("module"));
            } else {
                module = null;
            }
        } catch(JSONException e) {
            e.printStackTrace();
        } catch(ParseException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public int getModuleId() {
        return moduleId;
    }

    public String getDay() {
        return day;
    }

    public Date getTime() {
        return time;
    }

    public ModelModule getModule() {
        return module;
    }
}
