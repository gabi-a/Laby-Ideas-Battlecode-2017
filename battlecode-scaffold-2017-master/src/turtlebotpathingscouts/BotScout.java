package turtlebotpathingscouts;
import battlecode.common.*;
import turtlebotpathing.Comms;

public class BotScout {
	static RobotController rc;
	static Team myTeam = RobotPlayer.rc.getTeam();
	static Team enemyTeam = myTeam.opponent();
	static MapLocation[] initialEnemyArchonLocations = RobotPlayer.rc.getInitialArchonLocations(enemyTeam);
	static MapLocation[] initialAllyArchonLocations = RobotPlayer.rc.getInitialArchonLocations(myTeam);
	
	static MapLocation attackTarget = initialEnemyArchonLocations[0];
	
    static boolean exploreFlag = false;
    
	public static void turn(RobotController rc) throws GameActionException {
		BotScout.rc = rc;
		Util.reportDeath(rc);
		
		MapLocation myLocation = rc.getLocation();
	
		RobotInfo[] nearbyEnemies = rc.senseNearbyRobots(-1, enemyTeam);
		RobotInfo[] nearbyAllies = rc.senseNearbyRobots(-1, myTeam);
		TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
		
		MapLocation commsAttackLocation = Comms.readAttackLocation(rc);
		if(commsAttackLocation != null) {
			attackTarget = commsAttackLocation;
		}
		/* Movement
		 *
		 * If there are enemies nearby, hide in a tree if possible
		 * otherwise keep out of sight range of the nearest enemy
		 * 
		 */
		boolean moved = false;
		boolean hiding = false;
		
		//moved = Nav.avoidBullets(rc, myLocation);

		RobotInfo closestEnemy = null;
		moved = Nav.avoidBullets(rc, myLocation);
		
		if(nearbyEnemies.length > 0) {
			for(int i = 0; i<nearbyEnemies.length; i++) {
				if(nearbyEnemies[i].getType() != RobotType.ARCHON) {
					closestEnemy = nearbyEnemies[i];
					break;
				}
			}
		}
		
		if(!moved && closestEnemy != null) {
			
			MapLocation enemyLocation = closestEnemy.getLocation();
			
			if(closestEnemy.getType() == RobotType.SCOUT) {
				moved = Nav.tryPrecisionMove(rc, myLocation.directionTo(enemyLocation).opposite(), RobotType.SCOUT.strideRadius) ;
			}
			
			if(!moved && myLocation.distanceTo(enemyLocation) > RobotType.SCOUT.sensorRadius - 2f) {
				moved = Nav.tryPrecisionMove(rc, myLocation.directionTo(enemyLocation), RobotType.SCOUT.strideRadius) ;
			}
			if(!moved && nearbyTrees.length > 0) {
				TreeInfo closestTreeToHideIn = nearbyTrees[0];
				float closestDist = 1000f;
				for(TreeInfo tree:nearbyTrees) {
					float dist = myLocation.distanceTo(tree.location) + tree.location.distanceTo(enemyLocation);
					if(dist < closestDist) {
						closestDist = dist;
						closestTreeToHideIn = tree;
					}
				}
				
				float inside = closestTreeToHideIn.getRadius() != 1f || Util.willGetHit(rc, myLocation) ? 0.5f : 1.01f;
				
				MapLocation desiredPosition = closestTreeToHideIn.getLocation().add(closestTreeToHideIn.getLocation().directionTo(enemyLocation), inside);

				rc.setIndicatorLine(closestEnemy.getLocation(), enemyLocation, 255, 0, 0);
				rc.setIndicatorDot(enemyLocation, 255, 0, 0);
				rc.setIndicatorDot(closestEnemy.getLocation(), 0, 255, 0);
				
				
				hiding = desiredPosition.distanceTo(myLocation) < 0.1f;
				if(myLocation.directionTo(desiredPosition) != null) {
					moved = Nav.tryPrecisionMove(rc, myLocation.directionTo(desiredPosition), myLocation.distanceTo(desiredPosition)) ;
				}
				/*
				if(closestTreeToHideIn != null) {
					float deltaRadius = closestTreeToHideIn.getLocation().distanceTo(myLocation)+RobotType.SCOUT.bodyRadius - closestTreeToHideIn.getRadius();
					if(deltaRadius > 0f) {
						moved = Nav.tryPrecisionMove(rc, myLocation.directionTo(closestTreeToHideIn.getLocation()), Math.min(deltaRadius, RobotType.SCOUT.strideRadius));
					} else {
						moved = Nav.tryPrecisionMove(rc, myLocation.directionTo(closestEnemy.getLocation()), Math.min(0.9f*deltaRadius, RobotType.SCOUT.strideRadius));
					}
				}
				*/
			}
		}
		if(!moved && !hiding) {
			if(!exploreFlag) {
				Nav.pathTo(rc, attackTarget);
				if(myLocation.distanceTo(attackTarget) < 5f) exploreFlag = true;
			} else {
				Nav.explore(rc);
			}
		}
		
		
		// Action
		if(nearbyEnemies.length > 0) {
			closestEnemy = nearbyEnemies[0];
			if(rc.canFireSingleShot()) {
				MapLocation predictedEnemyLocation = Util.predictNextEnemyLocation(closestEnemy, myLocation);
				rc.fireSingleShot(myLocation.directionTo(closestEnemy.location));
			}
			for (int i = 0; i < nearbyEnemies.length; i++) {
				if(nearbyEnemies[i].getType() == RobotType.GARDENER /*|| enemies[i].getType() == RobotType.ARCHON*/) {
					Comms.writeAttackEnemy(rc, nearbyEnemies[i].getLocation(), nearbyEnemies[i].getID());
				}
				
				if(nearbyEnemies[i].getID() == Comms.readAttackID(rc)) {
					if(nearbyEnemies[i].getHealth() < 20f) {
						Comms.clearAttackEnemy(rc);
					}
				}
			}
			
		}
	
	}
}
