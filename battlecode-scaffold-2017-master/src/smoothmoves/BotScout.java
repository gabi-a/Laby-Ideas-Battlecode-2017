package smoothmoves;
import battlecode.common.*;
import battlecode.schema.Action;

public class BotScout {
	static RobotController rc;
	
	static Team us = RobotPlayer.rc.getTeam();
	static Team them = us.opponent();
	
	static MapLocation myLocation;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotScout.rc = rc;

		//RobotInfo[] bots = rc.senseNearbyRobots();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
		TreeInfo[] trees = rc.senseNearbyTrees();
		BulletInfo[] bullets = rc.senseNearbyBullets();
		myLocation = rc.getLocation();
		
		Direction moveDirection = null;
		float moveStride = RobotType.SCOUT.strideRadius;

		Util.reportEnemyBots(rc, enemies);
		
		/************* Determine where to move *******************/

		if(bullets.length > 0) {
			System.out.println("RUN!");
			MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets);
			if(moveLocation != null) {
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceTo(moveLocation);
			}
		}
		
		else {
			MapLocation moveLocation = myLocation;
			if(trees.length > 0) {
				for(int i = trees.length;i-->0;) {
					moveLocation = moveLocation.add(myLocation.directionTo(trees[i].location), (trees[i].getContainedBullets()/10) * 1f/(myLocation.distanceTo(trees[i].location)+1f));
				}
				
				moveDirection = myLocation.directionTo(moveLocation);
				if(moveDirection != null) moveDirection = Nav.tryMove(rc, moveDirection, 5f, 24, bullets);
				
				if(myLocation.distanceTo(moveLocation) < 0.01f) {
					moveDirection = Nav.explore(rc, bullets);
				}
				
			} else {
				moveDirection = Nav.explore(rc, bullets);
			}
		}
		
		/************* Determine what action to take *************/
		Direction shootDirection = null;
		byte action = Action.DIE_EXCEPTION;
		
		TreeInfo treeToShake = null;
		
		if(trees.length > 0) {
			for(int i = 0; i < trees.length;i++) {
				if(trees[i].getContainedBullets() > 0 && rc.canShake(trees[i].ID)) {
					action = Action.SHAKE_TREE;
					treeToShake = trees[i];
					break;
				}
			}
		}
		
		if(action != Action.SHAKE_TREE && enemies.length > 0) {
			if(rc.canFireSingleShot()) {
				action = Action.FIRE;
				shootDirection = myLocation.directionTo(enemies[0].location);

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
		case Action.FIRE:
			if(rc.canFireSingleShot()) rc.fireSingleShot(shootDirection);
			break;
		case Action.SHAKE_TREE:
			rc.shake(treeToShake.ID);
			break;
		default:
			break;
		}
	}
}
