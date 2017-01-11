package turtlegardens;
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

        // Move randomly
        Nav.tryMove(rc, randomDirection());

        // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again

    }

	static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }
		
	
	
}
