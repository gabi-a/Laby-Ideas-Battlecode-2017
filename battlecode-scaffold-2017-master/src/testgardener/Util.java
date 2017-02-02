package testgardener;
import battlecode.common.*;

public class Util {

	static final int enemyLocationsCacheSize = 2;
	
	static MapLocation[] enemyLocationsCached = new MapLocation[enemyLocationsCacheSize];
	static int enemyIDCached = -1;
	
	public static RobotInfo getClosestBot(RobotController rc, RobotInfo[] bots) {
		RobotInfo closestBot = null;
		RobotInfo bot;
		float closestDist = 100;
		MapLocation myLocation = rc.getLocation();
		for(int i = bots.length;i-->0;) {
			bot = bots[i];
			float dist = myLocation.distanceTo(bot.getLocation());
			if(dist < closestDist) {
				closestDist = dist;
				closestBot = bot;
			}
		}
		return closestBot;
	}
	
	public static TreeInfo getClosestTree(RobotController rc, TreeInfo[] trees) {
		TreeInfo closestTree = null;
		TreeInfo tree;
		float closestDist = 100;
		MapLocation myLocation = rc.getLocation();
		for(int i = trees.length;i-->0;) {
			tree = trees[i];
			float dist = myLocation.distanceTo(tree.getLocation());
			if(dist < closestDist) {
				closestDist = dist;
				closestTree = tree;
			}
		}
		return closestTree;
	}
	
	private static void pushEnemyLocation(MapLocation enemyLocation) {
		int i = 0;
		while(i<enemyLocationsCacheSize-1) {
			System.out.println(i+","+(i+1));
			enemyLocationsCached[i+1] = enemyLocationsCached[i];
			i++;
		}
		enemyLocationsCached[0] = enemyLocation;
	}
	
	public static MapLocation predictNextEnemyLocation(RobotInfo enemyTarget) {
		
		if(enemyTarget.getID() == enemyIDCached) {
			// Add current location to cache
			// Run prediction
			pushEnemyLocation(enemyTarget.getLocation());
			float directionRads = 0;
			float strideDist = 0;
			int i = 0;
			while(i<enemyLocationsCacheSize-1) {
				MapLocation beforeLoc = enemyLocationsCached[i];
				MapLocation afterLoc = enemyLocationsCached[i+1];
				if(afterLoc == null) {
					break;
				}
				strideDist += beforeLoc.distanceTo(afterLoc);
				if(beforeLoc != afterLoc) {
					directionRads += afterLoc.directionTo(beforeLoc).radians;
				}
				i++;
			}
			strideDist /= i;
			directionRads /= i;
			return enemyTarget.getLocation().add(new Direction(directionRads), strideDist);
		} else {
			// Clear cached locations
			// Add current location to cache
			enemyIDCached = enemyTarget.getID();
			enemyLocationsCached = new MapLocation[enemyLocationsCacheSize];
			pushEnemyLocation(enemyTarget.getLocation());
			return enemyTarget.getLocation();
		}
	}
	

}
