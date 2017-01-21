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
		RobotInfo[] bots = rc.senseNearbyRobots();
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
			MapLocation moveLocation = Nav.awayFromBulletsTreesAndBots(rc, myLocation, bullets, trees, bots);	
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
					goalLocation = goalLocation.add(myLocation.directionTo(trees[i].location), Math.max(0.5f, (trees[i].getContainedRobot() != null ? 1f : 0f) + 1f/(trees[i].health + 2f)));
				}
			}
			
			// Move away from bots
			if(bots.length > 0) {
				for(int i = bots.length;i-->0;) {
					goalLocation = goalLocation.add(myLocation.directionTo(bots[i].getLocation()).opposite(), (bots[i].team == us) ? 1f : 3f);
				}
			}
			moveDirection = myLocation.directionTo(goalLocation);
			if(moveDirection != null) moveDirection = Nav.tryMove(rc, myLocation.directionTo(goalLocation), 10f, 24, bullets);
			moveStride = myLocation.distanceTo(goalLocation);
			
			// Rescale stride distance
			moveStride = moveStride * RobotType.LUMBERJACK.strideRadius / (trees.length + bots.length);
			
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
