package onemorego;

import battlecode.common.*;

public strictfp class RobotPlayer {
	
	static RobotController rc;
	static RobotInfo[] enemies;
	
	public static void run(RobotController rc) throws GameActionException {
		RobotPlayer.rc = rc;
		while(true) {
			try {
				
				enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
				
				Util.updateBotCount(rc);
				Util.reportIfDead(rc);
				Util.communicateNearbyEnemies(rc, enemies);
				
				switch (rc.getType()) {
				case ARCHON:
				    BotArchon.turn();
				    break;
				case GARDENER:
					BotGardener.turn();
				    break;
				case SOLDIER:
					BotSoldier.turn();
				    break;
				case LUMBERJACK:
				    BotLumberjack.turn();
				    break;
				case TANK:
					BotTank.turn();
					break;
				case SCOUT:
					BotScout.turn();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Clock.yield();
		}
	}
}
