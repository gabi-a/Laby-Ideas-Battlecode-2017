package turtlebot;
import battlecode.common.*;

public class Comms {
	
	public static final int TREE_START = 0;
	public static final int TREE_END = 9;
	// Empty 10 - 21
	public static final int GARDENS_START = 22;
	public static final int GARDENS_END = 42;
	public static final int ARCHON_SCOUT_DELEGATION_START = 43;
	public static final int ARCHON_SCOUT_DELEGATION_END = 83;
	public static final int SCOUT_ARCHON_REQUEST_START = 84;
	public static final int SCOUT_ARCHON_REQUEST_END = 124;
	public static final int GARDENER_UNIVERSAL_HOLD_LOCATION = 125;
	public static final int GARDENER_UNIVERSAL_HOLD_ROUND = 126;
	public static final int ENEMY_START = 127;
	public static final int ENEMY_END = 226;
	public static final int ENEMY_ARCHON_START = 227;
	public static final int ENEMY_ARCHON_END = 229;
	public static final int LUMBERJACKS_COUNTER = 230;
	
	private static final int POINTER_OFFSET = 1;
    
    
    public static boolean writeGarden(RobotController rc, MapLocation loc) throws GameActionException {
    	// Loop through channels until one is empty
    	//System.out.format("\nWriting garden");
    	for(int i = GARDENS_END; i-->GARDENS_START;) {
    		if(rc.readBroadcast(i) == 0) {
    			rc.broadcast(i, Comms.packLocation(rc, loc));
    			return true;
    		}
    	}
    	return false;
    }
    
    public static MapLocation[] readGardenLocs(RobotController rc) throws GameActionException {
    	MapLocation[] gardenLocs = new MapLocation[GARDENS_END-GARDENS_START];
    	int j = 0;
    	for(int i = GARDENS_END; i-->GARDENS_START;) {
    		int data = rc.readBroadcast(i);
    		if(data == 0) {
    			gardenLocs[j] = null;
    		} else {
    			gardenLocs[j] = Comms.unpackLocation(rc, data);
    		}
    		j++;
    	}
    	return gardenLocs;
    }
    
    
    public static void writeStack(RobotController rc, int stackStart, int stackEnd, int data) throws GameActionException {
        
        int stackPointer = rc.readBroadcast(stackStart);
        
        if (stackStart + stackPointer + 1 > stackEnd) {
            System.out.println("Oops! Exceeded stack limit.");
            return;
        }

        rc.broadcast(stackStart + POINTER_OFFSET + stackPointer, data);
        rc.broadcast(stackStart, stackPointer + 1);
    }
    
    public static int readStack(RobotController rc, int stackStart, int stackEnd) throws GameActionException {
        
        int stackPointer = rc.readBroadcast(stackStart);
        
        if (stackPointer == 0) {
            return -1;
        }
        
        stackPointer--;
        
        int data = rc.readBroadcast(stackStart + POINTER_OFFSET + stackPointer);

		return data;
    }
    
    public static int popStack(RobotController rc, int stackStart, int stackEnd) throws GameActionException {
        
        int returnValue = Comms.readStack(rc, stackStart, stackEnd);
        
        int stackPointer = rc.readBroadcast(stackStart);
        
        if (stackPointer == 0) {
            return -1;
        }
        
        stackPointer--;
        
        rc.broadcast(stackStart + POINTER_OFFSET + stackPointer, 0);
        rc.broadcast(stackStart, stackPointer);
        
        return returnValue;
    }

	public static int packLocation(RobotController rc, MapLocation loc) {
        Team myTeam = rc.getTeam();
        MapLocation referencePoint = (rc.getInitialArchonLocations(myTeam))[0];
        MapLocation cornerPoint = referencePoint.add((float) Math.PI, 100).add((float) Math.PI * 0.5f, -100);
        
        int mapZoneX = (int) (loc.x - cornerPoint.x);
        int mapZoneY = (int) (loc.y - cornerPoint.y);
        
        // Debug only!
        if (mapZoneX < 0 || mapZoneY < 0 || mapZoneX > 200 || mapZoneY > 200) {
            System.out.format("\nWe shouldn't be here! Map zone X/Y < 0 or > 200, is %d,%d\n", mapZoneX, mapZoneY);
        }
        
        return (mapZoneX << 8) | (mapZoneY);
	}

	public static MapLocation unpackLocation(RobotController rc, int loc) {
		if(loc == -1) return null;

        int mapZoneX = (loc & 0xFF00) >> 8;
        int mapZoneY = loc & 0x00FF;
        
        Team myTeam = rc.getTeam();
        MapLocation referencePoint = (rc.getInitialArchonLocations(myTeam))[0];
        MapLocation cornerPoint = referencePoint.add((float) Math.PI, 100).add((float) Math.PI * 0.5f, -100);
        
        
        return new MapLocation(cornerPoint.x + mapZoneX, cornerPoint.y + mapZoneY);
	}

	public static int packTree(RobotController rc, TreeInfo tree){
		return packLocation(rc, tree.getLocation()) + /*count*(int)Math.pow(2,20) */+ tree.getID()*(int)Math.pow(2,24);
	}

	public static TreeInfo unpackTree(RobotController rc, int data, int our_ref){
		int id = data/(int)Math.pow(2, 24);
		//int count = (data - (id*(int)Math.pow(2, 24)))/(int)Math.pow(2,20);
		MapLocation loc = unpackLocation(rc, data - id*(int)Math.pow(2,24) /* - count*(int)Math.pow(2,20)*/);
		if(loc == null) return null;

		return new TreeInfo(id, null, loc, 0, 0, our_ref, null);
	}

	public static boolean addTree(RobotController rc, TreeInfo tree) throws GameActionException {
		System.out.format("Adding tree target at %f, %f\n", tree.location.x, tree.location.y);
		int i = TREE_END + 1;
		while (i --> TREE_START) {
			if (rc.readBroadcast(i) == 0) {
				rc.broadcast(i, Comms.packTree(rc, tree));
				return true;
			}
		}
		return false;
	}
	
	/* Note: We (ab)use TreeInfo.containedBullets to hold the channel the info came from */
	public static TreeInfo getTree(RobotController rc) throws GameActionException {
		int i = TREE_END + 1;
		int packedTree;
		while (i --> TREE_START) {
			if ((packedTree = rc.readBroadcast(i)) != 0) return unpackTree(rc, packedTree, i);
		}
		return null;
	}
	
	public static void removeTree(RobotController rc, TreeInfo tree) throws GameActionException {
		rc.broadcast(tree.containedBullets, 0);
	}

	public static void writeNumLumberjacks(RobotController rc, int data) throws GameActionException {
		rc.broadcast(LUMBERJACKS_COUNTER, data);
	}
	public static int readNumLumberjacks(RobotController rc) throws GameActionException {
		return rc.readBroadcast(LUMBERJACKS_COUNTER);
	}
	
	//channel 
	public static void writeGardenerUniversalHold(RobotController rc, MapLocation location, int holdRound) throws GameActionException {
		rc.broadcast(GARDENER_UNIVERSAL_HOLD_LOCATION, packLocation(rc, location));
		rc.broadcast(GARDENER_UNIVERSAL_HOLD_ROUND, holdRound);
	}
	
	public static MapLocation readGardenerUniversalHoldLocation(RobotController rc) throws GameActionException {
		return unpackLocation(rc, rc.readBroadcast(GARDENER_UNIVERSAL_HOLD_LOCATION));
	}
	
	public static int readGardenerUniversalHoldRound(RobotController rc) throws GameActionException {
		return rc.readBroadcast(GARDENER_UNIVERSAL_HOLD_ROUND);
	}
}
