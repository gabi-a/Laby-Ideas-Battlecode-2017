package onevonetests;
import battlecode.common.*;
import turtlebot.Comms;

public class RobotPlayer {

 	static RobotController rc;
 	static int gardenersCount = 0;
 	static int botsBuilt = 0;
 	
 	public static void run(RobotController rc) throws GameActionException {
 		RobotPlayer.rc = rc;
 		while(true) {
 			try {
 				switch (rc.getType()) {
			    case ARCHON:
			        runArchon();
			        break;
			    case GARDENER:
			        runGardener();
			        break;
			    case SOLDIER:
			        runSoldier();
			        break;
			    case LUMBERJACK:
			        runLumberjack();
			        break;
				}
				Clock.yield();
			}	catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 		}
    }

	private static void runLumberjack() throws GameActionException {
		// TODO Auto-generated method stub
		MapLocation myLocation = rc.getLocation();
		RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(RobotType.LUMBERJACK.sensorRadius, rc.getTeam().opponent());
		
		if(nearbyEnemies.length == 0) {	
			if(rc.canMove(Direction.getSouth())) {
				rc.move(Direction.getSouth());
			}
		}
		else {
			RobotInfo enemySoldier = nearbyEnemies[0];
			System.out.print(nearbyEnemies.length);
		}
		
	}

	private static void runSoldier() throws GameActionException {
		// TODO Auto-generated method stub
		
		if(rc.canMove(Direction.getNorth())) {
			rc.move(Direction.getNorth());
		}
		
	}

	private static void runGardener() throws GameActionException {
		
		Direction directionToBuild = Direction.getNorth();
		RobotType buildType = RobotType.SCOUT;
		
		switch(rc.getTeam()) {
		case A:
			directionToBuild = Direction.getNorth();
			buildType = RobotType.SOLDIER;
			break;
		case B:
			directionToBuild = Direction.getSouth();
			buildType = RobotType.LUMBERJACK;
			break;
		default:
			break;
		}
		
		if(botsBuilt < 1) {
			if(rc.isBuildReady() && rc.getTeamBullets() >= buildType.bulletCost) {
				if(rc.canBuildRobot(buildType, directionToBuild)) {
					rc.buildRobot(buildType, directionToBuild);
					botsBuilt++;
				}
			}
		}
		
	}

	private static void runArchon() throws GameActionException {
		
		Direction directionToBuild = Direction.getNorth();
		
		switch(rc.getTeam()) {
		case A:
			directionToBuild = Direction.getNorth();
			break;
		case B:
			directionToBuild = Direction.getSouth();
			break;
		default:
			break;
		}
		
		if(gardenersCount < 1) {
			if(rc.isBuildReady() && rc.getTeamBullets() >= RobotType.GARDENER.bulletCost) {
				if(rc.canBuildRobot(RobotType.GARDENER, directionToBuild)) {
					rc.buildRobot(RobotType.GARDENER, directionToBuild);
					gardenersCount++;
				}
			}
		}
		
	}
	
}
