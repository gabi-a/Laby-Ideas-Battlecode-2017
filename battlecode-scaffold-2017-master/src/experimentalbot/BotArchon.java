package experimentalbot;

import battlecode.common.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BotArchon {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotArchon.rc = rc;
		
		if(rc.getRoundNum() == 0) {
			rc.buildRobot(RobotType.GARDENER, new Direction(0));
		}
	}
	
}

