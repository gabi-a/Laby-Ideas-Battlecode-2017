package smoothmoves;
import battlecode.common.*;
import battlecode.schema.Action;

public class BotGardener {
	static RobotController rc;
	
	Team us = rc.getTeam();
	Team them = us.opponent();
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		
		RobotInfo[] bots = rc.senseNearbyRobots();
		TreeInfo[] trees = rc.senseNearbyTrees();
		BulletInfo[] bullets = rc.senseNearbyBullets();
		MapLocation myLocation = rc.getLocation();
		
		Direction moveDirection = null;
		float moveStride = RobotType.GARDENER.strideRadius;
		
		/************* Determine where to move *******************/
		
		MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets, trees, bots);
		moveDirection = myLocation.directionTo(moveLocation);
		moveStride = myLocation.distanceTo(myLocation);
		
		/************* Determine what action to take *************/
		
		byte action = Action.DIE_EXCEPTION;
		
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
		case Action.SPAWN_UNIT:
			break;
		default:
			break;
		}
		
	}
}
