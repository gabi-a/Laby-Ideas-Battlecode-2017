package smoothmoves;
import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	Team us = rc.getTeam();
	Team them = us.opponent();
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		
	}
}
