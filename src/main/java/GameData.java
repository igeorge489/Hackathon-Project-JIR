import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GameData {
    public static List<Level> loadLevels(String jsonPath) {
        List<Level> levels = new ArrayList<>();
        try {
            String content = new String(Files.readAllBytes(Paths.get(jsonPath)));
            JSONObject obj = new JSONObject(content);
            JSONArray images = obj.getJSONArray("images");
            JSONArray annotations = obj.getJSONArray("annotations");

            // 1. Create a Level for every image in the JSON
            for (int i = 0; i < images.length(); i++) {
                JSONObject imgObj = images.getJSONObject(i);
                int id = imgObj.getInt("id");
                String fileName = imgObj.getString("file_name");

                Level newLevel = new Level(i + 1, fileName);

                // 2. Find all trash hitboxes for THIS specific image
                for (int j = 0; j < annotations.length(); j++) {
                    JSONObject ann = annotations.getJSONObject(j);
                    if (ann.getInt("image_id") == id) {
                        JSONArray bbox = ann.getJSONArray("bbox");
                        // COCO bbox format: [x, y, width, height]
                        newLevel.addHitbox(
                            bbox.getInt(0), 
                            bbox.getInt(1), 
                            bbox.getInt(2), 
                            bbox.getInt(3)
                        );
                    }
                }
                levels.add(newLevel);
            }
        } catch (Exception e) {
            System.out.println("Error parsing JSON: " + e.getMessage());
        }
        return levels;
    }
}