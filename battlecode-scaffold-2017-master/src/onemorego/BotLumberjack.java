package onemorego;

import battlecode.common.*;

public class BotLumberjack {
	static RobotController rc;

	static TreeInfo treeTarget;
	static TreeInfo tempTreeTarget;
	static RobotInfo enemyTarget;
	//static MapLocation home;
	static Strategy strat;
	static boolean startupFlag = true;
	static int delegation;
	
	static Team enemyTeam = RobotPlayer.rc.getTeam().opponent();
	static MapLocation enemyArchonLocation = RobotPlayer.rc.getInitialArchonLocations(enemyTeam)[0];
	static MapLocation targetLocation = enemyArchonLocation;
	static boolean exploreFlag = false;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotLumberjack.rc = rc;
		Util.reportDeath(rc);
		
		BulletInfo[] bullets = rc.senseNearbyBullets();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		Util.communicateNearbyEnemies(rc, enemies);
		
		if(startupFlag) {
			delegation = Comms.lumberjackStack.pop(rc);
			startupFlag = false;
		}
		
		MapLocation commsTarget = Comms.readAttackLocation(rc, AttackGroup.B);
		if(commsTarget != null) {
			targetLocation = commsTarget;
		}
		
		if(enemies.length > 0 || delegation == 1) {
			strat = Strategy.OFFENSE;
		} else {
			strat = Strategy.LUMBERJACK;
		}
		
		if(strat == Strategy.OFFENSE) {
			rc.setIndicatorDot(rc.getLocation(), 255, 0, 0);
			rc.setIndicatorDot(targetLocation, 255, 179, 208);
			// let another lumberjack cut the tree
			if(treeTarget != null) {
				Comms.neutralTrees.push(rc, treeTarget);
				treeTarget = null;
			}
			
			enemyTarget = enemies.length > 0 ? enemies[0] : null;
			// Find target
			if(enemyTarget == null) {
				if(!exploreFlag) {
					if(!Nav.pathTo(rc, targetLocation, bullets)) {
						Nav.explore(rc, bullets);
					}
					if(rc.getLocation().distanceTo(targetLocation) < 5f) exploreFlag = true;
				} else {
					Nav.explore(rc, bullets);
				}
			}
			else {
				// get in range and kill
				float strikeRadius = 1f + enemyTarget.getType().bodyRadius;
				if(rc.getLocation().distanceTo(enemyTarget.getLocation()) <= strikeRadius && rc.senseNearbyRobots(strikeRadius, rc.getTeam()).length == 0){
					rc.strike();
				} 
				else {
					Nav.pathTo(rc, enemyTarget.getLocation(), bullets);
				}
			}
			
			// Do what a lumberjack does best
			if(tempTreeTarget == null) {
				TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
				if(trees.length == 0){
					if(!rc.hasMoved()) Nav.explore(rc, bullets);
					return;
				}
				tempTreeTarget = trees[0];
			}
			// Chop target
			if(rc.canChop(tempTreeTarget.getID())){
				rc.chop(tempTreeTarget.getID());
			}

			// Check if target is dead
			if(rc.canSenseLocation(tempTreeTarget.getLocation()) && !rc.canSenseTree(tempTreeTarget.getID())) {
				tempTreeTarget = null;
			}
			
		}

		if(strat == Strategy.DEFENSE) {
			rc.setIndicatorDot(rc.getLocation(), 0, 0, 255);

		}

		if(strat == Strategy.LUMBERJACK){
			rc.setIndicatorDot(rc.getLocation(), 0, 255, 0);
			// Find a target
			/*if(treeTarget == null) {
				treeTarget = Comms.neutralTrees.pop(rc);
			}*/
			
			if(treeTarget == null) {
				TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
				if(trees.length == 0){
					Nav.explore(rc, bullets);
					return;
				}
				treeTarget = trees[0];
			}
			
			// Go to target
			if(!rc.canInteractWithTree(treeTarget.getID())){
				Nav.pathTo(rc, treeTarget.getLocation(), bullets);
			}
			
			// Chop target
			if(rc.canChop(treeTarget.getID())){
				rc.chop(treeTarget.getID());
			}
			
			// Check if target is dead
			if(rc.canSenseLocation(treeTarget.getLocation()) && !rc.canSenseTree(treeTarget.getID())) {
				treeTarget = null;
			}
		}
	}
}
