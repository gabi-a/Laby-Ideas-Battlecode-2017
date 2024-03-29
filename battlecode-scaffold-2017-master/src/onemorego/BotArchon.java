package onemorego;

import battlecode.common.*;

public class BotArchon extends RobotPlayer {
	
	static int buildState = 0;
	static MapLocation myInitialLocation;
	
	static enum Archon {
		FIRST,
		SECOND,
		THIRD
	}

	static int numArchonsAtStart = 0;
	static Archon archonDesignation;
	
	public static void turn() throws GameActionException {
		
		BulletInfo[] bullets = rc.senseNearbyBullets();
		
		if(rc.getRoundNum() == 1) {
			Comms.gardenerPlanting.write(rc, 1);
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
				Comms.buildQueue.push(rc, RobotType.SOLDIER.ordinal());
				Comms.soldierStratStack.push(rc, Strategy.DEFENSE.ordinal());
				Comms.buildQueue.push(rc, RobotType.SOLDIER.ordinal());
				Comms.soldierStratStack.push(rc, Strategy.DEFENSE.ordinal());
				Comms.buildQueue.push(rc, RobotType.SCOUT.ordinal());
				Comms.buildQueue.push(rc, RobotType.SCOUT.ordinal());
				Comms.buildQueue.push(rc, RobotType.SCOUT.ordinal());
				Comms.buildQueue.push(rc, RobotType.SCOUT.ordinal());
				Comms.holdTreeProduction.write(rc, 1);
			}

			if(((Util.getNumBots(RobotType.SCOUT) >= 2 && Util.getNumBots(RobotType.SOLDIER) >= 1) || rc.getRoundNum() > 120) && buildState == 1) {
				Comms.holdTreeProduction.write(rc, 0);
				buildState = 2;
			}

			if(rc.getTreeCount() >= 8 && buildState == 2) {
				System.out.format("I'm pushing 10 lumberjacks, buildState=%d\n",buildState);
				Comms.holdTreeProduction.write(rc, 1);
				for(int i = 10;i-->0;) {
					Comms.buildQueue.push(rc, RobotType.SOLDIER.ordinal());
				}
				buildState = 3;
			}
			if(buildState == 3 && Util.getNumBots(RobotType.SOLDIER) >= 10) {
				Comms.holdTreeProduction.write(rc, 0);
				buildState = 4;
			}
			if(buildState == 4 && rc.getTreeCount() > 20) {
				rc.setIndicatorDot(rc.getLocation(), 200, 200, 0);
				Comms.holdTreeProduction.write(rc, 1);
				for(int i = 10;i-->0;) {
					Comms.buildQueue.push(rc, RobotType.LUMBERJACK.ordinal());
					Comms.lumberjackStack.push(rc, 1);
				}
				buildState = 5;
			}
			if(buildState == 5 && Util.getNumBots(RobotType.LUMBERJACK) >= 10) {
				Comms.holdTreeProduction.write(rc, 0);
				buildState = 6;
			}
			if(buildState == 6) {
				Comms.buildQueue.push(rc, RobotType.SOLDIER.ordinal());
				Comms.buildQueue.push(rc, RobotType.SOLDIER.ordinal());
				Comms.buildQueue.push(rc, RobotType.SOLDIER.ordinal());
				Comms.buildQueue.push(rc, RobotType.SCOUT.ordinal());
				Comms.buildQueue.push(rc, RobotType.SOLDIER.ordinal());
				Comms.buildQueue.push(rc, RobotType.SOLDIER.ordinal());
				Comms.buildQueue.push(rc, RobotType.SOLDIER.ordinal());
				Comms.buildQueue.push(rc, RobotType.SCOUT.ordinal());
				buildState = 7;
			}
		}
		
		if(myInitialLocation.distanceTo(rc.getLocation()) > 15f) {
			Nav.tryMove(rc, rc.getLocation().directionTo(myInitialLocation), bullets);
			Nav.heading = Nav.randomDirection();
		}
		else {
			Nav.explore(rc, bullets);
		}
		if(Comms.gardenerPlanting.read(rc) == 1 && Util.getNumBots(RobotType.GARDENER) < 1 + rc.getRoundNum()/100) {
			if (tryHireGardener()) Comms.gardenerPlanting.write(rc, 0);
		}
		
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
	}
	
}

