package teststrategy;
import battlecode.common.*;
import battlecode.schema.Action;
import teststrategy.BotArchon.MapSize;

public class BotSoldier {
	static RobotController rc;
	
	static Team us = RobotPlayer.rc.getTeam();
	static Team them = us.opponent();
	static MapLocation enemyBase;
	
	static boolean trapped = false;
	static RobotInfo trackedEnemy;
	static float fireOffsetDegrees = 10f;
	//static int shootCooldown = 10;
	
	
	static int turnsSinceLastSeen = 0;
	static final int TURNS_SHOOT_UNSEEN = 20;
	//static final int TURNS_MOVE_UNSEEN = 5;
	
	//static MapLocation nextEnemyLocation;
	
	static MapSize mapSize;
	
	static boolean beenToEnemyArchon = false;
	
	public static void turn(RobotController rc) throws GameActionException {
		BotSoldier.rc = rc;
		
		//System.out.println("Bytecodes used: "+Clock.getBytecodeNum());
		
		if (enemyBase == null) {
			enemyBase = rc.getInitialArchonLocations(them)[0];
		}
		if(mapSize == null) {
			mapSize = MapSize.values()[Comms.mapSize.read(rc)];
		}
		
		Direction shootDirection = null;
		Direction moveDirection = null;
		float moveStride = RobotType.SOLDIER.strideRadius;
		
		BulletInfo[] bullets = rc.senseNearbyBullets();
		TreeInfo[] trees = rc.senseNearbyTrees();
		MapLocation myLocation = rc.getLocation();
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, them);
		//RobotInfo[] closeEnemies = rc.senseNearbyRobots(3f, them);
		
		Util.shakeIfAble(rc, trees);
		
		Util.updateMyPostion(rc);
		Util.reportEnemyBots(rc, enemies);
		/*
		if(bullets.length == 0) {
			Util.updateMyPostion(rc);
			Util.reportEnemyBots(rc, enemies);
		}
		*/
		
		/************* Determine where to move *******************/

		MapLocation moveLocation = null;
		
		if(bullets.length > 0) {
			moveLocation = Nav.awayFromBullets(rc, myLocation, bullets);
			if(moveLocation != null) {
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceTo(moveLocation);
			}
		}
		
		// If we haven't needed to dodge bullets, look for nearby enemies
		if(moveDirection == null && enemies.length > 0) {
			
			RobotInfo closestEnemy = enemies[0];
			if(closestEnemy.type == RobotType.LUMBERJACK) {
				//System.out.println("Lumberjacks.");
				if(myLocation.distanceTo(closestEnemy.location) < 3f) {
					moveDirection = closestEnemy.location.directionTo(myLocation);
				} else if(myLocation.distanceTo(closestEnemy.location) > 4f) {
					moveLocation = Nav.pathTo(rc, closestEnemy.location.add(closestEnemy.location.directionTo(myLocation), 3f));
					if(moveLocation != null) {
						moveDirection = myLocation.directionTo(moveLocation);
						moveStride = myLocation.distanceTo(moveLocation);
					}
				}
			} else if (!Util.goodToShootNotTrees(rc, myLocation, closestEnemy)){
				//System.out.println("Move to enemy");
				moveLocation = Nav.pathTo(rc, closestEnemy.location, closestEnemy.type.bodyRadius);
				if(moveLocation != null) {
					moveDirection = myLocation.directionTo(moveLocation);
					moveStride = myLocation.distanceTo(moveLocation);
				}
			}
			
		}
		
		// If theres no enemies nearby and we haven't had to dodge bullets, look for enemies in comms
		if(moveDirection == null) {
			
			RobotInfo[] enemiesAttackingGardenersOrArchons = Comms.enemiesAttackingGardenersOrArchons.arrayBots(rc);
			moveLocation = findClosestBotLocationToPathTo(enemiesAttackingGardenersOrArchons, bullets);
			if(moveLocation != null) {
				moveDirection = myLocation.directionTo(moveLocation);
				moveStride = myLocation.distanceTo(moveLocation);
			}
		}
		
		//  If we don't have any gardeners to protect, look for passive enemies
		if(moveDirection == null) {		
			RobotInfo passiveEnemy = Util.getBestPassiveEnemy(rc);
			if (passiveEnemy != null) {
				moveLocation = Nav.pathTo(rc, passiveEnemy.location);
				if(moveLocation != null) {
					moveDirection = myLocation.directionTo(moveLocation);
					moveStride = myLocation.distanceTo(moveLocation);
				}
			}
			
		}
		
		// If no passives, look for active enemies
		if(moveDirection == null) {
			RobotInfo[] enemiesSighted = Comms.enemiesSighted.arrayBots(rc);
			for(int i = enemiesSighted.length;i-->0;) {
				if(enemiesSighted[i] != null) {
					moveLocation = Nav.pathTo(rc, enemiesSighted[i].location);
					if(moveLocation != null) {
						moveDirection = myLocation.directionTo(moveLocation);
						moveStride = myLocation.distanceTo(moveLocation);
					}
				}
			}
		}
		
		// If no active enemies, go to initial archon location, unless already arrived there
		if(moveDirection == null) {
			
			if(!beenToEnemyArchon) {
				if(myLocation.distanceTo(enemyBase) < RobotType.SOLDIER.sensorRadius) {
					beenToEnemyArchon = true;
				}
				moveLocation = Nav.pathTo(rc, enemyBase);
				if(moveLocation != null) {
					moveDirection = myLocation.directionTo(moveLocation);
					moveStride = myLocation.distanceTo(moveLocation);
				}
			}
			
		}
		
		if(moveDirection == null) {
			
			if(trees.length > 0) {
				MapLocation goalLocation = myLocation;
				for(int i = trees.length; i --> 0;) {
					goalLocation = goalLocation.add(trees[i].location.directionTo(myLocation));
				}
				moveLocation = Nav.pathTo(rc, goalLocation);
				if(moveLocation != null) {
					moveDirection = myLocation.directionTo(moveLocation);
					moveStride = myLocation.distanceTo(moveLocation);
				}
			}
		}
		
		if(moveDirection == null) {
			moveDirection = Nav.explore(rc, bullets);
		}
		
		//if(moveDirection != null) moveDirection = Nav.tryMove(rc, moveDirection, 10f, 24, bullets);
		
		//System.out.println("Deciding how to move");
		
		/*
		boolean dodgeBullets = false;
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
			RobotInfo[] enemiesAttackingGardenersOrArchons = Comms.enemiesAttackingGardenersOrArchons.arrayBots(rc);
			moveDirection = Nav.tryMove(rc, myLocation.directionTo(enemyBase), 5f, 24, bullets);
			for(int i = enemiesAttackingGardenersOrArchons.length;i-->0;) {
				if(enemiesAttackingGardenersOrArchons[i] != null) {
					MapLocation moveLocation = Nav.pathTo(rc, enemiesAttackingGardenersOrArchons[i].location, bullets);
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
					MapLocation moveLocation = null;
					
					//if(nextEnemyLocation != null && turnsSinceLastSeen < TURNS_MOVE_UNSEEN) {
					//	moveLocation = Nav.pathTo(rc, nextEnemyLocation, bullets);
					//}
					
					System.out.println(mapSize);
					
					if(moveLocation == null) {
						switch(mapSize) {
						//case LARGE:
						//	break;
						default:
							RobotInfo passiveEnemy = Util.getBestPassiveEnemy(rc);
							if (passiveEnemy != null && myLocation.distanceTo(passiveEnemy.location) > 5f) {
								//System.out.println("Pathing to passive enemy");
								moveLocation = Nav.pathTo(rc, passiveEnemy.location, bullets);
							} else {
								RobotInfo[] enemiesSighted = Comms.enemiesSighted.arrayBots(rc);
								for(int i = enemiesSighted.length;i-->0;) {
									if(enemiesSighted[i] != null) {
										moveLocation = Nav.pathTo(rc, enemiesSighted[i].location, bullets);
									}
								}
							}
						}
					}
					if(moveLocation != null) {
						moveDirection = myLocation.directionTo(moveLocation);
						moveStride = myLocation.distanceTo(moveLocation);
					}
					
				}
			}
		}
		*/
		
		
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
			RobotInfo enemyArchonCache = null;
			
			// Find an enemy that we can safely attack
			for(int i = 0; i < enemies.length; i++) {
				if(enemies[i].type == RobotType.ARCHON) {
					enemyArchonCache = enemies[i];
				}
				else if(Util.goodToShootNotTrees(rc, myLocation, enemies[i])) {
					enemyToAttack = enemies[i];
					break;
				}
			}
			
			if(enemyToAttack == null && enemyArchonCache != null) {
				enemyToAttack = enemyArchonCache;
			}
			
			if(enemyToAttack != null) {
				
				//nextEnemyLocation = Util.predictNextEnemyLocation(enemyToAttack);
				
				turnsSinceLastSeen = 0;
				
				if(trackedEnemy == null) {
					trackedEnemy = enemyToAttack;
				}
				
				float lateralMovement;
				
				if(enemies[0].ID == trackedEnemy.ID) {
					float H = myLocation.distanceTo(enemies[0].location);
					//float d = myLocation.distanceTo(trackedEnemy.location);
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
						
						else /*if(triadHitRadius < enemyToAttack.getRadius())*/ {
							//System.out.println("Gabi thinks all 3 will hit");
							action = Action.FIRE_TRIAD;
						}
						/*
						else {
							//System.out.println("Gabi thinks 1 might hit if we're lucky");
							action = Action.FIRE;
						}
						*/
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
						//System.out.println("Multiple bots, fire pentad");
					}
					else {
						action = Action.FIRE;
						//System.out.println("Only archon, fire single");
					}
				}

				// Otherwise do a bit of cheeky herding
				else {
					rc.setIndicatorDot(myLocation, 0, 0, 255);
					fireOffsetDegrees = -fireOffsetDegrees;
					shootDirection  = myLocation.directionTo(enemyToAttack.location).rotateLeftDegrees(fireOffsetDegrees);
					//if(shootCooldown <= 0) {
						action = Action.FIRE;
					//}
				}
				
				if(!Util.goodToShootNotTrees(rc, myLocation, enemyToAttack)) {
					action = Action.DIE_EXCEPTION;
				}

				trackedEnemy = enemyToAttack;
			}
			
			/*
			 * Attempt to fire at an enemy we cannot see
			 */
			
			else if(turnsSinceLastSeen < TURNS_SHOOT_UNSEEN && trackedEnemy != null){
				turnsSinceLastSeen ++;
				
				if( Util.goodToShootNotTrees(rc, myLocation, trackedEnemy) ) {
					action = Action.FIRE_TRIAD;
					shootDirection = myLocation.directionTo(trackedEnemy.location);
				}
				
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
				break;
			}
		case Action.FIRE_TRIAD:
			if(rc.canFireTriadShot()) {
				rc.fireTriadShot(shootDirection);
				break;
			}
		case Action.FIRE:
			if(rc.canFireSingleShot()) {
				rc.fireSingleShot(shootDirection);
				//shootCooldown = 2;
				break;
			}
		default:
			//shootCooldown--;
			break;
		}
	}
	
	static boolean leaveCurrentEngagement(RobotInfo[] enemies) {
		if (enemies.length == 0) return true;
		int[] count = Util.countBots(enemies);
		if(count[RobotType.GARDENER.ordinal()] > 0) {
			return false;
		}
		if(count[RobotType.SOLDIER.ordinal()] >= 1) {
			return false;
		}
		return true;
	}
	
	static MapLocation findClosestBotLocationToPathTo(RobotInfo[] bots, BulletInfo[] bullets) throws GameActionException {
		for(int i = bots.length;i-->0;) {
			if(bots[i] != null) {
				MapLocation moveLocation = Nav.pathTo(rc, bots[i].location);
				if(moveLocation != null) {
					return moveLocation;
				}
			}
		}
		return null;
	}
	
}
