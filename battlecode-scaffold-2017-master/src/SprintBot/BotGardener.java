package SprintBot;

import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		
		if(rc.getRoundNum() == 2) {
			rc.buildRobot(RobotType.SCOUT, Direction.NORTH);
		}
	}
}
