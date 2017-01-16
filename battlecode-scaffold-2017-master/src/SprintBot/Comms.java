package SprintBot;
import battlecode.common.*;

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

class CommsTree {
	CommsStack trees;

	public CommsTree(int start, int end) {
		trees = new CommsStack(start, end);
	}

	public static int packTree(RobotController rc, TreeInfo tree){
		return Comms.packLocation(rc, tree.getLocation()) + tree.getID()*(int)Math.pow(2,20);
	}

	public static TreeInfo unpackTree(RobotController rc, int data){
		int id = data/(int)Math.pow(2, 20);
		MapLocation loc = Comms.unpackLocation(rc, data - id*(int)Math.pow(2,20));
		if(loc == null) return null;

		return new TreeInfo(id, null, loc, 0, 0, 0, null);
	}
}

public class Comms {
	public static final CommsTree neutralTrees;
	public static final CommsTree enemyTrees;

	/* Example usage:
	 * Comms.neutralTrees.push(TreeInfo stuff);
	 * TreeInfo stuff = Comms.neutralTrees.pop();
	 */

	static {
		neutralTrees = new CommsTree(0,10);
		enemyTrees = new CommsTree(11,21);
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
		int mapZoneX = (packedLocation & 0xFF00) >> 8;
		int mapZoneY = packedLocation & 0x00FF;
		
		Team myTeam = rc.getTeam();
		MapLocation referencePoint = (rc.getInitialArchonLocations(myTeam))[0];
		MapLocation cornerPoint = referencePoint.add((float) Math.PI, 100).add((float) Math.PI * 0.5f, -100);
		
		
		return new MapLocation(cornerPoint.x + mapZoneX, cornerPoint.y + mapZoneY);
	}
}
