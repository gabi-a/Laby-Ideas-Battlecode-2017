package testgardener;
import battlecode.common.*;

public class Comms {

	public static final int BUILD_START = 0;
	public static final int BUILD_END = 100;
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
	public static final int ROBOT_NUMS_START = 703; // End is 709
	public static final int ATTACK_LOCATION = 710;
	public static final int ATTACK_ID = 711;
	private static final int FOUND_ENEMY = 712;
	
    private static final int POINTER_OFFSET = 1;
    
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
        if (mapZoneX < 0 || mapZoneY < 0 || mapZoneX > 200 || mapZoneY > 200) {
            System.out.format("\nWe shouldn't be here! Map zone X/Y < 0 or > 200, is %d,%d\n", mapZoneX, mapZoneY);
        }
        
        int packedLocation = (mapZoneX << 8) | (mapZoneY);

        rc.broadcast(stackStart + POINTER_OFFSET + stackPointer, packedLocation);
        rc.broadcast(stackStart, stackPointer + 1);
    }
    
    public static void writeBuildStack(RobotController rc, RobotType botType, int prio) throws GameActionException {
        
        int stackPointer = rc.readBroadcast(BUILD_START);
        
        if (BUILD_START + stackPointer + 1 > BUILD_END) {
            System.out.println("Oops! Exceeded stack limit.");
            return;
        }
        
        int packedData = (botType.ordinal() << 8) | (prio);
        
        rc.broadcast(BUILD_START + POINTER_OFFSET + stackPointer, packedData);
        rc.broadcast(BUILD_START, stackPointer + 1);
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
    
    public static int readBuildStack(RobotController rc) throws GameActionException {
        
        int stackPointer = rc.readBroadcast(BUILD_START);
        
        if (stackPointer == 0) {
            return 0;
        }
        
        stackPointer--;
        
        return rc.readBroadcast(BUILD_START + POINTER_OFFSET + stackPointer);
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
    
    public static int[] popBuildStack(RobotController rc) throws GameActionException {
        
        int packedData = Comms.readBuildStack(rc);
        
        int stackPointer = rc.readBroadcast(BUILD_START);
        
        if (stackPointer == 0) {
            return null;
        }
        
        stackPointer--;
        
        rc.broadcast(BUILD_START + POINTER_OFFSET + stackPointer, 0);
        rc.broadcast(BUILD_START, stackPointer);
        
        int botType = (packedData & 0xFF00) >> 8;
        int prio = packedData & 0x00FF;
        
        return new int[]{botType, prio};
    }

	public static int packLocation(MapLocation loc) {
		return (int)loc.x + (int)loc.y*(int)Math.pow(2,10);
	}

	public static MapLocation unpackLocation(int loc) {
		return new MapLocation(loc%(int)Math.pow(2,10), loc/(int)Math.pow(2,10));
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
	
	// uses channel 500 for the lols
	public static void writeNumLumberjacks(RobotController rc, int data) throws GameActionException {
		rc.broadcast(LUMBERJACKS_COUNTER, data);
	}
	public static int readNumLumberjacks(RobotController rc) throws GameActionException {
		return rc.readBroadcast(LUMBERJACKS_COUNTER);
	}
	
	//channel 
	public static void writeGardenerUniversalHold(RobotController rc, MapLocation location, int holdRound) throws GameActionException {
		rc.broadcast(GARDENER_UNIVERSAL_HOLD_LOCATION, packLocation(location));
		rc.broadcast(GARDENER_UNIVERSAL_HOLD_ROUND, holdRound);
	}
	
	public static MapLocation readGardenerUniversalHoldLocation(RobotController rc) throws GameActionException {
		return unpackLocation(rc.readBroadcast(GARDENER_UNIVERSAL_HOLD_LOCATION));
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
		rc.broadcast(ATTACK_LOCATION, Comms.packLocation(loc));
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
		return Comms.unpackLocation(data);
	}
	
	public static int readAttackID(RobotController rc) throws GameActionException {
		return rc.readBroadcast(ATTACK_ID);
	}
	
	public static void writeFoundEnemy(RobotController rc) throws GameActionException {
		rc.broadcast(FOUND_ENEMY, 1);
	}
	
	public static boolean readFoundEnemy(RobotController rc) throws GameActionException {
		return rc.readBroadcast(FOUND_ENEMY) == 1 ? true : false;
	}
}
