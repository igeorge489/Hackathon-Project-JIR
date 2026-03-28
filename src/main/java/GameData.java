import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Rectangle;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GameData {
	public static List<Level> loadLevels(String jsonPath) {
	    List<Level> allLevels = new ArrayList<>();
	    
	    try {
	        // 1. Read the JSON file
	        String content = new String(Files.readAllBytes(Paths.get(jsonPath)));
	        JSONObject json = new JSONObject(content);
	        
	        JSONArray images = json.getJSONArray("images");
	        JSONArray annotations = json.getJSONArray("annotations");

	        // 2. Loop through every image defined in the JSON
	        for (int i = 0; i < images.length(); i++) {
	            JSONObject imgObj = images.getJSONObject(i);
	            int id = imgObj.getInt("id");
	            String fileName = imgObj.getString("file_name");

	            // 3. Find all trash hitboxes that belong to THIS image ID
	            List<Rectangle> levelHitboxes = new ArrayList<>();
	            for (int j = 0; j < annotations.length(); j++) {
	                JSONObject ann = annotations.getJSONObject(j);
	                if (ann.getInt("image_id") == id) {
	                    JSONArray bbox = ann.getJSONArray("bbox");
	                    // COCO format is [x, y, width, height]
	                    levelHitboxes.add(new Rectangle(
	                    	    (int) bbox.getDouble(0), 
	                    	    (int) bbox.getDouble(1), 
	                    	    (int) bbox.getDouble(2), 
	                    	    (int) bbox.getDouble(3)
	                    	));
	                }
	            }

	            // --- THE SKIP LOGIC ---
	            // Only add the level if we actually found hitboxes for it
	            if (!levelHitboxes.isEmpty()) {
	                allLevels.add(new Level(fileName, levelHitboxes));
	            } else {
	                // This keeps your console clean so you know why an image disappeared
	                System.out.println("Skipping level (No annotations found): " + fileName);
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    return allLevels;
	}
}