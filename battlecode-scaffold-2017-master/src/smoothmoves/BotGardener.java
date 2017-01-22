package smoothmoves;
import battlecode.common.*;
import battlecode.schema.Action;

public class BotGardener {
	static RobotController rc;
	
	static Team us = RobotPlayer.rc.getTeam();
	static Team them = us.opponent();
	
	static Direction spawnDirection = Direction.NORTH;
	static boolean settled = false;

	static MapLocation myLocation;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		
		RobotInfo[] bots = rc.senseNearbyRobots();
		TreeInfo[] trees = rc.senseNearbyTrees();
		BulletInfo[] bullets = rc.senseNearbyBullets();
		myLocation = rc.getLocation();
		
		Direction moveDirection = null;
		float moveStride = RobotType.GARDENER.strideRadius;
		
		/************* Determine where to move *******************/

		
		if(!settled) {
			
			// Keep a distance from archons and gardeners
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

			spawnDirection = setSpawnDirection(myLocation);
			
			
			if(spawnDirection == null) {
				settled = false;
				MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets, trees, bots);
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceTo(myLocation);
			}
		}
		
		/************* Determine what action to take *************/
		
		byte action = Action.DIE_EXCEPTION;

		buildUnit();
		waterTrees();
		
		if(settled) {
			plantTrees();
		}
		
		else {
			buildUnit();
		}
		
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
		
		TreeInfo[] closeTrees = rc.senseNearbyTrees(2f);
		RobotInfo[] closeRobots = rc.senseNearbyRobots(4f);
		
		int bigTrees = 0;
		for(TreeInfo tree : closeTrees) {
			if(tree.radius >= 1) {
				bigTrees++;
			}
		}
		
		if(bigTrees < 2 && closeRobots.length == 0 && (rc.onTheMap(rc.getLocation(), 3f))) {
			return true;
		}
		
		return false;
	}
	
	public static Direction setSpawnDirection(MapLocation myLocation) throws GameActionException {
		Direction testDirection = myLocation.directionTo(rc.getInitialArchonLocations(them)[0]);
		for(int i = 72;i-->0;) {
			rc.setIndicatorDot(rc.getLocation().add(testDirection,2f), 0, 255, 0);
			if(rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(testDirection, 2f), 1f) || !rc.onTheMap(rc.getLocation().add(testDirection, 2f), 1f)) {
				testDirection = testDirection.rotateLeftDegrees(5f);
				continue;
			} 
			else {
				return testDirection;
			}	
		}
		return null;
	}
	
	public static void plantTrees() throws GameActionException {
		Direction buildDirection = spawnDirection.rotateLeftDegrees(60);
		for(int i = 5; i-->0;) {
			rc.setIndicatorDot(rc.getLocation().add(buildDirection,2f), 255, 0, 0);
			if(rc.canPlantTree(buildDirection)) {
				rc.plantTree(buildDirection);
			}
			buildDirection = buildDirection.rotateLeftDegrees(60);
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

	public static void buildUnit() throws GameActionException {
		int[] units = Comms.ourBotCount.array(rc);

		// first units to spawn
		if(firstUnits(units)) return;

		// leave some bullets for shooting
		if(rc.getTeamBullets() < 130) return;

		// later round spawning
		//if(units[RobotType.LUMBERJACK.ordinal()] < units[RobotType.SOLDIER.ordinal()]){
		//	tryToBuild(RobotType.LUMBERJACK);
		//} else {
			tryToBuild(RobotType.SOLDIER);
		//}
	}

	public static boolean firstUnits(int[] units) throws GameActionException {
		if(units[RobotType.SCOUT.ordinal()] == 0){
			if(tryToBuild(RobotType.SCOUT)) return true;
		}
		if(rc.senseNearbyTrees().length > 2){
			if(units[RobotType.LUMBERJACK.ordinal()] == 0){
				if(tryToBuild(RobotType.LUMBERJACK)) return true;
			}
		} else {
			if(units[RobotType.SOLDIER.ordinal()] == 0){
				if(tryToBuild(RobotType.SOLDIER)) return true;
			}
		}

		return false;
	}
	
	public static boolean tryToBuild(RobotType typeToBuild) throws GameActionException {
		if(spawnDirection == null) spawnDirection = setSpawnDirection(myLocation);
		if(spawnDirection == null) return false;
		Direction buildDirection = spawnDirection;
		for(int i = 6; i-->0;) {
			if(rc.canBuildRobot(typeToBuild, buildDirection)) {
				rc.buildRobot(typeToBuild, buildDirection);
				Comms.ourBotCount.incrementNumBots(rc, typeToBuild);
				return true;
			}
			buildDirection = buildDirection.rotateLeftDegrees(60);
			continue;
 		}
	return false;
	}
}
