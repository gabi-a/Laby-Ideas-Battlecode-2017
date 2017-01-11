package turtlebot;

import battlecode.common.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BotArchon {
    
    public static int count = 0;
    public static boolean startupFlag = true;
    public static MapLocation[] gardenSpawns = new MapLocation[81];
    public static int gardenSpawnPointer = 0;
    public static final float GARDEN_DISTANCE = 5f;
    
    public static void turn(RobotController rc) throws GameActionException {
        
        MapLocation selfLoc = rc.getLocation();
        
        if (startupFlag) {
            for (int i = -4; i <= 4; i++ ) {
                for (int j = -4; i <= 4; i++) {
                    gardenSpawns[i + 4 + 9 * (j + 4)] =
                            selfLoc.add(0f, GARDEN_DISTANCE * i)
                            .add((float) Math.PI * 0.5f, GARDEN_DISTANCE * j);
                }
            }
        }
        
        while (count < 2) {
            int didHire = BotArchon.tryHireGardener(rc) ? 1 : 0;
            count += didHire;
            if (didHire != 0) {
                BotArchon.delegateGardener(rc);
            }
        }
        
        while(BotArchon.checkUnnassingedGardener(rc)) {
            BotArchon.delegateGardener(rc);
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
    
    public static void delegateGardener(RobotController rc) throws GameActionException {
        Comms.writeStack(rc, 0, 20, gardenSpawns[gardenSpawnPointer]);
        System.out.format("Delegrated (%f, %f)", gardenSpawns[gardenSpawnPointer].x, gardenSpawns[gardenSpawnPointer].y);
        gardenSpawnPointer++;
        gardenSpawnPointer %= 81;
    }
    
    public static boolean checkUnnassingedGardener(RobotController rc) throws GameActionException {
        if(Comms.popStack(rc, 21, 40) != null) {
            return true;
        }
        return false;
    }
}
