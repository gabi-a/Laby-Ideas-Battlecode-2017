package turtlebot;

import battlecode.common.*;

public class BotGardener {
	
    static final double PROTECTION_RADIUS = 10.0f;
    static final double PROTECTION_DELTA = 1.0f;
    static int trappedCount = 0;
    static boolean clockwiseFlag = false; 

    public static void turn(RobotController rc) throws GameActionException {
 

        MapLocation archonLoc = new MapLocation(rc.readBroadcast(0), rc.readBroadcast(1));
        MapLocation selfLoc = rc.getLocation();

        double diffX = archonLoc.x - selfLoc.x;
        double diffY = archonLoc.y - selfLoc.y;

        double protRingDistance = Math.abs(Math.sqrt(diffX * diffX + diffY * diffY)) - PROTECTION_RADIUS;
    	
        // If we're below the protection radius, go away from our archon
        if (protRingDistance < -PROTECTION_DELTA || protRingDistance > PROTECTION_DELTA) {
            Direction moveDirection = new Direction(archonLoc, selfLoc);
            if (protRingDistance > PROTECTION_DELTA) {
                moveDirection = moveDirection.opposite();
            }
            if (rc.canMove(moveDirection)) {
                rc.move(moveDirection);
            } else if (rc.canMove(moveDirection.rotateLeftRads((float) Math.PI * 0.25f))) {
                rc.move(moveDirection.rotateLeftRads((float) Math.PI * 0.25f));
            } else if (rc.canMove(moveDirection.rotateRightRads((float) Math.PI * 0.25f))) {
                rc.move(moveDirection.rotateRightRads((float) Math.PI * 0.25f));
            }
        } // Else, are we trapped?
        else if (trappedCount >= 4) {
            Direction moveDirection = new Direction(selfLoc, archonLoc);
            moveDirection = moveDirection.rotateLeftRads((float) (Math.random() - 0.5));
            //Fall back
            if (rc.canMove(moveDirection)) {
                rc.move(moveDirection);
            }
            trappedCount = 0;
        } // Else, we're all good. So do we have spare bullets?
        else {
            TreeInfo[] nearbyTreeList = rc.senseNearbyTrees();
            for (TreeInfo tree : nearbyTreeList) {
                if (rc.canWater(tree.ID)) {
                    rc.water(tree.ID);
                }
            }
            Direction plantDirection = new Direction(archonLoc, selfLoc);
            if (rc.canPlantTree(plantDirection)) {
                rc.plantTree(plantDirection);
            }
            Direction moveDirection
                    = clockwiseFlag ? plantDirection.rotateLeftRads((float) Math.PI * 0.5f)
                            : plantDirection.rotateRightRads((float) Math.PI * 0.5f);
            if (rc.canMove(moveDirection)) {
                rc.move(moveDirection);
                trappedCount = 0;
            } else {
                clockwiseFlag = !clockwiseFlag;
                trappedCount++;
            }
        }
        
    }

}
