package smoothmoves;
import battlecode.common.*;

public class Util {
	
	static boolean reportedDeath = false;
	
	public static void reportIfDead(RobotController rc) throws GameActionException {
		if(!reportedDeath && rc.getHealth() <= 5f) {
			Comms.ourBotCount.decrementNumBots(rc, rc.getType());
    		reportedDeath = true;
    	}
	}
	
	public static void reportEnemyBots(RobotController rc, RobotInfo[] enemies) throws GameActionException {
		
		RobotInfo[] enemyGardeners = Comms.enemyGardenersArray.arrayBots(rc);
		
		for(int i = enemyGardeners.length;i-->0;) {
			if(enemyGardeners[i] != null) rc.setIndicatorDot(enemyGardeners[i].location, 0, 0, 255);
			if(enemyGardeners[i] != null && rc.canSenseLocation(enemyGardeners[i].location) && !rc.canSenseRobot(enemyGardeners[i].ID)) {
				Comms.enemyGardenersArray.deleteBot(rc, enemyGardeners[i]);
			}
		}
		
		for(int i = enemies.length;i-->0;) {
			if(enemies[i].type == RobotType.GARDENER && enemies[i].health > 5f) {
				Comms.enemyGardenersArray.writeBot(rc, enemies[i]);
			}
		}
		
	}
	
}
