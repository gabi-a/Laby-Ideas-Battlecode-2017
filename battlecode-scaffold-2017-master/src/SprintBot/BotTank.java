package SprintBot;

import battlecode.common.*;

public class BotTank {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotTank.rc = rc;
		Util.reportDeath(rc);
	}
}
