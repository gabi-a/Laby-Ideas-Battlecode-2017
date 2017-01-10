package turtlebot;
import battlecode.common.*;

public class Comms {

	static int getNumGardeners(RobotController rc) throws GameActionException {
		return rc.readBroadcast(0);
	}
	
	static void writeNumGardeners(RobotController rc, int num) throws GameActionException {
		rc.broadcast(0, num);
	}
	
}
