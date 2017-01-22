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
			if(rc.canSenseRobot(enemyGardeners[i].ID) && rc.senseRobot(enemyGardeners[i].ID).health < 5f) {
				// Delete the bot
			}
		}
		
		for(int i = enemies.length;i-->0;) {
			if(enemies[i].type == RobotType.GARDENER && enemies[i].health > 5f) {
				Comms.enemyGardenersArray.writeBot(rc, enemies[i]);
			}
		}
	}
	
}
