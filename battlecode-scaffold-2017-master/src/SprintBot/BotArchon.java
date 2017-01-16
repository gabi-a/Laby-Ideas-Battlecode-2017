package SprintBot;
import battlecode.common.*;

public class BotArchon {
	static RobotController rc;
	
<<<<<<< HEAD
=======
	static enum Archon {
		FIRST,
		SECOND,
		THIRD
	}
	
	static Archon archonDesignation;
	static int numArchonsAtStart;
	
	static Team myTeam;
	static MapLocation myLocation;
	
>>>>>>> refs/heads/SprintScout
	public static void turn(RobotController rc) throws GameActionException {
		BotArchon.rc = rc;
<<<<<<< HEAD
		Util.updateBotCount(rc);
		Util.reportDeath(rc);
		
		if(rc.getTeamBullets() > 10000) {
			rc.donate(10000);
		}
		
		if(rc.getTeamBullets() >= 100 && Util.getNumBots(RobotType.GARDENER) < 1 + 0.7f * rc.getTreeCount()/Util.G) {
			tryHireGardener();
		}
		
		Nav.treeBug(rc);
		
	}
	
	public static boolean tryHireGardener() throws GameActionException {
		Direction hireDirection = new Direction(0);
		for (int i = 0; i < 8; i++) {
			if (rc.canHireGardener(hireDirection) && rc.onTheMap(rc.getLocation().add(hireDirection, 5f))) {
				rc.hireGardener(hireDirection);
				Util.increaseNumBotsByOne(rc, RobotType.GARDENER);
				return true;
			}
			hireDirection = hireDirection.rotateLeftRads((float) Math.PI * 0.25f);
		}
		return false;
=======
		
		// CACHE COSTLY VARIABLE CALLS
		myLocation = rc.getLocation();
		myTeam = rc.getTeam();
		
		// SETUP
		if(rc.getRoundNum() == 1) {
			myTeam = rc.getTeam();
			MapLocation[] archonLocs = rc.getInitialArchonLocations(myTeam); 
			numArchonsAtStart = archonLocs.length;
			for(int i = 0; i < archonLocs.length; i++) {
				if(archonLocs[i] == myLocation) {
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
			if(archonDesignation == Archon.FIRST) {
				rc.hireGardener(Direction.getEast());
			}
		}
>>>>>>> refs/heads/SprintScout
	}
	
}

