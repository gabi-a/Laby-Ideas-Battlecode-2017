package SprintBot;
import battlecode.common.*;

public class Util {
	
	static boolean reportedDeath = false;
	static int[] botsBuilt = new int[6];
	
	
	// Track number of each bots alive
	public static void updateBotCount(RobotController rc) throws GameActionException {
		for(int i = 6;i-->=0;) {
			botsBuilt[i] = Comms.readNumRobots(rc, RobotType.values()[i]);
		}
	}
	public static void reportDeath(RobotController rc) throws GameActionException {
		if(!reportedDeath && rc.getHealth() < 10f) {
    		Comms.writeNumRobots(rc, rc.getType(), botsBuilt[rc.getType().ordinal()]);
    		reportedDeath = true;
    	}
	}
	public static int getNumBots(RobotType type) {
		return botsBuilt[type.ordinal()];
	}
	
	
}
