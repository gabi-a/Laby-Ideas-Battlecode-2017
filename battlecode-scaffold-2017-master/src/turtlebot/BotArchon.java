package turtlebot;

import battlecode.common.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BotArchon {
	static RobotController rc;
	
	static MapLocation homeLocation;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotArchon.rc = rc;
		
		if(rc.getRoundNum() == 1) {
			Comms.writeArchonLocation(rc);
			return;
		}
		
		if(rc.getRoundNum() == 2) {
			
			homeLocation = Comms.readHomeLocation(rc);
			if(homeLocation == null) {
				MapLocation[] archonLocations = Comms.readArchonLocations(rc);
				int dx = 0;
				int dy = 0;
				int archonCount = 0;
				for(int i = 3; i-- > 1;) {
					if(archonLocations[i] != null) {
						archonCount++;
						dx += archonLocations[i].x;
						dy += archonLocations[i].y;
					}
				}
				homeLocation = new MapLocation(dx/archonCount, dy/archonCount);
				Comms.writeHomeLocation(rc, homeLocation);
			}
		}
		
		int gardenersCount = Comms.getNumGardeners(rc);
		
		if(gardenersCount <= 2) {
			if(rc.isBuildReady() && rc.getTeamBullets() >= RobotType.GARDENER.bulletCost) {
				for(int direction = 360; (direction -= 15) > 0;) {
					Direction directionToBuild = new Direction((float)Math.toRadians((double)direction));
					if(rc.canBuildRobot(RobotType.GARDENER, directionToBuild)) {
						rc.buildRobot(RobotType.GARDENER, directionToBuild);
						gardenersCount++;
						Comms.writeNumGardeners(rc, gardenersCount);
						break;
					}
				}
			}
		}
		
		Nav.tryMove(rc, rc.getLocation().directionTo(homeLocation));
		
	}
}
