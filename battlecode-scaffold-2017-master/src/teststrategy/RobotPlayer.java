package teststrategy;

import battlecode.common.*;

public strictfp class RobotPlayer {
	
	static RobotController rc;
	
	static int donateThreshold = 400;
	
	public static void run(RobotController rc) throws GameActionException {
		RobotPlayer.rc = rc;
		while(true) {
			
			Util.reportIfDead(rc);
			
			if(rc.getRoundNum() > rc.getRoundLimit() - 2) {
				rc.donate(rc.getTeamBullets());
			}
			
			if(rc.getOpponentVictoryPoints() > 0 || rc.getTreeCount() > 70) {
				donateThreshold = 310;
			}
			
			if(rc.getTeamBullets() > donateThreshold) {
				int amountToDonate = (int) (rc.getTeamBullets() - donateThreshold);
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
