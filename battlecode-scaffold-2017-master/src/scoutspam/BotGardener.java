package scoutspam;

import battlecode.common.*;
import static turtlebotpathing.BotGardener.broadcastUnassignedScout;
import static turtlebotpathing.BotGardener.spawnDirection;
import static turtlebotpathing.BotGardener.treeDirections;

public class BotGardener {
	
	public static enum MoveState {
		LEFT,
		RIGHT,
		TOWARD_LEFT,
		TOWARD_RIGHT
	}
	
	static RobotController rc;
	static int roundNum;
	static MapLocation myLocation;
	static MoveState moveState = MoveState.TOWARD_RIGHT;
	public static Direction spawnDirection = null;
    public static Direction[] treeDirections = new Direction[5];
    public static final float MULTIPLICITY = 0.333334f;
	static boolean startupFlag = true;
	
	public static void turn(RobotController rc) throws GameActionException {
		
		BotGardener.rc = rc;
		roundNum = rc.getRoundNum();
		myLocation = rc.getLocation();
		
		if(startupFlag) {
			setSpawnDirection(myLocation);
			startupFlag = false;
		}
		
		if(rc.getRoundNum() > 30) {
			plantTrees();
			waterTrees();
		}
		
		tryToBuild(RobotType.SCOUT);
	}
	
	private static boolean tryToBuild(RobotType typeToBuild) throws GameActionException {
		if(rc.canBuildRobot(typeToBuild, spawnDirection)) {
			rc.buildRobot(typeToBuild, spawnDirection);
			return true;
		}
		return false;
	}
	
	public static int setSpawnDirection(MapLocation myLocation) throws GameActionException {
    	int validPlantCount = 0;
        Direction direction = new Direction(0f);
        for(int i=0; i<6; i++) {
            if(!rc.isCircleOccupied(myLocation.add(direction, 2f),0.9f) || rc.isLocationOccupiedByRobot(myLocation.add(direction, 2f))) {
                if(spawnDirection == null) {
                    spawnDirection = direction;
                }
                else {
                    treeDirections[validPlantCount] = direction;
                    validPlantCount++;
                }
            }
            direction = direction.rotateLeftRads((float) Math.PI * MULTIPLICITY);
        }
        if(spawnDirection == null) {
            spawnDirection = new Direction(0);
        }
    	return validPlantCount;
    }
	
	public static boolean plantTrees() throws GameActionException {
    	for (Direction plantDirection : treeDirections) {
			if (plantDirection != null && rc.canPlantTree(plantDirection)) {
				rc.plantTree(plantDirection);
				return true;
			}
		}
    	return false;
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
}
