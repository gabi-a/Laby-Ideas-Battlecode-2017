package smoothmoves;

import battlecode.common.*;

public strictfp class RobotPlayer {
	
	static RobotController rc;
	
	public static void run(RobotController rc) throws GameActionException {
		RobotPlayer.rc = rc;
		while(true) {
			Util.reportIfDead(rc);
			Util.updateMyPostion(rc);
			
			if(rc.getRoundNum() > rc.getRoundLimit() - 2) {
				rc.donate(rc.getTeamBullets());
			}
			
			if(rc.getTeamBullets() > 1000 * (7.5f + rc.getRoundNum() * 0.0041666666f)) {
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
