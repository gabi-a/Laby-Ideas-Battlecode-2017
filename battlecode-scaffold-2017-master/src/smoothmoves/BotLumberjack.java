package smoothmoves;

import battlecode.common.*;

public class BotLumberjack {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotLumberjack.rc = rc;
		
		MapLocation myLocation = rc.getLocation();
		
		BulletInfo[] closeBullets = rc.senseNearbyBullets(4f);
		if(closeBullets.length > 0) {
			rc.move(Nav.awayFromBullets(myLocation, closeBullets));
		} else {
			rc.move(myLocation.directionTo(new MapLocation(155, 268)));
		}
		
	}
}
