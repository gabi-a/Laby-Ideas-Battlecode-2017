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
		System.out.format("\nGardeners %d, Trees %d\n",Util.getNumBots(RobotType.GARDENER),rc.getTreeCount());
		if(rc.getTeamBullets() >= 100 && Util.getNumBots(RobotType.GARDENER) <= Math.min(7, rc.getTreeCount()/Util.G)) {
			tryHireGardener();
		}
		
		//Nav.treeBug(rc);
		Nav.explore(rc);
	}
	
	public static boolean tryHireGardener() throws GameActionException {
		Direction hireDirection = new Direction(0);
		for (int i = 0; i < 8; i++) {
			if (rc.canHireGardener(hireDirection) && rc.onTheMap(rc.getLocation().add(hireDirection, 5f))) {
				rc.hireGardener(hireDirection);
				Util.increaseNumBotsByOne(rc, RobotType.GARDENER);
				return true;
			}
			hireDirection = hireDirection.rotateLeftRads((float) Math.PI * 0.25f);
		}
		return false;
	}
	
}

