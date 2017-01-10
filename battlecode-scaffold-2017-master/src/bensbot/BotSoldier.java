package bensbot;
import battlecode.common.*;

public class BotSoldier {
	static MapLocation target = new MapLocation(0,0);

	public static void turn(RobotController rc) throws GameActionException {
		// TODO: do stuff
		rc.move(target);
	}
	
}
