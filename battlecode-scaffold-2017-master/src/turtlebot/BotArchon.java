package turtlebot;

import battlecode.common.*;

public class BotArchon {

	public static final float GARDEN_DISTANCE = 10f;
	public static final int EXPLORE_SPOKES = 24;

	public static int count = 0;
	public static boolean startupFlag = true;
	public static Direction[] exploreDirections = new Direction[EXPLORE_SPOKES];
	public static int exploreDirectionsPointer = 0;

	public static void turn(RobotController rc) throws GameActionException {

		if (!Nav.explore(rc)) {
			System.out.format("\nCouldn't move");
		}

		if (rc.getTeamBullets() >= 10000) {
			rc.donate(10000);
		}

		MapLocation selfLoc = rc.getLocation();

		if (rc.getRoundNum() == 1) {
			Comms.writeGarden(rc, selfLoc);
		}

		// Nav.avoidBullets(rc, selfLoc);

		while (count <= 7) {
			count += BotArchon.tryHireGardener(rc) ? 1 : 0;
		}

	}

	public static boolean tryHireGardener(RobotController rc) throws GameActionException {
		Direction hireDirection = new Direction(0);
		for (int i = 0; i < 8; i++) {
			if (rc.canHireGardener(hireDirection) && rc.onTheMap(rc.getLocation().add(hireDirection, 5f))) {
				rc.hireGardener(hireDirection);
				return true;
			}
			hireDirection = hireDirection.rotateLeftRads((float) Math.PI * 0.25f);
		}
		return false;
	}
}
