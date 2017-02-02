package turtlebot;
import battlecode.common.*;

public class Comms {

	
	public static final int GARDENS_START = 100;
	public static final int GARDENS_END = 120;
    public static final int ARCHON_SCOUT_DELEGATION_START = 200;
    public static final int ARCHON_SCOUT_DELEGATION_END = 240;
    public static final int SCOUT_ARCHON_REQUEST_START = 250;
    public static final int SCOUT_ARCHON_REQUEST_END = 290;
	public static final int LUMBERJACKS_COUNTER = 500;
	public static final int GARDENER_UNIVERSAL_HOLD_LOCATION = 501;
	public static final int GARDENER_UNIVERSAL_HOLD_ROUND = 502;
	public static final int ENEMY_START = 600;
	public static final int ENEMY_END = 699;
	public static final int ENEMY_ARCHON_START = 700;
	public static final int ENEMY_ARCHON_END = 702;

	public static final int HIGH_PRIORITY_TREE_START = 7;
	public static final int HIGH_PRIORITY_TREE_END = 16;
	public static final int LOW_PRIORITY_TREE_START = 17;
	public static final int LOW_PRIORITY_TREE_END = 26;

	public static final int ROBOT_NUMS_START = 703; // End is 709
	public static final int ATTACK_LOCATION = 710;
	public static final int ATTACK_ID = 711;

	
    private static final int POINTER_OFFSET = 1;
    
    
    // Up to 20 gardeners
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
            //System.out.println("Oops! Exceeded stack limit.");
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

    public static void pushQueue(RobotController rc, int queueStart, int queueEnd, int data) throws GameActionException {
		int queueData = rc.readBroadcast(queueStart);
        int head = (queueData & 0xFF00) >> 8;
        int tail = queueData & 0x00FF;

		int newTail = tail + 1;
		if(tail == queueEnd){
			newTail = queueStart + 1;
		}

		if(newTail == head){
			System.out.println("error: queue overflow");
			return;
		}

		rc.broadcast(queueStart + 1 + tail, data);

        rc.broadcast(queueStart, (head << 8) | (newTail));
	}

    public static int popQueue(RobotController rc, int queueStart, int queueEnd) throws GameActionException {
		int queueData = rc.readBroadcast(queueStart);
        int head = (queueData & 0xFF00) >> 8;
        int tail = queueData & 0x00FF;

		int newHead = head + 1;
		if(head == queueEnd){
			newHead = queueStart + 1;
		}
		if(tail == head) return -1; // empty queue

		int data = rc.readBroadcast(queueStart + 1 + head);

        rc.broadcast(queueStart, (newHead << 8) | (tail));
		return data;
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

	// Uses channels 7 to 26
	// 10 high priority trees, 10 low priority trees
	public static int packTree(RobotController rc, TreeInfo tree, int count){
		return packLocation(rc, tree.getLocation()) + count*(int)Math.pow(2,20)+ tree.getID()*(int)Math.pow(2,24);
	}

	public static TreeInfo unpackTree(RobotController rc, int data){
		int id = data/(int)Math.pow(2, 24);
		int count = (data - (id*(int)Math.pow(2, 24)))/(int)Math.pow(2,20);
		MapLocation loc = unpackLocation(rc, data - id*(int)Math.pow(2,24) - count*(int)Math.pow(2,20));
		if(loc == null) return null;

		return new TreeInfo(id, null, loc, 0, 0, count, null);
	}

	public static void pushHighPriorityTree(RobotController rc, TreeInfo tree, int count) throws GameActionException {
		writeStack(rc, HIGH_PRIORITY_TREE_START, HIGH_PRIORITY_TREE_END, packTree(rc, tree, count));
	}

	public static TreeInfo popHighPriorityTree(RobotController rc) throws GameActionException {
		return unpackTree(rc, popStack(rc, HIGH_PRIORITY_TREE_START, HIGH_PRIORITY_TREE_END));
	}

	public static void pushLowPriorityTree(RobotController rc, TreeInfo tree, int count) throws GameActionException {
		pushQueue(rc, LOW_PRIORITY_TREE_START, LOW_PRIORITY_TREE_END, packTree(rc, tree, count));
	}

	public static TreeInfo popLowPriorityTree(RobotController rc) throws GameActionException {
		return unpackTree(rc, popQueue(rc, LOW_PRIORITY_TREE_START, LOW_PRIORITY_TREE_END));
	}
	
	// uses channel 500 for the lols
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
	
	public static void writeNumRobots(RobotController rc, RobotType type, int num) throws GameActionException {
		rc.broadcast(ROBOT_NUMS_START-1+type.ordinal(), num);
	}
	
	public static int readNumRobots(RobotController rc, RobotType type) throws GameActionException {
		return rc.readBroadcast(ROBOT_NUMS_START-1+type.ordinal());
	}
	
	public static void writeAttackEnemy(RobotController rc, MapLocation loc, int id) throws GameActionException {
		rc.broadcast(ATTACK_LOCATION, Comms.packLocation(rc,loc));
		rc.broadcast(ATTACK_ID, id);
	}
	
	public static void clearAttackEnemy(RobotController rc) throws GameActionException {
		rc.broadcast(ATTACK_LOCATION, 0);
		rc.broadcast(ATTACK_ID, 0);
	}
	
	public static MapLocation readAttackLocation(RobotController rc) throws GameActionException {
		int data = rc.readBroadcast(ATTACK_LOCATION);
		if(data == 0)
			return null;
		return Comms.unpackLocation(rc,data);
	}
	
	public static int readAttackID(RobotController rc) throws GameActionException {
		return rc.readBroadcast(ATTACK_ID);
	}
	
}
