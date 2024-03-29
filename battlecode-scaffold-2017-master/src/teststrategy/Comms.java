package teststrategy;
import battlecode.common.*;
import java.util.*;

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
	
	public static final CommsInt treeUnitCount;
	
	public static final CommsInt mapSize;
	
	public static final CommsInt tankGardeners;
	
	static {
		ourBotCount = new CommsBotCount(0,6);
		theirBotCount = new CommsBotCount(10,20);
		enemyGardenersArray = new CommsBotArray(30, 40);
		enemyArchonsArray = new CommsBotArray(50, 60);
		enemiesAttackingGardenersOrArchons = new CommsBotArray(70,80);
		ourLumberjackAndSoldiers = new CommsBotArray(500,550);
		archonTreeCount = new CommsArray(110,113);
		archonCount = new CommsInt(120);
		mapSize = new CommsInt(125);
		enemiesSighted = new CommsBotArray(130,140);
		treeUnitCount = new CommsInt(150);
		tankGardeners = new CommsInt(160);
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
	int length;
	int cacheRounds = 0;
	private int[] array;
	private int[] lastUpdated;

	public CommsArray(int start, int end){
		arrayStart = start;
		arrayEnd = end;
		length = end - start + 1;
		array = new int[length];
		lastUpdated = new int[length];
		Arrays.fill(array, 0);
		Arrays.fill(lastUpdated, -1);
	}

	public void write(RobotController rc, int index, int data) throws GameActionException {
		if(index > length || index < 0){
			System.out.println("error: out of comms array bounds");
			return;
		}

		rc.broadcast(arrayStart + index, data);
		array[index] = data;
		lastUpdated[index] = rc.getRoundNum();
	}

	public int read(RobotController rc, int index) throws GameActionException {
		if(index > length || index < 0){
			System.out.println("error: out of comms array bounds");
			return -1;
		}

		if(lastUpdated[index] < rc.getRoundNum() - cacheRounds){
			array[index] = rc.readBroadcast(arrayStart + index);
			lastUpdated[index] = rc.getRoundNum();
		}

		return array[index];
	}

	public int[] array(RobotController rc) throws GameActionException {
		for(int i = 0; i < length; i++){
			if(lastUpdated[i] < rc.getRoundNum() - cacheRounds){
				array[i] = rc.readBroadcast(arrayStart + i);
				lastUpdated[i] = rc.getRoundNum();
			}
		}

		return array;
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

class CommsBotArray extends CommsArray {
	int cacheSize = 10;
	int[] recentBots;
	int[] recentBotsIndex;
	
	CommsBotArray(int start, int end) {
		super(start, end);
		recentBots = new int[cacheSize];
		recentBotsIndex = new int[cacheSize];
		Arrays.fill(recentBots, 0);
		Arrays.fill(recentBotsIndex, 0);
	}

	public int getIndex(RobotController rc, int id) throws GameActionException {
		// check cache
		for(int i = 0; i < cacheSize; i++){
			if(recentBots[i] == id) return recentBotsIndex[i];
		}

		// check array
		int index = -1;
		for(int i = 0; i < length; i++){
			RobotInfo bot = unpackBot(rc, super.read(rc, i));
			if(bot == null){
				if(index == -1) index = i;
				continue;
			}
			if(bot.ID == id){
				index = i;
				break;
			}
		}

		if(index != -1){
			for(int i = 0; i < cacheSize; i++){
				if(recentBots[i] == 0){
					recentBots[i] = id;
					recentBotsIndex[i] = index;
				}
			}
		}

		return index;
	}

	public void writeBot(RobotController rc, RobotInfo robot) throws GameActionException {
		super.write(rc, getIndex(rc, robot.ID), packBot(rc, robot));
	}

	public RobotInfo readBot(RobotController rc, RobotInfo robot) throws GameActionException {
		// todo
		return null;
	}

	public RobotInfo readBot(RobotController rc, int index) throws GameActionException {
		return unpackBot(rc, super.read(rc, index));
	}

	public void deleteBot(RobotController rc, RobotInfo robot) throws GameActionException {
		int index = -1;
		for(int i = 0; i < length; i++){
			RobotInfo bot = unpackBot(rc, super.read(rc, i));
			if(bot != null && bot.ID == robot.ID){
				super.write(rc, i, 0);
				break;
			}
		}
	}

	public RobotInfo[] arrayBots(RobotController rc) throws GameActionException {
		int[] intArray = super.array(rc);
		RobotInfo[] botArray = new RobotInfo[intArray.length];

		for(int i = 0; i < intArray.length; i++){
			botArray[i] = unpackBot(rc, intArray[i]);
		}

		return botArray;
	}

	public int packBot(RobotController rc, RobotInfo robot) {
		int loc = Comms.packLocation(rc, robot.getLocation());
		return (robot.getID() << 16) | (loc);
	}

	public RobotInfo unpackBot(RobotController rc, int data) {
		if(data == 0) return null;

		int id = (data) >> 16;
		MapLocation loc = Comms.unpackLocation(rc, data & 0xFFFF);

		return new RobotInfo(id, null, null, loc, 0, 0, 0);
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
