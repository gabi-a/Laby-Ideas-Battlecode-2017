package turtlebot;

import battlecode.common.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BotArchon {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotArchon.rc = rc;
		
		int gardenersCount = Comms.getNumGardeners(rc);
		
		if(gardenersCount < 2) {
			
		}
		
	}
}
