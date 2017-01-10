package turtlebot;
import battlecode.common.*;

public class Comms {

	public static int compressLocation(MapLocation loc) {
		return (int)loc.x + (int)loc.y*128;
	}

	public static MapLocation unpackLocation(int loc) {
		return new MapLocation(loc%128, loc/128);
	}

	// Uses channel 0
	public static int getNumGardeners(RobotController rc) throws GameActionException {
		return rc.readBroadcast(0);
	}
	
	// Uses channel 0
	public static void writeNumGardeners(RobotController rc, int num) throws GameActionException {
		rc.broadcast(0, num);
	}

	// Uses channels 1 to 6
	// There could be an issue if both coordinates are 0
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
	
	// Uses channels 1 to 6
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

	// Uses channels 7 to 26
	// 10 high priority trees, 10 low priority trees
	public static void pushHighPriorityTree(RobotController rc, MapLocation loc){
		for(int i = 7; i < 17; i++){
			if(!rc.readBroadcast(i)){
				rc.broadcast(i, compressLocation(loc));
				break;
			}
		}
	}

	public static MapLocation popHighPriorityTree(RobotController rc){
		for(int i = 16; i > 6; i--){
			if(rc.readBroadcast(i)){
				MapLocation loc = unpackLocation(rc.readBroadcast(i));
				rc.broadcast(i, 0);
				return loc;
			}
		}
	}

	public static void pushLowPriorityTree(RobotController rc, MapLocation loc){
		for(int i = 17; i < 27; i++){
			if(!rc.readBroadcast(i)){
				rc.broadcast(i, compressLocation(loc));
				break;
			}
		}
	}

	public static MapLocation popLowPriorityTree(RobotController rc){
		for(int i = 26; i > 16; i--){
			if(rc.readBroadcast(i)){
				MapLocation loc = unpackLocation(rc.readBroadcast(i));
				rc.broadcast(i, 0);
				return loc;
			}
		}
	}
	
}
