package experimentalbot;

import battlecode.common.*;

public class BotScout {

	static RobotController rc;

	public static enum MoveState {
		LEFT,
		RIGHT,
		TOWARD_LEFT,
		TOWARD_RIGHT
	}

	
	static Team myTeam;
	static boolean startupFlag = true;


	public static void turn(RobotController rc) throws GameActionException {
		
		// Set variables so we never have to call those functions again
		if (startupFlag) {
			startupFlag = false;
		}
		
		BotScout.rc = rc;
		
		Nav.pathTo(rc, rc.getInitialArchonLocations(rc.getTeam().opponent())[0], new RobotType[]{RobotType.SOLDIER});

	}
	
	
}
