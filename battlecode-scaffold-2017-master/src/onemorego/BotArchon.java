package onemorego;

import battlecode.common.*;

public class BotArchon {
	static RobotController rc;
	
	static int buildState = 0;
	static MapLocation myInitialLocation;
	
	static enum Archon {
		FIRST,
		SECOND,
		THIRD
	}

	static int numArchonsAtStart = 0;
	static Archon archonDesignation;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotArchon.rc = rc;
		Util.updateBotCount(rc);
		
		BulletInfo[] bullets = rc.senseNearbyBullets();
		
		if(rc.getRoundNum() == 1) {
			Comms.writeGardenerFinishedPlanting(rc, 1);
			myInitialLocation = rc.getLocation();
			MapLocation[] archonLocs = rc.getInitialArchonLocations(rc.getTeam()); 
			numArchonsAtStart = archonLocs.length;
			for(int i = 0; i < archonLocs.length; i++) {
				if(archonLocs[i] == rc.getLocation()) {
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
		}
		
		if(rc.getRoundNum() > rc.getRoundLimit() - 2) {
			rc.donate(rc.getTeamBullets());
		}
		
		if(rc.getTeamBullets() > 10000) {
			rc.donate(10000);
		}
		
		// Spawning schedule:
		// 1. Spawn 2 scouts 
		// 2. Spawn lumberjacks if there's trees around
		// 3. All going well, build up bullets and then send waves of soldiers
		if(archonDesignation == Archon.FIRST) {
			if(rc.getRoundNum() == 1) {
				buildState = 1;
				Comms.buildStack.push(rc, RobotType.SCOUT.ordinal());
				Comms.buildStack.push(rc, RobotType.SCOUT.ordinal());
				Comms.buildStack.push(rc, RobotType.SCOUT.ordinal());
				Comms.buildStack.push(rc, RobotType.SCOUT.ordinal());
				Comms.writeHoldTreeProduction(rc, 1);
			}

			if((Util.getNumBots(RobotType.SCOUT) >= 4 || rc.getRoundNum() > 60) && buildState == 1) {
				Comms.writeHoldTreeProduction(rc, 0);
				buildState = 2;
			}

			if(rc.getTreeCount() >= 8 && buildState == 2) {
				System.out.format("I'm pushing 10 trees, buildState=%d\n",buildState);
				Comms.writeHoldTreeProduction(rc, 1);
				for(int i = 10;i-->0;) {
					Comms.buildStack.push(rc, RobotType.SOLDIER.ordinal());
				}
				buildState = 3;
			}
			if(Util.getNumBots(RobotType.SOLDIER) >= 10 && buildState == 3) {
				Comms.writeHoldTreeProduction(rc, 0);
				buildState = 4;
			}
		}
		
		if(myInitialLocation.distanceTo(rc.getLocation()) > 15f) {
			Nav.tryMove(rc, rc.getLocation().directionTo(myInitialLocation), bullets);
			Nav.heading = Nav.randomDirection();
		}
		else {
			Nav.explore(rc, bullets);
		}
		if(Comms.readGardenerFinishedPlanting(rc) == 1) {
			if (tryHireGardener()) Comms.writeGardenerFinishedPlanting(rc, 0);
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

