package teststrategy;
import battlecode.common.*;
import battlecode.schema.Action;
import teststrategy.BotArchon.MapSize;

public class BotGardener {
	static RobotController rc;
	
	static Team us = RobotPlayer.rc.getTeam();
	static Team them = us.opponent();
	
	static int numInitialArchons = RobotPlayer.rc.getInitialArchonLocations(us).length;
	static Direction spawnDirection = RobotPlayer.rc.getLocation().directionTo(RobotPlayer.rc.getInitialArchonLocations(them)[0]);
	static boolean settled = false;

	static MapLocation myLocation;
	
	static boolean lotsOfTrees = false;
	static boolean treeCountFlag = false;
	
	static int settleThreshold = 3;
	
	static int turnsAlive = 0;
	
	static final float TREE_HEURISTIC_THRESHOLD = 77;
	
	static MapLocation goalLocation;
	static MapLocation enemyBase;
	
	static MapSize mapSize;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		turnsAlive++;
		if(turnsAlive % 10 == 0) {
			settleThreshold = Math.max(0, settleThreshold - 1);
		}
		if(mapSize == null) {
			System.out.println("Comms map Size: "+Comms.mapSize.read(rc));
			mapSize = MapSize.values()[Comms.mapSize.read(rc)];
		}
		Util.updateMyPostion(rc);
		
		/*
		 * Get an idea for how many trees on the map based on the archons perceptions
		 */
		if(!treeCountFlag) {
			MapLocation[] theirArchonLocs = rc.getInitialArchonLocations(them);
			enemyBase = theirArchonLocs[0];
			int[] archonTrees = Comms.archonTreeCount.array(rc);
			int heuristic = 100;
			for(int i = numInitialArchons;i-->0;) {
				if(archonTrees[i] < heuristic) {
					heuristic = archonTrees[i];
				}
			}
			if(heuristic > TREE_HEURISTIC_THRESHOLD) {
				lotsOfTrees = true;
			}
			treeCountFlag = true;
			System.out.format("Lots of trees: %b\n"
							+ "heuristic: %d\n"
							+ "threshold: %f\n",lotsOfTrees,heuristic,TREE_HEURISTIC_THRESHOLD);
		}
		
		RobotInfo[] bots = rc.senseNearbyRobots();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
		TreeInfo[] trees = rc.senseNearbyTrees();
		BulletInfo[] bullets = rc.senseNearbyBullets(3f);
		myLocation = rc.getLocation();

		if(goalLocation == null) goalLocation = myLocation;
		
		Direction moveDirection = null;
		float moveStride = RobotType.GARDENER.strideRadius;
		
		Util.reportEnemyBots(rc, enemies);
		
		/************* Determine where to move *******************/

		if(bullets.length > 0) {
			MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets);
			if (moveLocation != null) {
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceTo(moveLocation);
			}
		}
		
		else if(!settled) {
			
			if(bots.length > 0) {
				for(int i = bots.length;i-->0;) {
					if(bots[i].getType() == RobotType.GARDENER || bots[i].getType() == RobotType.ARCHON) {
						goalLocation = goalLocation.add(bots[i].location.directionTo(myLocation), 5f);//, 10f/(1f + bots[i].location.distanceTo(myLocation)));
					}
				}
			}
			
			if(trees.length > 0) {
				for(int i = trees.length;i-->0;) {
					goalLocation = goalLocation.add(trees[i].location.directionTo(myLocation), 2f);//, 10f/(1f + trees[i].location.distanceTo(myLocation)));
				}
			}
			
			if (!rc.onTheMap(myLocation.add(Direction.NORTH, RobotType.GARDENER.sensorRadius-1f))) goalLocation=goalLocation.add(Direction.SOUTH, 20f);
			if (!rc.onTheMap(myLocation.add(Direction.SOUTH, RobotType.GARDENER.sensorRadius-1f))) goalLocation=goalLocation.add(Direction.NORTH, 20f);
			if (!rc.onTheMap(myLocation.add(Direction.WEST, RobotType.GARDENER.sensorRadius-1f))) goalLocation=goalLocation.add(Direction.EAST, 20f);
			if (!rc.onTheMap(myLocation.add(Direction.EAST, RobotType.GARDENER.sensorRadius-1f))) goalLocation=goalLocation.add(Direction.WEST, 20f);
			
			// Stay away from the enemy base
			goalLocation = goalLocation.add(myLocation.directionTo(enemyBase).opposite(), 3f);//, 10f/(myLocation.distanceTo(enemyBase)+1f));
			
			rc.setIndicatorDot(goalLocation, 100, 0, 100);
			MapLocation moveLocation = Nav.pathTo(rc, goalLocation);
			if(moveLocation != null) {
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceTo(moveLocation);
			}
			settled = settleHere();
			
		} else {
			if(rc.senseTreeAtLocation(myLocation.add(spawnDirection)) != null) spawnDirection = setSpawnDirection();
			
			if(spawnDirection == null) {
				settled = false;
				MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets);
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceTo(myLocation);
			}
		}
		
		/************* Determine what action to take *************/
		
		byte action = Action.DIE_EXCEPTION;

		int[] units = Comms.ourBotCount.array(rc);

		
		if(settled && !firstUnits(units)/*&& rc.getTeamBullets() > 100*/) {
			plantTrees();
		}
		
		buildUnit(units);
		waterTrees();
		
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
		
		switch(action) {
		case Action.SPAWN_UNIT:
			break;
		default:
			break;
		}
		
	}
	
	public static boolean settleHere() throws GameActionException {
		
		spawnDirection = setSpawnDirection();
		if(spawnDirection == null) return false;
		
		int treesCanPlant = getMaxTrees();
		//System.out.println("Trees can plant:" +treesCanPlant );
		if(treesCanPlant > settleThreshold && turnsAlive > 30) {
			return true;
		}
		
		/*
		TreeInfo[] closeTrees = rc.senseNearbyTrees(2f);
		RobotInfo[] closeRobots = rc.senseNearbyRobots(2f);
		
		int bigTrees = 0;
		for(TreeInfo tree : closeTrees) {
			if(tree.radius >= 1) {
				bigTrees++;
			}
		}
		
		if(bigTrees < 2 && closeRobots.length == 0 && (rc.onTheMap(rc.getLocation(), 3f))) {
			return true;
		}
		*/
		
		return false;
	}
	
	public static Direction setSpawnDirection() throws GameActionException {
		//System.out.println("Set spawn direction");
		Direction testDirection = myLocation.directionTo(rc.getInitialArchonLocations(them)[0]);
		for(int i = 0;i<36;i++) {
			rc.setIndicatorDot(rc.getLocation().add(testDirection.rotateLeftDegrees(5f * i),2f), 0, 255, 0);
			if( !(rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(testDirection.rotateLeftDegrees(5f * i), 2f), 1f) || !rc.onTheMap(rc.getLocation().add(testDirection.rotateLeftDegrees(5f * i), 2f), 1f))) {			
				return testDirection.rotateLeftDegrees(5f * i);
			}
			rc.setIndicatorDot(rc.getLocation().add(testDirection.rotateRightDegrees(5f * i),2f), 0, 255, 0);
			if( !(rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(testDirection.rotateRightDegrees(5f * i), 2f), 1f) || !rc.onTheMap(rc.getLocation().add(testDirection.rotateRightDegrees(5f * i), 2f), 1f))) {			
				return testDirection.rotateRightDegrees(5f * i);
			}
		}
		return null;
	}
	
	public static int getMaxTrees() throws GameActionException {
		int treesCanPlant = 0;
		Direction buildDirection = spawnDirection.rotateLeftDegrees(60);
		for(int i = 5; i-->0;) {
			rc.setIndicatorDot(rc.getLocation().add(buildDirection,2f), 255, 0, 0);
			if( rc.senseNearbyTrees(rc.getLocation().add(buildDirection,2f), 1f, Team.NEUTRAL).length == 0) {
				treesCanPlant++;
			}
			buildDirection = buildDirection.rotateLeftDegrees(60);
		}
		return treesCanPlant;
	}
	
	public static void plantTrees() throws GameActionException {
		Direction buildDirection = spawnDirection.rotateLeftDegrees(180);
		if(rc.canPlantTree(buildDirection)) {
			rc.plantTree(buildDirection);
		}
		for(int i = 3; i-->0;) {
			rc.setIndicatorDot(rc.getLocation().add(buildDirection.rotateLeftDegrees(60f * i),2f), 255, 0, 0);
			if(rc.canPlantTree(buildDirection.rotateLeftDegrees(60f * i))) {
				rc.plantTree(buildDirection.rotateLeftDegrees(60f * i));
			}
			rc.setIndicatorDot(rc.getLocation().add(buildDirection.rotateRightDegrees(60f * i),2f), 255, 0, 0);
			if(rc.canPlantTree(buildDirection.rotateRightDegrees(60f * i))) {
				rc.plantTree(buildDirection.rotateRightDegrees(60f * i));
			}
		}
	}
	
	public static void waterTrees() throws GameActionException {
		
		TreeInfo[] trees = rc.senseNearbyTrees(2f, rc.getTeam());
		TreeInfo tree = null;
		TreeInfo treeToWater = null;
		float lowestHealth = 100f;
		for(int i = trees.length;i-->0;) {
			tree = trees[i];
			if(tree.health < lowestHealth && rc.canWater(tree.ID)) {
				lowestHealth = tree.health;
				treeToWater = tree;
			}
		}
		if(tree != null) {
			rc.water(treeToWater.ID);
		}
	}

	public static void buildUnit(int[] units) throws GameActionException {
		//int[] units = Comms.ourBotCount.array(rc);

		// first units to spawn
		if(firstUnits(units)) return;

		
		//System.out.println("\nLumberjacks: "+Comms.ourBotCount.readNumBots(rc, RobotType.LUMBERJACK)+"\nTanks: "+Comms.ourBotCount.readNumBots(rc, RobotType.TANK));

		if(lotsOfTrees && mapSize != MapSize.VSMALL) {
			if(2*units[RobotType.SOLDIER.ordinal()] < units[RobotType.LUMBERJACK.ordinal()]) {
				tryToBuild(RobotType.SOLDIER);
			}
			else if(units[RobotType.LUMBERJACK.ordinal()] <= 6 + 6 * units[RobotType.TANK.ordinal()]) {
				tryToBuild(RobotType.LUMBERJACK);
			} else {
				if(!tryToBuild(RobotType.TANK)) {
					tryToBuild(RobotType.LUMBERJACK);
				}
			}
		} 
		
		else {
			// leave some bullets for shooting
			if(rc.getTeamBullets() < 130) return;
			if( (units[RobotType.SOLDIER.ordinal()] > 4 *units[RobotType.LUMBERJACK.ordinal()]) ) {
				tryToBuild(RobotType.LUMBERJACK);
			} else {
				tryToBuild(RobotType.SOLDIER);
			}
		}
		if(units[RobotType.SCOUT.ordinal()] == 0) {
			tryToBuild(RobotType.SCOUT);
		}

	}

	public static boolean firstUnits(int[] units) throws GameActionException {

		if(turnsAlive >= 60) return false;
		
		if(mapSize == MapSize.LARGE) return false;
		if(!lotsOfTrees || mapSize == MapSize.VSMALL) {
			if(units[RobotType.SOLDIER.ordinal()] <= 1){
				tryToBuild(RobotType.SOLDIER); 
				return true;
			}
		}
		else {
			if(units[RobotType.SCOUT.ordinal()] == 0){
				tryToBuild(RobotType.SCOUT); 
				return true;
			}
		}
		return false;
	}
	
	public static boolean tryToBuild(RobotType typeToBuild) throws GameActionException {
		if(spawnDirection == null) spawnDirection = setSpawnDirection();
		if(spawnDirection == null) return false;
		Direction buildDirection = spawnDirection;
		for(int i = 0; i < 2; i++) {
			if(rc.canBuildRobot(typeToBuild, buildDirection.rotateLeftDegrees(60f * i))) {
				rc.buildRobot(typeToBuild, buildDirection.rotateLeftDegrees(60f * i));
				Comms.ourBotCount.incrementNumBots(rc, typeToBuild);
				return true;
			}
			if(rc.canBuildRobot(typeToBuild, buildDirection.rotateRightDegrees(60f * i))) {
				rc.buildRobot(typeToBuild, buildDirection.rotateRightDegrees(60f * i));
				Comms.ourBotCount.incrementNumBots(rc, typeToBuild);
				return true;
			}
 		}
	return false;
	}
}
