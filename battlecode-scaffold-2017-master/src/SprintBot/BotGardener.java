package SprintBot;

import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		if(rc.canBuildRobot(RobotType.LUMBERJACK, Direction.NORTH)){
			rc.buildRobot(RobotType.LUMBERJACK, Direction.NORTH);
		}
	}
}
