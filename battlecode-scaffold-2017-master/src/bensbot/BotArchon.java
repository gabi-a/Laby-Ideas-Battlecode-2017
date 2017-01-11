package bensbot;

import battlecode.common.*;

public class BotArchon {
	
	public static void turn(RobotController rc) throws GameActionException {
		Direction dir = new Direction(0);
		if(rc.readBroadcast(0) == 0){
			rc.hireGardener(dir);
			rc.broadcast(0,1);
		}
	}
	
}

