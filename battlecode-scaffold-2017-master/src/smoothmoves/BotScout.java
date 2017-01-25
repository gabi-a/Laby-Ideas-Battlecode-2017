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
		RobotInfo[] enemies = rc.senseNearbyRobots(5f, them);
		TreeInfo[] trees = rc.senseNearbyTrees();
		BulletInfo[] bullets = rc.senseNearbyBullets();
		myLocation = rc.getLocation();
		
		Direction moveDirection = null;
		float moveStride = RobotType.SCOUT.strideRadius;

		Util.reportEnemyBots(rc, enemies);
		
		/************* Determine where to move *******************/

		boolean dodgeBullets = false;
		
		if(bullets.length > 0 || (enemies.length > 0 && enemies[0].type == RobotType.LUMBERJACK && enemies[0].location.distanceTo(myLocation) < 4f)) {
			
			boolean hideInTree = false;
			
			// Hide in a tree if possible
			if(trees.length > 0) {
				TreeInfo bestTree = null;
				for(int i = trees.length; i-->0;) {
					if(trees[i].radius > 1f) {
						bestTree = trees[i];
					}
				}
				if(bestTree != null && bestTree.location.distanceTo(myLocation) < 5f) {
					MapLocation moveLocation = Nav.pathTo(rc, bestTree.location, bullets);
					if(moveLocation != null) {
						moveDirection = myLocation.directionTo(moveLocation);
						moveStride = myLocation.distanceTo(moveLocation);
						hideInTree = moveDirection != null && rc.canMove(moveDirection, moveStride);
						dodgeBullets = hideInTree;
					}
				}
			}
			
			// Otherwise dodge bullets
			if(!hideInTree && bullets.length > 0) {
				MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets, enemies);
				if(moveLocation != null) {
					moveDirection = myLocation.directionTo(moveLocation);
					moveStride = myLocation.distanceTo(moveLocation);
					dodgeBullets = moveDirection != null && rc.canMove(moveDirection, moveStride);
				}
			}
		}
		
		if(!dodgeBullets) {
			
			boolean attackGardener = false;
			
			if(enemies.length > 0) {
				RobotInfo enemyGardener = Util.getGardenerAndAllPassive(enemies);
				if(enemyGardener != null && (enemyGardener.location.distanceTo(myLocation) > 3f || !Util.goodToShootNotTrees(rc, myLocation, enemyGardener))) {
					MapLocation moveLocation = Nav.pathTo(rc, enemyGardener.location, bullets);
					if(moveLocation != null) {
						moveDirection = myLocation.directionTo(moveLocation);
						moveStride = myLocation.distanceTo(moveLocation);
						attackGardener = true;
					}
				}
			}
			
			if(!attackGardener) {
				MapLocation moveLocation = applyTreeGravity(myLocation, trees);
				moveDirection = myLocation.directionTo(moveLocation);
				if(moveDirection != null && myLocation.distanceTo(moveLocation) > 0.01f) {
					moveDirection = Nav.tryMove(rc, moveDirection, 5f, 24, bullets);
				}
				else {
					moveDirection = Nav.explore(rc, bullets);
				}
			}
		}
		
		/************* Do Move ***********************************/
		
		/*
		 * All checks to see if this move is possible should already
		 * have taken place
		 */
		if(moveDirection != null && rc.canMove(moveDirection, moveStride))
			rc.move(moveDirection, moveStride);
		
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
		
		if(action != Action.SHAKE_TREE && enemies.length > 0 /*&& enemies[0].type == RobotType.SCOUT || enemies[0].type == RobotType.GARDENER*/) {
			if(rc.canFireSingleShot()) {
				action = Action.FIRE;
				shootDirection = myLocation.directionTo(enemies[0].location);
			}
		}
		
		/************* Do post move action *********************************/
		
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
	
	private static MapLocation applyTreeGravity(MapLocation myLocation, TreeInfo[] trees) {
		MapLocation moveLocation = myLocation;
		if(trees.length == 0) {
			return moveLocation;
		}
		else {
			for(int i = trees.length;i-->0;) {
				moveLocation = moveLocation.add(myLocation.directionTo(trees[i].location), (trees[i].getContainedBullets()/10) * 1f/(myLocation.distanceTo(trees[i].location)+1f));
			}
		}
		return moveLocation;
	}
}
