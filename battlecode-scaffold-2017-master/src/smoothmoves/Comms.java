package smoothmoves;
import battlecode.common.*;

public class Comms {

	public static final int NumChannelsToReadEachRound = 5;
	public static final int NumChannelsToWriteEachRound = 5;
	
	public static final int[] LocalComms;
	public static final int[] ActualComms;
	public static final int CommsStart = 0;
	public static final int CommsEnd = 100;
	
	public static int currentReadChannel = 0;
	public static int currentWriteChannel = 0;
	
	/* Example usage:
	 * Comms.neutralTrees.push(RobotController rc, TreeInfo stuff);
	 * TreeInfo stuff = Comms.neutralTrees.pop(RobotController rc);
	 */

	public static final CommsBotCount ourBotCount;
	public static final CommsBotCount theirBotCount;
	
	public static final CommsBotArray enemyGardenersArray;
	public static final CommsBotArray enemyArchonsArray;
	
	public static final CommsBotArray enemiesAttackingUs;
	
	public static final CommsBotArray ourLumberjackAndSoldiers;
	
	public static final CommsInt archonCount;
	public static final CommsArray archonTreeCount;
	
	static {
		
		LocalComms = new int[CommsEnd];
		ActualComms = new int[CommsEnd];
		/*
		 * IMPORTANT
		 * 
		 * Make sure the channels are all within the size of the local comms array!
		 * e.g. CommsEnd = 100, so DO NOT initialize an int as 
		 * new CommsInt(101);
		 * 
		 */
		ourBotCount = new CommsBotCount(0,6);
		theirBotCount = new CommsBotCount(7,13);
		enemyGardenersArray = new CommsBotArray(14, 24);
		enemyArchonsArray = new CommsBotArray(25, 35);
		enemiesAttackingUs = new CommsBotArray(36,46);
		ourLumberjackAndSoldiers = new CommsBotArray(47,67);
		archonTreeCount = new CommsArray(68,71);
		archonCount = new CommsInt(72);
	}
	
	public static void readLocalComms(RobotController rc) throws GameActionException {
		
		int stop = currentReadChannel + NumChannelsToReadEachRound;
		
		while(currentReadChannel++ < stop) {
			// Uncomment the line below to crash your terminal
			//System.out.println("\n The read channel is:"+currentReadChannel);
			if(currentReadChannel == CommsEnd) {
				stop -= currentReadChannel;
				currentReadChannel = 0;
			}
			LocalComms[currentReadChannel] = rc.readBroadcast(currentReadChannel);
			ActualComms[currentReadChannel] = LocalComms[currentReadChannel];
		}
		currentReadChannel--;
	}
	
	public static void writeLocalComms(RobotController rc) throws GameActionException {
		int updated = 0;
		int stop = currentWriteChannel;
		
		while(updated < 5 && ++currentWriteChannel != stop) {
			if(currentWriteChannel == CommsEnd) {
				currentWriteChannel = 0;
			}
			if(ActualComms[currentWriteChannel] != LocalComms[currentWriteChannel]) {
				rc.broadcast(currentWriteChannel, LocalComms[currentWriteChannel]);
				System.out.println("Update channel "+currentWriteChannel);
				updated++;
			}
		}
		
		/*
		while(currentWriteChannel++ < stop) {
			if(currentWriteChannel == CommsEnd) {
				stop -= currentWriteChannel;
				currentWriteChannel = 0;
			}
			if(ActualComms[currentWriteChannel] != LocalComms[currentWriteChannel]) {
				rc.broadcast(currentWriteChannel, LocalComms[currentWriteChannel]);
				System.out.println("Update channel "+currentWriteChannel);
				updated++;
			} else {
				stop++;
			}
		}
		currentWriteChannel--;
		System.out.println(updated+" channels updated ");
		*/
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
		Comms.LocalComms[index] = data;
	}

	public int read(RobotController rc) throws GameActionException {
		return Comms.LocalComms[index];
		//return Comms.LocalComms[index];
	}

	public void increment(RobotController rc, int increment) throws GameActionException {
		Comms.LocalComms[index] = Comms.LocalComms[index] + increment;
	}

	public void decrement(RobotController rc, int decrement) throws GameActionException {
		Comms.LocalComms[index] = Comms.LocalComms[index] - decrement;
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
		int stackPointer = Comms.LocalComms[stackStart];
		
		if (stackStart + stackPointer + 1 > stackEnd) {
			System.out.println("Oops! Exceeded stack limit.");
			return;
		}

		Comms.LocalComms[stackStart + offset + stackPointer] = data;
		Comms.LocalComms[stackStart] = stackPointer + 1;
	}

	public int pop(RobotController rc) throws GameActionException {
		int stackPointer = Comms.LocalComms[stackStart];
		
		if (stackPointer == 0) {
			return -1;
		}
		
		stackPointer--;
		
		int data = Comms.LocalComms[stackStart + offset + stackPointer];

		Comms.LocalComms[stackStart + offset + stackPointer] = 0;
		Comms.LocalComms[stackStart] = stackPointer;
		
		return data;
	}

	public int length(RobotController rc) throws GameActionException {
		return Comms.LocalComms[stackStart];
	}

	public int[] array(RobotController rc) throws GameActionException {
		int stackPointer = Comms.LocalComms[stackStart];
		if (stackPointer == 0) {
			return null;
		}

		int[] data = new int[stackPointer];
		for(int i = 0; i < stackPointer; i++){
			data[i] = Comms.LocalComms[stackStart + offset + i];
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
		int queueData = Comms.LocalComms[queueStart];
		int head = (queueData & 0xFF00) >> 8;
		int tail = queueData & 0x00FF;

		int newTail = tail + 1;
		if(newTail > queueEnd) newTail = queueStart + offset;

		if(newTail == head){
			System.out.println("queue overflow");
			return;
		}

		Comms.LocalComms[queueStart + offset + tail] = data;

		queueData = (head << 8) | (newTail);
		Comms.LocalComms[queueStart] = queueData;
	}

	public int pop(RobotController rc) throws GameActionException {
		int queueData = Comms.LocalComms[queueStart];
		int head = (queueData & 0xFF00) >> 8;
		int tail = queueData & 0x00FF;

		int newHead = head + 1;
		if(newHead > queueEnd) newHead = queueStart + offset;

		if(tail == head) {
			return -1;	// queue is empty
		}

		int data = Comms.LocalComms[queueStart + offset + head];

		queueData = (newHead << 8) | (tail);
		Comms.LocalComms[queueStart] = queueData;

		return data;
	}

	public int length(RobotController rc) throws GameActionException {
		int queueData = Comms.LocalComms[queueStart];
		int head = (queueData & 0xFF00) >> 8;
		int tail = queueData & 0x00FF;

		return head > tail? head - tail : tail - head;
	}

	public int[] array(RobotController rc) throws GameActionException {
		int queueData = Comms.LocalComms[queueStart];
		int head = (queueData & 0xFF00) >> 8;
		int tail = queueData & 0x00FF;

		if(tail == head) {
			return null;	// queue is empty
		}

		int length = head > tail? head - tail : tail - head;

		int[] data = new int[length];
		for(int i = 0; i < length; i++){
			if(head > queueEnd) head = queueStart + offset;
			data[i] = Comms.LocalComms[queueStart + offset + head];
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

		Comms.LocalComms[arrayStart + index] = data;
		// array[index] = data;
		// lastUpdated[index] = rc.getRoundNum();
	}

	public int read(RobotController rc, int index) throws GameActionException {
		if(index > arrayEnd - arrayStart){
			System.out.println("error: out of comms array bounds");
			return -1;
		}

		return Comms.LocalComms[arrayStart + index];

		// if(lastUpdated[index] != rc.getRoundNum()){
			// array[index] = Comms.LocalComms[arrayStart + index];
			// lastUpdated[index] = rc.getRoundNum();
		// }

		// return array[index];
	}

	public int[] array(RobotController rc) throws GameActionException {
		int[] data = new int[arrayEnd - arrayStart];
		for(int i = 0; i < arrayEnd - arrayStart; i++){
			data[i] = Comms.LocalComms[arrayStart + i];
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

class CommsBotArray extends CommsArray {
	CommsBotArray(int start, int end) {
		super(start, end);
	}

	public void writeBot(RobotController rc, RobotInfo robot) throws GameActionException {
		// check id cache

		// search array
		int index = -1;
		for(int i = 0; i < super.length(); i++){
			RobotInfo bot = unpackBot(rc, super.read(rc, i));
			if(bot == null){
				if(index == -1) index = i;
				continue;
			}
			if(bot.ID == robot.ID){
				index = i;
				break;
			}
		}

		super.write(rc, index, packBot(rc, robot));
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
		for(int i = 0; i < super.length(); i++){
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
