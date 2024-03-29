package onemorego;
import battlecode.common.*;

enum Strategy {
	OFFENSE, DEFENSE, LUMBERJACK
}

public class Util {
	static boolean reportedDeath = false;
	static int[] botsBuilt = new int[6];
	
	
	// Track number of each bots alive
	public static void updateBotCount(RobotController rc) throws GameActionException {
		for(int i = 6;i-->0;) {
			botsBuilt[i] = Comms.readNumRobots(rc, RobotType.values()[i]);
		}
	}
	public static void reportIfDead(RobotController rc) throws GameActionException {
		if(!reportedDeath && rc.getHealth() <= 5f) {
			updateBotCount(rc);
    		Comms.writeNumRobots(rc, rc.getType(), botsBuilt[rc.getType().ordinal()] - 1);
    		reportedDeath = true;
    	}
	}
	public static int getNumBots(RobotType type) {
		return botsBuilt[type.ordinal()];
	}
	public static void increaseNumBotsByOne(RobotController rc, RobotType type) throws GameActionException {
		updateBotCount(rc);
		botsBuilt[type.ordinal()] = botsBuilt[type.ordinal()] + 1;
		Comms.writeNumRobots(rc, type, botsBuilt[type.ordinal()]);
	}
	
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
	
	public static TreeInfo findBestTree(RobotController rc, TreeInfo[] trees, RobotInfo closestEnemy) {
		TreeInfo bestTree = null;
		
		if(trees.length == 0) {
			return null;
		}

		// Find best tree
		float shortestDistanceToEnemy = 1000f;
		for(int i = trees.length;i-->0;) {
			if(trees[i].radius < 1f) {
				continue;
			}
			if(rc.senseNearbyRobots(trees[i].location, trees[i].radius, null).length != 0) {
				continue;
			}
			float distanceToEnemy = trees[i].location.distanceTo(closestEnemy.location);
			if(distanceToEnemy < shortestDistanceToEnemy) {
				shortestDistanceToEnemy = distanceToEnemy;
				bestTree = trees[i];
			}
		}
		
		return bestTree;
	}
	
	public static void communicateNearbyEnemies(RobotController rc, RobotInfo[] enemies) throws GameActionException {
		for(int i = enemies.length; i-->0;) {
			switch (enemies[i].getType()) {
				case GARDENER:
					Comms.writeAttackEnemy(rc, enemies[i].getLocation(), enemies[i].getID(), AttackGroup.A);
					Comms.writeAttackEnemy(rc, enemies[i].getLocation(), enemies[i].getID(), AttackGroup.B);
					break;
				case ARCHON:
					Comms.writeAttackEnemy(rc, enemies[i].getLocation(), enemies[i].getID(), AttackGroup.B);
					break;
				default:
					Comms.writeAttackEnemy(rc, enemies[i].getLocation(), enemies[i].getID(), AttackGroup.C);
					break;
			}
			//CLEARING
			if(enemies[i].getID() == Comms.readAttackID(rc, AttackGroup.A) && enemies[i].getHealth() < 5f) {
				Comms.clearAttackEnemy(rc, AttackGroup.A);
			}
			if(enemies[i].getID() == Comms.readAttackID(rc, AttackGroup.B) && enemies[i].getHealth() < 5f) {
				Comms.clearAttackEnemy(rc, AttackGroup.B);
			}
		}
	}
	
	public static RobotInfo getClosestEnemyExceptArchon(RobotController rc, RobotInfo[] enemies) throws GameActionException {
		
		RobotInfo closestEnemy = null;
		
		if(enemies.length == 0) {
			return null;
		}

		for(int i = 0; i < enemies.length; i++) {
			if(enemies[i].getType() != RobotType.ARCHON) {
				closestEnemy = enemies[i];
				break;
			}
		}
		
		return closestEnemy;
	}
	
	public static RobotInfo getClosestEnemy(RobotController rc, RobotInfo[] enemies) throws GameActionException {
		
		if(enemies.length == 0) {
			return null;
		}

		for(int i = 0; i<enemies.length; i++) {
			if(enemies[i].getType() == RobotType.GARDENER) {
				Comms.writeAttackEnemy(rc, enemies[i].getLocation(), enemies[i].getID(), AttackGroup.A);
			} else if (enemies[i].getType() == RobotType.ARCHON) {
				Comms.writeAttackEnemy(rc, enemies[i].getLocation(), enemies[i].getID(), AttackGroup.B);
			}
			if(enemies[i].getID() == Comms.readAttackID(rc, AttackGroup.A) && enemies[i].getHealth() < 20f) {
					Comms.clearAttackEnemy(rc, AttackGroup.A);
			}
		}
		return enemies[0];
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
		for(int i = treesBetweenUs.length; i-->0;) {
			if(!(rc.getType() == RobotType.SCOUT && treesBetweenUs[i].location.distanceTo(myLocation) < 1f) && Util.doesLineIntersectWithCircle(myLocation, enemyBot.location, treesBetweenUs[i].location, treesBetweenUs[i].getRadius())) {
				goodToShoot = false;
				break;
			}
		}
		return goodToShoot;
	}
	
}
