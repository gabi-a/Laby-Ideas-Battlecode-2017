package bensbot;
import battlecode.common.*;

public class Nav {
	public static Direction pathfind(RobotController rc, MapLocation finish) throws GameActionException {
		return rc.getLocation().directionTo(finish).rotateLeftDegrees(90+(float)Math.random()*180);
	}
}
