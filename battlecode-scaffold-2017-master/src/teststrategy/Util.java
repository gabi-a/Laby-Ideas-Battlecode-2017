package teststrategy;
import battlecode.common.*;

public class Util {
	
	static Team us = RobotPlayer.rc.getTeam();
	static Team them = us.opponent();
	
	static boolean reportedDeath = false;
	
	public static void reportIfDead(RobotController rc) throws GameActionException {
		if(!reportedDeath && rc.getHealth() <= Math.max(rc.getType().maxHealth / 5, 5f)) {
			Comms.ourBotCount.decrementNumBots(rc, rc.getType());
    		reportedDeath = true;
    	}
	}
	
	public static void reportEnemyBots(RobotController rc, RobotInfo[] enemies) throws GameActionException {
		
		MapLocation myLocation = rc.getLocation();
		RobotInfo[] enemyGardeners = Comms.enemyGardenersArray.arrayBots(rc);
		
		for(int i = enemyGardeners.length;i-->0;) {
			if(enemyGardeners[i] != null) rc.setIndicatorDot(enemyGardeners[i].location, 0, 0, 255);
			if(enemyGardeners[i] != null && myLocation.distanceTo(enemyGardeners[i].location) < 3f && !rc.canSenseRobot(enemyGardeners[i].ID)) {
				Comms.enemyGardenersArray.deleteBot(rc, enemyGardeners[i]);
			}
		}
		
		RobotInfo[] enemyArchons = Comms.enemyArchonsArray.arrayBots(rc);
		
		for(int i = enemyArchons.length;i-->0;) {
			if(enemyArchons[i] != null) rc.setIndicatorDot(enemyArchons[i].location, 0, 0, 255);
			if(enemyArchons[i] != null && myLocation.distanceTo(enemyArchons[i].location) < 3f && !rc.canSenseRobot(enemyArchons[i].ID)) {
				Comms.enemyArchonsArray.deleteBot(rc, enemyArchons[i]);
			}
		}
		
		RobotInfo[] enemiesAttackingGardenersOrArchons = Comms.enemiesAttackingGardenersOrArchons.arrayBots(rc);
		
		for(int i = enemiesAttackingGardenersOrArchons.length;i-->0;) {
			if(enemiesAttackingGardenersOrArchons[i] != null) rc.setIndicatorDot(enemiesAttackingGardenersOrArchons[i].location, 0, 0, 255);
			if(enemiesAttackingGardenersOrArchons[i] != null && myLocation.distanceTo(enemiesAttackingGardenersOrArchons[i].location) < 3f && !rc.canSenseRobot(enemiesAttackingGardenersOrArchons[i].ID)) {
				Comms.enemiesAttackingGardenersOrArchons.deleteBot(rc, enemiesAttackingGardenersOrArchons[i]);
			}
		}
		
		RobotInfo[] enemiesSighted = Comms.enemiesSighted.arrayBots(rc);
		
		for(int i = enemiesSighted.length;i-->0;) {
			if(enemiesSighted[i] != null) rc.setIndicatorDot(enemiesSighted[i].location, 0, 0, 255);
			if(enemiesSighted[i] != null && myLocation.distanceTo(enemiesSighted[i].location) < 3f && !rc.canSenseRobot(enemiesSighted[i].ID)) {
				Comms.enemiesSighted.deleteBot(rc, enemiesSighted[i]);
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
				Comms.enemiesAttackingGardenersOrArchons.writeBot(rc, enemies[i]);
			}
			else {
				Comms.enemiesSighted.writeBot(rc, enemies[i]);
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
	
	static boolean doesLineIntersectWithCircle(MapLocation lineStart, Direction lineDir, MapLocation circleLocation, float circleRadius) {

		//System.out.format("Line Start: %f,%f  Circle Loc: %f,%f\n", lineStart.x,lineStart.y,circleLocation.x,circleLocation.y);
		double theta = lineDir.radiansBetween(lineStart.directionTo(circleLocation));
		theta =  theta % 2*Math.PI;
		double hypotenuse = lineStart.distanceTo(circleLocation);
		double perpendicularDist = hypotenuse * Math.sin(theta);
		
		//RobotPlayer.rc.setIndicatorLine(lineStart,lineStart.add(lineDir, 10f), 100, 50, 0);
		//RobotPlayer.rc.setIndicatorDot(circleLocation, 0, 100, 50);
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
		
		RobotInfo[] botsBetweenUs = rc.senseNearbyRobots(halfwayLocation, senseRadius, us);
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
		
		RobotInfo[] botsBetweenUs = rc.senseNearbyRobots(halfwayLocation, senseRadius, us);
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
		return null;//new RobotInfo(0, them, RobotType.ARCHON, rc.getInitialArchonLocations(them)[0], 0, 0, 0);
		
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
	
	static void shakeIfAble(RobotController rc, TreeInfo[] trees) throws GameActionException{
		if(trees.length > 0 && trees[0] != null && rc.canShake(trees[0].ID)) {
			rc.shake(trees[0].ID);
		}
	}
	
	/*
	 * Enemy prediction
	 * 
	 * 
	 * 
	 */
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
				//RobotPlayer.rc.setIndicatorLine(afterLoc, beforeLoc, 0, 0,255);
				moveDirections[i] = beforeLoc.directionTo(afterLoc);
				strideDist += beforeLoc.distanceTo(afterLoc);
				i++;
			}
			strideDist /= i;
			i = 0;
			while(i<enemyLocationsCacheSize-2) {
				//System.out.format("%d Bytecodes left: %d\n",i, Clock.getBytecodesLeft());
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
			RobotPlayer.rc.setIndicatorLine(enemyLocationsCached[0],enemyLocationsCached[0].add(nextMove,strideDist),255,0,0);
			return enemyLocationsCached[0].add(nextMove,strideDist);
			
		} else {
			//System.out.format("IDs: %d %d\n", enemyIDCached, enemyTarget.getID());
			// Clear cached locations
			// Add current location to cache
			enemyIDCached = enemyTarget.getID();
			enemyLocationsCached = new MapLocation[enemyLocationsCacheSize];
			pushEnemyLocation(enemyTarget.getLocation());
			return enemyTarget.getLocation();
		}
	}
	
}
