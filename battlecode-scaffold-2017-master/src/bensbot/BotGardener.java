package bensbot;

import battlecode.common.*;

public class BotGardener {
	static MapLocation tree_patch = new MapLocation(265,130);
	static int tree_count = 0;

	public static void turn(RobotController rc) throws GameActionException {
		if(rc.getLocation() != tree_patch){
			// move to tree patch
			// TODO: add code to choose a patch
			rc.move(tree_patch);
		} else {
			// spawn trees
			// TODO: add code to make nice patch
			if(rc.canPlantTree(Direction.getNorth())){
				rc.plantTree(Direction.getNorth());
			}
		}
		// spawn other units
		// TODO: Let's not just spawn units randomly
		if(rc.canBuildRobot(RobotType.LUMBERJACK, Direction.getSouth())){
			rc.buildRobot(RobotType.LUMBERJACK, Direction.getSouth());
		}
	}
	
}
