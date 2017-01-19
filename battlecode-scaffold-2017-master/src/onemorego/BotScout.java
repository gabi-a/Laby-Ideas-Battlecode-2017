package onemorego;
import battlecode.common.*;

public class BotScout {
	static RobotController rc;
	
	static Team enemyTeam = RobotPlayer.rc.getTeam().opponent();
	static MapLocation enemyArchonLocation = RobotPlayer.rc.getInitialArchonLocations(enemyTeam)[0];
	static MapLocation targetLocation = enemyArchonLocation;
	
	static boolean exploreFlag = false;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotScout.rc = rc;
		Util.reportIfDead(rc);
		
		//TODO: Don't shoot into trees (waste of bullets)
			
		/*
		 * 
		 * Scouts care about killing gardeners _only_
		 * 
		 * 
		 */
		
		MapLocation commsTarget = Comms.readAttackLocation(rc, AttackGroup.A);
		if(commsTarget != null) {
			if(commsTarget != targetLocation) {
				exploreFlag = false;
			}
			targetLocation = commsTarget;
		}

		BulletInfo[] bullets = rc.senseNearbyBullets(6f);
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, enemyTeam);
		Util.communicateNearbyEnemies(rc, enemies);
		MapLocation myLocation = rc.getLocation();

		MapLocation[] safeMoveLocations = Nav.getSafeMoveLocations(rc, bullets);

		TreeInfo[] trees = rc.senseNearbyTrees();
		for(int i = 0; i < trees.length && rc.canInteractWithTree(trees[i].getID()); i++){
			if(trees[i].getContainedBullets() > 0){
				rc.shake(trees[i].getID());
				return;
			}
		}
		RobotInfo closestEnemy = Util.getClosestEnemyExceptArchon(rc, enemies);
		
		if(closestEnemy != null) {
			
			
			rc.setIndicatorLine(myLocation,closestEnemy.location, 100, 0, 100);
			TreeInfo bestTree = Util.findBestTree(rc, trees, closestEnemy);

			if(myLocation.distanceTo(closestEnemy.location) <= 2.1f && closestEnemy.type != RobotType.GARDENER && closestEnemy.type != RobotType.SCOUT) {
				Nav.tryMove(rc, myLocation.directionTo(closestEnemy.location).opposite(), bullets);
			}
			else if(closestEnemy.type == RobotType.SCOUT) {
				Nav.tryPrecisionMove(rc, myLocation.directionTo(closestEnemy.location),myLocation.distanceTo(closestEnemy.location), bullets);
			} else if(closestEnemy.type == RobotType.GARDENER && (bestTree == null || bestTree.location.distanceTo(myLocation) > 2f)) {
				Nav.tryMove(rc, myLocation.directionTo(closestEnemy.location), bullets);
			}
			else if(bestTree != null) {
				//rc.setIndicatorDot(attackLocation, 0, 0, 255);
				MapLocation attackLocation = (closestEnemy.attackCount == 0) ? bestTree.location.add(bestTree.location.directionTo(closestEnemy.location), bestTree.radius - RobotType.SCOUT.bodyRadius + 0.1f) : bestTree.location.add(bestTree.location.directionTo(closestEnemy.location), bestTree.radius - RobotType.SCOUT.bodyRadius - 0.01f);
				rc.setIndicatorDot(attackLocation, 0, 0, 255);
				Direction attackDirection = myLocation.directionTo(attackLocation);
				float attackDistance = myLocation.distanceTo(attackLocation);
				if(myLocation.directionTo(attackLocation) != null && rc.canMove(attackDirection,  attackDistance)) {
					rc.move(attackDirection,  attackDistance);
				}

			} 
			else {
				//rc.setIndicatorDot(myLocation, 0, 255, 0);

				// Do safe moves
				MapLocation safeMoveLocation = null;
				for(int i = safeMoveLocations.length;i-->0;) {
					if(safeMoveLocations[i] == null) continue;
					safeMoveLocation = safeMoveLocations[i];
					break;
				}
				if(safeMoveLocation != null) {
					Nav.pathTo(rc, safeMoveLocation, bullets);
				} 
				else {
					Nav.tryMove(rc, myLocation.directionTo(closestEnemy.location).opposite(), bullets);
				}
			}

			MapLocation predictedEnemyLocation = Util.predictNextEnemyLocation(closestEnemy);

			if(rc.canFireSingleShot() && (myLocation.distanceTo(closestEnemy.location) < RobotType.SCOUT.bodyRadius + closestEnemy.type.bodyRadius + 0.3f || predictedEnemyLocation.distanceTo(closestEnemy.location) < 1.3f) && Util.goodToShoot(rc, myLocation, closestEnemy)) {
				rc.fireSingleShot(rc.getLocation().directionTo(closestEnemy.location));
			}

		} 
		else {
			//rc.setIndicatorDot(myLocation, 0, 0, 255);
			if(!exploreFlag) {
				if(!Nav.pathTo(rc, targetLocation, bullets)) {
					Nav.explore(rc, bullets);
				}
				if(myLocation.distanceTo(targetLocation) < 9f) exploreFlag = true;
			} else {
				Nav.explore(rc, bullets);
			}
		}
		
	}
}
