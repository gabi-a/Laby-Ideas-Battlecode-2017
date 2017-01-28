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

		Util.updateMyPostion(rc);
		
		//RobotInfo[] bots = rc.senseNearbyRobots();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
		TreeInfo[] trees = rc.senseNearbyTrees();
		BulletInfo[] bullets = rc.senseNearbyBullets();
		myLocation = rc.getLocation();
		
		Direction moveDirection = null;
		float moveStride = RobotType.SCOUT.strideRadius;

		Util.reportEnemyBots(rc, enemies);
		
		/************* Determine where to move *******************/

		boolean dodgeBullets = false;
		
		if(bullets.length > 0) {
			
			MapLocation dodgeBulletsLocation = Nav.awayFromBullets(rc, myLocation, bullets);
			
			if(dodgeBulletsLocation != null || (enemies.length > 0 && enemies[0].type == RobotType.LUMBERJACK)) {
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
				if(dodgeBulletsLocation != null && !hideInTree && bullets.length > 0) {
					moveDirection = myLocation.directionTo(dodgeBulletsLocation);
					moveStride = myLocation.distanceTo(dodgeBulletsLocation);
					dodgeBullets = moveDirection != null && rc.canMove(moveDirection, moveStride);
				}
			}
		}
		
		if(!dodgeBullets) {
			
			boolean attackGardener = false;
			System.out.format("Enemies around: %b\n", enemies.length > 0);
			if(enemies.length > 0 && enemies[0].type != RobotType.LUMBERJACK) {
				RobotInfo enemyGardener = Util.getGardenerAndAllPassive(enemies);
				System.out.format("There is a safe gardener: %b\n", enemyGardener != null);
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
				//MapLocation moveLocation = applyTreeGravity(myLocation, trees);
				//moveDirection = myLocation.directionTo(moveLocation);
				
				MapLocation goalLocation = bestTree(trees);
				MapLocation moveLocation = null;
				if(goalLocation != null)
					moveLocation = Nav.pathTo(rc, goalLocation, bullets);
				if (moveLocation != null){
					moveDirection = myLocation.directionTo(moveLocation);
					moveStride = myLocation.distanceTo(moveLocation);
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
		
		if(action != Action.SHAKE_TREE && enemies.length > 0 && (enemies[0].type == RobotType.GARDENER || enemies[0].health < 20)) {
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
	
	private static MapLocation bestTree(TreeInfo[] trees) {
		if(trees.length == 0) return null;
		for(int i = 0; i < trees.length; i++) {
			if(trees[i].containedBullets > 0)
				return trees[i].location;
		}
		return null;
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
