package seeding;
import battlecode.common.*;

public class Util {
	
	static boolean reportedDeath = false;
	
	public static void reportIfDead(RobotController rc) throws GameActionException {
		if(!reportedDeath && rc.getHealth() <= Math.max(rc.getType().maxHealth / 5, 5f)) {
			Comms.ourBotCount.decrementNumBots(rc, rc.getType());
    		reportedDeath = true;
    	}
	}
	
	public static void reportEnemyBots(RobotController rc, RobotInfo[] enemies) throws GameActionException {
		
		RobotInfo[] enemyGardeners = Comms.enemyGardenersArray.arrayBots(rc);
		
		for(int i = enemyGardeners.length;i-->0;) {
			if(enemyGardeners[i] != null) rc.setIndicatorDot(enemyGardeners[i].location, 0, 0, 255);
			if(enemyGardeners[i] != null && rc.canSenseLocation(enemyGardeners[i].location) && !rc.canSenseRobot(enemyGardeners[i].ID)) {
				Comms.enemyGardenersArray.deleteBot(rc, enemyGardeners[i]);
			}
		}
		
		RobotInfo[] enemyArchons = Comms.enemyArchonsArray.arrayBots(rc);
		
		for(int i = enemyArchons.length;i-->0;) {
			if(enemyArchons[i] != null) rc.setIndicatorDot(enemyArchons[i].location, 0, 0, 255);
			if(enemyArchons[i] != null && rc.canSenseLocation(enemyArchons[i].location) && !rc.canSenseRobot(enemyArchons[i].ID)) {
				Comms.enemyArchonsArray.deleteBot(rc, enemyArchons[i]);
			}
		}
		
		RobotInfo[] enemiesAttackingUs = Comms.enemiesAttackingUs.arrayBots(rc);
		
		for(int i = enemiesAttackingUs.length;i-->0;) {
			if(enemiesAttackingUs[i] != null) rc.setIndicatorDot(enemiesAttackingUs[i].location, 0, 0, 255);
			if(enemiesAttackingUs[i] != null && rc.canSenseLocation(enemiesAttackingUs[i].location) && !rc.canSenseRobot(enemiesAttackingUs[i].ID)) {
				Comms.enemiesAttackingUs.deleteBot(rc, enemiesAttackingUs[i]);
			}
		}
		
		for(int i = enemies.length;i-->0;) {
			if(enemies[i].type == RobotType.GARDENER && enemies[i].health > 5f) {
				Comms.enemyGardenersArray.writeBot(rc, enemies[i]);
			}
			else if(enemies[i].type == RobotType.ARCHON && enemies[i].health > 5f) {
				Comms.enemyArchonsArray.writeBot(rc, enemies[i]);
			}
			else if(rc.getType() == RobotType.GARDENER || rc.getType() == RobotType.ARCHON) {
				Comms.enemiesAttackingUs.writeBot(rc, enemies[i]);
			}
		}
		
	}
	
	// Uses approx 40 bytecodes
	static boolean doesLineIntersectWithCircle(MapLocation lineStart, MapLocation lineEnd, MapLocation circleLocation, float circleRadius) {

		if(lineStart.directionTo(circleLocation) == null || lineEnd.directionTo(circleLocation) == null) return false;
		
		//System.out.format("Line Start: %f,%f  Circle Loc: %f,%f\n", lineStart.x,lineStart.y,circleLocation.x,circleLocation.y);
		float theta = lineStart.directionTo(lineEnd).radiansBetween(lineStart.directionTo(circleLocation));
		float hypotenuse = lineStart.distanceTo(circleLocation);
		float perpendicularDist = (float) (hypotenuse * Math.sin(theta));
		
		RobotPlayer.rc.setIndicatorLine(lineStart,lineEnd, 100, 50, 0);
		RobotPlayer.rc.setIndicatorDot(circleLocation, 0, 100, 50);
		return perpendicularDist < circleRadius;
		
	}
	
	public static MapLocation halfwayLocation(MapLocation loc1, MapLocation loc2) {
		return loc1.add(new Direction(loc1, loc2), loc1.distanceTo(loc2) / 2);
	}
	
	
	public static boolean goodToShoot(RobotController rc, MapLocation myLocation, RobotInfo enemyBot) {
		
		MapLocation halfwayLocation = Util.halfwayLocation(myLocation, enemyBot.location);
		float senseRadius = myLocation.distanceTo(enemyBot.location) - RobotType.SOLDIER.bodyRadius - enemyBot.getType().bodyRadius;
		
		// There is no space in between us so no point continuing
		if(senseRadius < 0.5f) return true;
		
		RobotInfo[] botsBetweenUs = rc.senseNearbyRobots(halfwayLocation, senseRadius, rc.getTeam());
		TreeInfo[] treesBetweenUs = rc.senseNearbyTrees(halfwayLocation, senseRadius, null);
		boolean goodToShoot = true;
		for(int i = botsBetweenUs.length; i-->0;) {
			if(botsBetweenUs[i].getID() != enemyBot.getID() && botsBetweenUs[i].getID() != rc.getID() && Util.doesLineIntersectWithCircle(myLocation, enemyBot.location, botsBetweenUs[i].location, botsBetweenUs[i].getRadius())) {
				goodToShoot = false;
				break;
			}
		}
		/*
		for(int i = treesBetweenUs.length; i-->0;) {
			if(!(rc.getType() == RobotType.SCOUT && treesBetweenUs[i].location.distanceTo(myLocation) < 1f) && Util.doesLineIntersectWithCircle(myLocation, enemyBot.location, treesBetweenUs[i].location, treesBetweenUs[i].getRadius())) {
				goodToShoot = false;
				break;
			}
		}
		*/
		return goodToShoot;
	}
	
	public static boolean goodToShootNotTrees(RobotController rc, MapLocation myLocation, RobotInfo enemyBot) {
		
		MapLocation halfwayLocation = Util.halfwayLocation(myLocation, enemyBot.location);
		float senseRadius = myLocation.distanceTo(enemyBot.location) - RobotType.SOLDIER.bodyRadius - enemyBot.getType().bodyRadius;
		
		// There is no space in between us so no point continuing
		if(senseRadius < 0.5f) return true;
		
		RobotInfo[] botsBetweenUs = rc.senseNearbyRobots(halfwayLocation, senseRadius, rc.getTeam());
		TreeInfo[] treesBetweenUs = rc.senseNearbyTrees(halfwayLocation, senseRadius, null);
		boolean goodToShoot = true;
		for(int i = botsBetweenUs.length; i-->0;) {
			if(botsBetweenUs[i].getID() != enemyBot.getID() && botsBetweenUs[i].getID() != rc.getID() && Util.doesLineIntersectWithCircle(myLocation, enemyBot.location, botsBetweenUs[i].location, botsBetweenUs[i].getRadius())) {
				goodToShoot = false;
				break;
			}
		}
		
		for(int i = treesBetweenUs.length; i-->0;) {
			if(!(rc.getType() == RobotType.SCOUT && treesBetweenUs[i].location.distanceTo(myLocation) < 1f) && Util.doesLineIntersectWithCircle(myLocation, enemyBot.location, treesBetweenUs[i].location, treesBetweenUs[i].getRadius())) {
				goodToShoot = false;
				break;
			}
		}
		
		return goodToShoot;
	}
	
	public static int[] countBots(RobotInfo[] bots) {
		int[] count = {0,0,0,0,0,0};
		for(int i = bots.length;i-->0;) {
			count[bots[i].type.ordinal()] += 1;
		}
		return count;
	}

	public static void updateMyPostion(RobotController rc) throws GameActionException {
		switch(rc.getType()) {
		case SOLDIER:
			Comms.ourLumberjackAndSoldiers.writeBot(rc, new RobotInfo(rc.getID(), null, null, rc.getLocation(), 0f, 0, 0) );
			break;
		case LUMBERJACK:
			Comms.ourLumberjackAndSoldiers.writeBot(rc, new RobotInfo(rc.getID(), null, null, rc.getLocation(), 0f, 0, 0) );
			break;
		}
	}
	
	public static MapLocation getClosestToEnemyBase(RobotController rc) throws GameActionException {
		RobotInfo enemy = getBestPassiveEnemy(rc);
		if(enemy == null) return null;
		MapLocation enemyLocation = enemy.location;
		if(enemyLocation == null) return null;
		RobotInfo[] ourBots = Comms.ourLumberjackAndSoldiers.arrayBots(rc);
		float dmin = 10000f;
		RobotInfo bestBot = null;
		for(int i = ourBots.length;i-->0;) {
			if(ourBots[i] != null && ourBots[i].location.distanceTo(enemyLocation) < dmin) {
				dmin = ourBots[i].location.distanceTo(enemyLocation);
				bestBot = ourBots[i];
			}
		}
		if (bestBot == null) return null;
		return bestBot.location;
	}
	
	public static RobotInfo getBestPassiveEnemy(RobotController rc ) throws GameActionException {
		
		boolean foundGardener = false;
		RobotInfo[] enemyGardeners = Comms.enemyGardenersArray.arrayBots(rc);
		for(int i = 0; i < enemyGardeners.length;i++) {
			if(enemyGardeners[i] != null) {
				return enemyGardeners[i];
			}
		}
		if(!foundGardener) {
			RobotInfo[] enemyArchons = Comms.enemyArchonsArray.arrayBots(rc);
			for(int i = 0; i < enemyArchons.length;i++) {
				if(enemyArchons[i] != null) {
					return enemyArchons[i];
				}
			}
		}
		return null;
		
	}
		
	static RobotInfo getGardenerAndAllPassive(RobotInfo[] enemies) {
		RobotInfo gardener = null;
		for(int i = 0; i < enemies.length;i++) {
			if(enemies[i].type == RobotType.GARDENER) {
				gardener = enemies[i];
			}
			else if(enemies[i].type != RobotType.ARCHON && enemies[i].health > 0.5f * enemies[i].type.maxHealth) {
				return null;
			}
		}
		return gardener;
	}
}
