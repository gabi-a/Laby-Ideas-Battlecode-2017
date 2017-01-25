package smoothmoves;
import battlecode.common.*;
import battlecode.schema.Action;

public class BotGardener {
	static RobotController rc;
	
	static Team us = RobotPlayer.rc.getTeam();
	static Team them = us.opponent();
	
	static Direction spawnDirection = RobotPlayer.rc.getLocation().directionTo(RobotPlayer.rc.getInitialArchonLocations(them)[0]);
	static boolean settled = false;

	static MapLocation myLocation;
	
	static boolean lotsOfTrees = false;
	static boolean treeCountFlag = false;
	
	static int settleThreshold = 3;
	
	static int turnsAlive = 0;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		turnsAlive++;
		if(turnsAlive % 10 == 0) {
			settleThreshold = Math.max(0, settleThreshold - 1);
		}
		
		/*
		 * Get an idea for how many trees on the map based on the archons perceptions
		 */
		if(!treeCountFlag) {
			int[] archonTrees = Comms.archonTreeCount.array(rc);
			int count = 0;
			for(int i = archonTrees.length;i-->0;) {
				count += archonTrees[i];
			}
			if(count/Comms.archonCount.read(rc) > 4f) {
				lotsOfTrees = true;
			}
			treeCountFlag = true;
			System.out.println("Tree count: "+count);
		}
		
		RobotInfo[] bots = rc.senseNearbyRobots();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
		TreeInfo[] trees = rc.senseNearbyTrees();
		BulletInfo[] bullets = rc.senseNearbyBullets();
		myLocation = rc.getLocation();
		
		Direction moveDirection = null;
		float moveStride = RobotType.GARDENER.strideRadius;
		
		Util.reportEnemyBots(rc, enemies);
		
		/************* Determine where to move *******************/

		if(bullets.length > 0) {
			MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets, trees);
			if (moveLocation != null) {
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceTo(moveLocation);
			}
		}
		
		else if(!settled) {
			
			// Keep a distance from archons and gardeners
			MapLocation moveLocation = myLocation;
			if(bots.length > 0) {
				for(int i = bots.length;i-->0;) {
					if(bots[i].getType() == RobotType.GARDENER || bots[i].getType() == RobotType.ARCHON) {
						moveLocation = moveLocation.add(bots[i].location.directionTo(myLocation), 2f);
					}
				}
			}
			if(trees.length > 0) {
				for(int i = trees.length;i-->0;) {
					moveLocation = moveLocation.add(trees[i].location.directionTo(myLocation), (float) (2f*Math.pow(1f/(1f+trees[i].location.distanceTo(myLocation)), 2)));
				}
			}
			
			if (!rc.onTheMap(myLocation.add(Direction.NORTH, 5f))) moveLocation=moveLocation.add(Direction.SOUTH, 3f);
			if (!rc.onTheMap(myLocation.add(Direction.SOUTH, 5f))) moveLocation=moveLocation.add(Direction.NORTH, 3f);
			if (!rc.onTheMap(myLocation.add(Direction.WEST, 5f))) moveLocation=moveLocation.add(Direction.EAST, 3f);
			if (!rc.onTheMap(myLocation.add(Direction.EAST, 5f))) moveLocation=moveLocation.add(Direction.WEST, 3f);

			
			// Stay away from the enemy base
			moveLocation = moveLocation.add(myLocation.directionTo(rc.getInitialArchonLocations(them)[0]).opposite(), 1/myLocation.distanceTo(rc.getInitialArchonLocations(them)[0]));
			rc.setIndicatorDot(moveLocation, 100, 0, 100);
			
			moveDirection = myLocation.directionTo(moveLocation);
			moveDirection = Nav.tryMove(rc, moveDirection, 5f, 24, bullets);
			moveStride = myLocation.distanceTo(moveLocation); //* RobotType.GARDENER.strideRadius / (trees.length + bots.length);
			
			settled = settleHere();
			
		} else {

			if(rc.senseTreeAtLocation(myLocation.add(spawnDirection)) != null) spawnDirection = setSpawnDirection();
			
			if(spawnDirection == null) {
				settled = false;
				MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets, trees, bots);
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
		System.out.println("Trees can plant:" +treesCanPlant );
		if(treesCanPlant > settleThreshold) {
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
		System.out.println("Set spawn direction");
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
			if(!rc.isLocationOccupiedByTree(rc.getLocation().add(buildDirection,2f))) {
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

		// leave some bullets for shooting
		if(rc.getTeamBullets() < 130) return;

		// later round spawning
		//if(units[RobotType.LUMBERJACK.ordinal()] < units[RobotType.SOLDIER.ordinal()]){
		//	tryToBuild(RobotType.LUMBERJACK);
		//} else {
		if( (lotsOfTrees &&  Comms.ourBotCount.readNumBots(rc, RobotType.LUMBERJACK) < 10) || (Comms.ourBotCount.readNumBots(rc, RobotType.SOLDIER) > 4 * Comms.ourBotCount.readNumBots(rc, RobotType.LUMBERJACK)) ) {
			tryToBuild(RobotType.LUMBERJACK);
		} else {
			tryToBuild(RobotType.SOLDIER);
		}
		//}
	}

	public static boolean firstUnits(int[] units) throws GameActionException {

		if(turnsAlive >= 60) return false;
		
		if(units[RobotType.SOLDIER.ordinal()] <= 1){
			tryToBuild(RobotType.SOLDIER); 
			return true;
		}
		if(lotsOfTrees && !settled){
			if(units[RobotType.LUMBERJACK.ordinal()] == 0){
				if(tryToBuild(RobotType.LUMBERJACK)) return true;
			}
		}
		if(units[RobotType.SCOUT.ordinal()] == 0){
			tryToBuild(RobotType.SCOUT); 
			return true;
		}
		
		return false;
	}
	
	public static boolean tryToBuild(RobotType typeToBuild) throws GameActionException {
		if(spawnDirection == null) spawnDirection = setSpawnDirection();
		if(spawnDirection == null) return false;
		Direction buildDirection = spawnDirection;
		for(int i = 3; i-->0;) {
			if(rc.canBuildRobot(typeToBuild, buildDirection.rotateLeftDegrees(10f * i))) {
				rc.buildRobot(typeToBuild, buildDirection.rotateLeftDegrees(10f * i));
				Comms.ourBotCount.incrementNumBots(rc, typeToBuild);
				return true;
			}
			if(rc.canBuildRobot(typeToBuild, buildDirection.rotateRightDegrees(10f * i))) {
				rc.buildRobot(typeToBuild, buildDirection.rotateRightDegrees(10f * i));
				Comms.ourBotCount.incrementNumBots(rc, typeToBuild);
				return true;
			}
 		}
	return false;
	}
}
