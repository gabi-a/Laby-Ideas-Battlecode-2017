package turtlebot;

import battlecode.common.*;

public class BotLumberjack {
	static RobotController rc;
	static TreeInfo target = null;
	static TreeInfo path_target = null;
	static final int HOME_RADIUS= 20;
	static int moves_blocked = 0;
	
	static boolean toldArchonsImDead = false;
	
	public static void turn(RobotController rc) throws GameActionException {
		// TODO: Find trees, kill trees
		BotLumberjack.rc = rc;
		
		if(!toldArchonsImDead && rc.getHealth() < 10) {
			int lumberjacks = Comms.readNumLumberjacks(rc) - 1;
			Comms.writeNumLumberjacks(rc, lumberjacks);
			toldArchonsImDead = true;
		}
		
		MapLocation myLocation = rc.getLocation();

		if(target == null){
			BotLumberjack.rc = rc;
			Comms comms = new Comms();
			target = comms.popHighPriorityTree(rc);
			if(target != null) {
				// stay on current tree or switch to new high priority?
			} else {
				target = comms.popLowPriorityTree(rc);
				if(target != null){

				} else {
					target = nearestTree(rc);
					if(target == null){
						MapLocation loc = new MapLocation(0,0);
						Direction dir = rc.getLocation().directionTo(loc).opposite();
						while(!rc.canMove(dir))
							dir = rc.getLocation().directionTo(loc).rotateLeftDegrees(90+(float)Math.random()*180);
						rc.move(dir);
						return;
					}
				}
			}
		}

		if(path_target != null){
				if(rc.canChop(path_target.getID())) rc.chop(path_target.getID());

				// Stop trying to chop if the tree is dead
				if(rc.getLocation().distanceTo(path_target.getLocation()) < 7 && !rc.canSenseTree(path_target.getID())) path_target = null;
		}
		if(rc.canChop(target.getID())){
			rc.chop(target.getID());
			return;
		}
		if(rc.canMove(target.getLocation())){
			rc.move(target.getLocation());
		} else {
			moves_blocked++;
			if(moves_blocked < 10){
				MapLocation loc = new MapLocation(0,0);
				Direction dir = new Direction((float)Math.random() * 2 * (float)Math.PI);
				while(!rc.canMove(dir))
					dir = new Direction((float)Math.random() * 2 * (float)Math.PI);
				rc.move(dir);
			} else {
				path_target = nearestTree(rc);
				moves_blocked = 0;
			}
			if(path_target != null){
				if(rc.canChop(path_target.getID())) rc.chop(path_target.getID());

				// Stop trying to chop if the tree is dead
				if(rc.getLocation().distanceTo(path_target.getLocation()) < 7 && !rc.canSenseTree(path_target.getID())) path_target = null;
			}
		}
		if(rc.canChop(target.getID())) rc.chop(target.getID());

		// Stop trying to chop if the tree is dead
		if(rc.getLocation().distanceTo(target.getLocation()) < 7 && !rc.canSenseTree(target.getID())) target = null;
	}

	public static TreeInfo nearestTree(RobotController rc) throws GameActionException {
		TreeInfo[] trees = new TreeInfo[0];
		trees = rc.senseNearbyTrees();
		int tree_id = -1;
		float dist = 200;
		for(int i = 0; i < trees.length; i++){
			float new_dist = rc.getLocation().distanceTo(trees[i].getLocation());
			if(trees[i].getTeam() == Team.NEUTRAL && new_dist < dist){
				tree_id = i;
				dist = new_dist;
			}
		}
		if(trees.length == 0 || tree_id == -1){
			Direction dir = new Direction((float)Math.random() * 2 * (float)Math.PI);
			while(!rc.canMove(dir))
				dir = new Direction((float)Math.random() * 2 * (float)Math.PI);
			rc.move(dir);
			return null;
		}
		return trees[tree_id];
	}
}
