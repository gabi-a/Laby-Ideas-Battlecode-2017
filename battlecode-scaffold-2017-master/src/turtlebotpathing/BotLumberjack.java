package turtlebotpathing;

import battlecode.common.*;

public class BotLumberjack {
	static RobotController rc;
	static TreeInfo target = null;
	static TreeInfo oldTarget= null;
	static final int HOME_RADIUS= 20;
	
	static boolean toldArchonsImDead = false;
	
	static boolean arrivedAtEnemy = false;
	static boolean broadcastArrival = false;
	
	
	static int lastChopID = 0;
	
	public static void turn(RobotController rc) throws GameActionException {
		// TODO: Find trees, kill trees
		BotLumberjack.rc = rc;
		
		Team enemyTeam = rc.getTeam().opponent();
		MapLocation myLocation = rc.getLocation();
		
		MapLocation moveTarget = Comms.readAttackLocation(rc);
		
		if(target == null) {
			target = Comms.popHighPriorityTree(rc);
			if(target == null) {
				target = Comms.popLowPriorityTree(rc);
			}
		}
		
		// Action
		boolean choppingWood = false;
        RobotInfo[] enemiesInStrikeRange = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemyTeam);
        if(enemiesInStrikeRange.length > 0 && rc.canStrike()) {
            rc.strike();
        } else {
            if(target != null && rc.canSenseLocation(target.getLocation()) && !rc.canSenseTree(target.getID())) {
                target = null;
            }
            
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
            if(rc.canChop(lastChopID)) {
                rc.chop(lastChopID);
                choppingWood = true;
            } else {
	            for(int i = nearbyTrees.length;i-->0;) {
	                if(rc.canChop(nearbyTrees[i].ID)) {
	                    rc.chop(nearbyTrees[i].ID);
	                    lastChopID = nearbyTrees[i].ID;
	                    choppingWood = true;
	                    break;
	                }
	            }
            }
        }
        
		// Movement
		boolean moved = false;
		moved = Nav.avoidBullets(rc, myLocation);
		
		RobotInfo[] enemies = rc.senseNearbyRobots(RobotType.LUMBERJACK.sensorRadius, enemyTeam);
		if(!moved) {
			if(enemies.length > 0) {
				RobotInfo closestEnemy = getClosestEnemyBot(rc, enemies);
				moved = Nav.pathTo(rc, closestEnemy.getLocation());
				System.out.format("\n1 moved: %b", moved);
			} else {
				if(moveTarget != null) {
					moved = Nav.lumberjackPathTo(rc, moveTarget, choppingWood);
				}
				if(!moved) {
					if(target != null) {
						moved = Nav.pathTo(rc, target.getLocation());
						System.out.format("\n2 moved: %b", moved);
					} else {
						TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
						if(nearbyTrees.length > 0) {
							TreeInfo nearbyTree = nearbyTrees[0];
							moved = Nav.pathTo(rc, nearbyTree.getLocation());
							System.out.format("\n3 moved: %b", moved);
						}
						if(!moved) {
							moved = Nav.explore(rc);
							System.out.format("\n4 moved: %b", moved);
						}
					}
				}
				
				if(!moved) {
					moved = Nav.explore(rc);
					System.out.format("\n5 moved: %b", moved);
				}
			}
		}
    	//if (!moved) //System.out.println("\nI'm Stuck");
    	
		
		/*
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
		*/
	}
	/*
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
		if(target != null && rc.canChop(target.getID())){
			rc.chop(target.getID());
		}
		if(!Nav.tryMove(rc, rc.getLocation().directionTo(target.getLocation()))){
			if(oldTarget == null) oldTarget = target;
			target = nearestTree();
			//cutTree();
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
	*/
	public static RobotInfo getClosestEnemyBot(RobotController rc, RobotInfo[] bots) throws GameActionException {
		RobotInfo closestBot = null;
		RobotInfo bot;
		float closestDist = 100;
		MapLocation myLocation = rc.getLocation();
		for(int i = bots.length;i-->0;) {
			bot = bots[i];
			
			if(bot.getType() == RobotType.GARDENER || bot.getType() == RobotType.ARCHON) {
				Comms.writeFoundEnemy(rc);
				Comms.writeAttackEnemy(rc, bot.getLocation(), bot.getID());
			}

			if(bot.getID() == Comms.readAttackID(rc)) {
				if(bot.getHealth() < 20f) {
					Comms.clearAttackEnemy(rc);
				}
			}
			
			float dist = myLocation.distanceTo(bot.getLocation());
			if(dist < closestDist) {
				closestDist = dist;
				closestBot = bot;
			}
		}
		return closestBot;
	}
}
