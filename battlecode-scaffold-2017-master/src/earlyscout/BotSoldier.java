package earlyscout;
import battlecode.common.*;

public class BotSoldier {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotSoldier.rc = rc;
		Team enemy = rc.getTeam().opponent();

		MapLocation myLocation = rc.getLocation();
		RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		RobotInfo target = null;
		float targetRating = 10000;
		float curRating;
		for (int i = 0; i < robots.length; i++) {
			curRating = rateTarget(robots[i]);
			if (curRating < targetRating) {
				target = robots[i];
				targetRating = curRating;
			}
		}

		float dist = 0;
		Direction dir = new Direction(0);
		if (target != null) {
			dir = rc.getLocation().directionTo(target.location);
			dist = target.location.distanceTo(rc.getLocation());
		}

	        if(!Nav.avoidBullets(rc, myLocation)) {
			if (target != null) {
				if (dist >= 6) {
					Nav.tryMove(rc, dir);
				}
				else if (dist < 4 && rc.canMove(dir.opposite())) {
					Nav.tryMove(rc, dir.opposite());
				}
			}
			else {
				Nav.tryMove(rc, Nav.randomDirection());
			}
		}

		if (target != null && rc.canFireSingleShot()) {
			rc.fireSingleShot(dir);
		}
	}

        private static float rateTarget(RobotInfo target) {
		float score;
		score = target.location.distanceTo(rc.getLocation());
		if (target.type == RobotType.ARCHON) score -= 2;
		return score;
	}

	
		
	
	
}
