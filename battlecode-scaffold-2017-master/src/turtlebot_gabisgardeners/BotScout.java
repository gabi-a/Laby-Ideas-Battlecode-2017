package turtlebot_gabisgardeners;

import battlecode.common.*;

public class BotScout {
    static final float BULLET_SPREAD_FACTOR = 0.0f;
    static final float STRIDE_RADIUS = 2.5f;
    static boolean suspended = false;
    static MapLocation lockLocation = null;
    static int trapped = 0;

    public static void turn(RobotController rc) throws GameActionException {

        // High priority first
        RobotType[] SCOUT_PRIORITY_QUEUE
                = {RobotType.LUMBERJACK, RobotType.TANK,
                    RobotType.SOLDIER, RobotType.GARDENER,
                    RobotType.SCOUT, RobotType.ARCHON};

        Team myTeam = rc.getTeam();
        MapLocation selfLoc = rc.getLocation();

        // Find enemies, off the stack, or look at archon locations.
        if (!suspended) {      

            if (lockLocation == null) {
                lockLocation = BotScout.getLockLocation(rc, selfLoc);
            }
            
            if (lockLocation != null) {
                // If we're at the destination...
                if (rc.canSensePartOfCircle(lockLocation, 1.0f) || trapped >= 20) {
                    lockLocation = null;
                    trapped = 0;
                }
                // Else keep moving, if we can
                else {
                    Direction moveDirection = new Direction(selfLoc, lockLocation);
                    if (rc.canMove(moveDirection)) {
                        rc.move(moveDirection);
                    }
                    else if (!rc.onTheMap(selfLoc.add(moveDirection, STRIDE_RADIUS * 2))) {
                        System.out.format("I'm at (%f, %f), and couldn't get to (%f, %f)", selfLoc.x, selfLoc.y, lockLocation.x, lockLocation.y);
                        lockLocation = null;
                    }
                    else {
                        trapped++;
                    }
                }
            }
        }

        RobotInfo[] nearbyRobotInfo = rc.senseNearbyRobots(-1, myTeam.opponent());
        MapLocation targetLoc = null;

        // Check in order of priority.
        // Probably a way to do this with less bytecodes
        outerloop:
        for (RobotType type : SCOUT_PRIORITY_QUEUE) {
            for (RobotInfo botInfo : nearbyRobotInfo) {
                if (botInfo.team == myTeam.opponent()) {
                    if (botInfo.type == type) {
                        targetLoc = botInfo.location;
                        break outerloop;
                    }
                }
            }
        }

        if (targetLoc != null) {
            //suspended = true;
            Direction shootDirection = new Direction(selfLoc, targetLoc);
            // Bit of randomness
            shootDirection = shootDirection.rotateLeftRads(((float) Math.random() - 0.5f) * BULLET_SPREAD_FACTOR);
            if (rc.canFireSingleShot()) {
                rc.fireSingleShot(shootDirection);
            }
        } else {
            suspended = false;
        }
    }
    
    public static MapLocation getLockLocation(RobotController rc, MapLocation selfLoc) throws GameActionException {
        
        MapLocation location = Comms.popStack(rc, 0, 50);
        Team myTeam = rc.getTeam();
        
        if (location != null) {
            return location;
        }
        
        MapLocation[] enemyLocList = rc.getInitialArchonLocations(myTeam.opponent());
        for (MapLocation enemyLoc : enemyLocList) {
            if (location == null || selfLoc.distanceTo(enemyLoc) < selfLoc.distanceTo(location)) {
                location = enemyLoc;
            }
        }
        return location;
    }
}
