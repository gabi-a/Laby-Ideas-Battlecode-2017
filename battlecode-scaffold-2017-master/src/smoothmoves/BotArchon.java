package smoothmoves;
import battlecode.common.*;
import battlecode.schema.Action;
import onemorego.Util;

public class BotArchon {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotArchon.rc = rc;
		
		RobotInfo[] bots = rc.senseNearbyRobots();
		TreeInfo[] trees = rc.senseNearbyTrees();
		BulletInfo[] bullets = rc.senseNearbyBullets();
		MapLocation myLocation = rc.getLocation();
		
		Direction moveDirection = null;
		float moveStride = RobotType.ARCHON.strideRadius;
		
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
			tryHireGardener();
			break;
		default:
			break;
		}
		
	}
	
	public static boolean tryHireGardener() throws GameActionException {
		Direction hireDirection = new Direction(0);
		for (int i = 0; i < 8; i++) {
			if (rc.canHireGardener(hireDirection) && rc.onTheMap(rc.getLocation().add(hireDirection, 5f))) {
				rc.hireGardener(hireDirection);
				Util.increaseNumBotsByOne(rc, RobotType.GARDENER);
				return true;
			}
			hireDirection = hireDirection.rotateLeftRads((float) Math.PI * 0.25f);
		}
		return false;
	}
}

