package SprintBot;

import battlecode.common.*;

public class BotScout {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotScout.rc = rc;
		Util.reportDeath(rc);
	}
}