package SprintBot;
import battlecode.common.*;

public class BugBotGardener {
	static RobotController rc;
	static boolean scoutWave = false;
	public static void turn(RobotController rc) throws GameActionException {
		BugBotGardener.rc = rc;
		Util.updateBotCount(rc);
		Util.reportDeath(rc);
		
		MapLocation myLocation = rc.getLocation();
		Nav.treeBug(rc);
		waterTrees();
		
		if(Util.getNumBots(RobotType.LUMBERJACK) < 1) {
			tryToBuild(RobotType.LUMBERJACK);
		}
		
		if(rc.getTeamBullets() > 200) {
			scoutWave = true;
		}
		
		if(scoutWave) {
			tryToBuild(RobotType.SCOUT);
		} else {
			plantTrees(myLocation);
		}
		
		//if(rc.getTeamBullets() >= 80 && Util.getNumBots(RobotType.SCOUT) < 1 + rc.getTreeCount()/Util.S) {
		//	tryToBuild(RobotType.SCOUT);
		//}
		
		//if(rc.getTeamBullets() > 50 && rc.getTreeCount() < Util.getNumBots(RobotType.GARDENER)*Util.T) {
		//	plantTrees(myLocation);
		//}
	}
	
	
	
	public static boolean waterTrees() throws GameActionException {
    	for (TreeInfo treeInfo : rc.senseNearbyTrees(1.5f, rc.getTeam())) {
            if (treeInfo.health <= 0.9f * treeInfo.maxHealth && rc.canWater(treeInfo.ID)) {
                rc.water(treeInfo.ID);
                return true;
            }
        }
    	return false;
    }
	
	public static boolean checkIcanEscape(MapLocation myLocation, Direction plantDirection) throws GameActionException {
    	plantDirection = plantDirection.rotateLeftRads((float) Math.PI * 0.3333333f);
    	int occupied = 0;
    	for (int i = 0; i < 5; i++) {
    		rc.setIndicatorDot(myLocation.add(plantDirection, 2f), 255, 0, 0);
    		if(rc.isCircleOccupiedExceptByThisRobot(myLocation.add(plantDirection, 1.8f), 2f) || !rc.onTheMap(myLocation.add(plantDirection, 2f))) {
    			occupied++;
    		}
    		plantDirection = plantDirection.rotateLeftRads((float) Math.PI * 0.3333333f);
    	}
    	//System.out.format("Spaces occupied: %d\n", occupied);
    	return occupied < 4;
    }
	
	public static boolean plantTrees(MapLocation myLocation) throws GameActionException {
    	
    	Direction plantDirection = new Direction(0);
		for (int i = 0; i < 8; i++) {
			if (rc.canPlantTree(plantDirection) && checkIcanEscape(myLocation, plantDirection) && rc.onTheMap(myLocation, 3f)) {
				rc.plantTree(plantDirection);
				return true;
			}
			plantDirection = plantDirection.rotateLeftRads((float) Math.PI * 0.25f);
		}
		return false;
    }
	static boolean tryToBuild(RobotType typeToBuild) throws GameActionException {
		
    	Direction buildDirection = new Direction(0);
		for (int i = 0; i < 8; i++) {
			if (rc.canBuildRobot(typeToBuild, buildDirection) && rc.onTheMap(rc.getLocation().add(buildDirection, 5f))) {
				rc.buildRobot(typeToBuild, buildDirection);
				return true;
			}
			buildDirection = buildDirection.rotateLeftRads((float) Math.PI * 0.25f);
		}
		return false;
	}
}
