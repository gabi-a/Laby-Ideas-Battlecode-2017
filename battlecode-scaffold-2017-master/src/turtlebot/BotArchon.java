package turtlebot;

import battlecode.common.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BotArchon {
    
    static int count = 0;
    
    public static void turn(RobotController rc) throws GameActionException {
 
        while (count < 1) {
            int didHire = BotArchon.tryHireGardener(rc) ? 1 : 0;
            count += didHire;
            if (didHire != 0) {
                Comms.writeStack(rc, 0, 50, new MapLocation(5,15));
            }
        }
    }
    
    public static boolean tryHireGardener(RobotController rc) throws GameActionException {
        Direction hireDirection = new Direction(0);
        for (int i =0 ; i < 8; i++) {
            if(rc.canHireGardener(hireDirection)) {
                rc.hireGardener(hireDirection);
                return true;
            }
            hireDirection = hireDirection.rotateLeftRads((float) Math.PI * 0.25f);
        }
        return false;
    }
}
