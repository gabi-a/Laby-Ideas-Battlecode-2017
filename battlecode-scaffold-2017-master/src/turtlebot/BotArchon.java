package turtlebot;

import battlecode.common.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BotArchon {

    public static void turn(RobotController rc) throws GameActionException {
        int count = 0;
        float trialDirection = (float) Math.PI * 0.125f;

        while (true) {
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
            // Let the robots know where the Archons are
            MapLocation myLocation = rc.getLocation();
            rc.broadcast(0, (int) myLocation.x);
            rc.broadcast(1, (int) myLocation.y);

            Clock.yield();
        }
    }
}
