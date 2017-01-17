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
		
			
		/*
		 * 
		 * Scouts care about killing gardeners _only_
		 * 
		 * 
		 */

		BulletInfo[] bullets = rc.senseNearbyBullets();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, enemyTeam);
		
		MapLocation myLocation = rc.getLocation();
		MapLocation[] safeMoveLocations = Nav.getSafeMoveLocations(rc, bullets);
		TreeInfo[] trees = rc.senseNearbyTrees();
		
		if(enemies.length > 0) {
			rc.setIndicatorDot(myLocation, 255, 0, 0);
			

			RobotInfo closestEnemy = null;
			
			for(int i = 0; i<enemies.length; i++) {
				if(enemies[i].getType() != RobotType.ARCHON) {
					closestEnemy = enemies[i];
					break;
				}
			}
			
			if(closestEnemy != null) {
				rc.setIndicatorLine(myLocation,enemies[0].location, 100, 0, 100);
				
				if(myLocation.distanceTo(closestEnemy.location) <= 2.1f) {
					Nav.tryMove(rc, myLocation.directionTo(closestEnemy.location).opposite(), bullets);
				}
				
				else if(trees.length > 0) {
					TreeInfo bestTree = trees[0];
		
					// Find best tree
					float shortestDistanceToEnemy = 1000f;
					for(int i = trees.length;i-->0;) {
						if(rc.senseNearbyRobots(trees[i].location, trees[i].radius, rc.getTeam()).length != 0 || rc.senseNearbyRobots(trees[i].location, trees[i].radius, rc.getTeam().opponent()).length != 0) {
							continue;
						}
						float distanceToEnemy = trees[i].location.distanceTo(closestEnemy.location);
						if(distanceToEnemy < shortestDistanceToEnemy) {
							shortestDistanceToEnemy = distanceToEnemy;
							bestTree = trees[i];
						}
					}
					
					rc.setIndicatorDot(bestTree.location, 255, 0, 0);
					
					// If in the best tree already then move to attack pos if not there already
					boolean inAttackLocation = false;
					if(myLocation.distanceTo(bestTree.location) < RobotType.SCOUT.strideRadius) {
						MapLocation attackLocation = (bestTree.radius > RobotType.SCOUT.bodyRadius) ? bestTree.location.add(bestTree.location.directionTo(closestEnemy.location), bestTree.radius - RobotType.SCOUT.bodyRadius) : bestTree.location;
						rc.setIndicatorDot(attackLocation, 0, 0, 255);
						if(myLocation.directionTo(attackLocation) == null) {
							inAttackLocation = true; 
						} else if(rc.canMove(myLocation.directionTo(attackLocation),  1.00f * myLocation.distanceTo(attackLocation))){
							rc.move(myLocation.directionTo(attackLocation),  1.00f * myLocation.distanceTo(attackLocation));
						}
					}
				
					// If not in tree, go to it
					else {
						float shortestDistanceToTree = 1000f;
						MapLocation moveLocation = null;
						for(int i = safeMoveLocations.length;i-->0;) {
							if(safeMoveLocations[i] == null) continue;
							MapLocation safeMoveLocation = safeMoveLocations[i];
							float distanceToTree = safeMoveLocation.distanceTo(bestTree.location);
							if(distanceToTree < shortestDistanceToTree) {
								shortestDistanceToTree = distanceToTree;
								moveLocation = safeMoveLocation;
							}
						}
						if(moveLocation != null) {
							if(!Nav.pathTo(rc, moveLocation, bullets)) {
								Nav.tryMove(rc, myLocation.directionTo(moveLocation), bullets);
							}
						} else {
							Nav.tryMove(rc, myLocation.directionTo(closestEnemy.location).opposite(), bullets);
						}
					}
				} else {
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
		/*
		if(enemies.length > 0) {
			if(trees.length > 0) {
				
				for(int i = trees.length;i-->0;) {
					if(rc.senseNearbyRobots(trees[i].location, trees[i].radius, rc.getTeam()).length == 0) {
						closestTree = trees[i];
					}
				}
				
				if(closestTree == null) {
					closestTree = trees[0];
				}
				
				if(myLocation.distanceTo(closestTree.location) > closestTree.radius - 2f*RobotType.SCOUT.bodyRadius) {
					rc.setIndicatorLine(myLocation, closestTree.location, 100, 0, 0);
					
					
					MapLocation moveLocation = null;
					float shortestDistanceToTree = 1000;
					
					RobotInfo enemyToAttack = null;
					
					for(int i = safeMoveLocations.length;i-->0;) {
						for(int j = enemies.length; j-->0;) {
							if(safeMoveLocations[i] == null) continue;
							
							MapLocation safeMoveLocation = safeMoveLocations[i];
							float distanceToTree = safeMoveLocation.distanceTo(closestTree.location) + safeMoveLocation.distanceTo(enemies[j].location);
							if(distanceToTree < shortestDistanceToTree) {
								shortestDistanceToTree = distanceToTree;
								moveLocation = safeMoveLocation;
								enemyToAttack = enemies[j];
							}
						}
						
					}
					rc.setIndicatorLine(myLocation, moveLocation, 0, 100, 0);
					

					if(myLocation.distanceTo(closestTree.location) < RobotType.SCOUT.strideRadius) {
						MapLocation attackLocation = closestTree.location.add(closestTree.location.directionTo(enemyToAttack.location));
						Nav.tryPrecisionMove(rc, myLocation.directionTo(attackLocation), Math.min(RobotType.SCOUT.strideRadius, myLocation.distanceTo(attackLocation)));
					} else if (moveLocation != null) {
						Nav.tryPrecisionMove(rc, myLocation.directionTo(moveLocation), Math.min(RobotType.SCOUT.strideRadius, shortestDistanceToTree));
					}
				}
			}
			if(rc.canFireSingleShot()) {
				rc.fireSingleShot(myLocation.directionTo(enemies[0].location));
			}
			
		} else {
			if(!exploreFlag) {
				Nav.pathTo(rc, targetLocation);
				if(myLocation.distanceTo(targetLocation) < 5f) exploreFlag = true;
			} else {
				Nav.explore(rc);
			}
		}
		*/
		
	}
}
