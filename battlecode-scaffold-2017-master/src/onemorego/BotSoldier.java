package onemorego;
import battlecode.common.*;

public class BotSoldier {
	static RobotController rc;
	
	static Team enemyTeam = RobotPlayer.rc.getTeam().opponent();
	static MapLocation enemyArchonLocation = RobotPlayer.rc.getInitialArchonLocations(enemyTeam)[0];
	static MapLocation targetLocation = enemyArchonLocation;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotSoldier.rc = rc;
		Util.reportDeath(rc);
	
		
	}
	
}
