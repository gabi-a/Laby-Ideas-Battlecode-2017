package SprintBot;

import battlecode.common.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BotArchon {
	static RobotController rc;
	
	static enum Archon {
		FIRST,
		SECOND,
		THIRD
	}
	
	static Archon archonDesignation;
	static int numArchonsAtStart;
	
	static Team myTeam;
	static MapLocation myLocation;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotArchon.rc = rc;
		
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
	}
	
}

