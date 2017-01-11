package firstturtle;

import battlecode.common.*;

public class BotGardener {

    public static MapLocation targetLoc = null; 
    public static boolean atTargetLoc = false;
    public static int trappedCount = 0;
    
    public static final float POSITION_FIDELITY = 1f;
    public static final float MULTIPLICITY = 0.333334f;
    public static final Direction[] TREE_DIRECTIONS = 
        { 
          new Direction(0) , new Direction((float) Math.PI * MULTIPLICITY),
          new Direction((float) Math.PI * MULTIPLICITY * 2), new Direction((float) Math.PI * MULTIPLICITY * 3),
          new Direction((float) Math.PI * MULTIPLICITY * 4)
        };
    public static final Direction SPAWN_DIRECTION = new Direction((float) Math.PI * MULTIPLICITY * 5);
    public static final int TRAPPED_THRESHOLD = 10;
    
    public static void turn(RobotController rc) throws GameActionException {
        
        MapLocation selfLoc = rc.getLocation();

        if (targetLoc == null) {
            targetLoc = Comms.popStack(rc, 100, 120);
        }
        else if (trappedCount > TRAPPED_THRESHOLD) {
            targetLoc = null;
            BotGardener.broadcastUnassigned(rc);
        }
        else if (!atTargetLoc) {
            //System.out.format("Hm. %d, (%f - %f)\n", trappedCount, targetLoc.x, targetLoc.y);
            Direction moveDirection = new Direction(selfLoc, targetLoc);
            if(rc.canMove(moveDirection)) {
                rc.move(moveDirection);
                trappedCount = 0;
            }
            else {
                trappedCount++;
            }
            if(selfLoc.distanceTo(targetLoc) <= POSITION_FIDELITY) {
                atTargetLoc = true;
            }
        }
        else {
            for (Direction plantDirection : TREE_DIRECTIONS) {
                if (rc.canPlantTree(plantDirection)) {
                    rc.plantTree(plantDirection);
                }
            }
            for (TreeInfo treeInfo : rc.senseNearbyTrees(1.5f)) {
                if (treeInfo.health <= 0.9f * treeInfo.maxHealth && rc.canWater(treeInfo.ID)) {
                    rc.water(treeInfo.ID);
                }
            }
            RobotType typeToBuild;
            int lumberjacks = Comms.readNumLumberjacks(rc);
            if(rc.senseNearbyTrees(RobotType.GARDENER.sensorRadius, Team.NEUTRAL).length > 0 && lumberjacks < 10) {
            	typeToBuild = RobotType.LUMBERJACK;
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
    
    public static void broadcastUnassigned(RobotController rc) throws GameActionException {
        Comms.writeStack(rc, 121, 140, new MapLocation(0,0));
        trappedCount = 0;
        System.out.println("No, I'm trapped :|");
    }

}
