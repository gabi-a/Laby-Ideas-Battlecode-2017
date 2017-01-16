package SprintBot;

import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		if(rc.canBuildRobot(RobotType.LUMBERJACK, Direction.getNorth())){
			rc.buildRobot(RobotType.LUMBERJACK, Direction.getNorth());
		}
	}
}
