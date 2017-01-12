package turtlebot;

import battlecode.common.*;

public class BotGardener {

    public static boolean atTargetLoc = false;
    public static Direction[] treeDirections = new Direction[5];
    public static Direction spawnDirection = null;
    public static int numScouts = 0;
    
    public static final float MULTIPLICITY = 0.333334f;
    public static final int TRAPPED_THRESHOLD = 10;
    
    public static final int DISTANCE_BETWEEN_GARDENS = 7;
    
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
            		if(otherGardenLoc != null && myLocation.distanceTo(otherGardenLoc) < DISTANCE_BETWEEN_GARDENS) {
            			goodToSettle = false;
                		//System.out.format("\n Too close to another garden to settle");
            			break;
            		}
            	}
        	}
        	
        	if(goodToSettle) {
        		atTargetLoc = true;
                        spawnDirection = null;
        		//System.out.format("\nWrote garden succesfully: %b", Comms.writeGarden(rc, myLocation));
        	}
        }
        else {
            if(spawnDirection == null) {
                int validPlantCount = 0;
                Direction direction = new Direction(0f);
                for(int i=0; i<6; i++) {
                    if(rc.canPlantTree(direction)) {
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
                    atTargetLoc = false;
                    spawnDirection = new Direction(0);
                }
            }
            for (Direction plantDirection : treeDirections) {
                if (plantDirection != null && rc.canPlantTree(plantDirection)) {
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
            if(numScouts == 0) {
                typeToBuild = RobotType.SCOUT;
            }
            else if(nearbyTrees.length > 0) {
            	if(lumberjacks < 5) {
            		typeToBuild = RobotType.LUMBERJACK;
            	} else {
            		Comms.pushHighPriorityTree(rc, nearbyTrees[0], 5);
            		typeToBuild = RobotType.SOLDIER;
            	}
            } 
            else {
            	typeToBuild = RobotType.SOLDIER;
            }
            if (rc.canBuildRobot(typeToBuild, spawnDirection)) {
                rc.buildRobot(typeToBuild, spawnDirection);
                if(typeToBuild == RobotType.LUMBERJACK) {
                	Comms.writeNumLumberjacks(rc, lumberjacks+1);
                }
                else if(typeToBuild == RobotType.SCOUT) {
                    broadcastUnassignedScout(rc);
                    numScouts++;
                }
            }
            else {
                //System.out.println(":(");
            }
        }

    }
    
    public static void broadcastUnassignedScout(RobotController rc) throws GameActionException {
        Comms.writeStack(rc, Comms.SCOUT_ARCHON_REQUEST_START, Comms.SCOUT_ARCHON_REQUEST_END, rc.getLocation());
    }

}
