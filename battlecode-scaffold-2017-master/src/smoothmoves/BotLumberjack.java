package smoothmoves;

import battlecode.common.*;
import battlecode.schema.Action;

public class BotLumberjack {
	static RobotController rc;
	
	static Team us = RobotPlayer.rc.getTeam();
	static Team them = us.opponent();
	
	public static void turn(RobotController rc) throws GameActionException {
		BotLumberjack.rc = rc;
		
		MapLocation myLocation = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
		BulletInfo[] bullets = rc.senseNearbyBullets(4f);
		TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
		
		/************* Determine where to move *******************/
		Direction moveDirection = Direction.NORTH;
		float moveStride = RobotType.LUMBERJACK.strideRadius;
		
		/* If there are any bullets nearby, just dodge them and don't
		 * worry about going to the target 
		 */
		if(bullets.length > 0) {
			moveDirection = Nav.awayFromBullets(myLocation, bullets);
		}
		
		/*
		 * If there are enemies then get in strike range
		 */
		else if(enemies.length > 0){
			
			// Move towards scouts, gardeners and archons, away from other enemies
			RobotInfo bestCloseEnemy = enemies[0];
			int enemiesToRunFrom = 0;
			MapLocation goalLocation = myLocation;
			for(int i = enemies.length;i-->0;) {
				RobotInfo enemy = enemies[i];
				
				// Attack the weaklings
				if(enemy.getType() == RobotType.SCOUT || enemy.getType() == RobotType.GARDENER || enemy.getType() == RobotType.ARCHON) {
					bestCloseEnemy = enemy;
				} 
				
				// If there is a dangerous enemy within 5 units, run away!
				else if (myLocation.distanceTo(enemy.getLocation()) < 5f ){
					goalLocation.add(myLocation.directionTo(enemy.getLocation()).opposite());
					moveStride = 5f - myLocation.distanceTo(enemy.getLocation());
					enemiesToRunFrom++;
				}
			}
			
			// Don't be a wuss
			if(enemiesToRunFrom > 1) {
				moveDirection = myLocation.directionTo(goalLocation);
			}
			else {
				moveDirection = myLocation.directionTo(bestCloseEnemy.location);
				moveStride = myLocation.distanceTo(bestCloseEnemy.location);
			}
			
		}
		
		/*
		 * If there are trees around chop them
		 */
		else if(trees.length > 0){
			
		}
		
		/*
		 * Otherwise go to the enemy?
		 */
		else if(trees.length > 0){
			
		}
		
		/************* Determine what action to take *************/
		byte action = Action.DIE_EXCEPTION;
		int chopID = 0;
		
		/*
		 * If there are enemies in strike range, strike
		 */
		if(enemies.length > 0 && enemies[0].location.distanceTo(myLocation) < enemies[0].getRadius() + RobotType.LUMBERJACK.bodyRadius + 1f) {
			action = Action.LUMBERJACK_STRIKE;
		}
		
		/************* Do Move ***********************************/
		if(rc.canMove(moveDirection, moveStride)) {
			rc.move(moveDirection, moveStride);
		}
		
		/************* Do action *********************************/
		switch(action) {
		case Action.CHOP:
			if(rc.canChop(chopID)) rc.chop(chopID);
			break;
		case Action.LUMBERJACK_STRIKE:
			if(rc.canStrike()) rc.strike();
			break;
		default:
			break;
		}
	}
}
