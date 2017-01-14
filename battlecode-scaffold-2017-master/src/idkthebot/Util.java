package idkthebot;

import battlecode.common.*;

public class Util {

	static final int enemyLocationsCacheSize = 5;
	static MapLocation[] enemyLocationsCached = new MapLocation[enemyLocationsCacheSize];
	static int enemyIDCached = -1;

	private static void pushEnemyLocation(MapLocation enemyLocation) {
		int i = 0;
		while(i<enemyLocationsCacheSize-1) {
			//System.out.println(i+","+(i+1));
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
