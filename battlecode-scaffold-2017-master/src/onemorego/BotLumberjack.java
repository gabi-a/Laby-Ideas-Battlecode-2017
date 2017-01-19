package onemorego;

import battlecode.common.*;

public class BotLumberjack {
	static RobotController rc;

	static TreeInfo treeTarget;
	static TreeInfo tempTreeTarget;
	static RobotInfo enemyTarget;
	//static MapLocation home;
	static Strategy strategy;
	static boolean startupFlag = true;
	static int delegation;
	
	static Team enemyTeam = RobotPlayer.rc.getTeam().opponent();
	static MapLocation enemyArchonLocation = RobotPlayer.rc.getInitialArchonLocations(enemyTeam)[0];
	static MapLocation targetLocation = enemyArchonLocation;
	static boolean exploreFlag = false;
	
	public static void turn(RobotController rc) throws GameActionException {
		
		BotLumberjack.rc = rc;
		Util.reportIfDead(rc);
		
		BulletInfo[] bullets = rc.senseNearbyBullets();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
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
			strategy = Strategy.OFFENSE;
		} else {
			strategy = Strategy.LUMBERJACK;
		}
		
		switch(strategy) {
		
			case OFFENSE:
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
						if(rc.getLocation().distanceTo(targetLocation) < 5f) {
							exploreFlag = true;
						}
					} else {
						Nav.explore(rc, bullets);
					}
				}
				else {
					// get in range and kill
					float strikeRadius = RobotType.LUMBERJACK.bodyRadius + enemyTarget.getType().bodyRadius + 0.999f;
					if(rc.getLocation().distanceTo(enemyTarget.getLocation()) <= strikeRadius && rc.senseNearbyRobots(strikeRadius, rc.getTeam()).length <= 1){
						rc.strike();
					} 
					else if(rc.getLocation().distanceTo(enemyTarget.getLocation()) < 4f) {
						Nav.tryPrecisionMove(rc, rc.getLocation().directionTo(enemyTarget.getLocation()), rc.getLocation().distanceTo(enemyTarget.getLocation()), bullets);
					} 
					else {
						Nav.pathTo(rc, enemyTarget.getLocation(), bullets);
					}
				}

				// Do what a lumberjack does best
				if(tempTreeTarget == null && trees.length > 0) {
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
				break;
			
			case DEFENSE:
				rc.setIndicatorDot(rc.getLocation(), 0, 0, 255);
				break;
			
			case LUMBERJACK:
				rc.setIndicatorDot(rc.getLocation(), 0, 255, 0);

				if(treeTarget == null) {
					if(trees.length == 0){
						Nav.tryMove(rc, rc.getLocation().directionTo(targetLocation),bullets);
					}
					else {
						treeTarget = trees[0];
					}
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

				if(!rc.hasMoved() && !rc.hasAttacked()) {
					if(!Nav.explore(rc, bullets))
						Nav.pathTo(rc, targetLocation, bullets);
				}
				break;

		}
	}
}
