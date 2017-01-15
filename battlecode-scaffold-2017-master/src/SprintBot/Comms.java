package SprintBot;
import battlecode.common.*;

public class Comms {

	public static final int ROBOT_NUMS_START = 703; // End is 709
	
	public static void writeNumRobots(RobotController rc, RobotType type, int num) throws GameActionException {
		rc.broadcast(ROBOT_NUMS_START-1+type.ordinal(), num);
	}
	
	public static int readNumRobots(RobotController rc, RobotType type) throws GameActionException {
		return rc.readBroadcast(ROBOT_NUMS_START-1+type.ordinal());
	}

}
