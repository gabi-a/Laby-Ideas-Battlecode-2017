package smoothmoves;

import battlecode.common.*;

public strictfp class RobotPlayer {
	
	static RobotController rc;
	
	public static void run(RobotController rc) throws GameActionException {
		RobotPlayer.rc = rc;
		while(true) {
			System.out.println("Bytecodes used before utils: "+Clock.getBytecodeNum());
			Util.reportIfDead(rc);
			System.out.println("Bytecodes after death report: "+Clock.getBytecodeNum());
			Util.updateMyPostion(rc);
			System.out.println("Bytecodes used after utils: "+Clock.getBytecodeNum());
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
