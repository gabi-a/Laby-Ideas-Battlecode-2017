package turtlebot;

import battlecode.common.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BotArchon {
    
    static int count = 0;
    static boolean pushFlag = false;
    
    public static void turn(RobotController rc) throws GameActionException {
 
        float trialDirection = (float) Math.PI * 0.125f;
        MapLocation myLoc = rc.getLocation();

        while (count < 2) {
            Direction trial = new Direction(trialDirection);
            if (rc.canHireGardener(trial)) {
                rc.hireGardener(trial);
                count++;
            }
            trialDirection += Math.PI * 0.25f;
            if (trialDirection >= 2 * Math.PI) {
                trialDirection -= 2 * Math.PI;
            }
        }
        
        if (!pushFlag) {
            for (int i = 0; i < 8; i++) {
                Comms.writeStack(rc, myLoc.add((float)Math.PI * 0.25f * i, 20f));
            }
            pushFlag = true;
        }
        
        // Let the robots know where the Archons are
        MapLocation myLocation = rc.getLocation();
        rc.broadcast(0, (int) myLocation.x);
        rc.broadcast(1, (int) myLocation.y);
    }
}
