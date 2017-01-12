package turtlebot;

import battlecode.common.*;

public class BotLumberjack {
	static RobotController rc;
	static TreeInfo target = null;
	static TreeInfo oldTarget= null;
	static final int HOME_RADIUS= 20;
	
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
			// try and find a target
			target = Comms.popHighPriorityTree(rc);
			if(target != null) {
				// stay on current tree or switch to new high priority?
			} else {
				target = Comms.popLowPriorityTree(rc);
				if(target != null){

				} else {
					target = nearestTree();
					if(target == null && !rc.hasMoved()){
						Nav.explore(rc);
						return;
					}
				}
			}
		}

		if(target != null){
			cutTree();
			return;
		}
	}

	public static TreeInfo nearestTree() throws GameActionException {
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
			Nav.explore(rc);
			return null;
		}
		return trees[tree_id];
	}

	public static void cutTree() throws GameActionException{
		if(rc.canChop(target.getID())){
			rc.chop(target.getID());
		}
		if(!Nav.tryMove(rc, rc.getLocation().directionTo(target.getLocation()))){
			if(oldTarget == null) oldTarget = target;
			target = nearestTree();
			cutTree();
		}
		if(target == null) {
			return;
		}
		if(rc.canChop(target.getID())) rc.chop(target.getID());

		// Stop trying to chop if the tree is dead
		if(rc.getLocation().distanceTo(target.getLocation()) < 7 && !rc.canSenseTree(target.getID())){
			if(oldTarget != null){
				target = oldTarget;
				oldTarget = null;
			} else {
				target = null;
			}
		}
	}
}
