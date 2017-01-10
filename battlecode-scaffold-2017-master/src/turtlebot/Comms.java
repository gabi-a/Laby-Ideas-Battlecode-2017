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

	// Uses channels 7 to 26
	// 10 high priority trees, 10 low priority trees
	public static void pushHighPriorityTree(RobotController rc, TreeInfo tree) throws GameActionException {
		for(int i = 7; i < 17; i++){
			int data = rc.readBroadcast(i);
			if(data == 0){
				rc.broadcast(i, compressLocation(tree.getLocation()) + tree.getID()*16384);
				break;
			}
		}
	}

	public static TreeInfo popHighPriorityTree(RobotController rc) throws GameActionException {
		for(int i = 16; i > 6; i--){
			int data = rc.readBroadcast(i);
			if(data != 0){
				int id = data/16384;
				data -= id*16384;
				MapLocation loc = unpackLocation(rc.readBroadcast(i));
				rc.broadcast(i, 0);
				return new TreeInfo(id, Team.NEUTRAL, loc, 0, 0, 0, null);
			}
		}
		return null;
	}

	public static void pushLowPriorityTree(RobotController rc, TreeInfo tree) throws GameActionException {
		for(int i = 17; i < 27; i++){
			int data = rc.readBroadcast(i);
			if(data == 0){
				rc.broadcast(i, compressLocation(tree.getLocation()) + tree.getID()*16384);
				break;
			}
		}
	}

	public static TreeInfo popLowPriorityTree(RobotController rc) throws GameActionException {
		for(int i = 26; i > 16; i--){
			int data = rc.readBroadcast(i);
			if(data != 0){
				int id = data/16384;
				data -= id*16384;
				MapLocation loc = unpackLocation(data);
				rc.broadcast(i, 0);
				return new TreeInfo(id, Team.NEUTRAL, loc, 0, 0, 0, null);
			}
		}
		return null;
	}
	
}
