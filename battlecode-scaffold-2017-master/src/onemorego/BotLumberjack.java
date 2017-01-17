package onemorego;

import battlecode.common.*;

public class BotLumberjack {
	static RobotController rc;

	static TreeInfo treeTarget;
	static RobotInfo enemyTarget;
	static MapLocation home;
	static Strategy strat;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotLumberjack.rc = rc;
		Util.reportDeath(rc);
		
		BulletInfo[] bullets = rc.senseNearbyBullets();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		
		if(enemies.length > 0) {
			strat = Strategy.OFFENSE;
		} else {
			strat = Strategy.LUMBERJACK;
		}
		
		//if(strat == null){
		//	float randStrat = (float)Math.random();
		//	if(randStrat < 0.0) strat = Strategy.LUMBERJACK;
		//	else strat = Strategy.OFFENSE;
		//}

		if(strat == Strategy.OFFENSE) {
			
			// let another lumberjack cut the tree
			if(treeTarget != null) {
				Comms.neutralTrees.push(rc, treeTarget);
				treeTarget = null;
			}
			
			// update target
			if(enemyTarget != null){
				if(!rc.canSenseRobot(enemyTarget.getID())) enemyTarget = null;
				else enemyTarget = rc.senseRobot(enemyTarget.getID());
			}

			// Find target
			if(enemyTarget == null){
				if(enemies.length == 0){
					Nav.explore(rc, bullets);
					return;
				}
				float dist = 200;
				for(int i = 0; i < enemies.length; i++){
					float newDist = enemies[i].getLocation().distanceTo(rc.getLocation());
					if(newDist < dist){
						dist = newDist;
						enemyTarget = enemies[i];
					}
				}
			}

			// get in range and kill
			float strikeRadius = 2 + enemyTarget.getType().bodyRadius;
			if(rc.getLocation().distanceTo(enemyTarget.getLocation()) <= strikeRadius && rc.senseNearbyRobots(strikeRadius, rc.getTeam()).length == 0){
				rc.strike();
			} else {
				Nav.pathTo(rc, enemyTarget.getLocation(), bullets);
			}
		}

		if(strat == Strategy.DEFENSE) {

		}

		if(strat == Strategy.LUMBERJACK){
			// Find a target
			if(treeTarget == null) {
				treeTarget = Comms.neutralTrees.pop(rc);
			}
			if(treeTarget == null) {
				TreeInfo[] trees = rc.senseNearbyTrees();
				if(trees.length == 0){
					Nav.explore(rc, bullets);
					return;
				}
				float dist = 200;
				for(int i = 0; i < trees.length; i++){
					float newDist = trees[i].getLocation().distanceTo(rc.getLocation());
					if(newDist < dist){
						dist = newDist;
						treeTarget = trees[i];
					}
				}
			}

			// Go to target
			if(!rc.canInteractWithTree(treeTarget.getID())){
				Nav.pathTo(rc, treeTarget.getLocation(), bullets);
			}

			// Chop target
			if(rc.canInteractWithTree(treeTarget.getID())){
				rc.chop(treeTarget.getID());
			}

			// Check if target is dead
			if(rc.canSenseLocation(treeTarget.getLocation()) && !rc.canSenseTree(treeTarget.getID())) {
				treeTarget = null;
			}
		}
	}
}