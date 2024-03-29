package onemorego;
import battlecode.common.*;

enum AttackGroup {
	A, B, C, D, E, F
}

public class Comms {
	
	public static final int ROBOT_NUMS_START = 750; // End is 706
	
	// Up to 6 enemies to focus on attacking at once
	// Each enemy is stored in two channels: one for MapLocation and one for ID
	public static final int ATTACK_START = 900; // Goes to channel 12

	public static final CommsInt gardenerPlanting;
	public static final CommsInt holdTreeProduction;
	
	public static final CommsQueue buildQueue;
	public static final CommsStack lumberjackStack;
	public static final CommsStack soldierStratStack;
	public static final CommsStack soldierProtectionLocationStack;
	public static final CommsTree neutralTrees;
	//public static final CommsTree enemyTrees;

	/* Example usage:
	 * Comms.neutralTrees.push(RobotController rc, TreeInfo stuff);
	 * TreeInfo stuff = Comms.neutralTrees.pop(RobotController rc);
	 */

	static {
		neutralTrees = new CommsTree(101,120);
		//enemyTrees = new CommsTree(11,21);
		buildQueue = new CommsQueue(300, 500);
		lumberjackStack = new CommsStack(201,220);
		soldierStratStack = new CommsStack(230,270);
		soldierProtectionLocationStack = new CommsStack(510,540);
		gardenerPlanting = new CommsInt(0);
		holdTreeProduction = new CommsInt(1);
	}

	public static void writeAttackEnemy(RobotController rc, MapLocation loc, int id, AttackGroup group) throws GameActionException {
		rc.broadcast(ATTACK_START+group.ordinal(), Comms.packLocation(rc, loc));
		rc.broadcast(ATTACK_START+group.ordinal()+6, id);
	}
	
	public static void clearAttackEnemy(RobotController rc, AttackGroup group) throws GameActionException {
		rc.broadcast(ATTACK_START+group.ordinal(), 0);
		rc.broadcast(ATTACK_START+group.ordinal()+6, 0);
	}
	
	public static MapLocation readAttackLocation(RobotController rc, AttackGroup group) throws GameActionException {
		int data = rc.readBroadcast(ATTACK_START+group.ordinal());
		if(data == 0)
			return null;
		return Comms.unpackLocation(rc, data);
	}
	
	public static int readAttackID(RobotController rc, AttackGroup group) throws GameActionException {
		return rc.readBroadcast(ATTACK_START+group.ordinal()+6);
	}
	
	public static void writeNumRobots(RobotController rc, RobotType type, int num) throws GameActionException {
		rc.broadcast(ROBOT_NUMS_START+type.ordinal(), num);
	}
	
	public static int readNumRobots(RobotController rc, RobotType type) throws GameActionException {
		return rc.readBroadcast(ROBOT_NUMS_START+type.ordinal());
	}

	public static int packLocation(RobotController rc, MapLocation location) {
		Team myTeam = rc.getTeam();
		MapLocation referencePoint = (rc.getInitialArchonLocations(myTeam))[0];
		MapLocation cornerPoint = referencePoint.add((float) Math.PI, 100).add((float) Math.PI * 0.5f, -100);
		
		int mapZoneX = (int) (location.x - cornerPoint.x);
		int mapZoneY = (int) (location.y - cornerPoint.y);
		
		// Debug only!
		if (mapZoneX < 0 || mapZoneY < 0 || mapZoneX > 200 || mapZoneY > 200) {
			System.out.format("\nWe shouldn't be here! Map zone X/Y < 0 or > 200, is %d,%d\n", mapZoneX, mapZoneY);
		}
		
		return (mapZoneX << 8) | (mapZoneY);
	}

	public static MapLocation unpackLocation(RobotController rc, int packedLocation) {
		if(packedLocation == -1) return null;

		int mapZoneX = (packedLocation & 0xFF00) >> 8;
		int mapZoneY = packedLocation & 0x00FF;
		
		Team myTeam = rc.getTeam();
		MapLocation referencePoint = (rc.getInitialArchonLocations(myTeam))[0];
		MapLocation cornerPoint = referencePoint.add((float) Math.PI, 100).add((float) Math.PI * 0.5f, -100);
		
		
		return new MapLocation(cornerPoint.x + mapZoneX, cornerPoint.y + mapZoneY);
	}
	
}

class CommsInt {
	public int index;

	public CommsInt(int ind){
		index = ind;
	}

	public void write(RobotController rc, int data) throws GameActionException {
		rc.broadcast(index, data);
	}

	public int read(RobotController rc) throws GameActionException {
		return rc.readBroadcast(index);
	}
}

class CommsStack {
	public int stackStart;
	public int stackEnd;
	public int offset = 1;

	public CommsStack(int start, int end){
		stackStart = start;
		stackEnd = end;
	}

	public void push(RobotController rc, int data) throws GameActionException {
		int stackPointer = rc.readBroadcast(stackStart);
		
		if (stackStart + stackPointer + 1 > stackEnd) {
			System.out.println("Oops! Exceeded stack limit.");
			return;
		}

		rc.broadcast(stackStart + offset + stackPointer, data);
		rc.broadcast(stackStart, stackPointer + 1);
	}

	public int pop(RobotController rc) throws GameActionException {
		int stackPointer = rc.readBroadcast(stackStart);
		
		if (stackPointer == 0) {
			return -1;
		}
		
		stackPointer--;
		
		int data = rc.readBroadcast(stackStart + offset + stackPointer);

		rc.broadcast(stackStart + offset + stackPointer, 0);
		rc.broadcast(stackStart, stackPointer);
		
		return data;
	}
}

class CommsQueue {
	public int queueStart;
	public int queueEnd;
	public int offset = 1;

	public CommsQueue(int start, int end){
		queueStart = start;
		queueEnd = end;
	}

	public void push(RobotController rc, int data) throws GameActionException {
		int queueData = rc.readBroadcast(queueStart);
		int head = (queueData & 0xFF00) >> 8;
		int tail = queueData & 0x00FF;

		int newTail = tail + 1;
		if(newTail > queueEnd) newTail = queueStart + offset;

		if(newTail == head){
			System.out.println("queue overflow");
			return;
		}

		rc.broadcast(queueStart + offset + tail, data);

		queueData = (head << 8) | (newTail);
		rc.broadcast(queueStart, queueData);
	}

	public int pop(RobotController rc) throws GameActionException {
		int queueData = rc.readBroadcast(queueStart);
		int head = (queueData & 0xFF00) >> 8;
		int tail = queueData & 0x00FF;

		int newHead = head + 1;
		if(newHead > queueEnd) newHead = queueStart + offset;

		if(tail == head) {
			return -1;	// queue is empty
		}

		int data = rc.readBroadcast(queueStart + offset + head);

		queueData = (newHead << 8) | (tail);
		rc.broadcast(queueStart, queueData);

		return data;
	}
}

class CommsArray {
	public int arrayStart;
	public int arrayEnd;
	public int[] array;
	public int[] lastUpdated;

	public void write(RobotController rc, int index, int data) throws GameActionException {
		if(index > arrayEnd - arrayStart){
			System.out.println("error: out of comms array bounds");
			return;
		}

		rc.broadcast(arrayStart + index, data);
		array[index] = data;
		lastUpdated[index] = rc.getRoundNum();
	}

	public int read(RobotController rc, int index) throws GameActionException {
		if(index > arrayEnd - arrayStart){
			System.out.println("error: out of comms array bounds");
			return -1;
		}

		if(lastUpdated[index] != rc.getRoundNum()){
			array[index] = rc.readBroadcast(arrayStart + index);
			lastUpdated[index] = rc.getRoundNum();
		}

		return array[index];
	}
}

class CommsTree {
	CommsQueue trees;

	public CommsTree(int start, int end) {
		trees = new CommsQueue(start, end);
	}

	public void push(RobotController rc, TreeInfo tree) throws GameActionException {
		trees.push(rc, packTree(rc, tree));
	}

	public TreeInfo pop(RobotController rc) throws GameActionException {
		return unpackTree(rc, trees.pop(rc));
	}

	public static int packTree(RobotController rc, TreeInfo tree){
		return Comms.packLocation(rc, tree.getLocation()) + tree.getID()*(int)Math.pow(2,20);
	}

	public static TreeInfo unpackTree(RobotController rc, int data){
		if(data == -1) return null;

		int id = data/(int)Math.pow(2, 20);
		MapLocation loc = Comms.unpackLocation(rc, data - id*(int)Math.pow(2,20));
		if(loc == null) return null;

		return new TreeInfo(id, null, loc, 0, 0, 0, null);
	}
}
