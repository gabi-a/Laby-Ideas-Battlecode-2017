package SprintBot;

import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		Util.updateBotCount(rc);
		Util.reportDeath(rc);
		
	}
}
