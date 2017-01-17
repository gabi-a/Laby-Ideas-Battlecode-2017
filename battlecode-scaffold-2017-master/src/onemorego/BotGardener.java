package onemorego;

import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	static boolean settled = false;
	static Direction spawnDirection = null;
	static int maxTreesIcanPlant = 5;
	static boolean broadcastedFinished = false;
	
	static RobotType buildOrder = null;
	static boolean holdTreeProduction = false;
	
	static int treeIDIWantCut = 0;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		Util.updateBotCount(rc);
		
		BulletInfo[] bullets = rc.senseNearbyBullets();
		
		holdTreeProduction = Comms.readHoldTreeProduction(rc) == 1 ? true : false;
		
		TreeInfo[] treesInTheWay = rc.senseNearbyTrees(2f, Team.NEUTRAL);
		if(buildOrder == null && treesInTheWay.length > 0 && treeIDIWantCut != treesInTheWay[0].ID) {
			buildOrder = RobotType.LUMBERJACK;
			treeIDIWantCut = treesInTheWay[0].ID;
			Comms.neutralTrees.push(rc, treesInTheWay[0]);
		}
		
		if(buildOrder == null) {
			int data = Comms.buildStack.pop(rc);
			if (data != -1) buildOrder = RobotType.values()[data];
		}
		
		TreeInfo[] treesPlanted = rc.senseNearbyTrees(2f, rc.getTeam());
		System.out.println(maxTreesIcanPlant+","+treesPlanted.length+"\n");
		if(treesPlanted.length >= maxTreesIcanPlant && !broadcastedFinished) {
			rc.setIndicatorDot(rc.getLocation(), 0, 255, 0);
			broadcastedFinished = true;
			Comms.writeGardenerFinishedPlanting(rc, 1);
		}
		
		if(settled) {
		
			waterTrees();
			
			if(spawnDirection == null) {
				setSpawnDirection();
			}
			
			if(spawnDirection != null && buildOrder != null) {
				if (tryToBuild(buildOrder)) buildOrder = null;
			}
			
			if(spawnDirection != null && !holdTreeProduction) {
				maxTreesIcanPlant = maxTreesIcanPlant();
				plantTrees();
			}
			
		} else {
			Nav.treeBug(rc, bullets);
			settled = settleHere();
		}
		
	}
	
	public static boolean tryToBuild(RobotType typeToBuild) throws GameActionException {
		Direction buildDirection = spawnDirection;
		for(int i = 6; i-->0;) {
			if(rc.canBuildRobot(typeToBuild, buildDirection)) {
				rc.buildRobot(typeToBuild, buildDirection);
				return true;
			}
			buildDirection = buildDirection.rotateLeftDegrees(60);
			continue;
		}
		return false;
	}
	
	public static boolean settleHere() throws GameActionException {
		
		TreeInfo[] closeTrees = rc.senseNearbyTrees(2f);
		RobotInfo[] closeRobots = rc.senseNearbyRobots(2f);
		
		if(closeTrees.length < 2 && closeRobots.length == 0 && rc.onTheMap(rc.getLocation(), 3f)) {
			return true;
		}
		
		return false;
	}
	
	public static void setSpawnDirection() throws GameActionException {
		
		Direction testDirection = Direction.getNorth();
		for(int i = 72;i-->0;) {
			
			rc.setIndicatorDot(rc.getLocation().add(testDirection,2f), 0, 255, 0);
			
			if(rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(testDirection, 2f), 1f) || rc.isCircleOccupiedExceptByThisRobot(rc.getLocation().add(testDirection, 3f), 2f) || !rc.onTheMap(rc.getLocation().add(testDirection, 2f), 2f)) {
				testDirection = testDirection.rotateLeftDegrees(5);
				continue;
			} else {
				spawnDirection = testDirection;
				rc.setIndicatorDot(rc.getLocation().add(testDirection,2f), 0, 0, 255);
				break;
			}
			
		}
		
	}
	
	public static void plantTrees() throws GameActionException {
		Direction buildDirection = spawnDirection.rotateLeftDegrees(60);
		for(int i = 5; i-->0;) {
			
			//rc.setIndicatorDot(rc.getLocation().add(buildDirection,2f), 255, 0, 0);
			
			if(rc.canPlantTree(buildDirection)) {
				rc.plantTree(buildDirection);
				break;
			} else {
				buildDirection = buildDirection.rotateLeftDegrees(60);
				continue;
			}
			
		}
	}
	
	public static int maxTreesIcanPlant() throws GameActionException {
		Direction buildDirection = spawnDirection.rotateLeftDegrees(60);
		int maxTrees = 0;
		for(int i = 5; i-->0;) {
			
			rc.setIndicatorDot(rc.getLocation().add(buildDirection,2f), 255, 0, 0);
			
			if(rc.canPlantTree(buildDirection)) {
				maxTrees++;
				continue;
			} else {
				buildDirection = buildDirection.rotateLeftDegrees(60);
				continue;
			}
			
		}
		return maxTrees;
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
	
}
