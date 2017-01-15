package SprintBot;

import battlecode.common.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BotArchon {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotArchon.rc = rc;
		Util.updateBotCount(rc);
		Util.reportDeath(rc);
		
		
	}
	
	public static boolean tryHireGardener() throws GameActionException {
		Direction hireDirection = new Direction(0);
		for (int i = 0; i < 8; i++) {
			if (rc.canHireGardener(hireDirection) && rc.onTheMap(rc.getLocation().add(hireDirection, 5f))) {
				rc.hireGardener(hireDirection);
				return true;
			}
			hireDirection = hireDirection.rotateLeftRads((float) Math.PI * 0.25f);
		}
		return false;
	}
	
}
