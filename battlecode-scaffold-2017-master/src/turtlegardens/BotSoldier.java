package turtlegardens;
import battlecode.common.*;

public class BotSoldier {
	static RobotController rc;

	public static void turn(RobotController rc) throws GameActionException {
		BotSoldier.rc = rc;
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

		float dist;
		Direction dir;
		if (target != null) {
			dir = rc.getLocation().directionTo(target.location);
			dist = target.location.distanceTo(rc.getLocation());
			rc.fireSingleShot(dir);
			if (dist >= 6 && rc.canMove(dir)) {
				rc.move(dir);
			}
			else if (dist < 4 && rc.canMove(dir.opposite())) {
				rc.move(dir.opposite());
			}
		}
        }

        private static float rateTarget(RobotInfo target) {
		float score;
		score = target.location.distanceTo(rc.getLocation());
		if (target.type == RobotType.ARCHON) score -= 2;
		return score;
	}
}
