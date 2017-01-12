package turtlebot;

import battlecode.common.*;

public class BotScout {

	public static MapLocation moveTarget = null;
	public static boolean startupFlag = true;
	public static MapLocation homeMemoryLocation = null;
	public static boolean returning = false;
	public static RobotInfo enemyTarget = null;
	public static int trappedCount = 0;

	public static final float HOME_DISTANCE_DEFENSE_THRESHOLD = 7.5f;
	
	public static void turn(RobotController rc) throws GameActionException {
		MapLocation myLocation = rc.getLocation();

		RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		enemyTarget = null;
		float closestDistance = 10f;

		for (int i = 0; i < enemies.length; i++) {
			if (enemies[i].getType() == RobotType.GARDENER 
				|| enemies[i].getType() == RobotType.LUMBERJACK 
				|| rc.getRoundNum() > 500
				|| enemies[i].location.distanceTo(myLocation) < HOME_DISTANCE_DEFENSE_THRESHOLD) {
				if(enemies[i].location.distanceTo(myLocation) < closestDistance) {
					enemyTarget = enemies[i];
					closestDistance = enemies[i].location.distanceTo(myLocation);
				}
			} 
		}

		if (startupFlag) {
			homeMemoryLocation = myLocation;
			startupFlag = false;
		}

		if (moveTarget == null) {
			moveTarget = Comms.unpackLocation(rc, Comms.popStack(rc, Comms.ARCHON_SCOUT_DELEGATION_START, Comms.ARCHON_SCOUT_DELEGATION_END));
		}
		if( rc.getRoundNum() <= Comms.readGardenerUniversalHoldRound(rc) 
					&& Comms.readGardenerUniversalHoldLocation(rc).distanceTo(myLocation) >= 10f ) {
			moveTarget = Comms.readGardenerUniversalHoldLocation(rc);
			enemyTarget = null;
			returning = false;
		}
		if (enemyTarget == null) {
			if (moveTarget == null && returning == false) {
				Nav.explore(rc);
			} else {
				if (!returning) {
					Direction moveDirection = new Direction(myLocation, moveTarget);
					boolean successful = Nav.tryMove(rc, moveDirection);
					trappedCount += successful ? 0 : 1;
					if ((!rc.onTheMap(myLocation.add(moveDirection, 5f))) || trappedCount > 15 || myLocation.distanceTo(moveTarget) < 3f) {
						returning = true;
						trappedCount = 0;
					}
				} else {
					Direction moveDirection = new Direction(myLocation, homeMemoryLocation);
					boolean hasMoved = Nav.tryMove(rc, moveDirection);
					trappedCount += hasMoved ? 0 : 1;
					if (myLocation.distanceTo(homeMemoryLocation) < 3f || trappedCount > 15) {
						returning = false;
						moveTarget = null;
						trappedCount = 0;
						broadcastUnassigned(rc);
					}
				}
			}
		} else {
			Direction dir = rc.getLocation().directionTo(enemyTarget.location);
			float dist = enemyTarget.location.distanceTo(rc.getLocation());
			if (/*!Nav.avoidBullets(rc, myLocation) &&*/ !Nav.avoidLumberjacks(rc, myLocation)) {
				if ((dist >= 0f && enemyTarget.type != RobotType.LUMBERJACK) || dist >= 2f) {
					Nav.tryPrecisionMove(rc, dir, dist / 2);
				}
			}
			if (rc.canFireSingleShot()) {
				rc.fireSingleShot(dir);
			}
		}
	}

	public static void broadcastUnassigned(RobotController rc) throws GameActionException {
		Comms.writeStack(rc, Comms.SCOUT_ARCHON_REQUEST_START, Comms.SCOUT_ARCHON_REQUEST_END, Comms.packLocation(rc, rc.getLocation()));
	}

}
