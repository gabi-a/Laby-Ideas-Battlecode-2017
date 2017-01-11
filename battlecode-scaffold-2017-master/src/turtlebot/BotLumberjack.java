package turtlebot;

import battlecode.common.*;

public class BotLumberjack {
	static RobotController rc;
	static TreeInfo target = null;
	static MapLocation homeLocation;
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
		
		if(homeLocation == null) {
			homeLocation = Comms.readHomeLocation(rc);
		}

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
					TreeInfo[] trees = new TreeInfo[0];
					trees = rc.senseNearbyTrees();
					if(trees.length == 0){
						Direction dir = new Direction((float)Math.random() * 2 * (float)Math.PI);
						while(!rc.canMove(dir))
							dir = new Direction((float)Math.random() * 2 * (float)Math.PI);
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
						return;
					}

					target = trees[tree_id];
					for(int i = 0; i < trees.length; i++){
						if(trees[i].getTeam() == Team.NEUTRAL && (trees[i].getContainedBullets() != 0 || trees[i].getContainedRobot() != null)){
							Comms.pushLowPriorityTree(rc, trees[i], 3);
							System.out.println("added tree to queue");
						}
					}
				}
			}
		}

		if(rc.canChop(target.getID())){
			rc.chop(target.getID());
			return;
		}
		if(rc.canMove(target.getLocation())){
			rc.move(target.getLocation());
		} else {
			MapLocation loc = new MapLocation(0,0);
			Direction dir = new Direction((float)Math.random() * 2 * (float)Math.PI);
			while(!rc.canMove(dir))
				dir = new Direction((float)Math.random() * 2 * (float)Math.PI);
			rc.move(dir);
		}
		if(rc.canChop(target.getID())) rc.chop(target.getID());

		// Stop trying to chop if the tree is dead
		if(rc.getLocation().distanceTo(target.getLocation()) < 7 && !rc.canSenseTree(target.getID())) target = null;
	}
}
