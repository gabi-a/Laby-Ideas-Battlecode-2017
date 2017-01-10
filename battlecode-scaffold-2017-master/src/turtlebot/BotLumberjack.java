package turtlebot;

import battlecode.common.*;

public class BotLumberjack {
	static RobotController rc;

	public static void turn(RobotController rc) throws GameActionException {
		// TODO: Find trees, kill trees
		BotLumberjack.rc = rc;
		TreeInfo[] trees = new TreeInfo[0];
		for(int i = 1; i <=10; i++){
			trees = rc.senseNearbyTrees(i);
			if(trees.length != 0) break;
		}
		if(trees.length == 0){
        	Direction dir = new Direction((float)Math.random() * 2 * (float)Math.PI);
			rc.move(dir);  
			return;
		}
		int tree_id = -1;
		for(int i = 0; i < trees.length; i++){
			if(trees[i].getTeam() == Team.NEUTRAL && (trees[i].getContainedBullets() != 0 || trees[i].getContainedRobot() != null)){
				tree_id = i;
				break;
			}
		}

		if(tree_id == -1){
			MapLocation loc = new MapLocation(0,0);
			Direction dir = rc.getLocation().directionTo(loc).opposite();
			while(!rc.canMove(dir))
				dir = rc.getLocation().directionTo(loc).rotateLeftDegrees(90+(float)Math.random()*180);
			rc.move(dir);
		}

		MapLocation loc = trees[tree_id].location;
		int id = trees[tree_id].ID;
		if(rc.canChop(id)){
			rc.chop(id);
			return;
		}
		if(rc.canMove(loc)){
			rc.move(loc);
		} else {
			Direction dir = rc.getLocation().directionTo(loc);
			while(!rc.canMove(dir))
				dir = rc.getLocation().directionTo(loc).rotateLeftDegrees(90+(float)Math.random()*180);
			rc.move(dir);
		}
		if(rc.canChop(id)) rc.chop(id);
	}
}
