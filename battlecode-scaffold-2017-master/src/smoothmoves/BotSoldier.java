package smoothmoves;
import battlecode.common.*;
import battlecode.schema.Action;

public class BotSoldier {
	static RobotController rc;
	
	static Team us = RobotPlayer.rc.getTeam();
	static Team them = us.opponent();
	
	static boolean trapped = false;
	static RobotInfo trackedEnemy;
	static float fireOffsetDegrees = 30f;
	static int shootCooldown = 10;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotSoldier.rc = rc;
		
		Direction shootDirection = null;
		Direction moveDirection = null;
		float moveStride = RobotType.SOLDIER.strideRadius;
		
		BulletInfo[] bullets = rc.senseNearbyBullets();
		TreeInfo[] trees = rc.senseNearbyTrees();
		MapLocation myLocation = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
		RobotInfo[] closeEnemies = rc.senseNearbyRobots(3f, them);
		
		Util.reportEnemyBots(rc, enemies);
		
		/************* Determine where to move *******************/

		boolean dodgeBullets = false;
		
		if(bullets.length > 0 && closeEnemies.length > 0) {
			MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets, trees);	
			if(moveLocation != null) {
				System.out.println("Dodging bullets");
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceTo(moveLocation);
				dodgeBullets = moveDirection != null && rc.canMove(moveDirection, moveStride);
			}
		}
		
		if(!dodgeBullets) {
			boolean protectGardener = false;
			RobotInfo[] enemiesAttackingUs = Comms.enemiesAttackingUs.arrayBots(rc);
			moveDirection = Nav.tryMove(rc, myLocation.directionTo(rc.getInitialArchonLocations(them)[0]), 5f, 24, bullets);
			for(int i = enemiesAttackingUs.length;i-->0;) {
				if(enemiesAttackingUs[i] != null) {
					MapLocation moveLocation = Nav.pathTo(rc, enemiesAttackingUs[i].location, bullets);
					if(moveLocation != null) {
						moveDirection = myLocation.directionTo(moveLocation);
						moveStride = myLocation.distanceTo(moveLocation);
						protectGardener = true;
						break;
					}
				}
			}
			if(!(protectGardener && leaveCurrentEngagement(enemies))) {
				if(enemies.length > 0 ) {
					RobotInfo closestEnemy = enemies[0];
					if(closestEnemy.type == RobotType.LUMBERJACK) {
						if(myLocation.distanceTo(closestEnemy.location) < 3f) {
							moveDirection = closestEnemy.location.directionTo(myLocation);
						} else if(myLocation.distanceTo(closestEnemy.location) > 4f) {
							MapLocation moveLocation = Nav.pathTo(rc, closestEnemy.location.add(closestEnemy.location.directionTo(myLocation), 3f), bullets);
							if(moveLocation != null) {
								moveDirection = myLocation.directionTo(moveLocation);
								moveStride = myLocation.distanceTo(moveLocation);
							}
						}
					} else if (!Util.goodToShootNotTrees(rc, myLocation, closestEnemy)){
						MapLocation moveLocation = Nav.pathTo(rc, closestEnemy.location, bullets);
						if(moveLocation != null) {
							moveDirection = myLocation.directionTo(moveLocation);
							moveStride = myLocation.distanceTo(moveLocation);
						}
					}
					if(moveDirection != null) moveDirection = Nav.tryMove(rc, moveDirection, 5f, 24, bullets);
					
				}
				
				else {
					boolean foundGardener = false;
					RobotInfo[] enemyGardeners = Comms.enemyGardenersArray.arrayBots(rc);
					System.out.println("Finding gardeners");
					moveDirection = Nav.tryMove(rc, myLocation.directionTo(rc.getInitialArchonLocations(them)[0]), 5f, 24, bullets);
					for(int i = enemyGardeners.length;i-->0;) {
						if(enemyGardeners[i] != null) {
							MapLocation moveLocation = Nav.pathTo(rc, enemyGardeners[i].location, bullets);
							System.out.println("Trying to go to"+enemyGardeners[i].location);
							if(moveLocation != null) {
								moveDirection = myLocation.directionTo(moveLocation);
								moveStride = myLocation.distanceTo(moveLocation);
								foundGardener = true;
								break;
							}
						}
					}
					
					if(!foundGardener) {
						RobotInfo[] enemyArchons = Comms.enemyArchonsArray.arrayBots(rc);
						for(int i = enemyArchons.length;i-->0;) {
							if(enemyArchons[i] != null) {
								MapLocation moveLocation = Nav.pathTo(rc, enemyArchons[i].location, bullets);
								if(moveLocation != null) {
									moveDirection = myLocation.directionTo(moveLocation);
									moveStride = myLocation.distanceTo(moveLocation);
									break;
								}
							}
						}
					}
				}
			}
		}
		
		/*
		if(enemies.length > 0 && (moveDirection == null || moveDirection != null && !rc.canMove(moveDirection,moveStride))) {
			RobotInfo closestEnemy = enemies[0];
			if(closestEnemy.type == RobotType.LUMBERJACK) {
				if(myLocation.distanceTo(closestEnemy.location) < 3f) {
					moveDirection = closestEnemy.location.directionTo(myLocation);
				} else if(myLocation.distanceTo(closestEnemy.location) > 4f) {
					MapLocation moveLocation = Nav.pathTo(rc, closestEnemy.location.add(closestEnemy.location.directionTo(myLocation), 3f), bullets);
					if(moveLocation != null) {
						moveDirection = myLocation.directionTo(moveLocation);
						moveStride = myLocation.distanceTo(moveLocation);
					}
				}
			} else if (!Util.goodToShootNotTrees(rc, myLocation, closestEnemy)){
				MapLocation moveLocation = Nav.pathTo(rc, closestEnemy.location, bullets);
				if(moveLocation != null) {
					moveDirection = myLocation.directionTo(moveLocation);
					moveStride = myLocation.distanceTo(moveLocation);
				}
			}
			if(moveDirection != null) moveDirection = Nav.tryMove(rc, moveDirection, 5f, 24, bullets);
			
		} else {
			boolean protectGardener = false;
			RobotInfo[] enemiesAttackingUs = Comms.enemiesAttackingUs.arrayBots(rc);
			moveDirection = Nav.tryMove(rc, myLocation.directionTo(rc.getInitialArchonLocations(them)[0]), 5f, 24, bullets);
			for(int i = enemiesAttackingUs.length;i-->0;) {
				if(enemiesAttackingUs[i] != null) {
					MapLocation moveLocation = Nav.pathTo(rc, enemiesAttackingUs[i].location, bullets);
					if(moveLocation != null) {
						moveDirection = myLocation.directionTo(moveLocation);
						moveStride = myLocation.distanceTo(moveLocation);
						protectGardener = true;
						break;
					}
				}
			}
			if(!protectGardener) {
				boolean foundGardener = false;
				RobotInfo[] enemyGardeners = Comms.enemyGardenersArray.arrayBots(rc);
				moveDirection = Nav.tryMove(rc, myLocation.directionTo(rc.getInitialArchonLocations(them)[0]), 5f, 24, bullets);
				for(int i = enemyGardeners.length;i-->0;) {
					if(enemyGardeners[i] != null) {
						MapLocation moveLocation = Nav.pathTo(rc, enemyGardeners[i].location, bullets);
						if(moveLocation != null) {
							moveDirection = myLocation.directionTo(moveLocation);
							moveStride = myLocation.distanceTo(moveLocation);
							foundGardener = true;
							break;
						}
					}
				}
				
				if(!foundGardener) {
					RobotInfo[] enemyArchons = Comms.enemyArchonsArray.arrayBots(rc);
					for(int i = enemyArchons.length;i-->0;) {
						if(enemyArchons[i] != null) {
							MapLocation moveLocation = Nav.pathTo(rc, enemyArchons[i].location, bullets);
							if(moveLocation != null) {
								moveDirection = myLocation.directionTo(moveLocation);
								moveStride = myLocation.distanceTo(moveLocation);
								break;
							}
						}
					}
				}
			}
			
		}
		*/
		
		/************* Determine what action to take *************/
		byte action = Action.DIE_EXCEPTION;
		
		if(enemies.length > 0) {
			
			RobotInfo enemyToAttack = null;
			
			// Find an enemy that we can safely attack
			for(int i = 0; i < enemies.length; i++) {
				if(Util.goodToShoot(rc, myLocation, enemies[i])) {
					enemyToAttack = enemies[i];
					break;
				}
			}
			
			if(enemyToAttack != null) {
				
				if(trackedEnemy == null) {
					trackedEnemy = enemyToAttack;
				}
				
				// We have a lock!
				if(enemies[0].ID == trackedEnemy.ID) {
					
					float H = myLocation.distanceTo(enemies[0].location);
					float d = myLocation.distanceTo(trackedEnemy.location);
					float theta = myLocation.directionTo(enemyToAttack.location).radiansBetween(myLocation.directionTo(trackedEnemy.location));
					float lateralMovement = Math.abs((float) (H * Math.sin(theta)));
					
					rc.setIndicatorDot(trackedEnemy.location, 255, 0, 0);
					rc.setIndicatorDot(enemyToAttack.location, 255, 0, 0);
					
					// If not moving laterally relative to us, fire at will!
					if(lateralMovement < 0.5f || myLocation.distanceTo(enemyToAttack.location) < 4f || enemies.length > 1) {
						rc.setIndicatorDot(myLocation, 0, 255, 0);
						shootDirection  = myLocation.directionTo(enemyToAttack.location);
						if(enemyToAttack.type == RobotType.ARCHON) {
							action = Action.FIRE;
						} else {
							action = Action.FIRE_PENTAD;
						}
					}
					
					// Otherwise do a bit of cheeky herding
					else {
						rc.setIndicatorDot(myLocation, 0, 0, 255);
						fireOffsetDegrees = -fireOffsetDegrees;
						shootDirection  = myLocation.directionTo(enemyToAttack.location).rotateLeftDegrees(fireOffsetDegrees);
						if(shootCooldown <= 0) {
							action = Action.FIRE;
						}
					}
					
				}
				
				else {
					
				}
				
				trackedEnemy = enemyToAttack;
			}
		}
		
		/************* Do Move ***********************************/
		
		/*
		 * All checks to see if this move is possible should already
		 * have taken place
		 */
		if(moveDirection != null && rc.canMove(moveDirection, moveStride))
			rc.move(moveDirection, moveStride);
		
		/************* Do action *********************************/
		
		/*
		 * All checks to see if this action is possible should already
		 * have taken place
		 */
		
		switch(action) {
		case Action.FIRE_PENTAD:
			if(rc.canFirePentadShot()) {
				rc.firePentadShot(shootDirection);
				shootCooldown = 15;
				break;
			}
		case Action.FIRE:
			if(rc.canFireSingleShot()) {
				rc.fireSingleShot(shootDirection);
				shootCooldown = 10;
				break;
			}
		default:
			shootCooldown--;
			break;
		}
	}
	
	static boolean leaveCurrentEngagement(RobotInfo[] enemies) {
		if (enemies.length == 0) return true;
		int[] count = Util.countBots(enemies);
		if(count[RobotType.GARDENER.ordinal()] > 0) {
			return false;
		}
		if(count[RobotType.SOLDIER.ordinal()] >= 3) {
			return false;
		}
		return true;
	}
	
}
