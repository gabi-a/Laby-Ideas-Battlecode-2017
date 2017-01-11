package turtlebot;
import battlecode.common.*;

public class Comms {

    private static final int POINTER_OFFSET = 1;
    
    public static void writeStack(RobotController rc, int stackStart, int stackEnd, MapLocation location) throws GameActionException {
        
        int stackPointer = rc.readBroadcast(stackStart);
        
        if (stackStart + stackPointer + 1 > stackEnd) {
            System.out.println("Oops! Exceeded stack limit.");
            return;
        }
        
        Team myTeam = rc.getTeam();
        MapLocation referencePoint = (rc.getInitialArchonLocations(myTeam))[0];
        MapLocation cornerPoint = referencePoint.add((float) Math.PI, 100).add((float) Math.PI * 0.5f, -100);
        
        int mapZoneX = (int) (location.x - cornerPoint.x);
        int mapZoneY = (int) (location.y - cornerPoint.y);
        
        // Debug only!
        if (mapZoneX < 0 || mapZoneY < 0 || mapZoneX >= 200 || mapZoneY >= 200) {
            System.out.format("We shouldn't be here! Map zone X/Y < 0 or >= 200, is %d,%d\n", mapZoneX, mapZoneY);
        }
        
        int packedLocation = (mapZoneX << 8) | (mapZoneY);

        rc.broadcast(stackStart + POINTER_OFFSET + stackPointer, packedLocation);
        rc.broadcast(stackStart, stackPointer + 1);
    }
    
    public static MapLocation readStack(RobotController rc, int stackStart, int stackEnd) throws GameActionException {
        
        int stackPointer = rc.readBroadcast(stackStart);
        
        if (stackPointer == 0) {
            return null;
        }
        
        stackPointer--;
        
        int packedLocation = rc.readBroadcast(stackStart + POINTER_OFFSET + stackPointer);

        int mapZoneX = (packedLocation & 0xFF00) >> 8;
        int mapZoneY = packedLocation & 0x00FF;
        
        Team myTeam = rc.getTeam();
        MapLocation referencePoint = (rc.getInitialArchonLocations(myTeam))[0];
        MapLocation cornerPoint = referencePoint.add((float) Math.PI, 100).add((float) Math.PI * 0.5f, -100);
        
        
        return new MapLocation(cornerPoint.x + mapZoneX, cornerPoint.y + mapZoneY);
    }
    
    public static MapLocation popStack(RobotController rc, int stackStart, int stackEnd) throws GameActionException {
        
        MapLocation returnValue = Comms.readStack(rc, stackStart, stackEnd);
        
        int stackPointer = rc.readBroadcast(stackStart);
        
        if (stackPointer == 0) {
            return null;
        }
        
        stackPointer--;
        
        rc.broadcast(stackStart + POINTER_OFFSET + stackPointer, 0);
        rc.broadcast(stackStart, stackPointer);
        
        return returnValue;
    }
}