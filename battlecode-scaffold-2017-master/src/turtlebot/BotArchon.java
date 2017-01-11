package turtlebot;

import battlecode.common.*;

public class BotArchon {
	public static int count = 0;
	public static MapLocation[] gardenSpawns = new MapLocation[24];
	public static int gardenSpawnPointer = 0;
	public static final float GARDEN_DISTANCE = 10f;
	
	public static void turn(RobotController rc) throws GameActionException {
		
		MapLocation selfLoc = rc.getLocation();
		MapLocation homeLocation;
		
		if(rc.getRoundNum() == 1) {
			Comms.writeArchonLocation(rc);
			for (int r = 0; r < 3; r++ ) {
				for (int t = 0; t < 8; t++) {
					float distance = GARDEN_DISTANCE * (r+1);
					float radians = t * (float) Math.PI * 0.25f - 0.222f * (r+1);
					if (radians < 0f) {
						radians += 2 * (float) Math.PI;
					}
					gardenSpawns[8 * r + t] = selfLoc.add(new Direction(radians), distance);
				}
			}
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

		while (count < 2) {
			int didHire = BotArchon.tryHireGardener(rc) ? 1 : 0;
			count += didHire;
			if (didHire != 0) {
				BotArchon.delegateGardener(rc);
			}
		}

		while(BotArchon.checkUnnassingedGardener(rc)) {
			BotArchon.delegateGardener(rc);
		}
		
		homeLocation = Comms.readHomeLocation(rc); //LC: homeLocation needs to be inited properly
		Nav.tryMove(rc, rc.getLocation().directionTo(homeLocation));
	}
    
    public static boolean tryHireGardener(RobotController rc) throws GameActionException {
        Direction hireDirection = new Direction(0);
        for (int i =0 ; i < 8; i++) {
            if(rc.canHireGardener(hireDirection)) {
                rc.hireGardener(hireDirection);
                return true;
            }
            hireDirection = hireDirection.rotateLeftRads((float) Math.PI * 0.25f);
        }
        return false;
    }
    
    public static void delegateGardener(RobotController rc) throws GameActionException {
        Comms.writeStack(rc, 0, 20, gardenSpawns[gardenSpawnPointer]);
        System.out.format("Delegrated (%f, %f)", gardenSpawns[gardenSpawnPointer].x, gardenSpawns[gardenSpawnPointer].y);
        gardenSpawnPointer++;
        gardenSpawnPointer %= 24;
    }
    
    public static boolean checkUnnassingedGardener(RobotController rc) throws GameActionException {
        if(Comms.popStack(rc, 21, 40) != null) {
            return true;
        }
        return false;
    }
}
