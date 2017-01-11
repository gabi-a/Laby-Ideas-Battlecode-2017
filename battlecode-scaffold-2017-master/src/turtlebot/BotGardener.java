package turtlebot;

import battlecode.common.*;

public class BotGardener {

    public static boolean atTargetLoc = false;
    
    public static final float MULTIPLICITY = 0.333334f;
    public static final Direction[] TREE_DIRECTIONS = 
        { 
          new Direction(0) , new Direction((float) Math.PI * MULTIPLICITY),
          new Direction((float) Math.PI * MULTIPLICITY * 2), new Direction((float) Math.PI * MULTIPLICITY * 3),
          new Direction((float) Math.PI * MULTIPLICITY * 4)
        };
    public static final Direction SPAWN_DIRECTION = new Direction((float) Math.PI * MULTIPLICITY * 5);
    public static final int TRAPPED_THRESHOLD = 10;
    
    static int lumberjackCooldown = 51;
    static final int LUMBERJAC_COOLDOWN_TIME = 50;
    
    public static void turn(RobotController rc) throws GameActionException {
        
        MapLocation selfLoc = rc.getLocation();

        if (!atTargetLoc) {
            //System.out.format("Hm. %d, (%f - %f)\n", trappedCount, targetLoc.x, targetLoc.y);

        	Nav.explore(rc);
        	
        	boolean goodToSettle = true;
        	MapLocation myLocation = rc.getLocation();
        	
        	//RobotInfo[] nearbyBots = rc.senseNearbyRobots(5);
        	//if(nearbyBots.length > 0) {
        	//	goodToSettle = false;
        	//	System.out.format("\n Too many nearby bots to settle");
        	//}
        	
        	if(goodToSettle) {
            	MapLocation[] gardens = Comms.readGardenLocs(rc);
            	for(int i = gardens.length;i-->0;) {
            		MapLocation otherGardenLoc = gardens[i];
            		System.out.format("\nlooking at garden %d", i);
            		System.out.format("\ngarden exists %b", otherGardenLoc != null);
            		
            		
            		if(otherGardenLoc != null && myLocation.distanceTo(otherGardenLoc) < 10) {
            			goodToSettle = false;
                		System.out.format("\n Too close to another garden to settle");
            			break;
            		}
            	}
        	}
        	
        	if(goodToSettle) {
        		atTargetLoc = true;
        		System.out.format("\nWrote garden succesfully: %b", Comms.writeGarden(rc, myLocation));
        	}
        }
        else {
            for (Direction plantDirection : TREE_DIRECTIONS) {
                if (rc.canPlantTree(plantDirection)) {
                    rc.plantTree(plantDirection);
                }
            }
            for (TreeInfo treeInfo : rc.senseNearbyTrees(1.5f, rc.getTeam())) {
                if (treeInfo.health <= 0.9f * treeInfo.maxHealth && rc.canWater(treeInfo.ID)) {
                    rc.water(treeInfo.ID);
                }
            }
            RobotType typeToBuild;
            int lumberjacks = Comms.readNumLumberjacks(rc);
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees(RobotType.GARDENER.sensorRadius, Team.NEUTRAL);
            if(nearbyTrees.length > 0) {
            	if(lumberjacks < 5) {
            		typeToBuild = RobotType.LUMBERJACK;
            	} else {
            		Comms.pushHighPriorityTree(rc, nearbyTrees[0], 5);
            		typeToBuild = RobotType.SOLDIER;
            	}
            } else {
            	typeToBuild = RobotType.SOLDIER;
            }
            if (rc.canBuildRobot(typeToBuild, SPAWN_DIRECTION)) {
                rc.buildRobot(typeToBuild, SPAWN_DIRECTION);
                if(typeToBuild == RobotType.LUMBERJACK) {
                	Comms.writeNumLumberjacks(rc, lumberjacks+1);
                }
            }
            else {
                //System.out.println(":(");
            }
        }

    }

}
