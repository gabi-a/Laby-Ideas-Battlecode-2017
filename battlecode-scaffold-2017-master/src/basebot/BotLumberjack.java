package basebot;

import battlecode.common.*;

public class BotLumberjack {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotLumberjack.rc = rc;
	}
}
