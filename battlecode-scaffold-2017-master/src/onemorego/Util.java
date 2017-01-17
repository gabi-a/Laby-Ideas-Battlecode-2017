package onemorego;
import battlecode.common.*;

public class Util {
	static boolean reportedDeath = false;
	static int[] botsBuilt = new int[6];
	
	
	// Track number of each bots alive
	public static void updateBotCount(RobotController rc) throws GameActionException {
		for(int i = 6;i-->0;) {
			botsBuilt[i] = Comms.readNumRobots(rc, RobotType.values()[i]);
		}
	}
	public static void reportDeath(RobotController rc) throws GameActionException {
		if(!reportedDeath && rc.getHealth() < 10f) {
    		Comms.writeNumRobots(rc, rc.getType(), botsBuilt[rc.getType().ordinal()]);
    		reportedDeath = true;
    	}
	}
	public static int getNumBots(RobotType type) {
		return botsBuilt[type.ordinal()];
	}
	public static void increaseNumBotsByOne(RobotController rc, RobotType type) throws GameActionException {
		botsBuilt[type.ordinal()] = botsBuilt[type.ordinal()] + 1;
		Comms.writeNumRobots(rc, type, botsBuilt[type.ordinal()]);
	}
	
	static final int enemyLocationsCacheSize = 5;
	static MapLocation[] enemyLocationsCached = new MapLocation[enemyLocationsCacheSize];
	static int enemyIDCached = -1;

	private static void pushEnemyLocation(MapLocation enemyLocation) {
		int i = 0;
		MapLocation[] tempenemyLocationsCached = new MapLocation[enemyLocationsCacheSize];
		while(i<enemyLocationsCacheSize-1) {
			//System.out.println(i+","+(i+1));
			tempenemyLocationsCached[i+1] = enemyLocationsCached[i];
			i++;
		}
		
		tempenemyLocationsCached[0] = enemyLocation;
		enemyLocationsCached = tempenemyLocationsCached;
	}
	
	public static MapLocation predictNextEnemyLocation(RobotInfo enemyTarget) throws GameActionException {
		if(enemyTarget.getID() == enemyIDCached) {
			
			// Add current location to cache
			// Run prediction
			pushEnemyLocation(enemyTarget.getLocation());
			float deltaDirectionRads = 0;
			float strideDist = 0;
			int i = 0;
			Direction[] moveDirections = new Direction[enemyLocationsCacheSize-1];
			//System.out.format("Bytecodes left: %d\n", Clock.getBytecodesLeft());
			while(i<enemyLocationsCacheSize-1) {
				//System.out.format("%d Bytecodes left: %d\n",i, Clock.getBytecodesLeft());
				MapLocation afterLoc = enemyLocationsCached[i];
				MapLocation beforeLoc = enemyLocationsCached[i+1];
				if(beforeLoc == null || beforeLoc == afterLoc) break;
				RobotPlayer.rc.setIndicatorLine(afterLoc, beforeLoc, 0, 0,255);
				moveDirections[i] = beforeLoc.directionTo(afterLoc);
				strideDist += beforeLoc.distanceTo(afterLoc);
				i++;
			}
			strideDist /= i;
			i = 0;
			while(i<enemyLocationsCacheSize-2) {
				System.out.format("%d Bytecodes left: %d\n",i, Clock.getBytecodesLeft());
				Direction afterDir = moveDirections[i];
				Direction beforeDir = moveDirections[i+1];
				if(afterDir == null || beforeDir == null) break;
				deltaDirectionRads += beforeDir.radiansBetween(afterDir);
				i++;
			}
			if (i == 0) return enemyLocationsCached[0];
			//System.out.format("Bytecodes left: %d\n", Clock.getBytecodesLeft());
			deltaDirectionRads /= i;
			Direction nextMove = moveDirections[i-1].rotateLeftRads(deltaDirectionRads);
			
			//float distToEnemy = myLocation.distanceTo(enemyLocationsCached[0]);
			//float turnsToImpact = distToEnemy / RobotType.SCOUT.bulletSpeed ;
			
			return enemyLocationsCached[0].add(nextMove,strideDist);
			
		} else {
			System.out.format("IDs: %d %d\n", enemyIDCached, enemyTarget.getID());
			// Clear cached locations
			// Add current location to cache
			enemyIDCached = enemyTarget.getID();
			enemyLocationsCached = new MapLocation[enemyLocationsCacheSize];
			pushEnemyLocation(enemyTarget.getLocation());
			return enemyTarget.getLocation();
		}
	}
	
	public static TreeInfo findBestTree(RobotController rc, TreeInfo[] trees, RobotInfo closestEnemy) {
		TreeInfo bestTree = null;
		
		if(trees.length == 0) {
			return null;
		}

		// Find best tree
		float shortestDistanceToEnemy = 1000f;
		for(int i = trees.length;i-->0;) {
			if(trees[i].radius < 1f) {
				continue;
			}
			if(rc.senseNearbyRobots(trees[i].location, trees[i].radius, null).length != 0) {
				continue;
			}
			float distanceToEnemy = trees[i].location.distanceTo(closestEnemy.location);
			if(distanceToEnemy < shortestDistanceToEnemy) {
				shortestDistanceToEnemy = distanceToEnemy;
				bestTree = trees[i];
			}
		}
		
		return bestTree;
	}
	
	static boolean doesLineItersectWithCircle(MapLocation lineStart, MapLocation lineEnd, MapLocation circleLocation, float circleRadius) {
		
		float theta = lineStart.directionTo(lineEnd).radiansBetween(lineStart.directionTo(circleLocation));
		float hypotenuse = lineStart.distanceTo(circleLocation);
		float perpendicularDist = (float) (hypotenuse * Math.sin(theta));
		
		return perpendicularDist < circleRadius;
		
	}
	
	
}
