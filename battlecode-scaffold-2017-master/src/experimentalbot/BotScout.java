package experimentalbot;

import battlecode.common.*;

public class BotScout {

	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotScout.rc = rc;
		
		MapLocation myLocation = rc.getLocation();
		
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		RobotInfo enemyTarget = null;
		double lowestHealth = 10000d;

		for (int i = 0; i < enemies.length; i++) {
			if(enemies[i].health < lowestHealth) {
				enemyTarget = enemies[i];
				lowestHealth = enemies[i].health;
			}
		}
		boolean moved = false;
		if(enemyTarget != null) {
			Direction dir = rc.getLocation().directionTo(enemyTarget.location);
			float dist = enemyTarget.location.distanceTo(rc.getLocation());
			if (/*!Nav.avoidBullets(rc, myLocation) &&*/ !Nav.avoidLumberjacks(rc, myLocation)) {
				if ((dist >= 0f && enemyTarget.type != RobotType.LUMBERJACK) || dist >= 2f) {
					moved = Nav.tryPrecisionMove(rc, dir, 1.49f);
				}
			}
			if (rc.canFireSingleShot()) {
				rc.fireSingleShot(dir);
			}
		}
		if(!moved)
			Nav.pathTo(rc, rc.getInitialArchonLocations(rc.getTeam().opponent())[0], new RobotType[]{RobotType.SOLDIER});

	}
	
	
}
