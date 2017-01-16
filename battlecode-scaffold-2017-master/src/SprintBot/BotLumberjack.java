package SprintBot;

import battlecode.common.*;

enum Strategy {
	OFFENSE, DEFENSE, LUMBERJACK
}

public class BotLumberjack {
	static RobotController rc;

	static TreeInfo target;
	static MapLocation home;
	static Strategy strat = Strategy.LUMBERJACK;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotLumberjack.rc = rc;


		if(strat == Strategy.LUMBERJACK){
			// Find a target
			if(target == null) {
				target = Comms.neutralTrees.pop(rc);
			}
			if(target == null) {
				TreeInfo[] trees = rc.senseNearbyTrees();
				if(trees.length == 0){
					Nav.explore(rc);
					return;
				}
				float dist = 200;
				for(int i = 0; i < trees.length; i++){
					float newDist = trees[i].getLocation().distanceTo(rc.getLocation());
					if(newDist < dist){
						dist = newDist;
						target = trees[i];
					}
				}
			}

			// Go to target
			if(!rc.canInteractWithTree(target.getID())){
				Nav.tryMove(rc, rc.getLocation().directionTo(target.getLocation()));
			}

			// Chop target
			if(rc.canInteractWithTree(target.getID())){
				rc.chop(target.getID());
			}

			// Check if target is dead
			if(rc.canSenseLocation(target.getLocation()) && !rc.canSenseTree(target.getID())) {
				target = null;
			}
		}
	}
}
