package teststrategy;

import battlecode.common.*;

public strictfp class RobotPlayer {
	
	static RobotController rc;
	
	public static void run(RobotController rc) throws GameActionException {
		RobotPlayer.rc = rc;
		while(true) {
			
			Util.reportIfDead(rc);
			
			if(rc.getRoundNum() > rc.getRoundLimit() - 2) {
				rc.donate(rc.getTeamBullets());
			}
			
			if(rc.getTeamBullets() > 500) {
				int amountToDonate = (int) (rc.getTeamBullets() - 500);
				if(amountToDonate > 0) {
					rc.donate(amountToDonate);
				}
			}
			
			if(rc.getTeamBullets() > rc.getVictoryPointCost() * (1000 - rc.getTeamVictoryPoints())) {
				rc.donate(rc.getTeamBullets());
			}
			
			try {
				switch (rc.getType()) {
				case ARCHON:
				    BotArchon.turn(rc);
				    break;
				case GARDENER:
					BotGardener.turn(rc);
				    break;
				case SOLDIER:
					BotSoldier.turn(rc);
				    break;
				case LUMBERJACK:
				    BotLumberjack.turn(rc);
				    break;
				case TANK:
					BotTank.turn(rc);
					break;
				case SCOUT:
					BotScout.turn(rc);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			Clock.yield();
		}
	}
}
