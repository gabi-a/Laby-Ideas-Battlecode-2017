package experimentalbot;

import battlecode.common.*;

public class BotScout {

	static RobotController rc;
	static MapLocation[] initialArchonLocations;
	static boolean startupFlag = true;
	static RobotInfo enemyCache = null;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotScout.rc = rc;
		
		MapLocation myLocation = rc.getLocation();
		
		if(startupFlag) {
			// costs 100 bytecodes per call! cache it
			initialArchonLocations = rc.getInitialArchonLocations(rc.getTeam().opponent());
			startupFlag = false;
		}
		
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		RobotInfo enemyTarget = null;
		double lowestHealth = 10000d;

		for (int i = 0; i < enemies.length; i++) {
			if(enemies[i].health < lowestHealth) {
				enemyTarget = enemies[i];
				lowestHealth = enemies[i].health;
			}
		}
		if(enemyTarget != null) {
			if(enemyCache == null || enemyCache.ID != enemyTarget.ID) {
				enemyCache = enemyTarget;
			}
			Nav.scoutAttackMove(rc, myLocation, enemyTarget);
			
			// This is all you have to do for prediction! 
			MapLocation nextEnemyLocation = Util.predictNextEnemyLocation(enemyTarget);
			
			
			if (rc.canFireSingleShot()) {
				Direction dir = new Direction(myLocation, nextEnemyLocation);
				rc.fireSingleShot(dir);
			}
		}
		else {
			Nav.pathTo(rc, initialArchonLocations[0], new RobotType[]{RobotType.SOLDIER, RobotType.LUMBERJACK, RobotType.TANK});
		}

	}
	
	
}
