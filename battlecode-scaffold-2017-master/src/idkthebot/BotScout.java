package idkthebot;

import battlecode.common.*;
import turtlebotpathing.Comms;

public class BotScout {

	static RobotController rc;
	static MapLocation[] initialArchonLocations;
	static boolean startupFlag = true;
	static boolean exploreFlag = false;
	
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
			
			if(enemies[i].getType() == RobotType.GARDENER /*|| enemies[i].getType() == RobotType.ARCHON*/) {
				Comms.writeAttackEnemy(rc, enemies[i].getLocation(), enemies[i].getID());
			}
			
			if(enemies[i].getID() == Comms.readAttackID(rc)) {
				if(enemies[i].getHealth() < 20f) {
					Comms.clearAttackEnemy(rc);
				}
			}
			
			if(enemies[i].health < lowestHealth) {
				enemyTarget = enemies[i];
				lowestHealth = enemies[i].health;
			}
		}
		if(enemyTarget != null) {
			System.out.format("Bytecodes left before: %d\n", Clock.getBytecodesLeft());
			MapLocation nextEnemyLocation = Util.predictNextEnemyLocation(enemyTarget, myLocation);
			System.out.format("Bytecodes left after: %d\n", Clock.getBytecodesLeft());
			rc.setIndicatorLine(enemyTarget.getLocation(), nextEnemyLocation, 255, 0, 0);
			rc.setIndicatorDot(nextEnemyLocation, 255, 0, 0);
			rc.setIndicatorDot(enemyTarget.getLocation(), 0, 255, 0);
			RobotInfo predictedEnemy = new RobotInfo(enemyTarget.ID, enemyTarget.team,
				enemyTarget.type, nextEnemyLocation, enemyTarget.health, 
				enemyTarget.attackCount, enemyTarget.moveCount);
			
			Nav.scoutAttackMove(rc, myLocation, predictedEnemy);
			
			if (rc.canFireSingleShot()) {
				Direction dir = new Direction(myLocation, nextEnemyLocation);
				rc.fireSingleShot(dir);
			}
		}
		else {
			MapLocation moveTarget = Comms.readAttackLocation(rc);
			boolean moved = false;
			if(moveTarget != null) {
				moved = Nav.pathTo(rc, moveTarget, new RobotType[]{RobotType.SOLDIER, RobotType.LUMBERJACK, RobotType.TANK});
			}
			if(!moved) {
				if(myLocation.distanceTo(initialArchonLocations[0]) < 4f) {
					exploreFlag = true;
				}
				if(exploreFlag) {
					Nav.explore(rc);
				}
				else {
					Nav.pathTo(rc, initialArchonLocations[0], new RobotType[]{RobotType.SOLDIER, RobotType.LUMBERJACK, RobotType.TANK});
				}
			}
		}

	}
	
	
}
