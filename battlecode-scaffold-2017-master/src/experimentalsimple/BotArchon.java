package experimentalsimple;

import battlecode.common.*;

public class BotArchon {

	static enum Archon {
		FIRST, SECOND, THIRD
	}

	static RobotController rc;
	static int roundNum;
	static MapLocation myLocation;
	static Team myTeam;
	static int numArchonsAtStart = 0;
	static Archon archonDesignation;

	public static final float GARDEN_DISTANCE = 10f;
	public static final int EXPLORE_SPOKES = 24;

	public static int count = 0;
	public static boolean startupFlag = true;
	public static MapLocation[] exploreLocations = new MapLocation[EXPLORE_SPOKES];
	public static int exploreLocationsPointer = 0;

	public static void turn(RobotController rc) throws GameActionException {
		BotArchon.rc = rc;
		roundNum = rc.getRoundNum();
		myLocation = rc.getLocation();

		// GAME SETUP CODE
		if (roundNum == 1) {
			myTeam = rc.getTeam();
			MapLocation[] archonLocs = rc.getInitialArchonLocations(myTeam);
			numArchonsAtStart = archonLocs.length;
			for (int i = 0; i < archonLocs.length; i++) {
				if (archonLocs[i] == myLocation) {
					switch (i) {
					case 0:
						archonDesignation = Archon.FIRST;
						break;
					case 1:
						archonDesignation = Archon.SECOND;
						break;
					case 2:
						archonDesignation = Archon.THIRD;
						break;
					default:
						System.out.println("Shouldn't get here!");
					}
				}
			}
		} /*
			 * if(archonDesignation == Archon.FIRST) {
			 * rc.buildRobot(RobotType.GARDENER, new Direction(0)); }
			 */

		if (!Nav.avoidBullets(rc, myLocation) && !Nav.explore(rc)) {
			// //System.out.format("\nCouldn't move");
		}

		// Donate all of our bullets if we can win or the game's about to
		// end
		if (rc.getTeamBullets() >= 10000 || rc.getRoundNum() == rc.getRoundLimit() - 1) {
			rc.donate(rc.getTeamBullets());
		}

		int scoutsBuilt = Comms.readNumRobots(rc, RobotType.SCOUT);
		int lumberjacksBuilt = Comms.readNumRobots(rc, RobotType.LUMBERJACK);
		int gardenersBuilt = Comms.readNumRobots(rc, RobotType.GARDENER);

		if (rc.getRoundNum() == 1) {
			Comms.writeGarden(rc, myLocation);
			Direction exploreDir = new Direction(0);
			for (int i = 0; i < EXPLORE_SPOKES; i++) {
				exploreLocations[i] = myLocation.add(exploreDir, 100f);
				exploreDir = exploreDir.rotateLeftRads(7 * (float) Math.PI / EXPLORE_SPOKES);
			}
		}

		/*
		 * if ( (count <= 7 && !(Comms.readGardenerUniversalHoldRound(rc) <=
		 * rc.getRoundNum() &&
		 * Comms.readGardenerUniversalHoldLocation(rc).distanceTo( myLocation)
		 * <= 20f) && rc.getTeamBullets() > 60 && rc.getRoundNum() >= 20) ||
		 * count < 1 ) { //if(rc.onTheMap(myLocation,
		 * RobotType.ARCHON.sensorRadius-3)) count +=
		 * BotArchon.tryHireGardener(rc) ? 1 : 0; }
		 */

		if (gardenersBuilt <= (3 + rc.getRoundNum() / 100)) {
			gardenersBuilt += tryHireGardener() ? 1 : 0;
			Comms.writeNumRobots(rc, RobotType.GARDENER, gardenersBuilt);
		}

		while (checkUnassignedScout()) {
		}

	}

	public static boolean tryHireGardener() throws GameActionException {
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

	public static void delegateScout() throws GameActionException {
		MapLocation[] enemyArchons = rc.getInitialArchonLocations(rc.getTeam().opponent());
		int numEnemyArchons = enemyArchons.length;
		MapLocation delegationLocation = (exploreLocationsPointer < numEnemyArchons)
				? enemyArchons[exploreLocationsPointer] : exploreLocations[exploreLocationsPointer - numEnemyArchons];
		Comms.writeStack(rc, Comms.ARCHON_SCOUT_DELEGATION_START, Comms.ARCHON_SCOUT_DELEGATION_END,
				delegationLocation);
		exploreLocationsPointer++;
		exploreLocationsPointer %= (exploreLocations.length + numEnemyArchons);
	}

	public static boolean checkUnassignedScout() throws GameActionException {
		MapLocation unassignedCheck = Comms.popStack(rc, Comms.SCOUT_ARCHON_REQUEST_START,
				Comms.SCOUT_ARCHON_REQUEST_END);
		if (unassignedCheck != null) {
			delegateScout();
			System.out.println("DELEGATED");
			return true;
		}
		return false;
	}
}
