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
    private int duration;
    private int day;
    private Date time;
    private ModelModule module;
    private String location;
    private String lessonType;
    
    public ModelEvent(int day, Date time) {
        id = null;
        moduleId = null;
        duration = 1;
        day = day;
        this.time = time;
        module = null;
        location = null;
        lessonType = null;
    }

    public ModelEvent(JSONObject event) {
        try {
            id = event.getInt("ID");
            moduleId = event.getInt("ModuleID");
            duration = event.getInt("Duration");
            day = event.getInt("Day") + 1;
            location = event.getString("Room");
            lessonType = event.getString("Type");

            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
            time = df.parse(event.getString("Time"));

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

    public int getDuration() { return duration; }

    public int getDay() {
        return day;
    }

    public Date getTime() {
        return time;
    }

    public String getDate() {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        return df.format(time);
    }

    public ModelModule getModule() {
        return module;
    }

    public String getLocation() {
        return location;
    }

    public String getLessonType() {
        return lessonType;
    }
}
