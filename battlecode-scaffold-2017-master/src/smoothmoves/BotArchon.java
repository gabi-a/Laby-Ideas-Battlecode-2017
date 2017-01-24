package smoothmoves;
import battlecode.common.*;
import battlecode.schema.Action;

public class BotArchon {
	static RobotController rc;
	
	static Team us = RobotPlayer.rc.getTeam();
	static Team them = us.opponent();
	
	static int numInitialArchons = 0;
	
	static enum Archon {
		FIRST,
		SECOND,
		THIRD,
		EXTRA
	}

	static Archon archonDesignation;
	
	static boolean initialSpawningArchon = false;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotArchon.rc = rc;
		
		RobotInfo[] bots = rc.senseNearbyRobots();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
		TreeInfo[] trees = rc.senseNearbyTrees();
		BulletInfo[] bullets = rc.senseNearbyBullets();
		MapLocation myLocation = rc.getLocation();
		
		Util.reportEnemyBots(rc, enemies);
		
		Direction moveDirection = null;
		float moveStride = RobotType.ARCHON.strideRadius;
		
		/************* Setup game variables    *******************/
		if(rc.getRoundNum() == 1) {
			MapLocation[] ourArchonLocs = rc.getInitialArchonLocations(us); 
			MapLocation[] theirArchonLocs = rc.getInitialArchonLocations(them); 
			numInitialArchons = ourArchonLocs.length;
			for(int i = 0; i < ourArchonLocs.length; i++) {
				if(ourArchonLocs[i] == rc.getLocation()) {
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
							archonDesignation = Archon.EXTRA;
					}
				}
			}
			
			float longestDistance = 0f;
			MapLocation bestArchonLocation = myLocation;
			for(int i = ourArchonLocs.length;i-->0;) {
				MapLocation ourArchonLoc = ourArchonLocs[i];
				for(int j = theirArchonLocs.length;j-->0;) {
					if(theirArchonLocs[j].distanceTo(ourArchonLoc) > longestDistance) {
						longestDistance = theirArchonLocs[j].distanceTo(ourArchonLoc);
						bestArchonLocation = ourArchonLoc;
					}
				}
			}
			if(bestArchonLocation == myLocation) {
				initialSpawningArchon = true;
			}
		}
		
		
		/************* Determine where to move *******************/
		
		// Keep a distance from archons and gardeners
		if(bullets.length > 0) {
			MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets, trees, bots);
			moveDirection = myLocation.directionTo(moveLocation);
			moveStride = myLocation.distanceTo(myLocation);
		}
		
		else {
			MapLocation moveLocation = myLocation;
			if(bots.length > 0) {
				for(int i = bots.length;i-->0;) {
					if(bots[i].getType() == RobotType.GARDENER || bots[i].getType() == RobotType.ARCHON) {
						moveLocation = moveLocation.add(bots[i].location.directionTo(myLocation));
					}
				}
			}
			if(trees.length > 0) {
				for(int i = trees.length;i-->0;) {
					moveLocation = moveLocation.add(trees[i].location.directionTo(myLocation));
				}
			}
			
			if (!rc.onTheMap(myLocation.add(Direction.NORTH, 4f))) moveLocation=moveLocation.add(Direction.SOUTH, 5f);
			if (!rc.onTheMap(myLocation.add(Direction.SOUTH, 4f))) moveLocation=moveLocation.add(Direction.NORTH, 5f);
			if (!rc.onTheMap(myLocation.add(Direction.WEST, 4f))) moveLocation=moveLocation.add(Direction.EAST, 5f);
			if (!rc.onTheMap(myLocation.add(Direction.EAST, 4f))) moveLocation=moveLocation.add(Direction.WEST, 5f);
			
			// Stay away from the enemy base
			moveLocation = moveLocation.add(myLocation.directionTo(rc.getInitialArchonLocations(them)[0]).opposite(), 1/myLocation.distanceTo(rc.getInitialArchonLocations(them)[0]));
			rc.setIndicatorDot(moveLocation, 100, 0, 100);
			
			moveDirection = myLocation.directionTo(moveLocation);
			moveDirection = Nav.tryMove(rc, moveDirection, 5f, 24, bullets);
			moveStride = myLocation.distanceTo(moveLocation) * RobotType.GARDENER.strideRadius / (trees.length + bots.length);
		}
		
		/************* Determine what action to take *************/
		
		byte action = Action.DIE_EXCEPTION;
		
		if(rc.getTreeCount() >= 3*Comms.ourBotCount.readNumBots(rc, RobotType.GARDENER))
			action = Action.SPAWN_UNIT;
		
		/************* Do Move ***********************************/
		
		/*
		 * All checks to see if this move is possible should already
		 * have taken place
		 */
		if(moveDirection != null && rc.canMove(moveDirection, moveStride))
			rc.move(moveDirection, moveStride);
		
		/************* Do action *********************************/
		
		/*
		 * All checks to see if this action is possible should already
		 * have taken place
		 */
		if(isMyTurn()) {
			switch(action) {
			case Action.SPAWN_UNIT:
				tryHireGardener();
				break;
			default:
				break;
			}
		}	
	}
	
	public static boolean tryHireGardener() throws GameActionException {
		Direction hireDirection = new Direction(0);
		for (int i = 0; i < 8; i++) {
			if (rc.canHireGardener(hireDirection) && rc.onTheMap(rc.getLocation().add(hireDirection, 5f))) {
				rc.hireGardener(hireDirection);
				Comms.ourBotCount.incrementNumBots(rc, RobotType.GARDENER);
				return true;
			}
			hireDirection = hireDirection.rotateLeftRads((float) Math.PI * 0.25f);
		}
		return false;
	}
	
	public static boolean isMyTurn() {
		int roundNum = rc.getRoundNum();
		if(roundNum < 10) {
			return initialSpawningArchon;
		}
		switch(numInitialArchons) {
			case 1:
				return true;
			case 2:
				return archonDesignation == Archon.FIRST ? roundNum % 2 == 0 : roundNum % 2 == 1;
			case 3:
				return archonDesignation == Archon.FIRST ? roundNum % 3 == 0 : archonDesignation == Archon.SECOND ? roundNum % 3 == 1 : roundNum % 3 == 2;
		}
		// shouldn't get here
		return true;
	}
}

