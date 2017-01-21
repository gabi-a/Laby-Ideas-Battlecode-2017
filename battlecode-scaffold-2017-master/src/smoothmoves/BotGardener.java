package smoothmoves;
import battlecode.common.*;
import battlecode.schema.Action;

public class BotGardener {
	static RobotController rc;
	
	Team us = rc.getTeam();
	Team them = us.opponent();
	
	static Direction spawnDirection = Direction.NORTH;
	static boolean settled = false;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		
		RobotInfo[] bots = rc.senseNearbyRobots();
		TreeInfo[] trees = rc.senseNearbyTrees();
		BulletInfo[] bullets = rc.senseNearbyBullets();
		MapLocation myLocation = rc.getLocation();
		
		Direction moveDirection = null;
		float moveStride = RobotType.GARDENER.strideRadius;
		
		/************* Determine where to move *******************/
		
		if(!settled) {
			MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets, trees, bots);
			moveDirection = myLocation.directionTo(moveLocation);
			moveStride = myLocation.distanceTo(myLocation);
		} else {
			spawnDirection = setSpawnDirection();
			if(spawnDirection == null) {
				settled = false;
				MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets, trees, bots);
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceTo(myLocation);
			}
		}
		
		/************* Determine what action to take *************/
		
		byte action = Action.DIE_EXCEPTION;
		
		if(settled) {
			tryToBuild(RobotType.SOLDIER);
			plantTrees();
			waterTrees();
		}
		
		else {
			tryToBuild(RobotType.SOLDIER);
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
		
		return false;
	}
	
	public static Direction setSpawnDirection() throws GameActionException {
		Direction testDirection = Direction.getEast();
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
	
	public static boolean tryToBuild(RobotType typeToBuild) throws GameActionException {
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
