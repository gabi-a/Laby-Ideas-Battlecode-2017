package turtlebot;
import battlecode.common.*;

public class Comms {

	// Uses channel 0
	public static int getNumGardeners(RobotController rc) throws GameActionException {
		return rc.readBroadcast(0);
	}
	
	// Uses channel 0
	public static void writeNumGardeners(RobotController rc, int num) throws GameActionException {
		rc.broadcast(0, num);
	}

	// Uses channels 1 to 6
	// There could be an issue if one coordinate is 0
	public static void writeArchonLocation(RobotController rc) throws GameActionException {
		for(int channel = 8; (channel-=2) > 1;) {
			int x = rc.readBroadcast(channel);
			int y = rc.readBroadcast(channel+1);
			if(x == 0 && y == 0) {
				rc.broadcast(channel, (int) rc.getLocation().x);
				rc.broadcast(channel+1, (int) rc.getLocation().y);
				System.out.println(channel);
				break;
			}
		}
	}
	
	public static MapLocation[] readArchonLocations(RobotController rc) throws GameActionException {
		MapLocation[] archonLocations = new MapLocation[3];
		for(int channel = 8; (channel-=2) > 1;) {
			int x = rc.readBroadcast(channel);
			int y = rc.readBroadcast(channel+1);
			if(x != 0 && y != 0) {
				archonLocations[channel/2 - 1] = new MapLocation(x,y);
			}
		}
		return archonLocations;
	}
	
}
