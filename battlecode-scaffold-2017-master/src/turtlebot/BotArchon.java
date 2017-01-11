package turtlebot;

import battlecode.common.*;

public class BotArchon {
	public static int count = 0;
	public static MapLocation[] gardenSpawns = new MapLocation[24];
	public static int gardenSpawnPointer = 0;
	public static final float GARDEN_DISTANCE = 10f;
	
	public static void turn(RobotController rc) throws GameActionException {
		
		if(rc.getTeamBullets() >= 10000) {
			rc.donate(10000);
		}
		
		MapLocation selfLoc = rc.getLocation();

		while (count <= 7) {
			int didHire = BotArchon.tryHireGardener(rc) ? 1 : 0;
			count += didHire;
			if (didHire != 0) {
				BotArchon.delegateGardener(rc);
			}
		}
		
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
        Comms.writeStack(rc, 100, 120, gardenSpawns[gardenSpawnPointer]);
        System.out.format("Delegrated (%f, %f)", gardenSpawns[gardenSpawnPointer].x, gardenSpawns[gardenSpawnPointer].y);
        gardenSpawnPointer++;
        gardenSpawnPointer %= 24;
    }
    
    public static boolean checkUnnassingedGardener(RobotController rc) throws GameActionException {
        if(Comms.popStack(rc, 121, 140) != null) {
            return true;
        }
        return false;
    }
}
