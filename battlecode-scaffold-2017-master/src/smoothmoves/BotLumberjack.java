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
		BulletInfo[] bullets = rc.senseNearbyBullets();
		TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
		TreeInfo bestTree = null;
		
		/************* Determine where to move *******************/
		Direction moveDirection = null;
		float moveStride = RobotType.LUMBERJACK.strideRadius;
		
		/* If there are any bullets nearby, just dodge them and don't
		 * worry about going to the target 
		 */
		if(bullets.length > 0) {
			rc.setIndicatorDot(myLocation, 0, 255, 0);
			MapLocation moveLocation = Nav.awayFromBulletsAndTrees(rc, myLocation, bullets, trees);	
			moveDirection = myLocation.directionTo(moveLocation);
			moveStride = myLocation.distanceTo(moveLocation);
		} 
		
		/* 
		 * Otherwise move to target, trees, away from enemies
		 */
		else {
			
			MapLocation goalLocation = myLocation;
			
			// Move towards trees of low health or that contain bots
			if(trees.length > 0) {
				for(int i = trees.length;i-->0;) {
					goalLocation = goalLocation.add(myLocation.directionTo(trees[i].location), Math.max(1f, (trees[i].getContainedRobot() != null ? 1f : 0f) + 1f/trees[i].health));
				}
			}
			
			// Move away from enemies
			if(enemies.length > 0) {
				for(int i = enemies.length;i-->0;) {
					goalLocation = goalLocation.add(myLocation.directionTo(enemies[i].getLocation()).opposite());
				}
			}
			
			moveDirection = myLocation.directionTo(goalLocation);
			moveStride = myLocation.distanceTo(goalLocation);
			
			// Rescale stride distance
			moveStride = moveStride * RobotType.LUMBERJACK.strideRadius / (trees.length + enemies.length);
			
		}
		
		/*
		 * If there are enemies then get in strike range and we haven't moved yet
		 */
		/*
		if(enemies.length > 0 && ((moveDirection != null && !rc.canMove(moveDirection, moveStride)) || moveDirection == null)){
			// Move towards scouts, gardeners and archons, away from other enemies
			RobotInfo bestCloseEnemy = enemies[0];
			int enemiesToRunFrom = 0;
			MapLocation goalLocation = myLocation;
			for(int i = enemies.length;i-->0;) {
				RobotInfo enemy = enemies[i];
				
				// Attack the weaklings
				if(enemy.getType() == RobotType.GARDENER || enemy.getType() == RobotType.ARCHON) {
					bestCloseEnemy = enemy;
				} 
				
				// If there is a dangerous enemy and about to be in stride radius, run away!
				else if(myLocation.distanceTo(enemy.location) < 5f){
					goalLocation = goalLocation.add(myLocation.directionTo(enemy.getLocation()).opposite());
					moveStride = Math.min(RobotType.LUMBERJACK.strideRadius, Math.abs(4.999f - myLocation.distanceTo(enemy.getLocation())));
					enemiesToRunFrom++;
				}
			}
			rc.setIndicatorDot(goalLocation, 0, 0, 150);
			
			// Be a wuss
			if(enemiesToRunFrom > 0) {
				moveDirection = myLocation.directionTo(goalLocation);
			}
			else {
				moveDirection = myLocation.directionTo(bestCloseEnemy.location);
				moveStride = myLocation.distanceTo(bestCloseEnemy.location);
			}
			
		}
		*/
		/*
		 * If there are trees around and we haven't moved yet, head to them
		 */
		/*
		if(trees.length > 0 && ((moveDirection != null && !rc.canMove(moveDirection, moveStride)) || moveDirection == null)) {
			bestTree = trees[0];
			float lowestHealth = 1000f;
			for(int i = 0; i-->0;) {
				TreeInfo tree = trees[i];
				if(tree.health < lowestHealth) {
					lowestHealth = tree.health;
					bestTree = tree;
				}
			}
			moveDirection = myLocation.directionTo(bestTree.location);
			moveDirection = Nav.tryMove(rc, moveDirection, 5f, 12, bullets);
			moveStride = 0.9f*(myLocation.distanceTo(bestTree.location) - bestTree.radius - RobotType.LUMBERJACK.bodyRadius);
			
		}
		*/
		/*
		 * Otherwise go to the enemy?
		 */
		/*
		else if(trees.length > 0){
			
		}
		*/
		/************* Determine what action to take *************/
		byte action = Action.DIE_EXCEPTION;
		int chopID = 0;
		
		/*
		 * If there are enemies in strike range, strike
		 */
		if(enemies.length > 0 && enemies[0].location.distanceTo(myLocation) < enemies[0].getRadius() + RobotType.LUMBERJACK.bodyRadius + 1f) {
			action = Action.LUMBERJACK_STRIKE;
		}
		
		/*
		 * If there are trees, chop 'em
		 */
		else if(trees.length > 0) {
			
			bestTree = trees[0];
			float lowestHealth = 1000f;
			for(int i = 0; i-->0;) {
				TreeInfo tree = trees[i];
				if(tree.health < lowestHealth) {
					lowestHealth = tree.health;
					bestTree = tree;
				}
			}
			
			if(bestTree != null && rc.canChop(bestTree.ID)) {
				chopID = bestTree.ID;
				action = Action.CHOP;
			} else if(rc.canChop(trees[0].ID)) {
				chopID = trees[0].ID;
				action = Action.CHOP;
			} else {
				System.err.format("\nI wanted to chop a tree but I couldn't :(\n");
			}
		}
		
		/************* Do Move ***********************************/
		
		/*
		 * All checks to see if this move is possible should already
		 * have taken place
		 */
		if(moveDirection != null && rc.canMove(moveDirection, moveStride))
			rc.move(moveDirection, moveStride);
		
		/************* Do action *********************************/
		
		/*
		 * All checks to see if this action is possible should already
		 * have taken place
		 */
		
		switch(action) {
		case Action.CHOP:
			rc.chop(chopID);
			break;
		case Action.LUMBERJACK_STRIKE:
			rc.strike();
			break;
		default:
			break;
		}
	}
}
