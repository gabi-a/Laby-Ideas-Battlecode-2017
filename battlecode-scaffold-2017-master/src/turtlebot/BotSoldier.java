package turtlebot;
import battlecode.common.*;

public class BotSoldier {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotSoldier.rc = rc;
        Team enemy = rc.getTeam().opponent();

        MapLocation myLocation = rc.getLocation();

        // See if there are any nearby enemy robots
        RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

        // If there are some...
        if (robots.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
            if (rc.canFireSingleShot()) {
                // ...Then fire a bullet in the direction of the enemy.
                rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
            }
        }
        
        if(!avoidBullets(myLocation)) {

            Nav.tryMove(rc, randomDirection());
        }
    }

	private static boolean avoidBullets(MapLocation myLocation) throws GameActionException {
		
		BulletInfo[] bullets = rc.senseNearbyBullets();
		BulletInfo bullet;
		MapLocation goalLoc = myLocation;
		if(bullets.length == 0) {
			return true;
		}
		for(int i = bullets.length;i-->0;) {
			bullet = bullets[i];
			if(willCollideWithMe(bullet, myLocation)) {
				goalLoc = goalLoc.add(bullet.dir.rotateLeftDegrees(90));
			}
		}
		if(goalLoc == myLocation) {
			return true;
		}
		return Nav.tryMove(rc, myLocation.directionTo(goalLoc));
		
	}
	
	static boolean willCollideWithMe(BulletInfo bullet, MapLocation loc) {

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

	static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }
		
	
	
}
