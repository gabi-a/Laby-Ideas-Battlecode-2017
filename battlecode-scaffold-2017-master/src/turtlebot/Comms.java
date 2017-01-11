package turtlebot;
import battlecode.common.*;

public class Comms {
    
    public static final int START_CHANNEL = 100;
    public static final int POINTER_OFFSET = 1;
    
    public static void writeStack(RobotController rc, MapLocation location) throws GameActionException {
        
        int stackPointer = rc.readBroadcast(START_CHANNEL);
        
        Team myTeam = rc.getTeam();
        MapLocation referencePoint = (rc.getInitialArchonLocations(myTeam))[0];
        MapLocation cornerPoint = referencePoint.add((float) Math.PI, 100).add((float) Math.PI * 0.5f, 100);
        
        int mapZoneX = (int) (location.x - cornerPoint.x);
        int mapZoneY = (int) (location.y - cornerPoint.y);
        
        // Debug only!
        if (mapZoneX < 0 || mapZoneY < 0 || mapZoneX >= 200 || mapZoneY >= 200) {
            System.out.printf("We shouldn't be here! Map zone X/Y < 0 or >= 200");
        }
        
        int packedLocation = (mapZoneX & 0xFF) << 8 + (mapZoneY & 0xFF);

        rc.broadcast(START_CHANNEL + POINTER_OFFSET + stackPointer, packedLocation);
        rc.broadcast(START_CHANNEL, stackPointer + 1);
    }
    
    public static MapLocation readStack(RobotController rc, MapLocation location) throws GameActionException {
        
        int stackPointer = rc.readBroadcast(START_CHANNEL);
        
        if (stackPointer == 0) {
            return null;
        }
        
        stackPointer--;
        
        int packedLocation = rc.readBroadcast(START_CHANNEL + POINTER_OFFSET + stackPointer);
        int mapZoneX = packedLocation & 0xFF00;
        int mapZoneY = packedLocation & 0x00FF;
        
        Team myTeam = rc.getTeam();
        MapLocation referencePoint = (rc.getInitialArchonLocations(myTeam))[0];
        MapLocation cornerPoint = referencePoint.add((float) Math.PI, 100).add((float) Math.PI * 0.5f, 100);
        
        return new MapLocation(cornerPoint.x + mapZoneX, cornerPoint.y + mapZoneY);
    }
    
    public static void popStack(RobotController rc, MapLocation location) throws GameActionException {
        
        int stackPointer = rc.readBroadcast(START_CHANNEL);
        
        if (stackPointer == 0) {
            return;
        }
        
        stackPointer--;
        
        rc.broadcast(START_CHANNEL + POINTER_OFFSET + stackPointer, 0);
        rc.broadcast(START_CHANNEL, stackPointer);
    }
}