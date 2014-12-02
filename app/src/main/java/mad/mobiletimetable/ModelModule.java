package mad.mobiletimetable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Michael on 25/11/2014.
 */
public class ModelModule {
    private int id;
    private String title;
    private String code;
    private String lecturer;

    public ModelModule(JSONObject module) {
        try {
            id = module.getInt("ID");
            title = module.getString("ModuleTitle");
            code = module.getString("ModuleCode");
            lecturer = module.getString("Lecturer");
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    public int getId() { return id; }

    public String getTitle() {
        return title;
    }

    public String getCode() {
        return code;
    }

    public String getLecturer() { return lecturer; }
}
