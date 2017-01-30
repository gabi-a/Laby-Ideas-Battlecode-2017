package teststrategy;
import battlecode.common.*;

public class Comms {

	static Team us = RobotPlayer.rc.getTeam();
	static Team them = us.opponent();
	
	static MapLocation referencePoint = (RobotPlayer.rc.getInitialArchonLocations(us))[0];
	static MapLocation cornerPoint = referencePoint.add((float) Math.PI, 100).add((float) Math.PI * 0.5f, -100);
	
	/* Example usage:
	 * Comms.neutralTrees.push(RobotController rc, TreeInfo stuff);
	 * TreeInfo stuff = Comms.neutralTrees.pop(RobotController rc);
	 */

	public static final CommsBotCount ourBotCount;
	public static final CommsBotCount theirBotCount;
	
	public static final CommsBotArray enemyGardenersArray;
	public static final CommsBotArray enemyArchonsArray;
	
	public static final CommsBotArray enemiesAttackingGardenersOrArchons;
	
	public static final CommsBotArray enemiesSighted;
	
	public static final CommsBotArray ourLumberjackAndSoldiers;
	
	public static final CommsArray archonTreeCount;
	public static final CommsInt archonCount;
	
	public static final CommsInt mapSize;
	
	static {
		ourBotCount = new CommsBotCount(0,6);
		archonTreeCount = new CommsArray(110,113);
		archonCount = new CommsInt(120);
		mapSize = new CommsInt(125);
		theirBotCount = new CommsBotCount(200,232);
		enemyGardenersArray = new CommsBotArray(300, 432);
		enemyArchonsArray = new CommsBotArray(500, 632);
		enemiesAttackingGardenersOrArchons = new CommsBotArray(700,832);
		ourLumberjackAndSoldiers = new CommsBotArray(900,1032);
		enemiesSighted = new CommsBotArray(1300,1432);
	}
	
	public static int packLocation(RobotController rc, MapLocation location) {
				
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

	public void increment(RobotController rc, int increment) throws GameActionException {
		rc.broadcast(index, rc.readBroadcast(index) + increment);
	}

	public void decrement(RobotController rc, int decrement) throws GameActionException {
		rc.broadcast(index, rc.readBroadcast(index) - decrement);
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

	public int length(RobotController rc) throws GameActionException {
		return rc.readBroadcast(stackStart);
	}

	public int[] array(RobotController rc) throws GameActionException {
		int stackPointer = rc.readBroadcast(stackStart);
		if (stackPointer == 0) {
			return null;
		}

		int[] data = new int[stackPointer];
		for(int i = 0; i < stackPointer; i++){
			data[i] = rc.readBroadcast(stackStart + offset + i);
		}

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

	public int length(RobotController rc) throws GameActionException {
		int queueData = rc.readBroadcast(queueStart);
		int head = (queueData & 0xFF00) >> 8;
		int tail = queueData & 0x00FF;

		return head > tail? head - tail : tail - head;
	}

	public int[] array(RobotController rc) throws GameActionException {
		int queueData = rc.readBroadcast(queueStart);
		int head = (queueData & 0xFF00) >> 8;
		int tail = queueData & 0x00FF;

		if(tail == head) {
			return null;	// queue is empty
		}

		int length = head > tail? head - tail : tail - head;

		int[] data = new int[length];
		for(int i = 0; i < length; i++){
			if(head > queueEnd) head = queueStart + offset;
			data[i] = rc.readBroadcast(queueStart + offset + head);
		}

		return data;
	}
}

class CommsArray {
	private int arrayStart;
	private int arrayEnd;
	private int[] array;
	private int[] lastUpdated;

	public CommsArray(int start, int end){
		arrayStart = start;
		arrayEnd = end;
	}

	public int length() {
		return arrayEnd - arrayStart;
	}

	public void write(RobotController rc, int index, int data) throws GameActionException {
		if(index > arrayEnd - arrayStart){
			System.out.println("error: out of comms array bounds");
			return;
		}

		rc.broadcast(arrayStart + index, data);
		// array[index] = data;
		// lastUpdated[index] = rc.getRoundNum();
	}

	public int read(RobotController rc, int index) throws GameActionException {
		if(index > arrayEnd - arrayStart){
			System.out.println("error: out of comms array bounds");
			return -1;
		}

		return rc.readBroadcast(arrayStart + index);

		// if(lastUpdated[index] != rc.getRoundNum()){
			// array[index] = rc.readBroadcast(arrayStart + index);
			// lastUpdated[index] = rc.getRoundNum();
		// }

		// return array[index];
	}

	public int[] array(RobotController rc) throws GameActionException {
		int[] data = new int[arrayEnd - arrayStart];
		for(int i = 0; i < arrayEnd - arrayStart; i++){
			data[i] = rc.readBroadcast(arrayStart + i);
		}

		return data;
	}
}

class CommsBotCount extends CommsArray {
	
	CommsBotCount(int start, int end) {
		super(start, end);
	}
	
	public void writeNumBots(RobotController rc, RobotType type, int count) throws GameActionException {
		write(rc, type.ordinal(), count);
	}
	
	public int readNumBots(RobotController rc, RobotType type) throws GameActionException {
		return read(rc, type.ordinal());
	}
	
	public void incrementNumBots(RobotController rc, RobotType type) throws GameActionException {
		int count = read(rc, type.ordinal());
		write(rc, type.ordinal(), ++count);
	}
	
	public void decrementNumBots(RobotController rc, RobotType type) throws GameActionException {
		int count = read(rc, type.ordinal());
		write(rc, type.ordinal(), --count);
	}
	
}

class CommsBotArray {
	CommsArray x;
	CommsArray y;
	CommsArray id;
	int arrayStart;
	int arrayEnd;
	int length;

	CommsBotArray(int start, int end) {
		arrayStart = start;
		arrayEnd = end;
		length = (end - start)/3 + 1;

		x = new CommsArray(start, start+length-1);
		y = new CommsArray(start+length, start+length*2-1);
		id = new CommsArray(start+length*2, end);
	}

	public void writeBot(RobotController rc, RobotInfo robot) throws GameActionException {
		// check id cache

		// search array
		int index = -1;
		for(int i = 0; i < length; i++){
			int ID = id.read(rc,i);
			if(ID == 0){
				if(index == -1) index = i;
			}
			if(ID == robot.ID){
				index = i;
				break;
			}
		}

		MapLocation loc = robot.getLocation();
		x.write(rc, index, (int)loc.x);
		y.write(rc, index, (int)loc.y);
		id.write(rc, index, robot.ID);
	}

	public RobotInfo readBot(RobotController rc, RobotInfo robot) throws GameActionException {
		// todo
		return null;
	}

	public RobotInfo readBot(RobotController rc, int index) throws GameActionException {
		MapLocation loc = new MapLocation(x.read(rc, index), y.read(rc, index));
		return new RobotInfo(id.read(rc, index), null, null, loc, 0, 0, 0);
	}

	public void deleteBot(RobotController rc, RobotInfo robot) throws GameActionException {
		int index = -1;
		for(int i = 0; i < length; i++){
			if(id.read(rc,i) == robot.ID){
				x.write(rc, i, 0);
				y.write(rc, i, 0);
				id.write(rc, i, 0);
				break;
			}
		}
	}

	public RobotInfo[] arrayBots(RobotController rc) throws GameActionException {
		RobotInfo[] botArray = new RobotInfo[length];

		for(int i = 0; i < length; i++){
			MapLocation loc = new MapLocation(x.read(rc, i), y.read(rc, i));
			botArray[i] = new RobotInfo(id.read(rc, i), null, null, loc, 0, 0, 0);
		}

		return botArray;
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
