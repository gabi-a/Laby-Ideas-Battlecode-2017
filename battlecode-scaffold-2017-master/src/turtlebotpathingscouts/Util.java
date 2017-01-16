package turtlebotpathingscouts;
import battlecode.common.*;

public class Util {
	
	static boolean reportedDeath = false;
	static int[] botsBuilt = new int[6];
	
	
	// Track number of each bots alive
	public static void updateBotCount(RobotController rc) throws GameActionException {
		for(int i = 6;i-->0;) {
			botsBuilt[i] = Comms.readNumRobots(rc, RobotType.values()[i]);
		}
	}
	public static void reportDeath(RobotController rc) throws GameActionException {
		if(!reportedDeath && rc.getHealth() < 10f) {
    		Comms.writeNumRobots(rc, rc.getType(), botsBuilt[rc.getType().ordinal()]);
    		reportedDeath = true;
    	}
	}
	public static int getNumBots(RobotType type) {
		return botsBuilt[type.ordinal()];
	}
	public static void increaseNumBotsByOne(RobotController rc, RobotType type) throws GameActionException {
		botsBuilt[type.ordinal()] = botsBuilt[type.ordinal()] + 1;
		Comms.writeNumRobots(rc, type, botsBuilt[type.ordinal()]);
	}
	
public static boolean willGetHit(RobotController rc, MapLocation myLocation) throws GameActionException {
		
		BulletInfo[] bullets = rc.senseNearbyBullets();
		BulletInfo bullet;
		if(bullets.length == 0) {
			return false;
		}
		for(int i = bullets.length;i-->0;) {
			bullet = bullets[i];
			if(willCollideWithMe(rc, bullet, myLocation)) {
				return true;
			}
		}
		return false;
	}	
	
	static boolean willCollideWithMe(RobotController rc, BulletInfo bullet, MapLocation loc) {

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(loc);
        float distToRobot = bulletLocation.distanceTo(loc);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
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
	
	public static MapLocation predictNextEnemyLocation(RobotInfo enemyTarget, MapLocation myLocation) throws GameActionException {
		if(enemyTarget.getID() == enemyIDCached) {
			// Add current location to cache
			// Run prediction
			pushEnemyLocation(enemyTarget.getLocation());
			float deltaDirectionRads = 0;
			float strideDist = 0;
			int i = 0;
			Direction[] moveDirections = new Direction[enemyLocationsCacheSize-1];
			System.out.format("Bytecodes left: %d\n", Clock.getBytecodesLeft());
			while(i<enemyLocationsCacheSize-1) {
				System.out.format("%d Bytecodes left: %d\n",i, Clock.getBytecodesLeft());
				MapLocation afterLoc = enemyLocationsCached[i];
				MapLocation beforeLoc = enemyLocationsCached[i+1];
				if(beforeLoc == null || beforeLoc == afterLoc) break;
				RobotPlayer.rc.setIndicatorLine(afterLoc, beforeLoc, 0, 0,255);
				moveDirections[i] = beforeLoc.directionTo(afterLoc);
				strideDist += beforeLoc.distanceTo(afterLoc);
				i++;
			}
			strideDist /= i;
			i = 0;
			while(i<enemyLocationsCacheSize-2) {
				System.out.format("%d Bytecodes left: %d\n",i, Clock.getBytecodesLeft());
				Direction afterDir = moveDirections[i];
				Direction beforeDir = moveDirections[i+1];
				if(afterDir == null || beforeDir == null) break;
				deltaDirectionRads += beforeDir.radiansBetween(afterDir);
				i++;
			}
			if (i == 0) return enemyLocationsCached[0];
			System.out.format("Bytecodes left: %d\n", Clock.getBytecodesLeft());
			deltaDirectionRads /= i;
			Direction nextMove = moveDirections[i-1].rotateLeftRads(deltaDirectionRads);
			
			float distToEnemy = myLocation.distanceTo(enemyLocationsCached[0]);
			float turnsToImpact = distToEnemy / RobotType.SCOUT.bulletSpeed ;
			
			return enemyLocationsCached[0].add(nextMove,strideDist*turnsToImpact);
			
		} else {
			System.out.format("IDs: %d %d\n", enemyIDCached, enemyTarget.getID());
			// Clear cached locations
			// Add current location to cache
			enemyIDCached = enemyTarget.getID();
			enemyLocationsCached = new MapLocation[enemyLocationsCacheSize];
			pushEnemyLocation(enemyTarget.getLocation());
			return enemyTarget.getLocation();
		}
	}
	
	public static MapLocation halfwayLocation(MapLocation loc1, MapLocation loc2) {
		return loc1.add(new Direction(loc1, loc2), loc1.distanceTo(loc2) / 2);
	}
	

	public static RobotInfo getClosestBot(RobotController rc, RobotInfo[] bots) {
		RobotInfo closestBot = null;
		RobotInfo bot;
		float closestDist = 100;
		MapLocation myLocation = rc.getLocation();
		for(int i = bots.length;i-->0;) {
			bot = bots[i];
			float dist = myLocation.distanceTo(bot.getLocation());
			if(dist < closestDist) {
				closestDist = dist;
				closestBot = bot;
			}
		}
		return closestBot;
	}
	
	public static TreeInfo getClosestTree(RobotController rc, TreeInfo[] trees) {
		TreeInfo closestTree = null;
		TreeInfo tree;
		float closestDist = 100;
		MapLocation myLocation = rc.getLocation();
		for(int i = trees.length;i-->0;) {
			tree = trees[i];
			float dist = myLocation.distanceTo(tree.getLocation());
			if(dist < closestDist) {
				closestDist = dist;
				closestTree = tree;
			}
		}
		return closestTree;
	}

}
