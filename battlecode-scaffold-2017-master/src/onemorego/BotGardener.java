package onemorego;
import battlecode.common.*;

public class BotGardener extends RobotPlayer {
	
	static boolean settled = false;
	static Direction spawnDirection = null;
	static int maxTreesIcanPlant = 5;
	static boolean broadcastedFinished = false;
	
	static RobotType buildOrder = null;
	static boolean holdTreeProduction = false;
	
	static int turnsNotSettled = 0;
	
	static int treeIDIWantCut = 0;
	static boolean startupFlag = true;
	
	public static void turn() throws GameActionException {
		
		if(startupFlag) {
			Comms.soldierProtectionLocationStack.push(rc, Comms.packLocation(rc, rc.getLocation()));
			startupFlag = false;
		}
		
		RobotInfo enemy = Util.getClosestEnemyExceptArchon(rc, enemies);
		
		if(enemy != null) {
			Comms.writeAttackEnemy(rc, enemy.location, enemy.getID(), AttackGroup.B);
		}
		
		
		BulletInfo[] bullets = rc.senseNearbyBullets();
		
		holdTreeProduction = (Comms.holdTreeProduction.read(rc) == 1);
		
		TreeInfo[] treesInTheWay = rc.senseNearbyTrees(2f, Team.NEUTRAL);
		
		if(buildOrder == null) {
			if(treesInTheWay.length > 0 && treeIDIWantCut != treesInTheWay[0].ID) {
				buildOrder = RobotType.LUMBERJACK;
				treeIDIWantCut = treesInTheWay[0].ID;
				Comms.neutralTrees.push(rc, treesInTheWay[0]);
			}
			else {
				int data = Comms.buildQueue.pop(rc);
				if (data != -1) {
					buildOrder = RobotType.values()[data];
				}
			}
		}

		TreeInfo[] treesPlanted = rc.senseNearbyTrees(2f, rc.getTeam());
		if(treesPlanted.length >= maxTreesIcanPlant && !broadcastedFinished) {
			rc.setIndicatorDot(rc.getLocation(), 0, 255, 0);
			broadcastedFinished = true;
			Comms.gardenerPlanting.write(rc, 1);
		}
		
		if(spawnDirection != null && buildOrder != null) {
			if (tryToBuild(buildOrder)) buildOrder = null;
		}
		
		if(settled) {
			waterTrees();	
			if(spawnDirection == null) {
				spawnDirection = setSpawnDirection();
				if(spawnDirection == null) {
					settled = false;
					Nav.treeBug(rc, bullets);
				}
			}	
			if(spawnDirection != null && !holdTreeProduction) {
				maxTreesIcanPlant = maxTreesIcanPlant();
				plantTrees();
			}	
		} 
		else {
			Nav.treeBug(rc, bullets);
			turnsNotSettled ++;
			settled = settleHere();
		}
		
	}
	
	public static boolean tryToBuild(RobotType typeToBuild) throws GameActionException {
		Direction buildDirection = spawnDirection;
		for(int i = 6; i-->0;) {
			if(rc.canBuildRobot(typeToBuild, buildDirection)) {
				rc.buildRobot(typeToBuild, buildDirection);
				Util.increaseNumBotsByOne(rc, typeToBuild);
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
		
		int bigTrees = 0;
		for(TreeInfo tree : closeTrees) {
			if(tree.radius >= 1) {
				bigTrees++;
			}
		}
		
		if(bigTrees < 2 && closeRobots.length == 0 && (rc.onTheMap(rc.getLocation(), 3f)  || turnsNotSettled > 30 )) {
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
	
	public static int maxTreesIcanPlant() throws GameActionException {
		Direction buildDirection = spawnDirection.rotateLeftDegrees(60);
		int maxTrees = 0;
		for(int i = 5; i-->0;) {
			rc.setIndicatorDot(rc.getLocation().add(buildDirection,2f), 255, 0, 0);
			if(rc.canPlantTree(buildDirection)) {
				maxTrees++;
			}
			buildDirection = buildDirection.rotateLeftDegrees(60);
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
