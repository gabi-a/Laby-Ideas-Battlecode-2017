package turtlebot_gabisgardeners;
import battlecode.common.*;

public class Comms {

    private static final int POINTER_OFFSET = 1;
    
    /* STACK LAYOUT
        0 - 20 = GARDENER SPAWNS
        21 - 40 = GARDENER REQUESTS
    */
    
    public static void writeStack(RobotController rc, int stackStart, int stackEnd, MapLocation location) throws GameActionException {
        
        int stackPointer = rc.readBroadcast(stackStart);
        
        if (stackStart + stackPointer + 1 > stackEnd) {
            System.out.println("Oops! Exceeded stack limit.");
            return;
        }
        
        Team myTeam = rc.getTeam();
        MapLocation referencePoint = (rc.getInitialArchonLocations(myTeam))[0];
        MapLocation cornerPoint = referencePoint.add((float) Math.PI, 100).add((float) Math.PI * 0.5f, -100);
        
        int mapZoneX = (int) (location.x - cornerPoint.x);
        int mapZoneY = (int) (location.y - cornerPoint.y);
        
        // Debug only!
        if (mapZoneX < 0 || mapZoneY < 0 || mapZoneX >= 200 || mapZoneY >= 200) {
            System.out.format("We shouldn't be here! Map zone X/Y < 0 or >= 200, is %d,%d\n", mapZoneX, mapZoneY);
        }
        
        int packedLocation = (mapZoneX << 8) | (mapZoneY);

        rc.broadcast(stackStart + POINTER_OFFSET + stackPointer, packedLocation);
        rc.broadcast(stackStart, stackPointer + 1);
    }
    
    public static MapLocation readStack(RobotController rc, int stackStart, int stackEnd) throws GameActionException {
        
        int stackPointer = rc.readBroadcast(stackStart);
        
        if (stackPointer == 0) {
            return null;
        }
        
        stackPointer--;
        
        int packedLocation = rc.readBroadcast(stackStart + POINTER_OFFSET + stackPointer);

        int mapZoneX = (packedLocation & 0xFF00) >> 8;
        int mapZoneY = packedLocation & 0x00FF;
        
        Team myTeam = rc.getTeam();
        MapLocation referencePoint = (rc.getInitialArchonLocations(myTeam))[0];
        MapLocation cornerPoint = referencePoint.add((float) Math.PI, 100).add((float) Math.PI * 0.5f, -100);
        
        
        return new MapLocation(cornerPoint.x + mapZoneX, cornerPoint.y + mapZoneY);
    }
    
    public static MapLocation popStack(RobotController rc, int stackStart, int stackEnd) throws GameActionException {
        
        MapLocation returnValue = Comms.readStack(rc, stackStart, stackEnd);
        
        int stackPointer = rc.readBroadcast(stackStart);
        
        if (stackPointer == 0) {
            return null;
        }
        
        stackPointer--;
        
        rc.broadcast(stackStart + POINTER_OFFSET + stackPointer, 0);
        rc.broadcast(stackStart, stackPointer);
        
        return returnValue;
    }

	public static int packLocation(MapLocation loc) {
		return (int)loc.x + (int)loc.y*(int)Math.pow(2,10);
	}

	public static MapLocation unpackLocation(int loc) {
		return new MapLocation(loc%(int)Math.pow(2,10), loc/(int)Math.pow(2,10));
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
			int y = rc.readBroadcast(channel-1);
			if(x == 0 && y == 0) {
				rc.broadcast(channel, (int) rc.getLocation().x);
				rc.broadcast(channel-1, (int) rc.getLocation().y);
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
			int y = rc.readBroadcast(channel-1);
			if(x != 0 && y != 0) {
				archonLocations[channel/2 - 1] = new MapLocation(x,y);
			}
		}
		return archonLocations;
	}

	// Uses channels 7 to 26
	// 10 high priority trees, 10 low priority trees
	public static int packTree(TreeInfo tree, int count){
		return packLocation(tree.getLocation()) + count*(int)Math.pow(2,20)+ tree.getID()*(int)Math.pow(2,24);
	}

	public static TreeInfo unpackTree(int data){
		int id = data/(int)Math.pow(2, 24);
		int count = (data - (id*(int)Math.pow(2, 24)))/(int)Math.pow(2,20);
		MapLocation loc = unpackLocation(data - id*(int)Math.pow(2,24) - count*(int)Math.pow(2,20));

		return new TreeInfo(id, null, loc, 0, 0, count, null);
	}

	public static void pushHighPriorityTree(RobotController rc, TreeInfo tree, int count) throws GameActionException {
		for(int i = 7; i < 17; i++){
			int data = rc.readBroadcast(i);
			if(data == 0){
				rc.broadcast(i, packTree(tree, count));
				break;
			}
		}
	}

	public static TreeInfo popHighPriorityTree(RobotController rc) throws GameActionException {
		for(int i = 16; i > 6; i--){
			int data = rc.readBroadcast(i);
			if(data != 0){
				TreeInfo tree = unpackTree(data);
				if(tree.getContainedBullets() > 1){
					rc.broadcast(i, packTree(tree, tree.getContainedBullets()-1));
				} else rc.broadcast(i, 0);
				return tree;
			}
		}
		return null;
	}

	public static void pushLowPriorityTree(RobotController rc, TreeInfo tree, int count) throws GameActionException {
		for(int i = 17; i < 27; i++){
			int data = rc.readBroadcast(i);
			if(data == 0){
				rc.broadcast(i, packTree(tree, count));
				break;
			}
		}
	}

	public static TreeInfo popLowPriorityTree(RobotController rc) throws GameActionException {
		for(int i = 26; i > 16; i--){
			int data = rc.readBroadcast(i);
			if(data != 0){
				TreeInfo tree = unpackTree(data);
				if(tree.getContainedBullets() > 1){
					rc.broadcast(i, packTree(tree, tree.getContainedBullets()-1));
				} else rc.broadcast(i, 0);
				return tree;
			}
		}
		return null;
	}
	
	// Uses channels 27 to 28
	public static void writeHomeLocation(RobotController rc, MapLocation homeLocation) throws GameActionException {
		rc.broadcast(27, (int)homeLocation.x);
		rc.broadcast(28, (int)homeLocation.y);
	}
	
	public static MapLocation readHomeLocation(RobotController rc) throws GameActionException {
		int x = rc.readBroadcast(27);
		int y = rc.readBroadcast(28);
		if(x == 0 && y == 0) {
			return null;
		}
		return new MapLocation(x, y);
	}
	
	// uses channel 500 for the lols
	public static void writeNumLumberjacks(RobotController rc, int data) throws GameActionException {
		rc.broadcast(500, data);
	}
	public static int readNumLumberjacks(RobotController rc) throws GameActionException {
		return rc.readBroadcast(500);
	}
}