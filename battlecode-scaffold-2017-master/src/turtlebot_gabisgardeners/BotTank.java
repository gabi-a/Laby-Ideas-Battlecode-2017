package turtlebot_gabisgardeners;

import battlecode.common.*;

public class BotTank {
	static RobotController rc;
	
	public static void turn(RobotController rc) {
		BotTank.rc = rc;
	}
}