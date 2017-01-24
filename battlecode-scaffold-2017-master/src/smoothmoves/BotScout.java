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

		if(bullets.length > 0) {
			System.out.println("RUN!");
			MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets, enemies);
			if(moveLocation != null) {
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceTo(moveLocation);
			}
		}
		
		else {
			MapLocation moveLocation = applyTreeGravity(myLocation, trees);
			moveDirection = myLocation.directionTo(moveLocation);
			if(moveDirection != null && myLocation.distanceTo(moveLocation) > 0.01f) {
				moveDirection = Nav.tryMove(rc, moveDirection, 5f, 24, bullets);
			}
			else {
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
		
		if(action != Action.SHAKE_TREE && enemies.length > 0 && enemies[0].type == RobotType.SCOUT) {
			if(rc.canFireSingleShot()) {
				action = Action.FIRE;
				shootDirection = myLocation.directionTo(enemies[0].location);
			}
		}
		
		/************* Do pre move action *********************************/
		
		/*
		 * All checks to see if this action is possible should already
		 * have taken place
		 */
		
		switch(action) {
		case Action.SHAKE_TREE:
			rc.shake(treeToShake.ID);
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
		
		/************* Do post move action *********************************/
		
		/*
		 * All checks to see if this action is possible should already
		 * have taken place
		 */
		
		switch(action) {
		case Action.FIRE:
			if(rc.canFireSingleShot()) rc.fireSingleShot(shootDirection);
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
