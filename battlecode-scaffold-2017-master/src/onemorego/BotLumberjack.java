package onemorego;

import battlecode.common.*;

public class BotLumberjack {
	static RobotController rc;

	static TreeInfo treeTarget;
	static TreeInfo tempTreeTarget;
	static RobotInfo enemyTarget;
	static MapLocation home;
	static Strategy strat;
	static boolean startupFlag = true;
	static int delegation;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotLumberjack.rc = rc;
		Util.reportDeath(rc);
		
		BulletInfo[] bullets = rc.senseNearbyBullets();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		
		if(startupFlag) {
			delegation = Comms.lumberjackStack.pop(rc);
			startupFlag = false;
		}
		
		if(enemies.length > 0 || delegation == 1) {
			strat = Strategy.OFFENSE;
		} else {
			strat = Strategy.LUMBERJACK;
		}
		
		//if(strat == null){
		//	float randStrat = (float)Math.random();
		//	if(randStrat < 0.0) strat = Strategy.LUMBERJACK;
		//	else strat = Strategy.OFFENSE;
		//}

		if(strat == Strategy.OFFENSE) {
			if(Clock.getBytecodesLeft() < 1000) System.out.format("Line: %d Bytecodes Left: %d\n",new Throwable().getStackTrace()[0].getLineNumber(), Clock.getBytecodesLeft());
			// let another lumberjack cut the tree
			if(treeTarget != null) {
				Comms.neutralTrees.push(rc, treeTarget);
				treeTarget = null;
			}
			
			// update target
			if(enemyTarget != null){
				if(!rc.canSenseRobot(enemyTarget.getID())) enemyTarget = null;
				else enemyTarget = rc.senseRobot(enemyTarget.getID());
			}
			if(Clock.getBytecodesLeft() < 1000) System.out.format("Line: %d Bytecodes Left: %d\n",new Throwable().getStackTrace()[0].getLineNumber(), Clock.getBytecodesLeft());
			// Find target
			if(enemyTarget == null){
				if(enemies.length == 0){
					Nav.explore(rc, bullets);
					return;
				}
				if(Clock.getBytecodesLeft() < 1000) System.out.format("Line: %d Bytecodes Left: %d\n",new Throwable().getStackTrace()[0].getLineNumber(), Clock.getBytecodesLeft());
				float dist = 200;
				for(int i = 0; i < enemies.length; i++){
					float newDist = enemies[i].getLocation().distanceTo(rc.getLocation());
					if(newDist < dist){
						dist = newDist;
						enemyTarget = enemies[i];
					}
				}
				if(Clock.getBytecodesLeft() < 1000) System.out.format("Line: %d Bytecodes Left: %d\n",new Throwable().getStackTrace()[0].getLineNumber(), Clock.getBytecodesLeft());
			}
			if(Clock.getBytecodesLeft() < 1000) System.out.format("Line: %d Bytecodes Left: %d\n",new Throwable().getStackTrace()[0].getLineNumber(), Clock.getBytecodesLeft());
			// get in range and kill
			float strikeRadius = 2 + enemyTarget.getType().bodyRadius;
			if(rc.getLocation().distanceTo(enemyTarget.getLocation()) <= strikeRadius && rc.senseNearbyRobots(strikeRadius, rc.getTeam()).length == 0){
				rc.strike();
				if(Clock.getBytecodesLeft() < 1000) System.out.format("Line: %d Bytecodes Left: %d\n",new Throwable().getStackTrace()[0].getLineNumber(), Clock.getBytecodesLeft());
			} else {
				if(Clock.getBytecodesLeft() < 1000) System.out.format("Line: %d Bytecodes Left: %d\n",new Throwable().getStackTrace()[0].getLineNumber(), Clock.getBytecodesLeft());
				if(!Nav.pathTo(rc, enemyTarget.getLocation(), bullets)) {
					
					// Can't move, so do what a lumberjack does best
					if(tempTreeTarget == null) {
						TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
						if(trees.length == 0){
							Nav.explore(rc, bullets);
							return;
						}
						tempTreeTarget = trees[0];
					}
					if(Clock.getBytecodesLeft() < 1000) System.out.format("Line: %d Bytecodes Left: %d\n",new Throwable().getStackTrace()[0].getLineNumber(), Clock.getBytecodesLeft());
					// Chop target
					if(rc.canChop(tempTreeTarget.getID())){
						rc.chop(tempTreeTarget.getID());
					}

					// Check if target is dead
					if(rc.canSenseLocation(tempTreeTarget.getLocation()) && !rc.canSenseTree(tempTreeTarget.getID())) {
						tempTreeTarget = null;
					}
					if(Clock.getBytecodesLeft() < 1000) System.out.format("Line: %d Bytecodes Left: %d\n",new Throwable().getStackTrace()[0].getLineNumber(), Clock.getBytecodesLeft());
				}
			}
		}

		if(strat == Strategy.DEFENSE) {

		}

		if(strat == Strategy.LUMBERJACK){
			// Find a target
			/*if(treeTarget == null) {
				treeTarget = Comms.neutralTrees.pop(rc);
			}*/
			if(Clock.getBytecodesLeft() < 1000) System.out.format("Line: %d Bytecodes Left: %d\n",new Throwable().getStackTrace()[0].getLineNumber(), Clock.getBytecodesLeft());
			if(treeTarget == null) {
				TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
				if(trees.length == 0){
					Nav.explore(rc, bullets);
					return;
				}
				treeTarget = trees[0];
			}
			if(Clock.getBytecodesLeft() < 1000) System.out.format("Line: %d Bytecodes Left: %d\n",new Throwable().getStackTrace()[0].getLineNumber(), Clock.getBytecodesLeft());
			// Go to target
			if(!rc.canInteractWithTree(treeTarget.getID())){
				Nav.pathTo(rc, treeTarget.getLocation(), bullets);
			}
			if(Clock.getBytecodesLeft() < 1000) System.out.format("Line: %d Bytecodes Left: %d\n",new Throwable().getStackTrace()[0].getLineNumber(), Clock.getBytecodesLeft());
			// Chop target
			if(rc.canChop(treeTarget.getID())){
				rc.chop(treeTarget.getID());
			}
			if(Clock.getBytecodesLeft() < 1000) System.out.format("Line: %d Bytecodes Left: %d\n",new Throwable().getStackTrace()[0].getLineNumber(), Clock.getBytecodesLeft());
			// Check if target is dead
			if(rc.canSenseLocation(treeTarget.getLocation()) && !rc.canSenseTree(treeTarget.getID())) {
				treeTarget = null;
			}
			if(Clock.getBytecodesLeft() < 1000) if(Clock.getBytecodesLeft() < 1000) System.out.format("Line: %d Bytecodes Left: %d\n",new Throwable().getStackTrace()[0].getLineNumber(), Clock.getBytecodesLeft());
		}
	}
}
