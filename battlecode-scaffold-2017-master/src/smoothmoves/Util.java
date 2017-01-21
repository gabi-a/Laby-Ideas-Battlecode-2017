package smoothmoves;
import battlecode.common.*;

public class Util {
	
	static boolean reportedDeath = false;
	
	public static void reportIfDead(RobotController rc) throws GameActionException {
		if(!reportedDeath && rc.getHealth() <= 5f) {
			Comms.ourBotCount.decrementNumBots(rc, rc.getType());
    		reportedDeath = true;
    	}
	}
	
}
