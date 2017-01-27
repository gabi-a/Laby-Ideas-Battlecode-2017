package smoothmoves;
import battlecode.common.*;
import battlecode.schema.Action;

public class BotSoldier {
	static RobotController rc;
	
	static Team us = RobotPlayer.rc.getTeam();
	static Team them = us.opponent();
	static MapLocation enemyBase;
	
	static boolean trapped = false;
	static RobotInfo trackedEnemy;
	static float fireOffsetDegrees = 30f;
	static int shootCooldown = 10;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotSoldier.rc = rc;
		
		System.out.println("Bytecodes used: "+Clock.getBytecodeNum());
		
		if (enemyBase == null) {
			enemyBase = rc.getInitialArchonLocations(them)[0];
		}
		
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
		
		//System.out.println("Deciding how to move");
		
		if(bullets.length > 0 ) {
			MapLocation moveLocation = Nav.awayFromBullets(rc, myLocation, bullets);
			if(moveLocation != null) {
				//System.out.println("Dodging bullets");
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceTo(moveLocation);
				dodgeBullets = moveDirection != null && rc.canMove(moveDirection, moveStride);
			}
		}
		
		if(!dodgeBullets) {
			boolean protectGardener = false;
			//System.out.println("Deciding whether to protect gardener");
			RobotInfo[] enemiesAttackingUs = Comms.enemiesAttackingUs.arrayBots(rc);
			moveDirection = Nav.tryMove(rc, myLocation.directionTo(enemyBase), 5f, 24, bullets);
			for(int i = enemiesAttackingUs.length;i-->0;) {
				if(enemiesAttackingUs[i] != null) {
					MapLocation moveLocation = Nav.pathTo(rc, enemiesAttackingUs[i].location, bullets);
					if(moveLocation != null) {
						//System.out.println("Protect archon or gardener, enemy at: "+enemiesAttackingUs[i].location);
						moveDirection = myLocation.directionTo(moveLocation);
						moveStride = myLocation.distanceTo(moveLocation);
						protectGardener = true;
						break;
					}
				}
			}
			if(!(protectGardener && leaveCurrentEngagement(enemies))) {
				//System.out.println("Enemies?");
 				if(enemies.length > 0 ) {
					RobotInfo closestEnemy = enemies[0];
					if(closestEnemy.type == RobotType.LUMBERJACK) {
						//System.out.println("Lumberjacks.");
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
						//System.out.println("Move to enemy");
						MapLocation moveLocation = Nav.pathTo(rc, closestEnemy.location, bullets);
						if(moveLocation != null) {
							moveDirection = myLocation.directionTo(moveLocation);
							moveStride = myLocation.distanceTo(moveLocation);
						}
					}
					if(moveDirection != null) moveDirection = Nav.tryMove(rc, moveDirection, 5f, 24, bullets);
					
				}
				
				else {

					RobotInfo passiveEnemy = Util.getBestPassiveEnemy(rc);
					MapLocation moveLocation = null;
					if (passiveEnemy != null) {
						//System.out.println("Pathing to passive enemy");
						moveLocation = Nav.pathTo(rc, passiveEnemy.location, bullets);
					}

					if(moveLocation != null) {
						moveDirection = myLocation.directionTo(moveLocation);
						moveStride = myLocation.distanceTo(moveLocation);
					}
				}
			}
		}
		
		
		
		/************* Do Move ***********************************/
		
		/*
		 * All checks to see if this move is possible should already
		 * have taken place
		 */
		if(moveDirection != null && rc.canMove(moveDirection, moveStride))
			rc.move(moveDirection, moveStride);
		
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
				System.out.println("Cannot shoot "+enemies[i].ID);
			}
			
			if(enemyToAttack != null) {
				
				if(trackedEnemy == null) {
					trackedEnemy = enemyToAttack;
				}
				
				float lateralMovement;
				
				if(enemies[0].ID == trackedEnemy.ID) {
					float H = myLocation.distanceTo(enemies[0].location);
					float d = myLocation.distanceTo(trackedEnemy.location);
					float theta = myLocation.directionTo(enemyToAttack.location).radiansBetween(myLocation.directionTo(trackedEnemy.location));
					lateralMovement = Math.abs((float) (H * Math.sin(theta)));
				}
				else {
					// Always fire pentad on first lock
					lateralMovement = 0f;
				}

				rc.setIndicatorDot(trackedEnemy.location, 255, 0, 0);
				rc.setIndicatorDot(enemyToAttack.location, 255, 0, 0);

				// If not moving laterally relative to us, fire at will!
				if(lateralMovement < 0.5f || myLocation.distanceTo(enemyToAttack.location) < 4f || enemies.length > 1) {
					rc.setIndicatorDot(myLocation, 0, 255, 0);
					shootDirection  = myLocation.directionTo(enemyToAttack.location);
					if(enemyToAttack.type != RobotType.ARCHON && enemies.length == 1) {
						
						float distanceToEnemy = myLocation.distanceTo(enemyToAttack.location);
						
						float pentadHitRadius = distanceToEnemy * 0.7673f * 0.25f/*tan(37.5d)*/;
						rc.setIndicatorLine(enemyToAttack.location.add(shootDirection.rotateRightDegrees(90), pentadHitRadius), enemyToAttack.location.add(shootDirection.rotateLeftDegrees(90), pentadHitRadius), 255, 0, 0);
						float triadHitRadius = distanceToEnemy * 0.3640f * 0.25f/*tan(20d)*/;
						
						if(pentadHitRadius < enemyToAttack.getRadius()) {
							//System.out.println("Gabi thinks all 5 will hit");
							action = Action.FIRE_PENTAD;
						}
						
						else if(triadHitRadius < enemyToAttack.getRadius()) {
							//System.out.println("Gabi thinks all 3 will hit");
							action = Action.FIRE_TRIAD;
						}
						
						else {
							//System.out.println("Gabi thinks 1 might hit if we're lucky");
							action = Action.FIRE;
						}
						
						/*
						float d = myLocation.distanceTo(enemyToAttack.location);
						float rMe = rc.getType().bodyRadius;
						float rEn = enemyToAttack.type.bodyRadius;
						
						// Magic numbers ahead. These represent 1/sin(t), where
						// t is the spread angle
						if(d < 2f * rEn + rMe) {
							// All 5 will hit
						//	action = Action.FIRE_PENTAD;
							System.out.println("Adam thinks all 5 will hit");
						}
						else if(d < 2.924f * rEn + rMe) {
							// All 3 will hit
						//	action = Action.FIRE_TRIAD;
							System.out.println("Adam thinks all 3 will hit");
						}
						else if(d < 3.864f * rEn + rMe) {
							// 3 of 5 will hit
						//	action = Action.FIRE_PENTAD;
							System.out.println("Adam thinks 3 of 5 will hit");
						}
						else {
						//	action = Action.FIRE;
							System.out.println("Adam thinks 1 will hit");
						}
						*/
						
					}
					else if (enemyToAttack.type != RobotType.ARCHON && enemies.length > 1) {
						action = Action.FIRE_PENTAD;
						System.out.println("Multiple bots, fire pentad");
					}
					else {
						action = Action.FIRE;
						System.out.println("Only archon, fire single");
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

				trackedEnemy = enemyToAttack;
			}
		}
		
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
		case Action.FIRE_TRIAD:
			if(rc.canFireTriadShot()) {
				rc.fireTriadShot(shootDirection);
				shootCooldown = 10;
				break;
			}
		case Action.FIRE:
			if(rc.canFireSingleShot()) {
				rc.fireSingleShot(shootDirection);
				shootCooldown = 5;
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
