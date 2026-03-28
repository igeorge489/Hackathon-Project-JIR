/**
 * @author rayna_li
 * This class represents the levels that the game has
 */
public class Level {

	private int currentLevel;
	
	private long startTime;  
	
	private boolean levelStatus;

	public Level(int currentLevel) {
		this.currentLevel = currentLevel;
		startTime = System.currentTimeMillis();
		levelStatus = false;
	}
	
	public int getCurrentLevel() {
		return currentLevel;
	}
	
	public void setCurrentLevel(int lvl) {
		currentLevel = lvl;
	}
	
	public int getTime() {
		long timeFinished = System.currentTimeMillis();
		return (int)((timeFinished - startTime)/1000);
	}
	
	public void setLevelStatus(boolean status) {
		levelStatus = status;
	}
	
	public boolean getLevelStatus() {
		return levelStatus;
	}
}
