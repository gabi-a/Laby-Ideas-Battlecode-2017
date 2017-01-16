package SprintBot;
import battlecode.common.*;

public class BotGardener {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotGardener.rc = rc;
		Util.updateBotCount(rc);
		Util.reportDeath(rc);
		
		MapLocation myLocation = rc.getLocation();
		Nav.treeBug(rc);
		waterTrees();
		if(rc.getTeamBullets() > 50 && rc.getTreeCount() < Util.getNumBots(RobotType.GARDENER)*Util.T) {
			plantTrees(myLocation);
		}
	}
}
