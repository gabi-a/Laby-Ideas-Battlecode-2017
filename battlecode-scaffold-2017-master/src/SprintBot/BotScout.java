package SprintBot;

import battlecode.common.*;

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
		double enemyScore = -1000d;

		for (RobotInfo enemy : enemies) {
			if(enemy.type != RobotType.GARDENER) {
				continue;
			}
			double curEnemyScore = 0d;
			curEnemyScore -= enemy.location.distanceTo(myLocation);
			curEnemyScore -= enemy.health / 10;
			if(enemy.location.distanceTo(myLocation) <= 8f) {
				curEnemyScore += rc.senseNearbyTrees(enemy.location, 2f, enemy.team).length * 5f;
			}
			if(curEnemyScore > enemyScore) {
				enemyTarget = enemy;
				enemyScore = curEnemyScore;
			}
		}
		
		if(enemyTarget != null) {
			
			Nav.scoutAttackMove(rc, myLocation, enemyTarget);
			
			if (rc.canFireSingleShot() && myLocation.distanceTo(enemyTarget.location) <= 1.01f) {
				Direction dir = new Direction(myLocation, enemyTarget.location);
				rc.fireSingleShot(dir);
			}
		}
		else {
			if(myLocation.distanceTo(initialArchonLocations[0]) < 4f) {
				exploreFlag = true;
			}
			if(exploreFlag) {
				Nav.explore(rc);
			}
			else {
				Nav.pathTo(rc, initialArchonLocations[0], new RobotType[]{RobotType.SOLDIER, RobotType.LUMBERJACK, RobotType.TANK, RobotType.SCOUT});
			}
		}
	}
}
