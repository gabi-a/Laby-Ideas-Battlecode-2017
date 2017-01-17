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
		Util.reportDeath(rc);
		
		//TODO: Don't shoot into trees (waste of bullets)
			
		/*
		 * 
		 * Scouts care about killing gardeners _only_
		 * 
		 * 
		 */
		
		MapLocation commsTarget = Comms.readAttackLocation(rc);
		if(commsTarget != null) {
			targetLocation = commsTarget;
		}

		BulletInfo[] bullets = rc.senseNearbyBullets(6f);
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, enemyTeam);
		
		MapLocation myLocation = rc.getLocation();
		System.out.format("1 Bytecodes used: %d\n", Clock.getBytecodeNum());
		MapLocation[] safeMoveLocations = Nav.getSafeMoveLocations(rc, bullets);
		System.out.format("2 Bytecodes used: %d\n", Clock.getBytecodeNum());
		TreeInfo[] trees = rc.senseNearbyTrees();
		
		if(enemies.length > 0) {
			rc.setIndicatorDot(myLocation, 255, 0, 0);
			

			RobotInfo closestEnemy = null;
			
			for(int i = 0; i<enemies.length; i++) {
				
				if(enemies[i].getType() == RobotType.GARDENER || enemies[i].getType() == RobotType.ARCHON) {
					Comms.writeAttackEnemy(rc, enemies[i].getLocation(), enemies[i].getID());
				}
				
				if(enemies[i].getID() == Comms.readAttackID(rc)) {
					if(enemies[i].getHealth() < 20f) {
						Comms.clearAttackEnemy(rc);
					}
				}
				
				if(enemies[i].getType() != RobotType.ARCHON) {
					closestEnemy = enemies[i];
					break;
				}
			}
			
			if(closestEnemy != null) {
				
				rc.setIndicatorLine(myLocation,enemies[0].location, 100, 0, 100);
				TreeInfo bestTree = Util.findBestTree(rc, trees, closestEnemy);
				
				if(myLocation.distanceTo(closestEnemy.location) <= 2.1f) {
					Nav.tryMove(rc, myLocation.directionTo(closestEnemy.location).opposite(), bullets);
				}
				else if(bestTree != null) {
					
					rc.setIndicatorDot(bestTree.location, 255, 0, 0);

					MapLocation attackLocation = (bestTree.radius > RobotType.SCOUT.bodyRadius) ? bestTree.location.add(bestTree.location.directionTo(closestEnemy.location), bestTree.radius - RobotType.SCOUT.bodyRadius) : bestTree.location;
					rc.setIndicatorDot(attackLocation, 0, 0, 255);
					Direction attackDirection = myLocation.directionTo(attackLocation);
					float attackDistance = myLocation.distanceTo(attackLocation);
					if(myLocation.directionTo(attackLocation) != null && rc.canMove(attackDirection,  attackDistance)) {
						rc.move(attackDirection,  attackDistance);
					}
						
				} 
				else {
					rc.setIndicatorDot(myLocation, 0, 255, 0);
					
					// Do safe moves
					MapLocation safeMoveLocation = null;
					for(int i = safeMoveLocations.length;i-->0;) {
						if(safeMoveLocations[i] == null) continue;
						safeMoveLocation = safeMoveLocations[i];
						break;
					}
					if(safeMoveLocation != null) {
						if(!Nav.pathTo(rc, safeMoveLocation, bullets)) {
							Nav.tryMove(rc, myLocation.directionTo(safeMoveLocation), bullets);
						}
					} else {
						Nav.tryMove(rc, myLocation.directionTo(closestEnemy.location).opposite(), bullets);
					}
				}
				
				MapLocation predictedEnemyLocation = Util.predictNextEnemyLocation(closestEnemy);
				
				
				if(rc.canFireSingleShot() && predictedEnemyLocation.distanceTo(closestEnemy.location) < 1.3f) {
					rc.fireSingleShot(myLocation.directionTo(closestEnemy.location));
				}
			} else {
				rc.setIndicatorDot(myLocation, 0, 0, 255);
				if(!exploreFlag) {
					if(!Nav.pathTo(rc, targetLocation, bullets)) {
						Nav.explore(rc, bullets);
					}
					if(myLocation.distanceTo(targetLocation) < 5f) exploreFlag = true;
				} else {
					Nav.explore(rc, bullets);
				}
			}
		} 
		else {
			rc.setIndicatorDot(myLocation, 0, 0, 255);
			if(!exploreFlag) {
				if(!Nav.pathTo(rc, targetLocation, bullets)) {
					Nav.explore(rc, bullets);
				}
				if(myLocation.distanceTo(targetLocation) < 5f) exploreFlag = true;
			} else {
				Nav.explore(rc, bullets);
			}
		}
		
	}
}
