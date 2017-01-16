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
			if(enemy.type != RobotType.GARDENER && enemy.type != RobotType.SCOUT) {
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
			rc.setIndicatorDot(enemyTarget.location, 0, 255, 0);
			
			if(!Nav.avoidBullets(rc, myLocation, 1.5f)) {
				Nav.scoutAttackMove(rc, myLocation, enemyTarget);
			}
			
			// we can't use our cached location
			Direction dir = new Direction(rc.getLocation(), enemyTarget.location);
			if (rc.canFireSingleShot()) {
				rc.fireSingleShot(dir);
			}
		}
		else {
			if(myLocation.distanceTo(initialArchonLocations[0]) < 1f) {
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
