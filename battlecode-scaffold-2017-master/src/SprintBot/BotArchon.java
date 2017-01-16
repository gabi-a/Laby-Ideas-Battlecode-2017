package SprintBot;
import battlecode.common.*;

public class BotArchon {
	static RobotController rc;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotArchon.rc = rc;
		
		Util.updateBotCount(rc);
		Util.reportDeath(rc);
		
		if(rc.getTeamBullets() > 10000) {
			rc.donate(10000);
		}
		
		if(rc.getTeamBullets() >= 100 && Util.getNumBots(RobotType.GARDENER) < 1 + 0.7f * rc.getTreeCount()/Util.G) {
			tryHireGardener();
		}
		
		Nav.treeBug(rc);
		
	}
	
}

