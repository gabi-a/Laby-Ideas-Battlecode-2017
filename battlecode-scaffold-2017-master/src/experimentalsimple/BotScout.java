package experimentalsimple;

import battlecode.common.*;

public class BotScout {

	static RobotController rc;
	static MapLocation target = null;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotScout.rc = rc;
		
		MapLocation myLocation = rc.getLocation();
		
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		RobotInfo enemyTarget = null;
		double lowestHealth = 10000d;

		MapLocation tempTarget = Comms.popStack(rc, Comms.ENEMY_GARDENER_START, Comms.ENEMY_GARDENER_END);
		
		
		if(tempTarget != null) {
			Comms.writeStack(rc, Comms.ENEMY_GARDENER_START, Comms.ENEMY_GARDENER_END, tempTarget);
			target = tempTarget;
		} else {
			target = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
		}
		
		for (int i = 0; i < enemies.length; i++) {
			
			if(enemies[i].getType() == RobotType.GARDENER) {
				Comms.writeStack(rc, Comms.ENEMY_GARDENER_START, Comms.ENEMY_GARDENER_END, enemies[i].getLocation());
			}
			
			if (enemies[i].getType() == RobotType.GARDENER 
				|| enemies[i].getType() == RobotType.LUMBERJACK 
				|| enemies[i].getType() == RobotType.SCOUT
				|| rc.getRoundNum() > 500) {
				if(enemies[i].health < lowestHealth) {
					enemyTarget = enemies[i];
					lowestHealth = enemies[i].health;
				}
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
			moved = Nav.avoidBullets(rc, myLocation);
		if(!moved)
			Nav.pathTo(rc, target, new RobotType[]{RobotType.SOLDIER});

	}
	
	
}
