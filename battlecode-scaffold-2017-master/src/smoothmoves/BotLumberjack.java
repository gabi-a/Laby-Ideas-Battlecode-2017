package smoothmoves;

import battlecode.common.*;
import battlecode.schema.Action;

/*
 * TODO: Investigate bytecode usage issue
 * 
 */

public class BotLumberjack {
	static RobotController rc;
	
	static Team us = RobotPlayer.rc.getTeam();
	static Team them = us.opponent();
	
	public static void turn(RobotController rc) throws GameActionException {
		BotLumberjack.rc = rc;
		
		MapLocation myLocation = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
		RobotInfo[] allies = rc.senseNearbyRobots(-1, us);
		BulletInfo[] bullets = rc.senseNearbyBullets(4);
		TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
		TreeInfo bestTree = null;
		
		if(bullets.length == 0) {
			Util.updateMyPostion(rc);
		}
		
		/************* Determine where to move *******************/
		Direction moveDirection = null;
		float moveStride = RobotType.LUMBERJACK.strideRadius;
		
		/* If there are any bullets nearby, just dodge them and don't
		 * worry about going to the target 
		 */
		
		if(enemies.length > 0) {
			MapLocation closestEnemyLocation = enemies[0].location;
			MapLocation moveLocation = Nav.pathTo(rc, closestEnemyLocation, bullets);
			if(moveLocation != null) {
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceSquaredTo(moveLocation);
			}
		}
		
		else if(bullets.length > 0) {
			rc.setIndicatorDot(myLocation, 0, 255, 0);
			MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets);	
			if(moveLocation != null) {
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceTo(moveLocation);
			}
		} 
		/* 
		 * Otherwise move to target, trees, away from enemies
		 */
		else {
			MapLocation goalLocation = myLocation;
			
			// Move towards trees of low health or that contain bots
			if(trees.length > 0) {
				float score = 0;
				for(int i = 0; i < trees.length; i++){
					//System.out.println("A" + Clock.getBytecodeNum());
					float newScore = rateTree(trees[i]);
					if(newScore > score){
						score = newScore;
						goalLocation = trees[i].getLocation();
					}
				}
			} 
			rc.setIndicatorDot(goalLocation, 255, 0, 0);
			MapLocation closestToEnemyBase = Util.getClosestToEnemyBase(rc);
			MapLocation enemyBase = null;
			RobotInfo enemyBaseBot = Util.getBestPassiveEnemy(rc);
			if(enemyBaseBot != null) {
				enemyBase = enemyBaseBot.location;
			} else {
				enemyBase = rc.getInitialArchonLocations(them)[0];
			}
			rc.setIndicatorDot(enemyBase, 255, 0, 0);
			if(closestToEnemyBase != null && closestToEnemyBase != myLocation){
				goalLocation = goalLocation.add(myLocation.directionTo(closestToEnemyBase), 1f);
				rc.setIndicatorDot(closestToEnemyBase, 0, 255, 0);
			} 
			else if(enemyBase != null){
				goalLocation = goalLocation.add(myLocation.directionTo(enemyBase), 1f);
			}
			rc.setIndicatorDot(goalLocation, 0, 0, 255);
			
			//else {
			//	goalLocation = goalLocation.add(myLocation.directionTo(rc.getInitialArchonLocations(them)[0]), 2f);
			//}
			
			// Move away from ally bots
			if(allies.length > 0) {
				//System.out.println("B" + Clock.getBytecodeNum());
				for(int i = allies.length;i-->0;) {
					goalLocation = goalLocation.add(myLocation.directionTo(allies[i].getLocation()).opposite(), 0.2f);
				}
			}
			
			moveDirection = myLocation.directionTo(goalLocation);
			if(moveDirection != null) moveDirection = Nav.tryMove(rc, myLocation.directionTo(goalLocation), 10f, 24, bullets);
			moveStride = myLocation.distanceTo(goalLocation);
			
			// Rescale stride distance
			moveStride = Math.max(RobotType.LUMBERJACK.strideRadius, moveStride * RobotType.LUMBERJACK.strideRadius / (((trees.length == 0) ? 0.001f : 1f) + allies.length));

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
		
		/*
		 * If there are trees, chop 'em
		 */
		else if(trees.length > 0) {
			
			bestTree = trees[0];
			float lowestHealth = 1000f;
			for(int i = 0; i-->0;) {
				TreeInfo tree = trees[i];
				
				if(tree.getContainedRobot() != null && rc.canChop(tree.ID)) {
					bestTree = tree;
					break;
				}
				
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
				//System.err.format("\nI wanted to chop a tree but I couldn't :(\n");
			}
		}
		
		/************* Do action *********************************/
		
		/*
		 * All checks to see if this action is possible should already
		 * have taken place
		 * Note: we must do this before moving else we may move out of range
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
		
		/************* Do Move ***********************************/
		
		/*
		 * All checks to see if this move is possible should already
		 * have taken place
		 */
		if(moveDirection != null && rc.canMove(moveDirection, moveStride))
			rc.move(moveDirection, moveStride);
	}

	public static float rateTree(TreeInfo tree) throws GameActionException {
		return (7f-rc.getLocation().distanceTo(tree.getLocation())) + (tree.getContainedRobot() != null ? 3f: 0f) /* + health */;
	}
}
