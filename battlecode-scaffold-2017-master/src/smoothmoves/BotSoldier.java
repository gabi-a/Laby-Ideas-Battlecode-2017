package smoothmoves;
import battlecode.common.*;
import battlecode.schema.Action;

public class BotSoldier {
	static RobotController rc;
	
	static Team us = RobotPlayer.rc.getTeam();
	static Team them = us.opponent();
	
	static boolean trapped = false;
	static RobotInfo trackedEnemy;
	static float fireOffsetDegrees = 30f;
	static int shootCooldown = 10;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotSoldier.rc = rc;
		
		Direction shootDirection = null;
		Direction moveDirection = null;
		float moveStride = RobotType.SOLDIER.strideRadius;
		
		BulletInfo[] bullets = rc.senseNearbyBullets();
		TreeInfo[] trees = rc.senseNearbyTrees();
		MapLocation myLocation = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
		
		Util.reportEnemyBots(rc, enemies);
		
		/************* Determine where to move *******************/

		//if(bullets.length > 0) {
		//	MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets, trees);	
		//	moveDirection = myLocation.directionTo(moveLocation);
		//	moveStride = myLocation.distanceTo(moveLocation);
		//}
		
		if(enemies.length > 0 && (moveDirection == null || moveDirection != null && !rc.canMove(moveDirection,moveStride))) {
			RobotInfo closestEnemy = enemies[0];
			if(closestEnemy.type == RobotType.LUMBERJACK) {
				if(myLocation.distanceTo(closestEnemy.location) < 4f) {
					moveDirection = closestEnemy.location.directionTo(myLocation);
				} else if(myLocation.distanceTo(closestEnemy.location) > 5f) {
					moveDirection = myLocation.directionTo(closestEnemy.location);
					moveStride = myLocation.distanceTo(closestEnemy.location) - 4.5f;
				}
			} else {
				moveDirection = myLocation.directionTo(closestEnemy.location);
				moveStride = myLocation.distanceTo(closestEnemy.location) - closestEnemy.getRadius() - RobotType.SOLDIER.bodyRadius;
			}
			if(moveDirection != null) moveDirection = Nav.tryMove(rc, moveDirection, 5f, 24, bullets);
			
		} else {
			
			RobotInfo[] enemyGardeners = Comms.enemyGardenersArray.arrayBots(rc);
			moveDirection = Nav.tryMove(rc, myLocation.directionTo(rc.getInitialArchonLocations(them)[0]), 5f, 24, bullets);
			for(int i = enemyGardeners.length;i-->0;) {
				if(enemyGardeners[i] != null) {
					moveDirection = Nav.tryMove(rc, myLocation.directionTo(enemyGardeners[i].location), 5f, 24, bullets);
				}
			}
			
		}
		
		
		/************* Determine what action to take *************/
		byte action = Action.DIE_EXCEPTION;
		
		if(enemies.length > 0) {
			
			if(trackedEnemy == null) {
				trackedEnemy = enemies[0];
			}
			
			// We have a lock!
			if(enemies[0].ID == trackedEnemy.ID) {
				
				float H = myLocation.distanceTo(enemies[0].location);
				float d = myLocation.distanceTo(trackedEnemy.location);
				float theta = myLocation.directionTo(enemies[0].location).radiansBetween(myLocation.directionTo(trackedEnemy.location));
				float lateralMovement = Math.abs((float) (H * Math.sin(theta)));
				
				rc.setIndicatorDot(trackedEnemy.location, 255, 0, 0);
				rc.setIndicatorDot(enemies[0].location, 255, 0, 0);
				
				// If not moving laterally relative to us, fire at will!
				if(lateralMovement < 0.5f) {
					rc.setIndicatorDot(myLocation, 0, 255, 0);
					shootDirection  = myLocation.directionTo(enemies[0].location);
					action = Action.FIRE_PENTAD;
				}
				
				// Otherwise do a bit of cheeky herding
				else {
					rc.setIndicatorDot(myLocation, 0, 0, 255);
					fireOffsetDegrees = -fireOffsetDegrees;
					shootDirection  = myLocation.directionTo(enemies[0].location).rotateLeftDegrees(fireOffsetDegrees);
					if(shootCooldown <= 0) {
						action = Action.FIRE;
					}
				}
				
			}
			
			else {
				
			}
			
			trackedEnemy = enemies[0];
			
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
		case Action.FIRE_PENTAD:
			if(rc.canFirePentadShot()) {
				rc.firePentadShot(shootDirection);
				shootCooldown = 15;
				break;
			}
		case Action.FIRE:
			if(rc.canFireSingleShot()) {
				rc.fireSingleShot(shootDirection);
				shootCooldown = 10;
				break;
			}
		default:
			shootCooldown--;
			break;
		}
	}
}
