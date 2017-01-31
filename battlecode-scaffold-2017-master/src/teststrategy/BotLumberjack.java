package teststrategy;

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

	static MapLocation enemyBase = null;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotLumberjack.rc = rc;
		
		MapLocation myLocation = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
		RobotInfo[] allies = rc.senseNearbyRobots(-1, us);
		BulletInfo[] bullets = rc.senseNearbyBullets(4);
		TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
		TreeInfo bestTree = null;
		float moveTreeRadius = 0f;
		boolean treeContainsRobot = false;
		
		Util.shakeIfAble(rc);
		
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
			MapLocation moveLocation = Nav.pathTo(rc, closestEnemyLocation);
			if(moveLocation != null && moveLocation != myLocation) {
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceSquaredTo(moveLocation);
			}
		}
		
		
		if(moveDirection == null && bullets.length > 0) {
			rc.setIndicatorDot(myLocation, 0, 255, 0);
			MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets);	
			if(moveLocation != null && moveLocation != myLocation) {
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceTo(moveLocation);
			}
		} 
		/* 
		 * Otherwise move to target, trees, away from enemies
		 */
		if(moveDirection == null) {
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
						moveTreeRadius = trees[i].radius;
						treeContainsRobot = (trees[i].containedRobot != null); 
					}
				}
			} 
			//rc.setIndicatorDot(goalLocation, 255, 0, 0);
			MapLocation closestToEnemyBase = Util.getClosestToEnemyBase(rc);
			RobotInfo enemyBaseBot = Util.getBestPassiveEnemy(rc);
			if(enemyBaseBot != null) {
				enemyBase = enemyBaseBot.location;
			} else {
				enemyBase = rc.getInitialArchonLocations(them)[0];
			}
			//rc.setIndicatorDot(enemyBase, 0, 255, 0);
			boolean gardenerNearby = false;
			if(allies.length > 0) {
				for(int i = allies.length;i-->0;) {
					if(allies[i].getType() == RobotType.GARDENER) {
						gardenerNearby = true;
						rc.setIndicatorDot(allies[i].location, 0, 255, 255);
						break;
					}
				}
			}
			rc.setIndicatorDot(goalLocation, 255, 255, 0);
			
			if(!treeContainsRobot && !(goalLocation != myLocation && trees.length>0 && gardenerNearby)) {
				
				if(enemyBase != null){
						goalLocation = enemyBase;
				}
				 
				if(closestToEnemyBase != null && closestToEnemyBase != myLocation){
					goalLocation = goalLocation.add(myLocation.directionTo(closestToEnemyBase), 10f);
					rc.setIndicatorDot(closestToEnemyBase, 0, 255, 0);
				} 
				
				if(allies.length > 0) {
					for(int i = allies.length;i-->0;) {
							goalLocation = goalLocation.add(myLocation.directionTo(allies[i].getLocation()).opposite(), 1f);
				
					}
				}
			}
			rc.setIndicatorDot(goalLocation, 255, 0, 0);
			MapLocation moveLocation = Nav.pathTo(rc, goalLocation, moveTreeRadius);
			
			if(moveLocation != null) {
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceTo(goalLocation);
			
				// Rescale stride distance
				moveStride = Math.max(RobotType.LUMBERJACK.strideRadius, moveStride * RobotType.LUMBERJACK.strideRadius / (((trees.length == 0) ? 0.001f : 1f) + allies.length));
			}
		}
		
		if(moveDirection == null && trees.length == 0) {
			if(enemyBase == null) {
				RobotInfo somewhereToGo = Util.getBestPassiveEnemy(rc);
				MapLocation goalLocation = null;
				if(somewhereToGo != null) goalLocation = somewhereToGo.location;
				MapLocation moveLocation = Nav.pathTo(rc, goalLocation);
				if(moveLocation != null) moveDirection = myLocation.directionTo(moveLocation);
			}
		}
		
		System.out.println(moveDirection);
		
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
				
				if(tree.health < lowestHealth && rc.canChop(tree.ID)) {
					lowestHealth = tree.health;
					bestTree = tree;
				}
			}
			
			if(bestTree != null && rc.canChop(bestTree.ID)) {
				chopID = bestTree.ID;
				action = Action.CHOP;
			} else {
				for(int i = trees.length; i-->0;) {
					if(rc.canChop(trees[i].ID))  {
						chopID = trees[i].ID;
						action = Action.CHOP;
						break;
					}
				}
			}
			/*
			else {
				rc.setIndicatorDot(bestTree.location, 0, 255, 0);
				System.err.format("\nI wanted to chop a tree but I couldn't :(\n");
			}
			*/
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
