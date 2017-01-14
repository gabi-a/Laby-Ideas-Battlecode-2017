package idkthebot;

import battlecode.common.*;

public class BotSoldier {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotSoldier.rc = rc;
		
		Nav.explore(rc);
		
	}
	
}
