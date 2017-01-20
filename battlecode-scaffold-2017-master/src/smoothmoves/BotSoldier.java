package smoothmoves;
import battlecode.common.*;

public class BotSoldier {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotSoldier.rc = rc;
		
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		if(enemies.length > 0) { 
			rc.firePentadShot(rc.getLocation().directionTo(enemies[0].location).rotateLeftRads((float) (Math.random()-0.5)));
		}
	}
	
}
