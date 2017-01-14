package scoutspam;

import battlecode.common.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BotArchon {
	
	static enum Archon {
		FIRST,
		SECOND,
		THIRD
	}
	
	static RobotController rc;
	static int roundNum;
	static MapLocation myLocation;
	static Team myTeam;
	static int numArchonsAtStart = 0;
	static Archon archonDesignation;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotArchon.rc = rc;
		roundNum = rc.getRoundNum();
		myLocation = rc.getLocation();
		
		
		// GAME SETUP CODE
		if(roundNum == 1) {
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
				tryHireGardener();
			}
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
	
}

