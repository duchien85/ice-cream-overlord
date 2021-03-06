package com.dcoppetti.lordcream.entities;

import static com.dcoppetti.lordcream.IceCreamOverlordGame.PPM;

import com.dcoppetti.lordcream.handlers.CollisionHandler;

import net.dermetfan.gdx.physics.box2d.Box2DUtils;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

public class SlugEnemy extends Enemy {

	private Animation idleAnim;
	private Animation slideAnim;
	private float idleFPS = 4;
	private float slideFPS = 8;
	private float idleAnimTimer = 0;
	private float slideAnimTimer = 0;
	private float pushBackForce = 15f;
	
	private boolean inDeathZone = false;

	public SlugEnemy(World world, TextureRegion region, Vector2 position, float size) {
		super(world, region, position, size);
		this.recoverySpeed = 0.3f;
	}

	@Override
	public void collided(GameEntity b) {
		if(wasHit) return;
		if(b instanceof Cone) {
			Body coneBody = ((Cone) b).getBody();
			wasHit = true;
			Vector2 pushForce = new Vector2(coneBody.getLinearVelocity());
			pushForce.x *= pushBackForce;
			//this.body.setLinearVelocity(0, 0);
			this.body.applyForceToCenter(pushForce, true);
			tweenHitAnim();
		}
	}

	public void contactDeathZone() {
		inDeathZone = true;
		this.getAiBehavior().clear();
		tweenDeathAnim();
	}

	public void setAnimationRegions(Array<TextureRegion> idleRegions, Array<TextureRegion> slideRegions) {
    	idleAnim = new Animation(1f/idleFPS, idleRegions);
    	idleAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
    	setRegion(idleAnim.getKeyFrame(0));
    	
    	slideAnim = new Animation(1f/slideFPS, slideRegions);
    	slideAnim.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
    }

	@Override
	protected void createBody(World world, Vector2 position, float size) {
		float colliderWidth = size/1.5f;
        BodyDef bdef = new BodyDef();
        bdef.position.set(position);
        bdef.type = BodyDef.BodyType.DynamicBody;
        bdef.fixedRotation = true;
        CircleShape shape = new CircleShape();
        shape.setRadius(colliderWidth/2f);
        FixtureDef fdef = new FixtureDef();
        fdef.friction = 0.2f;
        fdef.restitution = 0.3f; // crucial to not have them stuck
		fdef.filter.categoryBits = CollisionHandler.CATEGORY_ENEMY;
		fdef.filter.maskBits = CollisionHandler.MASK_ENEMY;
		fdef.filter.groupIndex = CollisionHandler.GROUP_ENEMY;
        fdef.shape = shape;
        body = world.createBody(bdef);
        Fixture f = body.createFixture(fdef);
        shape.dispose();

		setUseOrigin(true);
		setAdjustSize(false);
		//setOrigin((getWidth()/2)+0.03f, (getHeight()/2)+0.05f);
		setOriginCenter();
		setX(-getWidth()/2 + Box2DUtils.width(body) / 2);
		setY(-getHeight()/2 + Box2DUtils.height(body) / 2);
		setScale(getScaleX()*size*2/PPM, getScaleY()*size*2/PPM);
		
		f.setUserData(this);
	}

	@Override
	protected void updateEnemy(float delta) {
		checkIfDead(delta);
		updateStates(delta);
		updateAnimations(delta);
	}

	private void updateStates(float delta) {
		if(body.getLinearVelocity().x > 0f || body.getLinearVelocity().x < 0f) {
			state = EnemyStates.Sliding;
			return;
		}
		state = EnemyStates.Idle;
	}

	private void checkIfDead(float delta) {
		if(!inDeathZone) return;
		if(deathTimer >= deathSpeed) {
			dead = true;
			return;
		}
		deathTimer += delta;
		this.body.setLinearVelocity(0, 0);
	}

	private void updateAnimations(float delta) {
		TextureRegion region = null;
		if(idleAnim != null) {
			switch (state) {
			case Idle:
				slideAnimTimer = 0;
				idleAnimTimer += delta;
				region = idleAnim.getKeyFrame(idleAnimTimer);
				setRegion(region);
				break;
			case Sliding:
				idleAnimTimer = 0;
				slideAnimTimer += delta;
				region = slideAnim.getKeyFrame(slideAnimTimer);
				setRegion(region);
				break;
//			case OnAir:
//				idleAnimTimer = 0;
//				slideAnimTimer = 0;
//				wallAnimTimer = 0;
//				jumpAnimTimer += delta;
//				region = jumpAnim.getKeyFrame(jumpAnimTimer);
//				setRegion(region);
//				break;
//			case OnWall:
//				idleAnimTimer = 0;
//				slideAnimTimer = 0;
//				jumpAnimTimer = 0;
//				wallAnimTimer += delta;
//				region = wallAnim.getKeyFrame(wallAnimTimer);
//				setRegion(region);
//				break;
			default:
				break;
			}
		}
//		if(facingLeft) {
//			setFlip(true, false);
//		} else {
//			setFlip(false, false);
//		}
	}

}
