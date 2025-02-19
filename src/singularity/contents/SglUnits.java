package singularity.contents;

import arc.Core;
import arc.Events;
import arc.audio.Sound;
import arc.func.Func2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Vec2;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.layout.Collapser;
import arc.scene.ui.layout.Table;
import arc.struct.Seq;
import arc.util.Interval;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.entities.Damage;
import mindustry.entities.Effect;
import mindustry.entities.UnitSorts;
import mindustry.entities.Units;
import mindustry.entities.bullet.*;
import mindustry.entities.bullet.LightningBulletType;
import mindustry.entities.effect.MultiEffect;
import mindustry.entities.part.HaloPart;
import mindustry.entities.part.RegionPart;
import mindustry.entities.pattern.ShootBarrel;
import mindustry.entities.pattern.ShootPattern;
import mindustry.entities.units.UnitController;
import mindustry.entities.units.WeaponMount;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.type.UnitType;
import mindustry.type.Weapon;
import mindustry.type.weapons.PointDefenseWeapon;
import mindustry.ui.Styles;
import mindustry.world.Block;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.draw.DrawBlock;
import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawMulti;
import mindustry.world.meta.BlockFlag;
import singularity.Sgl;
import singularity.graphic.MathRenderer;
import singularity.graphic.SglDraw;
import singularity.graphic.SglDrawConst;
import singularity.ui.StatUtils;
import singularity.util.MathTransform;
import singularity.world.SglFx;
import singularity.world.SglUnitSorts;
import singularity.world.blocks.product.HoveringUnitFactory;
import singularity.world.blocks.product.SglUnitFactory;
import singularity.world.blocks.turrets.*;
import singularity.world.blocks.turrets.EmpBulletType;
import singularity.world.draw.part.CustomPart;
import singularity.world.particles.SglParticleModels;
import singularity.world.unit.*;
import universecore.world.lightnings.LightningContainer;
import universecore.world.lightnings.generator.CircleGenerator;
import universecore.world.lightnings.generator.RandomGenerator;
import universecore.world.lightnings.generator.ShrinkGenerator;
import universecore.world.lightnings.generator.VectorLightningGenerator;
import universecore.world.particles.models.RandDeflectParticle;

import java.util.Iterator;

import static mindustry.Vars.*;

public class SglUnits implements ContentList{
  private static final Rand rand = Mathf.rand;

  public static final String EPHEMERAS = "ephemeras";
  public static final String TIMER = "timer";
  public static final String STATUS = "status";
  public static final String PHASE = "phase";
  public static final String SHOOTERS = "shooters";

  /**棱镜*/
  public static UnitType prism,
  /**光弧*/
  lightarc,
  /**黎明*/
  dawn;

  /**辉夜*/
  @UnitEntityType(UnitEntity.class)
  public static UnitType kaguya,
  /**虚宿*/
  emptiness;

  /**晨星*/
  @UnitEntityType(AirSeaAmphibiousUnit.AirSeaUnit.class)
  public static UnitType mornstar,
  /**极光*/
  aurora;

  @UnitEntityType(SglUnitEntity.class)
  public static UnitType unstable_energy_body;

  /**机械构造坞*/
  public static Block cstr_1,
  cstr_2,
  cstr_3;

  @Override
  public void load() {
    UnitTypeRegister.registerAll();

    mornstar = new AirSeaAmphibiousUnit("mornstar"){{
      requirements(
          Items.silicon, 420,
          Items.phaseFabric, 360,
          Items.surgeAlloy, 320,
          SglItems.aluminium, 380,
          SglItems.aerogel, 320,
          SglItems.crystal_FEX_power, 220,
          SglItems.strengthening_alloy, 280,
          SglItems.iridium, 200,
          SglItems.matrix_alloy, 220
      );

      speed = 0.84f;
      accel = 0.065f;
      drag = 0.03f;
      rotateSpeed = 1.8f;
      riseSpeed = 0.02f;
      boostMultiplier = 1.25f;
      faceTarget = true;
      health = 42500;
      lowAltitude = true;
      hitSize = 64;
      targetFlags = BlockFlag.allLogic;

      engineOffset = 0;
      engineSize = 0;

      setEnginesMirror(
          new UnitEngine(){{
            x = 16f;
            y = -44f;
            radius = 10;
            rotation = 45;
          }},
          new UnitEngine(){{
            x = 24f;
            y = -52f;
            radius = 6;
            rotation = 45;
          }},
          new UnitEngine(){{
            x = 34f;
            y = -52f;
            radius = 8;
            rotation = -45;
          }}
      );

      weapons.addAll(
          new SglWeapon(Sgl.modName + "-mornstar_cannon"){{
            recoil = 0;
            recoilTime = 120;
            cooldownTime = 120;

            reload = 90;
            rotate = true;
            mirror = false;

            rotateSpeed = 2.5f;

            layerOffset = 1;

            x = 0;
            y = 4;
            shootY = 25;

            shoot = new ShootBarrel(){{
              barrels = new float[]{
                  5.75f, 0, 0,
                  -5.75f, 0, 0
              };
              shots = 2;
              shotDelay = 0;
            }};

            bullet = new EmpMultiTrailBulletType(){
              {
                hitColor = trailColor = SglDrawConst.matrixNet;
                trailLength = 22;
                trailWidth = 2f;
                trailEffect = new MultiEffect(
                    SglFx.trailLineLong,
                    SglFx.railShootRecoil,
                    SglFx.movingCrystalFrag
                );
                trailRotation = true;
                trailChance = 1;

                shootEffect = new MultiEffect(
                    SglFx.shootRecoilWave,
                    SglFx.shootRail
                );
                hitEffect = SglFx.lightConeHit;
                despawnEffect = new MultiEffect(
                    SglFx.impactWaveSmall,
                    SglFx.spreadSparkLarge,
                    SglFx.diamondSparkLarge
                );
                smokeEffect = Fx.shootSmokeSmite;

                shootSound = Sounds.shootSmite;

                damage = 500;
                empDamage = 100;
                lifetime = 45;
                speed = 8;
                pierceCap = 4;
                hittable = false;

                fragBullet = new EdgeFragBullet();
                fragOnHit = true;
                fragBullets = 3;
                fragRandomSpread = 115;
              }

              @Override
              public void draw(Bullet b) {
                super.draw(b);
                Draw.color(SglDrawConst.matrixNet);
                SglDraw.gapTri(b.x, b.y, 18, 28, 16, b.rotation());
                SglDraw.drawTransform(b.x, b.y, -6, 0, b.rotation(), (x, y, r) -> {
                  SglDraw.drawDiamond(x, y, 16, 8, r);
                });
              }

              @Override
              public void hit(Bullet b) {
                super.hit(b);
                b.damage -= 125;
              }

              @Override
              public void init(Bullet b) {
                super.init(b);
                b.data = Pools.obtain(TrailMoveLightning.class, TrailMoveLightning::new);
              }

              @Override
              public void despawned(Bullet b) {
                super.despawned(b);
                if (b.data instanceof TrailMoveLightning l) Pools.free(l);
              }

              @Override
              public void updateTrail(Bullet b) {
                if(!headless && trailLength > 0){
                  if(b.trail == null){
                    b.trail = new Trail(trailLength);
                  }
                  b.trail.length = trailLength;

                  if (!(b.data instanceof TrailMoveLightning m)) return;
                  m.update();
                  SglDraw.drawTransform(b.x, b.y, 0, m.off, b.rotation(), (x, y, r) -> b.trail.update(x, y));
                }
              }
            };

            parts.addAll(
                new RegionPart("_blade"){{
                  under = true;
                  progress = PartProgress.recoil;
                  moveY = -3;
                  heatColor = Pal.turretHeat;
                  heatProgress = PartProgress.heat;
                }},
                new RegionPart("_body"){{
                  under = true;
                }}
            );
          }},
          new SglWeapon(Sgl.modName + "-mornstar_turret"){{
            x = 26;
            y = -28;
            shootY = 0;
            recoil = 5;
            recoilTime = 60;
            reload = 4;

            rotate = true;
            rotateSpeed = 8;

            customDisplay = (b, t) -> {
              t.row();
              t.add(Core.bundle.get("infos.damageAttenuationWithDist")).color(Pal.accent);
            };

            bullet = new BulletType(){
              {
                speed = 12;
                lifetime = 30;
                damage = 180;

                pierceCap = 1;

                despawnHit = true;

                shootSound = Sounds.bolt;

                hitColor = trailColor = SglDrawConst.matrixNet;
                hitEffect = new MultiEffect(
                    SglFx.spreadDiamondSmall,
                    SglFx.movingCrystalFrag
                );
                smokeEffect = Fx.colorSpark;
                shootEffect = new MultiEffect(
                    SglFx.railShootRecoil,
                    SglFx.crossLightMini
                );
                trailWidth = 3;
                trailLength = 8;

                trailEffect = SglFx.glowParticle;
                trailChance = 0.12f;
              }

              @Override
              public void draw(Bullet b) {
                super.draw(b);
                Draw.color(SglDrawConst.matrixNet);
                Tmp.v1.set(1, 0).setAngle(b.rotation());
                SglDraw.gapTri(b.x + Tmp.v1.x*3*b.fout(), b.y + Tmp.v1.y*3*b.fout(), 15, 22, 14, b.rotation());
                SglDraw.gapTri(b.x - Tmp.v1.x*3*b.fout(), b.y - Tmp.v1.y*3*b.fout(), 12, 18, 10, b.rotation());
                SglDraw.gapTri(b.x - Tmp.v1.x*5*b.fout(), b.y - Tmp.v1.y*5*b.fout(), 9, 12, 8, b.rotation());
                SglDraw.drawDiamond(b.x, b.y, 18, 6, b.rotation());
              }

              @Override
              public void update(Bullet b) {
                super.update(b);
                b.damage = (b.type.damage + b.type.damage*b.fout())*0.5f;
              }
            };
          }},
          new RelatedWeapon(Sgl.modName + "-lightedge"){
            {
              useAlternative = isFlying;
              mirror = false;
              x = 0;
              y = -25;
              shootCone = 180;

              recoil = 0;
              recoilTime = 1;

              alternate = false;

              linearWarmup = false;
              shootWarmupSpeed = 0.025f;
              minWarmup = 0.9f;

              reload = 60;

              bullet = new ContinuousBulletType(){
                {
                  speed = 0;
                  lifetime = 180;
                  length = 420;

                  damage = 80;
                  damageInterval = 5;

                  hitEffect = SglFx.railShootRecoil;

                  trailColor = hitColor = SglDrawConst.matrixNet;
                  trailEffect = SglFx.movingCrystalFrag;
                  trailInterval = 4;

                  shootEffect = new MultiEffect(
                      SglFx.shootCrossLight,
                      SglFx.explodeImpWaveSmall
                  );
                }

                @Override
                public void init(Bullet b) {
                  super.init(b);

                  Sounds.laserblast.at(b.x, b.y, 1.25f);

                  if (b.owner instanceof Unit u){
                    b.rotation(b.angleTo(u.aimX, u.aimY));
                  }
                }

                @Override
                public void update(Bullet b) {
                  super.update(b);

                  Effect.shake(4, 4, b.x, b.y);
                  updateTrailEffects(b);
                  if (b.owner instanceof Unit u){
                    b.rotation(Angles.moveToward(b.rotation(), b.angleTo(u.aimX, u.aimY), 8*Time.delta));
                  }
                }

                @Override
                public void applyDamage(Bullet b) {
                  Damage.collideLaser(b, length, largeHit, laserAbsorb, pierceCap);
                }

                @Override
                public void draw(Bullet b) {
                  super.draw(b);

                  float realLen = b.fdata;
                  float lerp = Mathf.clamp(b.time/40);
                  float out = Mathf.clamp((b.type.lifetime - b.time)/30);
                  lerp*=out;

                  lerp = 1 - Mathf.pow(1 - lerp, 3);

                  Draw.color(SglDrawConst.matrixNet);
                  Drawf.tri(b.x, b.y, 12*lerp, realLen, b.rotation());
                  Draw.color(Color.black);
                  Drawf.tri(b.x, b.y, 5*lerp, realLen*0.8f, b.rotation());
                  Draw.color(SglDrawConst.matrixNet);
                  Fill.circle(b.x, b.y, 4*out + 3*lerp);
                  SglDraw.drawDiamond(b.x, b.y, 24, 10*lerp, Time.time);
                  SglDraw.drawDiamond(b.x, b.y, 28, 12*lerp, -Time.time*1.2f);
                  Lines.stroke(0.8f);
                  Lines.circle(b.x, b.y, 6);
                  Draw.color(Color.black);
                  Fill.circle(b.x, b.y, 4f*lerp);

                  Draw.color(SglDrawConst.matrixNet);
                  SglDraw.gapTri(b.x + Angles.trnsx(Time.time, 8, 0), b.y + Angles.trnsy(Time.time, 8, 0), 8*lerp, 12 + 14*lerp, 8, Time.time);
                  SglDraw.gapTri(b.x + Angles.trnsx(Time.time + 180, 8, 0), b.y + Angles.trnsy(Time.time + 180, 8, 0), 8*lerp, 12 + 14*lerp, 8, Time.time + 180);
                  SglDraw.drawDiamond(b.x + Angles.trnsx(-Time.time*1.2f, 12, 0), b.y + Angles.trnsy(-Time.time*1.2f, 12, 0), 16, 5*lerp, -Time.time*1.2f);
                  SglDraw.drawDiamond(b.x + Angles.trnsx(-Time.time*1.2f + 180, 12, 0), b.y + Angles.trnsy(-Time.time*1.2f + 180, 12, 0), 16, 5*lerp, -Time.time*1.2f + 180);

                  float out2 = Mathf.pow(1 - out, 3);
                  Tmp.v1.set(35 + 30*out2, 0).setAngle(b.rotation());
                  Tmp.v2.set(Tmp.v1).setLength(8 + 10*lerp).rotate90(1);

                  float len = 100 + out2*80;
                  float an = Mathf.atan2(len/2, 8*lerp)*Mathf.radDeg;

                  Drawf.tri(b.x + Tmp.v1.x + Tmp.v2.x, b.y + Tmp.v1.y + Tmp.v2.y, len, 8*lerp, b.rotation() - 90 - an);
                  Drawf.tri(b.x + Tmp.v1.x - Tmp.v2.x, b.y + Tmp.v1.y - Tmp.v2.y, len, 8*lerp, b.rotation() + 90 + an);
                }
              };

              alternativeBullet = new BulletType(){
                {
                  splashDamage = 380;
                  splashDamageRadius = 32;

                  speed = 8;
                  lifetime = 360;
                  rangeOverride = 360;
                  homingDelay = 60;

                  homingPower = 0.03f;
                  homingRange = 360;

                  despawnShake = 6;

                  collides = false;
                  absorbable = false;
                  hittable = false;

                  keepVelocity = false;

                  trailColor = hitColor = SglDrawConst.matrixNet;
                  trailLength = 34;
                  trailWidth = 4.5f;

                  despawnEffect = new MultiEffect(
                      SglFx.explodeImpWave,
                      SglFx.crossLightSmall,
                      SglFx.diamondSparkLarge
                  );

                  trailEffect = SglFx.movingCrystalFrag;
                  trailInterval = 4;

                  fragBullet = new BulletType(){
                    {
                      damage = 60;
                      splashDamage = 80;
                      splashDamageRadius = 24;
                      speed = 4;
                      hitSize = 3;
                      lifetime = 120;
                      despawnHit = true;
                      hitEffect = SglFx.diamondSpark;
                      hitColor = SglDrawConst.matrixNet;

                      collidesTiles = false;

                      homingRange = 240;
                      homingPower = 0.035f;

                      trailColor = SglDrawConst.matrixNet;
                      trailLength = 25;
                      trailWidth = 3f;
                      trailEffect = SglFx.movingCrystalFrag;
                      trailInterval = 5;
                    }

                    @Override
                    public void draw(Bullet b) {
                      drawTrail(b);
                      Draw.color(hitColor);
                      Fill.circle(b.x, b.y, 4);
                      Draw.color(Color.black);
                      Fill.circle(b.x, b.y, 2.5f);
                    }

                    @Override
                    public void updateHoming(Bullet b) {
                      Posc target = Units.closestTarget(b.team, b.x, b.y, homingRange,
                            e -> e != null && e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                            t -> t != null && collidesGround && !b.hasCollided(t.id));

                      if (target == null){
                        b.vel.lerpDelta(Vec2.ZERO, homingPower);
                      }
                      else{
                        b.vel.lerpDelta(Tmp.v1.set(target.x() - b.x, target.y() - b.y).setLength(speed*0.5f), homingPower);
                      }
                    }
                  };
                  fragBullets = 5;

                  intervalBullet = new LightLaserBulletType(){
                    {
                      damage = 150;
                      empDamage = 20;
                    }

                    @Override
                    public void init(Bullet b, LightningContainer c) {
                      Teamc target = Units.closestTarget(b.team, b.x, b.y, range,
                          e -> e != null && e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                          t -> t != null && collidesGround && !b.hasCollided(t.id));

                      if (target != null) {
                        b.rotation(b.angleTo(target));
                      }

                      super.init(b, c);
                    }
                  };
                  bulletInterval = 15f;
                }

                @Override
                public void draw(Bullet b) {
                  super.draw(b);
                  Draw.color(SglDrawConst.matrixNet);

                  float lerp = Mathf.clamp(b.time/homingDelay);
                  lerp = 1 - Mathf.pow(1 - lerp, 2);

                  Fill.circle(b.x, b.y, 4 + 2*lerp);
                  SglDraw.drawDiamond(b.x, b.y, 22, 10*lerp, Time.time);
                  Lines.stroke(0.8f);
                  Lines.circle(b.x, b.y, 6);
                  Draw.color(Color.black);
                  Fill.circle(b.x, b.y, 3.75f*lerp);
                  Draw.color(SglDrawConst.matrixNet);

                  rand.setSeed(b.id);
                  for (int i = 0; i < 7; i++) {
                    float w = rand.random(1f, 2.5f)*(rand.nextFloat() > 0.5? 1: -1);
                    float f = rand.random(360f);
                    float r = rand.random(12f, 28f);
                    float size = rand.random(18f, 26f)*lerp;

                    float a = f + Time.time * w;
                    Tmp.v1.set(r, 0).setAngle(a);

                    SglDraw.drawHaloPart(b.x + Tmp.v1.x, b.y + Tmp.v1.y, size, size*0.5f, a);
                  }
                }

                @Override
                public void despawned(Bullet b) {
                  super.despawned(b);
                  Sounds.malignShoot.at(b, 2);
                }

                @Override
                public void update(Bullet b) {
                  super.update(b);

                  if (b.timer(4, 18f)){
                    Teamc target = Units.closestTarget(b.team, b.x, b.y, range,
                        e -> e != null && e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                        t -> t != null && collidesGround && !b.hasCollided(t.id));

                    fragBullet.create(b, b.x, b.y, target != null? b.angleTo(target): Mathf.random(0, 360));
                  }
                }

                @Override
                public void updateHoming(Bullet b) {
                  if(homingPower > 0.0001f && b.time >= homingDelay){
                    float realAimX = b.aimX < 0 ? b.x : b.aimX;
                    float realAimY = b.aimY < 0 ? b.y : b.aimY;

                    Posc target;
                    if(b.aimTile != null && b.aimTile.build != null && b.aimTile.build.team != b.team && collidesGround && !b.hasCollided(b.aimTile.build.id)){
                      target = b.aimTile.build;
                    }else{
                      target = Units.closestTarget(b.team, realAimX, realAimY, homingRange,
                          e -> e != null && e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                          t -> t != null && collidesGround && !b.hasCollided(t.id));
                    }

                    if(target != null){
                      float dst = target.dst(b);
                      float v = Mathf.lerpDelta(b.vel.len(), speed*(dst/homingRange), 0.05f);
                      b.vel.setLength(v);

                      float degA = b.rotation();
                      float degB = b.angleTo(target);

                      if (degA - degB > 180){
                        degB += 360;
                      }
                      else if (degA - degB < -180){
                        degB -= 360;
                      }

                      b.vel.setAngle(Mathf.lerpDelta(degA, degB, homingPower));
                    }
                    else{
                      b.vel.lerpDelta(0, 0, 0.03f);
                    }
                  }
                }
              };
            }

            @Override
            public void init(Unit unit, DataWeaponMount mount) {
              super.init(unit, mount);
              mount.setVar(EPHEMERAS, new Seq<>(Ephemera.class));
              mount.setVar(TIMER, new Interval(3));
            }

            @Override
            protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
              DataWeaponMount m = (DataWeaponMount) mount;
              Seq<Ephemera> seq = m.getVar(EPHEMERAS);
              for (Ephemera ephemera : seq) {
                if (ephemera.alpha > 0.9f) {
                  ephemera.shoot(unit, useAlternative.alt(unit) ? alternativeBullet: bullet);
                  ephemera.removed = true;
                  break;
                }
              }
            }

            @Override
            public void draw(Unit unit, DataWeaponMount mount) {
              super.draw(unit, mount);
              Draw.z(Layer.effect);
              SglDraw.drawTransform(unit.x, unit.y, mount.weapon.x, mount.weapon.y, unit.rotation - 90, (x, y, r) -> {
                Draw.color(SglDrawConst.matrixNet);
                Fill.circle(x, y, 6);
                Lines.stroke(0.8f);
                SglDraw.dashCircle(x, y, 8, 4, 180, Time.time);
                Lines.stroke(0.5f);
                Lines.circle(x, y, 10);

                Draw.alpha(1);
                SglDraw.drawDiamond(x, y, 20 + 14*mount.warmup, 2 + 3*mount.warmup, Time.time*1.2f);
                SglDraw.drawDiamond(x, y, 26 + 14*mount.warmup, 3 + 4*mount.warmup, -Time.time*1.2f);

                for (Ephemera ephemera : mount.<Seq<Ephemera>>getVar(EPHEMERAS)) {
                  Draw.color(SglDrawConst.matrixNet);

                  if (!ephemera.removed) {
                    Fill.circle(ephemera.x, ephemera.y, 4);
                    Lines.stroke(0.8f);
                    Lines.circle(ephemera.x, ephemera.y, 6);
                  }

                  for (int i = 0; i < 3; i++) {
                    Tmp.v1.set(16, 0).setAngle(Time.time + i*120);
                    float sin = Mathf.absin((Time.time*4 + i*120)*Mathf.degRad, 0.5f, 1);
                    sin = Math.max(sin, mount.warmup)*ephemera.alpha;
                    float w = sin*sin*sin*4;
                    SglDraw.drawDiamond(ephemera.x + Tmp.v1.x, ephemera.y + Tmp.v1.y, 20, w, Time.time + i*120);
                    SglDraw.drawDiamond(ephemera.x - Tmp.v1.x, ephemera.y - Tmp.v1.y, 20, w, Time.time + i*120);
                  }

                  ephemera.trail.draw(SglDrawConst.matrixNet, 4f*ephemera.alpha);
                }
              });
            }

            @Override
            public void update(Unit unit, DataWeaponMount mount) {
              Tmp.v1.set(mount.weapon.x, mount.weapon.y).rotate(unit.rotation - 90);
              float mx = unit.x + Tmp.v1.x;
              float my = unit.y + Tmp.v1.y;
              Seq<Ephemera> seq = mount.getVar(EPHEMERAS);

              if (seq.size < 4){
                if (mount.<Interval>getVar(TIMER).get(0, 240)) {
                  mount.totalShots++;

                  Ephemera ephemera = Pools.obtain(Ephemera.class, Ephemera::new);
                  ephemera.x = mx;
                  ephemera.y = my;
                  ephemera.move = Mathf.random(0.02f, 0.04f);
                  ephemera.angelOff = Mathf.random(15, 45) * (mount.totalShots%2 == 0 ? 1 : -1);
                  ephemera.bestDst = Mathf.random(18, 36);
                  ephemera.vel.rnd(Mathf.random(0.6f, 2));

                  seq.add(ephemera);
                }
              }

              if (seq.isEmpty()){
                mount.reload = mount.weapon.reload;
              }

              for (Iterator<Ephemera> iterator = seq.iterator(); iterator.hasNext(); ) {
                Ephemera ephemera = iterator.next();
                ephemera.alpha = Mathf.lerpDelta(ephemera.alpha, ephemera.removed? 0: 1, 0.015f);
                ephemera.trail.update(ephemera.x, ephemera.y);
                if (ephemera.removed){
                  if (ephemera.alpha <= 0.05f) {
                    iterator.remove();
                    Pools.free(ephemera);
                  }
                  continue;
                }

                ephemera.x += ephemera.vel.x * Time.delta;
                ephemera.y += ephemera.vel.y * Time.delta;

                float dst = (ephemera.bestDst + ephemera.bestDst * mount.warmup) - Mathf.dst(ephemera.x - mx, ephemera.y - my);
                float speed = dst / 30;

                ephemera.vel.lerpDelta(Tmp.v1.set(ephemera.x - unit.x, ephemera.y - unit.y).setLength2(1).scl(speed).rotate(ephemera.angelOff), 0.15f);
                Tmp.v1.set(ephemera.move + ephemera.move * mount.warmup, 0).rotate(Time.time);
                ephemera.vel.add(Tmp.v1);
              }
            }

            class Ephemera implements Pool.Poolable {
              float x, y, angelOff, move;
              float bestDst, alpha;
              boolean removed;
              final Vec2 vel = new Vec2();
              final Trail trail = new Trail(60);

              @Override
              public void reset() {
                x = y = 0;
                alpha = 0;
                removed = false;
                vel.setZero();
                trail.clear();
                bestDst = 0;
                move = 0;
                angelOff = 0;
              }

              public void shoot(Unit u, BulletType bullet) {
                Bullet b = bullet.create(u, x, y, vel.angle());
                if(b.type.speed > 0.01f) b.vel.set(vel);
                b.set(x, y);
                bullet.shootEffect.at(b.x, b.y, vel.angle(), b.type.hitColor);
                bullet.smokeEffect.at(b.x, b.y, vel.angle(), b.type.hitColor);
              }
            }
          }
      );
    }};

    kaguya = new SglUnitType("kaguya"){
      {
        requirements(
            Items.silicon, 460,
            Items.phaseFabric, 480,
            Items.surgeAlloy, 450,
            SglItems.aluminium, 520,
            SglItems.aerogel, 480,
            SglItems.crystal_FEX_power, 280,
            SglItems.strengthening_alloy, 340,
            SglItems.iridium, 140,
            SglItems.matrix_alloy, 220
        );

        speed = 1.1f;
        accel = 0.06f;
        drag = 0.04f;
        rotateSpeed = 1.5f;
        faceTarget = true;
        flying = true;
        health = 45000;
        lowAltitude = true;
        //canBoost = true;
        //boostMultiplier = 2.5f;
        hitSize = 70;
        targetFlags = BlockFlag.all;

        engineSize = 0;

        Func2<Float, Float, Weapon> laser = (dx, dy) -> new SglWeapon(Sgl.modName + "-kaguya_laser"){{
          this.x = dx;
          this.y = dy;
          mirror = true;
          reload = 30;
          recoil = 4;
          recoilTime = 30;
          shadow = 4;
          rotate = true;
          layerOffset = 0.1f;
          shootSound = Sounds.laser;

          shake = 3;

          bullet = new LaserBulletType(){{
            damage = 165f;
            lifetime = 20;
            sideAngle = 90f;
            sideWidth = 1.25f;
            sideLength = 15f;
            width = 16f;
            length = 450f;
            hitEffect = Fx.circleColorSpark;
            shootEffect = Fx.colorSparkBig;
            colors = new Color[]{SglDrawConst.matrixNetDark, SglDrawConst.matrixNet, Color.white};
            hitColor = colors[0];
          }};
        }};

        weapons.addAll(
            laser.get(19.25f, 16f),
            laser.get(13.5f, 33.5f),
            new SglWeapon(Sgl.modName + "-kaguya_cannon"){
              {
                x = 30.5f;
                y = -3.5f;
                mirror = true;

                cooldownTime = 120;
                recoil = 0;
                recoilTime = 120;
                reload = 90;
                shootX = 2;
                shootY = 22;
                rotate = true;
                rotationLimit = 30;
                rotateSpeed = 10;

                shake = 5;

                layerOffset = 0.1f;

                shootSound = Sounds.shockBlast;

                shoot.shots = 3;
                shoot.shotDelay = 10;

                parts.addAll(
                    new RegionPart("_shooter"){{
                      heatColor = SglDrawConst.matrixNet;
                      heatProgress = PartProgress.heat;
                      moveY = -6;
                      progress = PartProgress.recoil;
                    }},
                    new RegionPart("_body")
                );

                bullet = new MultiTrailBulletType(){
                  {
                    speed = 6;
                    lifetime = 75;
                    damage = 180;
                    splashDamage = 240;
                    splashDamageRadius = 36;

                    hitEffect = new MultiEffect(
                        Fx.shockwave,
                        Fx.bigShockwave,
                        SglFx.impactWaveSmall,
                        SglFx.spreadSparkLarge,
                        SglFx.diamondSparkLarge
                    );
                    despawnHit = true;

                    smokeEffect = Fx.shootSmokeSmite;
                    shootEffect = SglFx.railShootRecoil;
                    hitColor = SglDrawConst.matrixNet;
                    trailColor = SglDrawConst.matrixNet;
                    hitSize = 8;
                    trailLength = 36;
                    trailWidth = 4;

                    hitShake = 4;
                    hitSound = Sounds.dullExplosion;
                    hitSoundVolume = 3.5f;

                    trailEffect = SglFx.trailParticle;
                    trailChance = 0.5f;

                    fragBullet = new EdgeFragBullet();
                    fragBullets = 4;
                    fragOnHit = true;
                    fragOnAbsorb = true;
                  }

                  @Override
                  public void draw(Bullet b){
                    super.draw(b);
                    Drawf.tri(b.x, b.y, 12, 30, b.rotation());
                    Drawf.tri(b.x, b.y, 12, 12, b.rotation() + 180);
                  }
                };
              }
            },
            new PointDefenseWeapon(Sgl.modName + "-kaguya_point_laser"){{
              x = 30.5f;
              y = -3.5f;
              mirror = true;

              recoil = 0;
              reload = 12;
              targetInterval = 0;
              targetSwitchInterval = 0;

              layerOffset = 0.2f;

              bullet = new BulletType(){{
                damage = 62;
                rangeOverride = 420;
              }};
            }},
            new DataWeapon(Sgl.modName + "-lightedge"){
              {
                x = 0;
                y = -14;
                minWarmup = 0.98f;
                shootWarmupSpeed = 0.02f;
                linearWarmup = false;
                rotate = false;
                shootCone = 10;
                rotateSpeed = 10;
                shootY = 80;
                reload = 30;
                recoilTime = 60;
                recoil = 2;
                recoilPow = 0;
                targetSwitchInterval = 300;
                targetInterval = 0;

                mirror = false;
                continuous = true;
                alwaysContinuous = true;

                Weapon s = this;

                bullet = new PointLaserBulletType(){
                  {
                    damage = 240;
                    damageInterval = 5;
                    rangeOverride = 450;
                    shootEffect = SglFx.railShootRecoil;
                    hitColor = SglDrawConst.matrixNet;
                    hitEffect = SglFx.diamondSparkLarge;
                    shake = 5;
                  }

                  @Override
                  public float continuousDamage(){
                    return damage*(60/damageInterval);
                  }

                  @Override
                  public void update(Bullet b){
                    super.update(b);

                    if(b.owner instanceof Unit u){
                      for(WeaponMount mount: u.mounts){
                        if(mount.weapon == s){
                          float bulletX = u.x + Angles.trnsx(u.rotation - 90, x + shootX, y + shootY),
                              bulletY = u.y + Angles.trnsy(u.rotation - 90, x + shootX, y + shootY);

                          b.set(bulletX, bulletY);
                          Tmp.v2.set(mount.aimX - bulletX, mount.aimY - bulletY);
                          float angle = Mathf.clamp(Tmp.v2.angle() - u.rotation, -shootCone, shootCone);
                          Tmp.v2.setAngle(u.rotation).rotate(angle);

                          Tmp.v1.set(b.aimX - bulletX, b.aimY - bulletY).lerpDelta(Tmp.v2, 0.1f).clampLength(80, range);

                          b.aimX = bulletX + Tmp.v1.x;
                          b.aimY = bulletY + Tmp.v1.y;

                          shootEffect.at(bulletX, bulletY, Tmp.v1.angle(), hitColor);
                        }
                      }
                    }
                  }

                  @Override
                  public void draw(Bullet b){
                    super.draw(b);
                    Draw.draw(Draw.z(), () -> {
                      Draw.color(hitColor);
                      MathRenderer.setDispersion(0.1f);
                      MathRenderer.setThreshold(0.4f, 0.6f);

                      for(int i = 0; i < 3; i++){
                        MathRenderer.drawSin(b.x, b.y, b.aimX, b.aimY,
                            Mathf.randomSeed(b.id + i, 4f, 6f)*b.fslope(),
                            Mathf.randomSeed(b.id + i + 1, 360f, 720f),
                            Mathf.randomSeed(b.id + i + 2, 360f) - Time.time*Mathf.randomSeed(b.id + i + 3, 4f, 7f)
                        );
                      }
                    });
                  }
                };

                parts.addAll(
                    new CustomPart(){{
                      layer = Layer.effect;
                      progress = PartProgress.warmup;
                      draw = (x, y, r, p) -> {
                        Draw.color(SglDrawConst.matrixNet);
                        Fill.circle(x, y, 8);
                        Lines.stroke(1.4f);
                        SglDraw.dashCircle(x, y, 12, Time.time);

                        Draw.draw(Draw.z(), () -> {
                          MathRenderer.setThreshold(0.65f, 0.8f);
                          MathRenderer.setDispersion(1f);
                          MathRenderer.drawCurveCircle(x, y, 15, 2, 6, Time.time);
                          MathRenderer.setDispersion(0.6f);
                          MathRenderer.drawCurveCircle(x, y, 16, 3, 6, -Time.time);
                        });

                        Draw.alpha(0.65f);
                        SglDraw.gradientCircle(x, y, 20, 12, 0);

                        Draw.alpha(1);
                        SglDraw.drawDiamond(x, y, 24 + 18*p, 3 + 3*p, Time.time*1.2f);
                        SglDraw.drawDiamond(x, y, 30 + 18*p, 4 + 4*p, -Time.time*1.2f);
                      };
                    }}
                );
              }

              @Override
              public void init(Unit unit, DataWeaponMount mount){
                Shooter[] shooters = new Shooter[3];
                for(int i = 0; i < shooters.length; i++){
                  shooters[i] = new Shooter();
                }
                mount.setVar(SHOOTERS, shooters);
              }

              @Override
              public void update(Unit unit, DataWeaponMount mount){
                Shooter[] shooters = mount.getVar(SHOOTERS);
                for(Shooter shooter: shooters){
                  Vec2 v = MathTransform.fourierSeries(Time.time, shooter.param).scl(mount.warmup);
                  Tmp.v1.set(mount.weapon.x, mount.weapon.y).rotate(unit.rotation - 90);
                  shooter.x = Tmp.v1.x + v.x;
                  shooter.y = Tmp.v1.y + v.y;
                  shooter.trail.update(unit.x + shooter.x, unit.y + shooter.y);
                }
              }

              @Override
              protected void shoot(Unit unit, DataWeaponMount mount, float shootX, float shootY, float rotation){
                float mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
                    mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y);

                SglFx.shootRecoilWave.at(shootX, shootY, rotation, SglDrawConst.matrixNet);
                SglFx.impactWave.at(shootX, shootY, SglDrawConst.matrixNet);

                SglFx.impactWave.at(mountX, mountY, SglDrawConst.matrixNet);
                SglFx.crossLight.at(mountX, mountY, SglDrawConst.matrixNet);
                Shooter[] shooters = mount.getVar(SHOOTERS);
                for(Shooter shooter: shooters){
                  SglFx.impactWaveSmall.at(mountX + shooter.x, mountY + shooter.y);
                }
              }

              @Override
              public void draw(Unit unit, DataWeaponMount mount){
                Shooter[] shooters = mount.getVar(SHOOTERS);
                Draw.z(Layer.effect);

                float mountX = unit.x + Angles.trnsx(unit.rotation - 90, x, y),
                    mountY = unit.y + Angles.trnsy(unit.rotation - 90, x, y);

                float bulletX = mountX + Angles.trnsx(unit.rotation - 90, shootX, shootY),
                    bulletY = mountY + Angles.trnsy(unit.rotation - 90, shootX, shootY);

                Draw.color(SglDrawConst.matrixNet);
                Fill.circle(bulletX, bulletY, 6*mount.recoil);
                Draw.color(Color.white);
                Fill.circle(bulletX, bulletY, 3*mount.recoil);

                for(Shooter shooter: shooters){
                  Draw.color(SglDrawConst.matrixNet);
                  shooter.trail.draw(SglDrawConst.matrixNet, 3*mount.warmup);

                  float drawx = unit.x + shooter.x, drawy = unit.y + shooter.y;
                  Fill.circle(drawx, drawy, 4*mount.warmup);
                  Lines.stroke(0.65f*mount.warmup);
                  SglDraw.dashCircle(drawx, drawy, 6f*mount.warmup, 4, 180, Time.time);
                  SglDraw.drawDiamond(drawx, drawy, 4 + 8*mount.warmup, 3*mount.warmup, Time.time*1.45f);
                  SglDraw.drawDiamond(drawx, drawy, 8 + 10*mount.warmup, 3.6f*mount.warmup, -Time.time*1.45f);

                  Lines.stroke(3*mount.recoil, SglDrawConst.matrixNet);
                  Lines.line(drawx, drawy, bulletX, bulletY);
                  Lines.stroke(1.75f*mount.recoil, Color.white);
                  Lines.line(drawx, drawy, bulletX, bulletY);

                  Draw.alpha(0.5f);
                  Lines.line(mountX, mountY, drawx, drawy);
                }
              }

              static class Shooter{
                final Trail trail = new Trail(45);
                final float[] param;

                float x, y;

                {
                  param = new float[9];
                  for(int d = 0; d < 3; d++){
                    param[d*3] = Mathf.random(0.5f, 3f)/(d + 1)*(Mathf.randomBoolean()? 1: -1);
                    param[d*3 + 1] = Mathf.random(0f, 360f);
                    param[d*3 + 2] = Mathf.random(18f, 48f)/((d + 1)*(d + 1));
                  }
                }
              }
            }
        );
      }
    };

    aurora = new AirSeaAmphibiousUnit("aurora"){
      {
        requirements(
            Items.silicon, 360,
            Items.phaseFabric, 380,
            Items.surgeAlloy, 390,
            SglItems.aluminium, 400,
            SglItems.aerogel, 430,
            SglItems.crystal_FEX, 280,
            SglItems.crystal_FEX_power, 280,
            SglItems.strengthening_alloy, 340,
            SglItems.iridium, 320,
            SglItems.matrix_alloy, 380,
            SglItems.degenerate_neutron_polymer, 200
        );

        speed = 0.65f;
        accel = 0.06f;
        drag = 0.04f;
        rotateSpeed = 1.25f;
        riseSpeed = 0.02f;
        boostMultiplier = 1.2f;
        faceTarget = true;
        health = 52500;
        lowAltitude = true;
        hitSize = 75;
        targetFlags = BlockFlag.allLogic;

        engineOffset = 50;
        engineSize = 16;

        setEnginesMirror(
            new UnitEngine(){{
              x = 38f;
              y = -12;
              radius = 8;
              rotation = -45;
            }},
            new UnitEngine(){{
              x = 40f;
              y = -54;
              radius = 10;
              rotation = -45;
            }}
        );

        weapons.addAll(
            new SglWeapon(Sgl.modName + "-aurora_lightcone"){
              {
                shake = 5f;
                shootSound = Sounds.pulseBlast;
                x = 29;
                y = -30;
                shootY = 8;
                rotate = true;
                rotateSpeed = 3;
                recoil = 6;
                recoilTime = 60;
                cooldownTime = 60;
                reload = 60;
                shadow = 45;
                linearWarmup = false;
                shootWarmupSpeed = 0.03f;
                minWarmup = 0.8f;

                layerOffset = 1;

                parts.addAll(
                    new CustomPart(){{
                      x = 0;
                      y = -16f;
                      layer = Layer.effect;
                      progress = PartProgress.warmup;

                      draw = (x, y, r, p) -> {
                        Lines.stroke(p*1.6f, SglDrawConst.matrixNet);
                        Lines.circle(x, y, 3*p);

                        Tmp.v1.set(0, 9*p).setAngle(r + 180 + 40*p);
                        Tmp.v2.set(0, 9*p).setAngle(r + 180 - 40*p);

                        SglDraw.drawDiamond(x + Tmp.v1.x, y + Tmp.v1.y, 9*p, 7f*p, Tmp.v1.angle());
                        SglDraw.drawDiamond(x + Tmp.v2.x, y + Tmp.v2.y, 9*p, 7f*p, Tmp.v2.angle());

                        Tmp.v1.set(0, 9*p).setAngle(r + 180 + 100*p);
                        Tmp.v2.set(0, 9*p).setAngle(r + 180 - 100*p);

                        SglDraw.drawDiamond(x + Tmp.v1.x, y + Tmp.v1.y, 9*p, 7f*p, Tmp.v1.angle());
                        SglDraw.drawDiamond(x + Tmp.v2.x, y + Tmp.v2.y, 9*p, 7f*p, Tmp.v2.angle());
                      };
                    }},
                    new CustomPart(){{
                      x = 0;
                      y = -16f;
                      layer = Layer.effect;
                      progress = PartProgress.warmup.delay(0.7f);

                      draw = (x, y, r, p) -> {
                        Tmp.v1.set(0, 14*p).setAngle(r + 195);
                        Tmp.v2.set(0, 14*p).setAngle(r + 165);

                        SglDraw.gapTri(x + Tmp.v1.x, y + Tmp.v1.y, 3*p, 10, -3, Tmp.v1.angle());
                        SglDraw.gapTri(x + Tmp.v2.x, y + Tmp.v2.y, 3*p, 10, -3, Tmp.v2.angle());

                        Tmp.v1.set(0, 14*p).setAngle(r + 250);
                        Tmp.v2.set(0, 14*p).setAngle(r + 110);

                        SglDraw.gapTri(x + Tmp.v1.x, y + Tmp.v1.y, 4*p, 12f, -4, Tmp.v1.angle());
                        SglDraw.gapTri(x + Tmp.v2.x, y + Tmp.v2.y, 4*p, 12f, -4, Tmp.v2.angle());

                        Tmp.v1.set(0, 14*p).setAngle(r + 305);
                        Tmp.v2.set(0, 14*p).setAngle(r + 55);

                        SglDraw.gapTri(x + Tmp.v1.x, y + Tmp.v1.y, 3*p, 10, -3, Tmp.v1.angle());
                        SglDraw.gapTri(x + Tmp.v2.x, y + Tmp.v2.y, 3*p, 10, -3, Tmp.v2.angle());
                      };
                    }}
                );

                bullet = new EmpBulletType(){
                  {
                    trailLength = 36;
                    trailWidth = 2.2f;
                    trailColor = SglDrawConst.matrixNet;
                    trailRotation = true;
                    trailChance = 1;
                    hitSize = 8;
                    speed = 12;
                    lifetime = 40;
                    damage = 620;
                    range = 480;

                    empDamage = 26;

                    pierce = true;
                    hittable = false;
                    reflectable = false;
                    pierceArmor = true;
                    pierceBuilding = true;
                    absorbable = false;

                    trailEffect = new MultiEffect(
                        SglFx.lightConeTrail,
                        SglFx.lightCone,
                        SglFx.trailLineLong
                    );
                    hitEffect = SglFx.lightConeHit;
                    hitColor = SglDrawConst.matrixNet;

                    intervalBullet = new BulletType(){
                      {
                        damage = 132;
                        speed = 8;
                        hitSize = 3;
                        keepVelocity = false;
                        lifetime = 45;
                        hitColor = SglDrawConst.matrixNet;
                        hitEffect = SglFx.circleSparkMini;

                        despawnHit = true;

                        trailColor = SglDrawConst.matrixNet;
                        trailWidth = 3;
                        trailLength = 23;
                      }

                      @Override
                      public void draw(Bullet b) {
                        super.draw(b);
                        Draw.color(hitColor);
                        SglDraw.drawDiamond(b.x, b.y, 12*b.fout(), 6*b.fout(), b.rotation());
                      }

                      @Override
                      public void drawTrail(Bullet b) {
                        float z = Draw.z();
                        Draw.z(z - 0.0001f);
                        b.trail.draw(trailColor, trailWidth*b.fout());
                        Draw.z(z);
                      }

                      public void removed(Bullet b){
                        if(trailLength > 0 && b.trail != null && b.trail.size() > 0){
                          Fx.trailFade.at(b.x, b.y, trailWidth*b.fout(), trailColor, b.trail.copy());
                        }
                      }
                    };
                    bulletInterval = 4;
                  }

                  @Override
                  public void init(Bullet b) {
                    super.init(b);
                    TrailMoveLightning l = Pools.obtain(TrailMoveLightning.class, TrailMoveLightning::new);
                    l.range = 8f;
                    l.maxOff = 7.5f;
                    l.chance = 0.4f;

                    b.data = l;
                  }

                  @Override
                  public void updateBulletInterval(Bullet b) {
                    if(b.timer.get(2, bulletInterval)){
                      Bullet bull = intervalBullet.create(b, b.x, b.y, b.rotation());
                      bull.vel.scl(b.fout());
                      rand.setSeed(bull.id);
                      float scl = rand.random(3.65f, 5.25f)*(rand.nextBoolean()? 1: -1);
                      float mag = rand.random(2.8f, 5.6f)*b.fout();
                      bull.mover = e -> {
                        e.moveRelative(0f, Mathf.cos(e.time, scl, mag));
                      };
                    }
                  }

                  @Override
                  public void despawned(Bullet b) {
                    super.despawned(b);
                    if (b.data instanceof TrailMoveLightning l) Pools.free(l);
                  }

                  @Override
                  public void updateTrail(Bullet b) {
                    if(!headless && trailLength > 0){
                      if(b.trail == null){
                        b.trail = new Trail(trailLength);
                      }
                      b.trail.length = trailLength;

                      if (!(b.data instanceof TrailMoveLightning m)) return;
                      m.update();
                      SglDraw.drawTransform(b.x, b.y, 0, m.off, b.rotation(), (x, y, r) -> b.trail.update(x, y));
                    }
                  }

                  @Override
                  public void draw(Bullet b) {
                    super.draw(b);
                    Draw.color(SglDrawConst.matrixNet);
                    Drawf.tri(b.x, b.y, 8, 18, b.rotation());
                    for(int i : Mathf.signs){
                      Drawf.tri(b.x, b.y, 8f, 26f, b.rotation() + 156f*i);
                    }
                  }

                  @Override
                  public void update(Bullet b) {
                    super.update(b);
                    Damage.damage(b.team, b.x, b.y, hitSize, damage*Time.delta);
                  }
                };
            }},
            new SglWeapon(Sgl.modName + "-aurora_turret"){{
              shake = 4f;
              shootSound = Sounds.laser;
              x = 22;
              y = 20;
              shootY = 6;
              rotate = true;
              rotateSpeed = 6;
              recoil = 4;
              recoilTime = 20;
              cooldownTime = 60;
              reload = 20;
              shadow = 25;

              bullet = new LightLaserBulletType(){{
                damage = 425f;
                empDamage = 96;
                lifetime = 24;
                width = 16f;
                length = 480f;
                shootEffect = new MultiEffect(
                    SglFx.crossLightSmall,
                    SglFx.shootRecoilWave
                );
                colors = new Color[]{SglDrawConst.matrixNetDark, SglDrawConst.matrixNet, Color.white};
                hitColor = colors[0];

                generator.maxSpread = 11.25f;
                generator.minInterval = 6;
                generator.maxInterval = 15;

                lightningMinWidth = 2.2f;
                lightningMaxWidth = 3.8f;
              }};
            }},
            new RelatedWeapon(Sgl.modName + "-lightedge"){
              {
                x = 0;
                y = -22;
                shootY = 0;
                reload = 600;
                mirror = false;
                rotateSpeed = 0;
                shootCone = 0.5f;
                rotate = true;
                shootSound = Sounds.laserblast;
                ejectEffect = SglFx.railShootRecoil;
                recoilTime = 30;
                shake = 4;

                minWarmup = 0.9f;
                shootWarmupSpeed = 0.03f;

                shoot.firstShotDelay = 80;

                alternativeShoot = new ShootPattern(){
                  @Override
                  public void shoot(int totalShots, BulletHandler handler) {
                    for (int i = 0; i < shots; i++) {
                      handler.shoot(0, 0, Mathf.random(0, 360f), firstShotDelay + i*shotDelay);
                    }
                  }
                };
                alternativeShoot.shots = 12;
                alternativeShoot.shotDelay = 3;
                alternativeShoot.firstShotDelay = 0;
                useAlternative = isFlying;
                parentizeEffects = true;

                parts.addAll(
                    new HaloPart(){{
                      progress = PartProgress.warmup;
                      color = SglDrawConst.matrixNet;
                      layer = Layer.effect;
                      haloRotateSpeed = -1;
                      shapes = 2;
                      triLength = 0f;
                      triLengthTo = 26f;
                      haloRadius = 0;
                      haloRadiusTo = 14f;
                      tri = true;
                      radius = 6;
                    }},
                    new HaloPart(){{
                      progress = PartProgress.warmup;
                      color = SglDrawConst.matrixNet;
                      layer = Layer.effect;
                      haloRotateSpeed = -1;
                      shapes = 2;
                      triLength = 0f;
                      triLengthTo = 8f;
                      haloRadius = 0;
                      haloRadiusTo = 14f;
                      tri = true;
                      radius = 6;
                      shapeRotation = 180f;
                    }},
                    new HaloPart(){{
                      progress = PartProgress.warmup;
                      color = SglDrawConst.matrixNet;
                      layer = Layer.effect;
                      haloRotateSpeed = 1;
                      shapes = 2;
                      triLength = 0f;
                      triLengthTo = 12f;
                      haloRadius = 8;
                      tri = true;
                      radius = 8;
                    }},
                    new HaloPart(){{
                      progress = PartProgress.warmup;
                      color = SglDrawConst.matrixNet;
                      layer = Layer.effect;
                      haloRotateSpeed = 1;
                      shapes = 2;
                      triLength = 0f;
                      triLengthTo = 8f;
                      haloRadius = 8;
                      tri = true;
                      radius = 8;
                      shapeRotation = 180f;
                    }},
                    new CustomPart(){{
                      layer = Layer.effect;
                      progress = PartProgress.warmup;

                      draw = (x, y, r, p) -> {
                        Draw.color(SglDrawConst.matrixNet);
                        SglDraw.gapTri(x + Angles.trnsx(r + Time.time, 16, 0), y + Angles.trnsy(r + Time.time, 16, 0), 12*p, 42, 12, r + Time.time);
                        SglDraw.gapTri(x + Angles.trnsx(r + Time.time + 180, 16, 0), y + Angles.trnsy(r + Time.time + 180, 16, 0), 12*p, 42, 12, r + Time.time + 180);
                      };
                    }}
                );

                Weapon s = this;
                bullet = new ContinuousLaserBulletType(){
                  {
                    damage = 210;
                    lifetime = 180;
                    fadeTime = 30;
                    length = 720;
                    width = 6;
                    hitColor = SglDrawConst.matrixNet;
                    shootEffect = SglFx.explodeImpWave;
                    chargeEffect = SglFx.auroraCoreCharging;
                    chargeSound = Sounds.lasercharge;
                    fragBullets = 2;
                    fragSpread = 10;
                    fragOnHit = true;
                    fragRandomSpread = 60;
                    shake = 5;
                    incendAmount = 0;
                    incendChance = 0;

                    drawSize = 620;
                    pointyScaling = 0.7f;
                    oscMag = 0.85f;
                    oscScl = 1.1f;
                    frontLength = 70;
                    lightColor = SglDrawConst.matrixNet;
                    colors = new Color[]{
                        Color.valueOf("8FFFF0").a(0.6f),
                        Color.valueOf("8FFFF0").a(0.85f),
                        Color.valueOf("B6FFF7"),
                        Color.valueOf("D3FDFF")
                    };
                  }

                  @Override
                  public void update(Bullet b) {
                    super.update(b);
                    if (b.owner instanceof Unit u){
                      u.vel.lerp(0, 0, 0.1f);

                      float bulletX = u.x + Angles.trnsx(u.rotation - 90, x + shootX, y + shootY),
                          bulletY = u.y + Angles.trnsy(u.rotation - 90, x + shootX, y + shootY),
                          angle = u.rotation;

                      b.rotation(angle);
                      b.set(bulletX, bulletY);

                      for (WeaponMount mount : u.mounts) {
                        mount.reload = mount.weapon.reload;
                        if (mount.weapon == s){
                          mount.recoil = 1;
                        }
                      }

                      if(ejectEffect != null) ejectEffect.at(bulletX, bulletY, angle, b.type.hitColor);
                    }
                  }

                  @Override
                  public void draw(Bullet b) {
                    float realLength = Damage.findLaserLength(b, length);
                    float fout = Mathf.clamp(b.time > b.lifetime - fadeTime ? 1f - (b.time - (lifetime - fadeTime)) / fadeTime : 1f);
                    float baseLen = realLength * fout;
                    float rot = b.rotation();

                    for(int i = 0; i < colors.length; i++){
                      Draw.color(Tmp.c1.set(colors[i]).mul(1f + Mathf.absin(Time.time, 1f, 0.1f)));

                      float colorFin = i / (float)(colors.length - 1);
                      float baseStroke = Mathf.lerp(strokeFrom, strokeTo, colorFin);
                      float stroke = (width + Mathf.absin(Time.time, oscScl, oscMag)) * fout * baseStroke;
                      float ellipseLenScl = Mathf.lerp(1 - i / (float)(colors.length), 1f, pointyScaling);

                      Lines.stroke(stroke);
                      Lines.lineAngle(b.x, b.y, rot, baseLen - frontLength, false);

                      //back ellipse
                      Drawf.flameFront(b.x, b.y, divisions, rot + 180f, backLength, stroke / 2f);

                      //front ellipse
                      Tmp.v1.trnsExact(rot, baseLen - frontLength);
                      Drawf.flameFront(b.x + Tmp.v1.x, b.y + Tmp.v1.y, divisions, rot, frontLength * ellipseLenScl, stroke / 2f);
                    }

                    Tmp.v1.trns(b.rotation(), baseLen * 1.1f);

                    Drawf.light(b.x, b.y, b.x + Tmp.v1.x, b.y + Tmp.v1.y, lightStroke, lightColor, 0.7f);

                    Draw.color(SglDrawConst.matrixNet);

                    float step = 1/45f;
                    Tmp.v1.set(length, 0).setAngle(b.rotation());
                    float dx = Tmp.v1.x;
                    float dy = Tmp.v1.y;
                    for (int i = 0; i < 45; i++) {
                      if(i*step*length > realLength) break;

                      float lerp = Mathf.clamp(b.time/(fadeTime*step*i))*Mathf.sin(Time.time/2 - i*step*Mathf.pi*6);
                      Draw.alpha(0.4f + 0.6f*lerp);
                      SglDraw.drawDiamond(b.x + dx*step*i, b.y + dy*step*i, 8*fout, 16 + 20*lerp + 80*(1 - fout), b.rotation());
                    }
                    Draw.reset();
                  }
                };

                alternativeBullet = new BulletType(){
                  {
                    pierceArmor = true;
                    hitShake = 6;
                    damage = 280;
                    splashDamage = 420;
                    splashDamageRadius = 32;
                    absorbable = false;
                    hittable = true;
                    speed = 10;
                    lifetime = 120;
                    homingRange = 450;
                    homingPower = 0.25f;
                    hitColor = SglDrawConst.matrixNet;
                    hitEffect = new MultiEffect(
                        SglFx.explodeImpWave,
                        SglFx.diamondSpark
                    );

                    trailLength = 32;
                    trailWidth = 3;
                    trailColor = SglDrawConst.matrixNet;
                    trailEffect = new MultiEffect(
                        SglFx.movingCrystalFrag,
                        Fx.colorSparkBig
                    );
                    trailRotation = true;
                    trailInterval = 4;

                    despawnHit = true;

                    homingDelay = 30;

                    fragBullet = new BulletType(){
                      {
                        collides = false;
                        absorbable = false;

                        splashDamage = 260;
                        splashDamageRadius = 24;
                        speed = 1.2f;
                        lifetime = 64;

                        hitShake = 4;
                        hitSize = 3;

                        despawnHit = true;
                        hitEffect = new MultiEffect(
                            SglFx.explodeImpWaveSmall,
                            SglFx.diamondSpark
                        );
                        hitColor = SglDrawConst.matrixNet;

                        trailColor = SglDrawConst.matrixNet;
                        trailEffect = SglFx.glowParticle;
                        trailRotation = true;
                        trailInterval = 15f;

                        fragBullet = new LightningBulletType(){{
                          lightningLength = 14;
                          lightningLengthRand = 4;
                          damage = 24;
                        }};
                        fragBullets = 1;
                      }

                      @Override
                      public void draw(Bullet b) {
                        Draw.color(hitColor);
                        float fout = b.fout(Interp.pow3Out);
                        Fill.circle(b.x, b.y, 5f*fout);
                        Draw.color(Color.black);
                        Fill.circle(b.x, b.y, 2.6f*fout);
                      }
                    };
                    fragBullets = 3;
                  }

                  @Override
                  public void updateHoming(Bullet b) {
                    if (Mathf.chanceDelta(0.3f*b.vel.len()/speed)){
                      Fx.colorSpark.at(b.x, b.y, b.rotation(), b.type.hitColor);
                    }

                    if (b.time < homingDelay) {
                      b.vel.lerpDelta(0, 0, 0.06f);
                    }
                    else if(homingPower > 0.0001f && b.time >= homingDelay){
                      float realAimX = b.aimX < 0 ? b.x : b.aimX;
                      float realAimY = b.aimY < 0 ? b.y : b.aimY;

                      Teamc target;
                      if(b.aimTile != null && b.aimTile.build != null && b.aimTile.build.team != b.team && collidesGround && !b.hasCollided(b.aimTile.build.id)){
                        target = b.aimTile.build;
                      }else{
                        target = Units.closestTarget(b.team, realAimX, realAimY, homingRange,
                            e -> e != null && e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                            t -> t != null && collidesGround && !b.hasCollided(t.id));
                      }

                      if(target != null){
                        float v = Mathf.lerpDelta(b.vel.len(), speed, 0.08f);
                        b.vel.setLength(v);
                        b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower*(v/speed)*Time.delta*50f));
                      }
                      else{
                        b.vel.lerpDelta(0, 0, 0.06f);
                      }
                    }
                  }

                  @Override
                  public void draw(Bullet b) {
                    super.draw(b);
                    Draw.color(SglDrawConst.matrixNet);
                    Drawf.tri(b.x, b.y, 8, 24, b.rotation());
                    Drawf.tri(b.x, b.y, 8, 10, b.rotation() + 180);

                    Tmp.v1.set(1, 0).setAngle(b.rotation());
                    SglDraw.gapTri(b.x + Tmp.v1.x*8*b.fout(), b.y + Tmp.v1.y*3*b.fout(), 16, 24, 18, b.rotation());
                    SglDraw.gapTri(b.x + Tmp.v1.x*2*b.fout(), b.y - Tmp.v1.y*3*b.fout(), 12, 20, 16, b.rotation());
                    SglDraw.gapTri(b.x - Tmp.v1.x*2*b.fout(), b.y - Tmp.v1.y*5*b.fout(), 8, 14, 10, b.rotation());
                  }

                  public void hitEntity(Bullet b, Hitboxc entity, float health){
                    if(entity instanceof Unit unit && unit.shield > 0){
                      float damageShield = Math.min(Math.max(unit.shield, 0), b.type.damage*1.25f);
                      unit.shield -= damageShield;
                      Fx.colorSparkBig.at(b.x, b.y, b.rotation(), SglDrawConst.matrixNet);
                    }
                    super.hitEntity(b, entity, health);
                  }
                };
              }

              @Override
              public void draw(Unit unit, WeaponMount mount) {
                super.draw(unit, mount);
                Tmp.v1.set(0, y).rotate(unit.rotation - 90);
                float dx = unit.x + Tmp.v1.x;
                float dy = unit.y + Tmp.v1.y;

                Lines.stroke(1.6f*(mount.charging? 1: mount.warmup*(1 - mount.recoil)), SglDrawConst.matrixNet);
                Draw.alpha(0.7f*mount.warmup*(1 - unit.elevation));
                float disX = Angles.trnsx(unit.rotation - 90, 3*mount.warmup, 0);
                float disY = Angles.trnsy(unit.rotation - 90, 3*mount.warmup, 0);

                Tmp.v1.set(0, 720).rotate(unit.rotation - 90);
                float angle = Tmp.v1.angle();
                float distX = Tmp.v1.x;
                float distY = Tmp.v1.y;

                Lines.line(dx + disX, dy + disY, dx + distX + disX, dy + distY + disY);
                Lines.line(dx - disX, dy - disY, dx + distX - disX, dy + distY - disY);
                float step = 1/30f;
                float rel = (1 - mount.reload / reload)*mount.warmup*(1 - unit.elevation);
                for (float i = 0.001f; i <= 1; i += step){
                  Draw.alpha(rel > i? 1: Mathf.maxZero(rel - (i - step))/step);
                  Drawf.tri(dx + distX*i, dy + distY*i, 3, 2.598f, angle);
                }

                Draw.reset();

                Draw.color(SglDrawConst.matrixNet);
                float relLerp = mount.charging? 1: 1 - mount.reload / reload;
                float edge = Math.max(relLerp, mount.recoil*1.25f);
                Lines.stroke(0.8f*edge);
                Draw.z(Layer.bullet);
                SglDraw.dashCircle(dx, dy, 10, 4, 240, Time.time*0.8f);
                Lines.stroke(edge);
                Lines.circle(dx, dy, 8);
                Fill.circle(dx, dy, 5* relLerp);

                SglDraw.drawDiamond(dx, dy, 6 + 12*relLerp, 3*relLerp, Time.time);
                SglDraw.drawDiamond(dx, dy, 5 + 10*relLerp, 2.5f*relLerp, -Time.time*0.87f);
              }

              @Override
              public void update(Unit unit, WeaponMount mount) {
                float axisX = unit.x + Angles.trnsx(unit.rotation - 90,  x, y),
                    axisY = unit.y + Angles.trnsy(unit.rotation - 90,  x, y);

                if (mount.charging) mount.reload = mount.weapon.reload;

                if (unit.isFlying()){
                  mount.targetRotation = Angles.angle(axisX, axisY, mount.aimX, mount.aimY) - unit.rotation;
                  mount.rotation = mount.targetRotation;
                }
                else{
                  mount.rotation = 0;
                }

                if (mount.warmup < 0.01f){
                  mount.reload = Math.max(mount.reload - 0.2f*Time.delta, 0);
                }

                super.update(unit, mount);
              }
            }
        );
      }

      @Override
      public void init() {
        super.init();

        omniMovement = true;
      }
    };

    emptiness = new SglUnitType("emptiness"){
      {
        requirements(
            Items.phaseFabric, 200,
            Items.surgeAlloy, 280,
            SglItems.aerogel, 400,
            SglItems.crystal_FEX_power, 300,
            SglItems.strengthening_alloy, 560,
            SglItems.iridium, 380,
            SglItems.matrix_alloy, 420,
            SglItems.degenerate_neutron_polymer, 420,
            SglItems.anti_metter, 280
        );

        speed = 0.8f;
        accel = 0.065f;
        drag = 0.05f;
        rotateSpeed = 0.8f;
        faceTarget = true;
        health = 82500;
        lowAltitude = true;
        flying = true;
        hitSize = 85;
        targetFlags = BlockFlag.allLogic;

        engineSize = 0;

        setEnginesMirror(

        );

        class MayflyWeapon extends DataWeapon{
          float delay;

          final BulletType subBullet = new BulletType(){
            {
              damage = 80;
              splashDamage = 120;
              splashDamageRadius = 24;
              speed = 3;

              hitShake = 5;

              rangeOverride = 550;

              fragBullet = new LightningBulletType(){{
                lightningLength = 16;
                lightningLengthRand = 8;
                damage = 24;
              }};
              fragBullets = 3;

              hitSize = 3;
              lifetime = 90;
              homingDelay = 20;
              despawnHit = true;
              hitEffect = new MultiEffect(
                  SglFx.explodeImpWave,
                  SglFx.diamondSpark
              );
              hitColor = SglDrawConst.matrixNet;

              homingRange = 620;
              homingPower = 0.05f;

              trailColor = SglDrawConst.matrixNet;
              trailLength = 20;
              trailWidth = 2.4f;
              trailEffect = SglFx.trailParticle;
              trailChance = 0.16f;
            }

            @Override
            public void draw(Bullet b) {
              drawTrail(b);
              Draw.color(hitColor);
              Fill.circle(b.x, b.y, 6f);
              Draw.color(Color.black);
              Fill.circle(b.x, b.y, 3f);
            }

            @Override
            public void updateHoming(Bullet b) {
              if (b.time < homingDelay) return;

              Posc target = Units.closestTarget(b.team, b.x, b.y, homingRange,
                  e -> e != null && e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                  t -> t != null && collidesGround && !b.hasCollided(t.id));

              if (target != null) {
                b.vel.lerpDelta(Tmp.v1.set(target.x() - b.x, target.y() - b.y).setLength(10), homingPower);
              }
            }
          };

          private MayflyWeapon(){
            super(Sgl.modName + "-emptiness_mayfly");
            mirror = true;
            alternate = false;

            rotate = false;
            shootCone = 180;
            reload = 60;
            shootWarmupSpeed = 0.03f;
            linearWarmup = false;
            minWarmup = 0.9f;

            shootSound = Sounds.lasershoot;

            bullet = new BulletType(){
              {
                damage = 320;

                pierceCap = 3;
                pierceBuilding = true;

                fragBullets = 1;
                fragRandomSpread = 0;
                fragAngle = 0;
                fragBullet = new LightLaserBulletType(){
                  {
                    length = 140;
                    damage = 160;
                    empDamage = 41;
                  }

                  @Override
                  public void init(Bullet b, LightningContainer c) {
                    Teamc target = Units.closestTarget(b.team, b.x, b.y, range,
                        e -> e != null && e.checkTarget(collidesAir, collidesGround) && !b.hasCollided(e.id),
                        t -> t != null && collidesGround && !b.hasCollided(t.id));

                    if (target != null) {
                      b.rotation(b.angleTo(target));
                    }

                    super.init(b, c);
                  }
                };

                hitShake = 4;

                shootEffect = new MultiEffect(
                    SglFx.impactBubbleSmall,
                    Fx.colorSparkBig
                );

                hitColor = trailColor = SglDrawConst.matrixNet;
                hitEffect = SglFx.diamondSparkLarge;

                trailEffect = new MultiEffect(SglFx.movingCrystalFrag);
                trailChance = 0.4f;

                despawnHit = true;

                speed = 10;
                lifetime = 120;
                homingDelay = 30;
                homingPower = 0.15f;
                rangeOverride = 500;
                homingRange = 600;

                trailLength = 28;
                trailWidth = 4;
              }

              @Override
              public void draw(Bullet b) {
                super.draw(b);

                float delay = 1 - Mathf.pow(1 - Mathf.clamp(b.time/homingDelay), 2);

                Draw.color(SglDrawConst.matrixNet);
                Fill.circle(b.x, b.y, 6 + 2*delay);
                SglDraw.drawDiamond(b.x, b.y, 24, 8*delay, b.rotation());

                SglDraw.drawTransform(b.x, b.y, 4*delay, 0, b.rotation(), (x, y, r) -> {
                  SglDraw.gapTri(x, y, 12*delay, 16 + 16*delay, 14, r);
                });

                Draw.color(Color.black);
                Fill.circle(b.x, b.y, 5*delay);
              }

              @Override
              public void updateHoming(Bullet b) {
                if (b.time < homingDelay) {
                  b.vel.lerpDelta(0, 0, 0.06f);
                }

                if (Mathf.chanceDelta(0.3f*b.vel.len()/speed)){
                  Fx.colorSparkBig.at(b.x, b.y, b.rotation(), b.type.hitColor);
                }

                if(b.time >= homingDelay){
                  float realAimX = b.aimX < 0 ? b.x : b.aimX;
                  float realAimY = b.aimY < 0 ? b.y : b.aimY;

                  Teamc target;
                  if(b.aimTile != null && b.aimTile.build != null && b.aimTile.build.team != b.team && !b.hasCollided(b.aimTile.build.id)){
                    target = b.aimTile.build;
                  }else{
                    target = Units.closestTarget(b.team, realAimX, realAimY, homingRange,
                        e -> e != null && !b.hasCollided(e.id),
                        t -> t != null && !b.hasCollided(t.id));
                  }

                  if(target != null){
                    float v = Mathf.lerpDelta(b.vel.len(), speed, 0.08f);
                    b.vel.setLength(v);
                    b.vel.setAngle(Angles.moveToward(b.rotation(), b.angleTo(target), homingPower*(v/speed)*Time.delta*50f));
                  }
                  else{
                    b.vel.lerpDelta(0, 0, 0.06f);
                  }

                  if (b.vel.len() >= speed*0.8f){
                    if (b.timer(3, 3)) SglFx.weaveTrail.at(b.x, b.y, b.rotation(), hitColor);
                  }
                }
              }
            };
          }

          @Override
          public void addStats(UnitType u, Table t) {
            super.addStats(u, t);

            Table ic = new Table();
            StatUtils.buildAmmo(ic, subBullet);
            Collapser coll = new Collapser(ic, true);
            coll.setDuration(0.1f);

            t.table(ft -> {
              ft.left().defaults().left();
              ft.add(Core.bundle.format("infos.shots", 2));
              ft.button(Icon.downOpen, Styles.emptyi, () -> coll.toggle(false)).update(i -> i.getStyle().imageUp = (!coll.isCollapsed() ? Icon.upOpen : Icon.downOpen)).size(8).padLeft(16f).expandX();
            });
            t.row();
            t.add(coll).padLeft(16);
          }

          @Override
          public void init(Unit unit, DataWeaponMount mount) {
            super.init(unit, mount);

            MayflyStatus status = new MayflyStatus();
            status.x = unit.x + Angles.trnsx(unit.rotation() - 90, mount.weapon.x, mount.weapon.y);
            status.y = unit.y + Angles.trnsy(unit.rotation() - 90, mount.weapon.x, mount.weapon.y);

            status.rot.set(1, 0).setAngle(unit.rotation() + mount.rotation);

            mount.setVar(STATUS, status);
            mount.setVar(PHASE, Mathf.random(0, 360f));
          }

          @Override
          public void update(Unit unit, DataWeaponMount mount) {
            super.update(unit, mount);

            if (mount.getVar(STATUS) instanceof MayflyStatus stat){
              stat.update(unit, mount);
            }
          }

          @Override
          public void draw(Unit unit, WeaponMount mount) {
            if (mount instanceof DataWeaponMount m && m.getVar(STATUS) instanceof MayflyStatus stat){
              stat.draw(unit, m);
            }
          }

          @Override
          protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
            if (mount instanceof DataWeaponMount m && m.getVar(STATUS) instanceof MayflyStatus stat){
              Time.run(delay, () -> {
                bullet.create(unit, stat.x, stat.y, stat.rot.angle());
                bullet.shootEffect.at(stat.x, stat.y, stat.rot.angle(), bullet.hitColor);

                Time.run(12, () -> {
                  for (int sign : Mathf.signs) {
                    subBullet.create(unit, stat.x, stat.y, stat.rot.angle() + 25*sign);
                  }
                });
              });
            }
          }

          class MayflyStatus{
            float x, y;
            final Vec2 vel = new Vec2(), rot = new Vec2(), tmp1 = new Vec2(), tmp2 = new Vec2();
            final Trail trail1 = new Trail(20), trail2 = new Trail(20);
            final Trail trail = new Trail(28);
            final float off;

            final float[] farg1 = new float[9];
            final float[] farg2 = new float[9];

            {
              off = Mathf.random(0f, 360f);
              for(int d = 0; d < 3; d++){
                farg1[d*3] = Mathf.random(0.5f, 3f)/(d + 1)*(Mathf.randomBoolean()? 1: -1);
                farg1[d*3 + 1] = Mathf.random(0f, 360f);
                farg1[d*3 + 2] = Mathf.random(8f, 16f)/((d + 1)*(d + 1));
              }
              for(int d = 0; d < 3; d++){
                farg2[d*3] = Mathf.random(0.5f, 3f)/(d + 1)*(Mathf.randomBoolean()? 1: -1);
                farg2[d*3 + 1] = Mathf.random(0f, 360f);
                farg2[d*3 + 2] = Mathf.random(8f, 16f)/((d + 1)*(d + 1));
              }
            }

            public void update(Unit unit, DataWeaponMount mount){
              float movX = Angles.trnsx(mount.rotation, 0, 14)*mount.warmup;
              float movY = Angles.trnsy(mount.rotation, 0, 14)*mount.warmup;

              float targetX = unit.x + Angles.trnsx(unit.rotation() - 90, mount.weapon.x + movX, mount.weapon.y + movY);
              float targetY = unit.y + Angles.trnsy(unit.rotation() - 90, mount.weapon.x + movX, mount.weapon.y + movY);

              if (Sgl.config.animateLevel < 2){
                x = targetX;
                y = targetY;
                rot.set(1, 0).setAngle(unit.rotation() + mount.rotation);

                trail.clear();
                trail1.clear();
                trail2.clear();
                return;
              }

              if (Mathf.chanceDelta(0.03f*mount.warmup)){
                float dx = unit.x + Angles.trnsx(unit.rotation, -28, 0) - x;
                float dy = unit.y + Angles.trnsy(unit.rotation, -28, 0) - y;

                float dst = Mathf.dst(dx, dy);
                float ang = Mathf.angle(dx, dy);

                Tmp.v1.rnd(3);
                SglFx.moveParticle.at(x + Tmp.v1.x, y + Tmp.v1.y, ang, SglDrawConst.matrixNet, dst);
              }

              float dx = targetX - x;
              float dy = targetY - y;

              float dst = Mathf.len(dx, dy);
              Tmp.v1.set(1, 0).setAngle(unit.rotation() + mount.rotation);

              rot.lerpDelta(Tmp.v1, 0.05f);
              float speed = 2*(dst/24);

              vel.lerpDelta(Tmp.v1.set(dx, dy).setLength(speed).add(Tmp.v2.set(0.12f, 0).setAngle(Time.time*(mount.weapon.x > 0? 1: -1) + mount.getVar(PHASE, 0f))), 0.075f);

              x += vel.x*Time.delta;
              y += vel.y*Time.delta;

              tmp1.set(MathTransform.fourierSeries(Time.time, farg1)).scl(mount.warmup);
              tmp2.set(MathTransform.fourierSeries(Time.time, farg2)).scl(mount.warmup);

              trail.update(x, y);
              trail1.update(x + tmp1.x, y + tmp1.y);
              trail2.update(x + tmp2.x, y + tmp2.y);
            }

            public void draw(Unit unit, DataWeaponMount mount){
              float angle = rot.angle() - 90;
              Draw.rect(mount.weapon.region, x, y, angle);

              SglDraw.drawBloomUnderFlyUnit(() -> {
                trail.draw(SglDrawConst.matrixNet, 4);
                Draw.color(Color.black);
                Fill.circle(x, y, 4);
                Draw.reset();
              });

              float z = Draw.z();
              Draw.z(Layer.effect);

              Draw.color(SglDrawConst.matrixNet);

              Draw.draw(Draw.z(), () -> {
                float dx = Angles.trnsx(unit.rotation, -28, 0);
                float dy = Angles.trnsy(unit.rotation, -28, 0);
                MathRenderer.setDispersion((0.2f + Mathf.absin(Time.time/3f + off, 6, 0.4f))*mount.warmup);
                MathRenderer.setThreshold(0.3f, 0.8f);
                MathRenderer.drawSin(x, y, 3, unit.x + dx, unit.y + dy, 5, 120, -3*Time.time + off);
              });

              trail1.draw(SglDrawConst.matrixNet, 3*mount.warmup);
              trail2.draw(SglDrawConst.matrixNet, 3*mount.warmup);

              Draw.color(SglDrawConst.matrixNet);
              Fill.circle(x + tmp1.x, y + tmp1.y, 4f);
              Fill.circle(x + tmp2.x, y + tmp2.y, 4f);

              SglDraw.drawDiamond(x, y, 24, 10, angle);
              SglDraw.drawTransform(x, y, 0, 12, angle, (x, y, r) -> {
                SglDraw.gapTri(x, y, 12*mount.warmup, 22 + 24*mount.warmup, 8, r + 90);
              });
              SglDraw.drawTransform(x, y, 0, 10, angle - 180, (x, y, r) -> {
                SglDraw.gapTri(x, y, 9*mount.warmup, 12 + 8*mount.warmup, 6, r + 90);
              });

              Fill.circle(x, y, 6);
              Draw.color(Color.black);
              Fill.circle(x, y, 4);
              Draw.reset();

              Draw.z(Math.min(Layer.darkness, z - 1f));
              float e = Mathf.clamp(unit.elevation, shadowElevation, 1f) * shadowElevationScl * (1f - unit.drownTime);
              float x = this.x + shadowTX * e, y = this.y + shadowTY * e;
              Floor floor = world.floorWorld(x, y);

              float dest = floor.canShadow ? 1f : 0f;
              unit.shadowAlpha = unit.shadowAlpha < 0 ? dest : Mathf.approachDelta(unit.shadowAlpha, dest, 0.11f);
              Draw.color(Pal.shadow, Pal.shadow.a * unit.shadowAlpha);

              Draw.rect(mount.weapon.region, this.x + shadowTX*e, this.y + shadowTY*e, angle);
              Draw.color();
              Draw.z(z);
            }
          }
        }

        BulletType turretBullet = new EmpBulletType(){
          {
            damage = 420;
            empDamage = 37;

            pierceCap = 4;
            pierceBuilding = true;
            laserAbsorb = true;

            speed = 16;
            lifetime = 35;

            hitSize = 6;

            trailEffect = new MultiEffect(
                SglFx.trailLineLong,
                Fx.colorSparkBig
            );
            trailChance = 1;
            trailRotation = true;

            hitSound = Sounds.spark;

            hitEffect = Fx.circleColorSpark;
            hitColor = SglDrawConst.matrixNet;

            shootEffect = Fx.circleColorSpark;

            despawnHit = true;

            trailLength = 38;
            trailWidth = 4;
            trailColor = SglDrawConst.matrixNet;
          }

          @Override
          public void init(Bullet b) {
            super.init(b);
            TrailMoveLightning l = Pools.obtain(TrailMoveLightning.class, TrailMoveLightning::new);
            l.chance = 0.5f;
            l.maxOff = 6;
            l.range = 12;
            b.data = l;
          }

          @Override
          public void updateTrail(Bullet b) {
            if(!headless && trailLength > 0){
              if(b.trail == null){
                b.trail = new Trail(trailLength);
              }
              b.trail.length = trailLength;

              if (!(b.data instanceof TrailMoveLightning m)) return;
              m.update();
              SglDraw.drawTransform(b.x, b.y, 0, m.off, b.rotation(), (x, y, r) -> b.trail.update(x, y));
            }
          }

          @Override
          public void removed(Bullet b) {
            super.removed(b);
            if (b.data instanceof TrailMoveLightning){
              Pools.free(b.data);
            }
          }
        };
        weapons.addAll(
            new SglWeapon(Sgl.modName + "-emptiness_turret"){
              {
                x = 17f;
                y = 26.5f;
                rotate = true;
                shootCone = 6;
                rotateSpeed = 5f;
                recoilTime = 45;
                recoil = 6;

                shake = 4;

                reload = 30;
                shootSound = Sounds.spark;

                bullet = turretBullet;
              }
            },
            new SglWeapon(Sgl.modName + "-emptiness_turret"){
              {
                x = 22f;
                y = -1f;
                rotate = true;
                shootCone = 6;
                rotateSpeed = 5f;
                recoilTime = 45;
                recoil = 6;

                shake = 4;

                reload = 30;
                shootSound = Sounds.spark;

                bullet = turretBullet;
              }
            },
            new SglWeapon(Sgl.modName + "-emptiness_cannon"){
              {
                x = 27f;
                y = -35f;
                rotate = true;
                shootCone = 6;
                rotateSpeed = 3.5f;
                recoilTime = 60;
                recoil = 6;

                shootSound = Sounds.plasmaboom;

                shake = 5;

                reload = 60;

                bullet = new MultiTrailBulletType(){
                  {
                    damage = 60;
                    splashDamage = 560;
                    splashDamageRadius = 18;

                    pierceCap = 5;
                    pierceBuilding = true;

                    hitEffect = new MultiEffect(
                        SglFx.diamondSparkLarge,
                        SglFx.spreadSparkLarge
                    );
                    despawnEffect = SglFx.explodeImpWaveSmall;

                    hitShake = 6;
                    hitSound = Sounds.spark;

                    speed = 10;
                    lifetime = 60;
                    trailEffect = new MultiEffect(
                        Fx.colorSparkBig,
                        SglFx.movingCrystalFrag,
                        SglFx.polyParticle
                    );
                    trailChance = 0.3f;
                    trailColor = SglDrawConst.matrixNet;
                    trailRotation = true;

                    shootEffect = SglFx.shootRail;
                    smokeEffect = Fx.shootSmokeSmite;
                    hitColor = SglDrawConst.matrixNet;

                    trailLength = 34;
                    trailWidth = 4;
                    hitSize = 6;
                  }

                  @Override
                  public void draw(Bullet b) {
                    super.draw(b);

                    Draw.color(hitColor);
                    SglDraw.gapTri(b.x, b.y, 12, 28, -10, b.rotation());
                  }
                };
              }
            },
            new MayflyWeapon(){{
              x = 58.5f;
              y = -13.75f;
              baseRotation = -45;
            }},
            new MayflyWeapon(){{
              x = 57.5f;
              y = -37.75f;
              baseRotation = -90;

              delay = 20;
            }},
            new MayflyWeapon() {{
              x = 52.5f;
              y = -65.75f;
              baseRotation = -135;

              delay = 40;
            }},
            new SglWeapon(Sgl.modName + "-lightedge"){
              {
                x = 0;
                y = -28f;
                mirror = false;
                recoil = 0;

                targetSwitchInterval = 80;

                shootSound = Sounds.laserblast;

                reload = 750;
                cooldownTime = 30;

                minWarmup = 0.95f;
                linearWarmup = false;
                shootWarmupSpeed = 0.014f;

                class BlastLaser extends singularity.world.blocks.turrets.LightningBulletType {
                  float blastDelay = 24;
                  float damageInterval = 5;
                  float laserShake = 5, damageShake = 12;
                  Effect laserEffect = Fx.none;
                  Sound laserSound = Sounds.laserbig;
                  boolean blackZone = true;

                  {
                    collides = false;
                    hittable = false;
                    absorbable = false;
                    pierce = true;
                    pierceBuilding = true;
                    pierceArmor = true;

                    fragOnHit = true;//仅用于禁用despawnen时生成子弹

                    keepVelocity = false;

                    speed = 0;

                    hitColor = SglDrawConst.matrixNet;
                  }

                  static final VectorLightningGenerator gen = new VectorLightningGenerator();

                  @Override
                  public float continuousDamage() {
                    return damage*(60/damageInterval);
                  }

                  @Override
                  public void init() {
                    super.init();
                    drawSize = range;
                  }

                  @Override
                  public void update(Bullet b) {
                    super.update(b);

                    Effect.shake(laserShake, laserShake, b);
                    if (b.timer(1, damageInterval)){
                      Damage.collideLaser(b, Mathf.len(b.aimX - b.x, b.aimY - b.y)*Mathf.clamp(b.time/blastDelay), true, false, -1);
                    }
                  }

                  @Override
                  public void hit(Bullet b, float x, float y){
                    hitEffect.at(x, y, b.rotation(), hitColor);
                    hitSound.at(x, y, hitSoundPitch, hitSoundVolume);

                    Effect.shake(hitShake, hitShake, b);
                  }

                  @Override
                  public void init(Bullet b, LightningContainer cont) {
                    gen.maxSpread = hitSize*2.3f;
                    gen.minInterval = 2f*hitSize;
                    gen.maxInterval = 3.8f*hitSize;

                    gen.vector.set(b.aimX - b.x, b.aimY - b.y).limit(range);
                    b.aimX = b.x + gen.vector.x;
                    b.aimY = b.y + gen.vector.y;
                    cont.lerp = Interp.pow4Out;
                    cont.lifeTime = lifetime;
                    cont.time = blastDelay;
                    cont.maxWidth = 5;
                    cont.minWidth = 3;

                    float ax = b.aimX, ay = b.aimY;

                    for (int i = 0; i < 4; i++) {
                      cont.create(gen);
                    }
                    Time.run(blastDelay, () -> {
                      Effect.shake(damageShake, damageShake, b);
                      createSplashDamage(b, ax, ay);
                      laserSound.at(ax, ay);
                      laserEffect.at(ax, ay, b.rotation(), hitColor);
                      createFrags(b, b.aimX, b.aimY);
                    });
                  }

                  @Override
                  public void createFrags(Bullet b, float x, float y) {
                    if (fragBullet != null && fragBullets > 0) {
                      Unit[] arr = SglUnitSorts.findEnemies(fragBullets, b, fragBullet.range, (us, u) -> {
                        if (b.dst(u) < fragBullet.splashDamageRadius) return false;

                        for (Unit e : us) {
                          if (e == null) break;

                          if (e.dst(u) < fragBullet.splashDamageRadius) return false;
                        }

                        return true;
                      }, UnitSorts.farthest);
                      for (int i = 0; i < arr.length; i++) {
                        if (arr[i] != null) {
                          Tmp.v1.set(arr[i].x - x, arr[i].y - y);

                          fragBullet.create(b.owner, b.team, x, y, Tmp.v1.angle(),
                              fragBullet.damage, 1, 1, null, null,
                              x + Tmp.v1.x, y + Tmp.v1.y
                          );
                        }
                        else {
                          float a = b.rotation() + Mathf.range(fragRandomSpread/2) + fragAngle + ((i - fragBullets/2f)*fragSpread);

                          Tmp.v1.set(fragBullet.range*Mathf.random(0.6f, 1f), 0).setAngle(a);
                          fragBullet.create(b.owner, b.team, x, y, a,
                              fragBullet.damage, 1, 1, null, null,
                              x + Tmp.v1.x, y + Tmp.v1.y
                          );
                        }
                      }
                    }
                  }

                  @Override
                  public void draw(Bullet b) {
                    float in = Mathf.clamp(b.time/blastDelay);
                    Tmp.v1.set(b.aimX - b.x, b.aimY - b.y).scl(in);

                    float dx = b.x + Tmp.v1.x;
                    float dy = b.y + Tmp.v1.y;

                    Draw.color(hitColor);
                    float fout = b.fout(Interp.pow3Out);
                    Fill.circle(dx, dy, hitSize*1.6f*fout);
                    Lines.stroke(hitSize*(1 + in)*fout);
                    Lines.line(b.x, b.y, dx, dy);

                    Lines.stroke(hitSize*0.1f*fout);
                    Lines.circle(dx, dy, hitSize*1.9f*fout);

                    Draw.color(Color.white);
                    Fill.circle(dx, dy, hitSize*1.2f*fout);
                    Lines.stroke(hitSize*(1 + in)*fout*0.75f);
                    Lines.line(b.x, b.y, dx, dy);

                    if (blackZone) {
                      float z = Draw.z();
                      Draw.z(z + 0.0001f);
                      Draw.color(Color.black);
                      Fill.circle(dx, dy, hitSize*0.6f*fout);
                      Lines.stroke(hitSize*(1 + in)*fout/2.8f);
                      Lines.line(b.x, b.y, dx, dy);
                      Draw.z(z);
                    }

                    Draw.color(hitColor);
                    Fill.circle(b.x, b.y, hitSize*(1.2f + in)*fout);

                    super.draw(b);
                  }
                }

                bullet = new BlastLaser(){
                  {
                    damage = 160;
                    damageInterval = 5;

                    rangeOverride = 600;
                    splashDamage = 3280;
                    splashDamageRadius = 120;
                    lifetime = 240;
                    hitSize = 12;

                    laserEffect = new MultiEffect(
                        SglFx.laserBlastWeaveLarge,
                        SglFx.circleSparkLarge,
                        SglFx.impactBubbleBig
                    );
                    shootEffect = new MultiEffect(
                        SglFx.shootCrossLightLarge,
                        SglFx.explodeImpWaveBig,
                        SglFx.impactWaveBig,
                        SglFx.impactBubble
                    );
                    hitEffect = new MultiEffect(
                        Fx.colorSparkBig,
                        SglFx.diamondSparkLarge
                    );

                    hitColor = SglDrawConst.matrixNet;

                    fragBullets = 3;
                    fragSpread = 120;
                    fragRandomSpread = 72;
                    fragBullet = new BlastLaser(){
                      {
                        damage = 120;
                        damageInterval = 5;

                        rangeOverride = 360;
                        splashDamage = 1400;
                        splashDamageRadius = 60;
                        lifetime = 186;
                        hitSize = 9;

                        hitEffect = new MultiEffect(
                            Fx.circleColorSpark,
                            SglFx.diamondSparkLarge
                        );

                        blackZone = false;

                        laserEffect = SglFx.explodeImpWaveLaserBlase;

                        final RandomGenerator branch = new RandomGenerator();
                        RandomGenerator g = new RandomGenerator(){{
                          maxLength = 140;
                          maxDeflect = 55;

                          branchChance = 0.2f;
                          minBranchStrength = 0.8f;
                          maxBranchStrength = 1;
                          branchMaker = (vert, strength) -> {
                            branch.maxLength = 60*strength;
                            branch.originAngle = vert.angle + Mathf.random(-90, 90);

                            return branch;
                          };
                        }};

                        fragBullets = 8;
                        fragBullet = SglTurrets.lightning(108, 32, 62, 5.2f, SglDrawConst.matrixNet, b -> {
                          g.originAngle = b.rotation();
                          return g;
                        });
                        fragBullet.rangeOverride = 120;
                      }
                    };
                  }

                  @Override
                  public void createSplashDamage(Bullet b, float x, float y) {
                    super.createSplashDamage(b, x, y);

                    Angles.randLenVectors(System.nanoTime(), Mathf.random(15, 22), 4, 6.5f,
                        (dx, dy) -> SglParticleModels.floatParticle.create(x, y, hitColor, dx, dy, Mathf.random(5.25f, 7f)));
                  }
                };

                parts.addAll(
                    new CustomPart(){{
                      layer = Layer.effect;
                      progress = PartProgress.warmup;

                      draw = (x, y, r, p) -> {
                        Draw.color(SglDrawConst.matrixNet);

                        float dx = Angles.trnsx(r, 1, 0);
                        float dy = Angles.trnsy(r, 1, 0);

                        for (int i = 0; i < 4; i++) {
                          int len = 20 + i*25 - (i%2)*6;
                          float rx = x + dx*len;
                          float ry = y + dy*len;

                          SglDraw.gapTri(rx, ry, Mathf.absin(Time.time/4 - i*Mathf.pi, 1, (10 - 2*i)*p), (10 + (6 + (i%2)*6)*p) - i, (i%2 == 0? -1: 1)*(5 + 4*p - i), r);
                        }

                        SglDraw.drawDiamond(x, y, 44 + 20*p, 8 + 4*p, Time.time);
                        SglDraw.drawDiamond(x, y, 38 + 14*p, 6 + 4*p, -Time.time*1.2f);
                        SglDraw.drawDiamond(x, y, 32 + 8*p, 4 + 3*p, Time.time*1.3f);
                        Fill.circle(x, y, 12f);
                        Draw.color(Color.white);
                        Fill.circle(x, y, 9f);
                        Draw.color(Color.black);
                        Fill.circle(x, y, 7.5f);
                      };
                    }}
                );
              }

              @Override
              protected Teamc findTarget(Unit unit, float x, float y, float range, boolean air, boolean ground) {
                return Units.bestTarget(unit.team, x, y, range, u -> unit.checkTarget(air, ground), t -> ground, SglUnitSorts.denser);
              }

              @Override
              public void draw(Unit unit, WeaponMount mount) {
                float x = unit.x + Angles.trnsx(unit.rotation() - 90, mount.weapon.x, mount.weapon.y);
                float y = unit.y + Angles.trnsy(unit.rotation() - 90, mount.weapon.x, mount.weapon.y);

                float angle = Mathf.angle(mount.aimX - x, mount.aimY - y);
                float dst = Mathf.dst(mount.aimX - x, mount.aimY - y);

                float angDiff = MathTransform.innerAngle(angle, unit.rotation());

                float lerp = Mathf.clamp((18 - Math.abs(angDiff))/18f)*Mathf.clamp(mount.warmup - 0.05f);
                float stLerp = lerp*(1f - Mathf.clamp((dst - 500f)/100f));

                float z = Draw.z();
                Draw.z(Layer.effect);
                Lines.stroke(4f*stLerp*Mathf.clamp(1 - mount.reload/mount.weapon.reload), unit.team.color);
                Lines.line(x, y, mount.aimX, mount.aimY);
                Lines.square(mount.aimX, mount.aimY, 18, 45);

                float l = Math.max(Mathf.clamp(mount.warmup/mount.weapon.minWarmup)*Mathf.clamp(1 - mount.reload/mount.weapon.reload), mount.heat);
                Lines.stroke(4f*l*stLerp);
                SglDraw.arc(mount.aimX, mount.aimY, 62, 360*l, -Time.time*1.2f);

                Lines.stroke(4f*Mathf.clamp(mount.warmup/mount.weapon.minWarmup), SglDrawConst.matrixNet);
                SglDraw.drawCornerTri(mount.aimX, mount.aimY, 46, 8, MathTransform.gradientRotateDeg(Time.time*0.85f, 38, 1/3f, 3), true);

                for (int i = 0; i < 3; i++) {
                  SglDraw.drawTransform(mount.aimX, mount.aimY, 54, 0, -1.4f*Time.time + i*120, (rx, ry, r) -> {
                    Draw.rect(((TextureRegionDrawable) SglDrawConst.matrixArrow).getRegion(), rx, ry, 12*stLerp, 12*stLerp, r + 90);
                  });
                }

                Draw.z(z);

                super.draw(unit, mount);
              }
            }
        );
      }

      @Override
      public void load() {
        super.load();
        shadowRegion = region;
      }
    };

    unstable_energy_body = new SglUnitType("unstable_energy_body"){
      public static final float FULL_SIZE_ENERGY = 3680;

      {
        Events.on(EventType.ClientLoadEvent.class, e -> {
          //immunities.addAll(content.statusEffects());
          Sgl.empHealth.setEmpDisabled(this);
        });

        isEnemy = false;

        health = 10;
        hidden = true;
        hitSize = 32;
        playerControllable = false;
        createWreck = false;
        createScorch = false;
        logicControllable = false;
        useUnitCap = false;

        aiController = () -> new UnitController(){
          @Override
          public void unit(Unit unit) {
            //no ai
          }

          @Override
          public Unit unit() {
            // no ai
            return null;
          }
        };
      }

      final CircleGenerator generator = new CircleGenerator();

      final ShrinkGenerator linGen = new ShrinkGenerator(){{
        minInterval = 2.8f;
        maxInterval = 4f;
        maxSpread = 4f;
      }};

      @Override
      public Unit create(Team team) {
        SglUnitEntity res = (SglUnitEntity) super.create(team);

        res.setVar("controlTime", Time.time);

        return res;
      }

      @Override
      public void init(SglUnitEntity unit) {
        LightningContainer cont = new LightningContainer();
        cont.time = 0;
        cont.lifeTime = 18;
        cont.minWidth = 0.8f;
        cont.maxWidth = 1.8f;
        unit.setVar("lightnings", cont);

        LightningContainer lin = new LightningContainer();
        lin.headClose = true;
        lin.endClose = true;
        lin.time = 12;
        lin.lifeTime = 22;
        lin.minWidth = 1.2f;
        lin.maxWidth = 2.4f;
        unit.setVar("lin", lin);
      }

      @Override
      public void update(Unit u) {
        SglUnitEntity unit = (SglUnitEntity) u;

        super.update(unit);

        LightningContainer lightnings = unit.getVar("lightnings");
        LightningContainer lin = unit.getVar("lin");
        if (Mathf.chanceDelta(0.08f)){
          generator.radius = hitSize*Math.min(unit.health/FULL_SIZE_ENERGY, 2);
          generator.minInterval = 4.5f;
          generator.maxInterval = 6.5f;
          generator.maxSpread = 5f;
          lightnings.create(generator);

          Angles.randLenVectors(System.nanoTime(), 1, 1.8f, 2.75f,
              (x, y) -> SglParticleModels.floatParticle.create(u.x, u.y, Pal.reactorPurple, x, y, Mathf.random(3.55f, 4.25f))
                  .setVar(RandDeflectParticle.STRENGTH, 0.22f));
        }

        if (Mathf.chanceDelta(0.1f)){
          linGen.minRange = linGen.maxRange = hitSize*Math.min(unit.health/FULL_SIZE_ENERGY, 2);
          int n = Mathf.random(1, 3);
          for (int i = 0; i < n; i++) {
            lin.create(linGen);
          }
        }

        if (unit.handleVar("timer", (float t) -> t - Time.delta, 15f) <= 0){
          unit.setVar("timer", 12f);
          generator.minInterval = 3.5f;
          generator.maxInterval = 4.5f;
          generator.maxSpread = 4f;
          generator.radius = hitSize*Math.min(unit.health/FULL_SIZE_ENERGY, 2)/2;
          lightnings.create(generator);
        }

        lightnings.update();
        lin.update();

        unit.hitSize = hitSize*Math.min(unit.health/FULL_SIZE_ENERGY, 2);
        float controlTime = 900 - Time.time + unit.getVar("controlTime", 0f);
        if (controlTime <= 0){
          if (unit.health >= 1280){
            Effect.shake(8f, 120f, u.x, u.y);
            Damage.damage(u.x, u.y, unit.hitSize*5, unit.health/FULL_SIZE_ENERGY*4680);

            Sounds.largeExplosion.at(u.x, u.y, 0.8f, 3.5f);

            SglFx.reactorExplode.at(u.x, u.y, 0, unit.hitSize*5);
            Angles.randLenVectors(System.nanoTime(), Mathf.random(20, 34), 2.8f, 6.5f, (x, y) -> {
              float len = Tmp.v1.set(x, y).len();
              SglParticleModels.floatParticle.create(u.x, u.y, Pal.reactorPurple, x, y, Mathf.random(5f, 7f)*((len - 3)/4.5f));
            });
          }

          unit.kill();
        }
        else if (controlTime <= 300){
          float bullTime = unit.handleVar("bullTime", (float f) -> f - Time.delta, 0f);
          if (bullTime <= 0){
            SglTurrets.spilloverEnergy.create(u, u.team, u.x, u.y, Mathf.random(0, 360f), Mathf.random(0.5f, 1));
            unit.health -= 180;
            unit.setVar("bullTime", Math.max(controlTime/10, 2));
          }
          
          if (Mathf.chanceDelta(1 - controlTime/300)) {
            float lerp = (900 - Time.time + unit.getVar("controlTime", 0f))/900;
            Tmp.v1.rnd(Mathf.random(u.hitSize/(3 - lerp), Math.max(u.hitSize/(2.5f - lerp), 15)));
            SglFx.impWave.at(u.x + Tmp.v1.x, u.y + Tmp.v1.y);
          }
        }
      }

      @Override
      public void draw(Unit u) {
        SglUnitEntity unit = (SglUnitEntity) u;

        Draw.z(Layer.effect);

        float radius = u.hitSize;
        float lerp = (900 - Time.time + unit.getVar("controlTime", 0f))/900;
        float lerpStart = Mathf.clamp((1 - lerp)/0.1f);
        float lerpEnd = Interp.pow3Out.apply(Mathf.clamp(lerp/0.2f));

        Lines.stroke(radius*0.055f*lerpStart, Pal.reactorPurple);
        Lines.circle(u.x, u.y, radius*lerpEnd + radius*Interp.pow2In.apply(1 - lerpStart));

        Draw.draw(Draw.z(), () -> {
          MathRenderer.setThreshold(0.4f, 0.7f);
          MathRenderer.setDispersion(lerpStart*1.2f);
          Draw.color(Pal.reactorPurple);
          MathRenderer.drawCurveCircle(u.x, u.y, radius*0.7f + radius*Interp.pow2In.apply(1 - lerpStart), 3, radius*0.6f, Time.time*1.2f);
          MathRenderer.setDispersion(lerpStart);
          Draw.color(SglDrawConst.matrixNet);
          MathRenderer.drawCurveCircle(u.x, u.y, radius*0.72f + radius*Interp.pow2In.apply(1 - lerpStart), 4, radius*0.67f, Time.time*1.6f);
        });

        Draw.color(SglDrawConst.matrixNet);
        Fill.circle(u.x, u.y, radius/(2.4f - lerp)*Interp.pow2Out.apply(lerpStart)*lerpEnd);
        Lines.stroke(lerp);
        Lines.circle(u.x, u.y, radius*1.2f*lerpEnd);
        unit.<LightningContainer>getVar("lightnings").draw(u.x, u.y);
        unit.<LightningContainer>getVar("lin").draw(u.x, u.y);

        Draw.color(Color.white);
        Fill.circle(u.x, u.y, Mathf.maxZero(radius/(2.6f - lerp))*Interp.pow2Out.apply(lerpStart)*lerpEnd);
      }

      @Override
      public void read(SglUnitEntity sglUnitEntity, Reads read, int revision) {
        sglUnitEntity.getVar("controlTime", Time.time + read.f());
      }

      @Override
      public void write(SglUnitEntity sglUnitEntity, Writes write) {
        write.f(Time.time - sglUnitEntity.getVar("controlTime", 0f));
      }
    };

    cstr_1 = new SglUnitFactory("cstr_1"){{
      requirements(Category.units, ItemStack.with(
          Items.silicon, 120,
          Items.graphite, 160,
          Items.thorium, 90,
          SglItems.aluminium, 120,
          SglItems.strengthening_alloy, 135
      ));
      size = 5;
      liquidCapacity = 240;

      energyCapacity = 256;
      basicPotentialEnergy = 256;

      consCustom = (u, c) -> {
        c.power(Mathf.round(u.health/u.hitSize)*0.02f).showIcon = true;
      };

      sizeLimit = 24;
      healthLimit = 7200;
      machineLevel = 4;

      newBooster(1.5f);
      consume.liquid(Liquids.cryofluid, 2.4f);
      newBooster(1.8f);
      consume.liquid(SglLiquids.FEX_liquid, 2f);
    }};

    cstr_2 = new HoveringUnitFactory("cstr_2"){{
      requirements(Category.units, ItemStack.with(
          Items.silicon, 180,
          Items.surgeAlloy, 160,
          Items.phaseFabric, 190,
          SglItems.aluminium, 200,
          SglItems.aerogel, 120,
          SglItems.strengthening_alloy, 215,
          SglItems.matrix_alloy, 180,
          SglItems.crystal_FEX, 140,
          SglItems.iridium, 100
      ));
      size = 7;
      liquidCapacity = 280;
      energyCapacity = 1024;
      basicPotentialEnergy = 1024;

      payloadSpeed = 1;

      consCustom = (u, c) -> {
        c.power(Mathf.round(u.health/u.hitSize)*0.02f).showIcon = true;
        if (u.hitSize >= 38) c.energy(u.hitSize/24);
      };

      matrixDistributeOnly = true;

      sizeLimit = 68;
      healthLimit = 43000;
      machineLevel = 6;
      timeMultiplier = 18;
      baseTimeScl = 0.25f;

      outputRange = 340;

      hoverMoveMinRadius = 36;
      hoverMoveMaxRadius = 72;
      defHoverRadius = 23.5f;

      laserOffY = 2;

      newBooster(1.6f);
      consume.liquid(Liquids.cryofluid, 3.2f);
      newBooster(1.9f);
      consume.liquid(SglLiquids.FEX_liquid, 2.6f);
      newBooster(2.4f);
      consume.liquid(SglLiquids.phase_FEX_liquid, 2.6f);

      draw = new DrawMulti(
          new DrawDefault(),
          new DrawBlock() {
            @Override
            public void draw(Building build) {
              HoveringUnitFactoryBuild b = (HoveringUnitFactoryBuild) build;
              Draw.z(Layer.effect);
              Draw.color(b.team.color);

              Lines.stroke(2*b.warmup());

              Lines.circle(b.x, b.y, 12*b.warmup());
              Lines.square(b.x, b.y, size*tilesize, Time.time*1.25f);
              Lines.square(b.x, b.y, 32, Time.time*3.25f);

              Draw.color(Pal.reactorPurple);
              Lines.square(b.x, b.y, 28, -MathTransform.gradientRotateDeg(Time.time, 0) + 45f);

              for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 3; j++) {
                  float lerp = b.warmup()*(1 - Mathf.clamp((Time.time + 30*j)%90/70));
                  SglDraw.drawTransform(b.x, b.y, 40 + j*18, 0, i*90 + 45, (rx, ry, r) -> {
                    Draw.rect(((TextureRegionDrawable) SglDrawConst.matrixArrow).getRegion(), rx, ry, 15*lerp, 15*lerp, r + 90);
                  });
                }
              }
            }
          }
      );
    }};

    cstr_3 = new HoveringUnitFactory("cstr_3"){{
      requirements(Category.units, ItemStack.with(
          Items.silicon, 240,
          Items.surgeAlloy, 240,
          Items.phaseFabric, 200,
          SglItems.strengthening_alloy, 280,
          SglItems.matrix_alloy, 280,
          SglItems.crystal_FEX, 200,
          SglItems.crystal_FEX_power, 160,
          SglItems.iridium, 150,
          SglItems.degenerate_neutron_polymer, 100
      ));
      size = 9;
      liquidCapacity = 420;
      energyCapacity = 4096;
      basicPotentialEnergy = 4096;

      payloadSpeed = 1.2f;

      consCustom = (u, c) -> {
        c.power(Mathf.round(u.health/u.hitSize)*0.02f).showIcon = true;
        if (u.hitSize >= 38) c.energy(u.hitSize/24);
      };

      matrixDistributeOnly = true;

      sizeLimit = 120;
      healthLimit = 126000;
      machineLevel = 8;
      timeMultiplier = 16;
      baseTimeScl = 0.22f;

      beamWidth = 0.8f;
      pulseRadius = 5f;
      pulseStroke = 1.7f;

      outputRange = 420;

      hoverMoveMinRadius = 48;
      hoverMoveMaxRadius = 98;
      defHoverRadius = 29f;

      laserOffY = 4;

      newBooster(1.6f);
      consume.liquid(Liquids.cryofluid, 4f);
      newBooster(1.9f);
      consume.liquid(SglLiquids.FEX_liquid, 3.8f);
      newBooster(2.4f);
      consume.liquid(SglLiquids.phase_FEX_liquid, 3.8f);

      draw = new DrawMulti(
          new DrawDefault(),
          new DrawBlock() {
            @Override
            public void draw(Building build) {
              HoveringUnitFactoryBuild b = (HoveringUnitFactoryBuild) build;
              Draw.z(Layer.effect);
              Draw.color(b.team.color);

              Lines.stroke(2.2f*b.warmup());

              Lines.circle(b.x, b.y, 18*b.warmup());
              Lines.square(b.x, b.y, size*tilesize, Time.time*1.25f);
              SglDraw.drawCornerTri(b.x, b.y, 58, 14, Time.time*3.5f, true);

              Draw.color(Pal.reactorPurple);
              Lines.square(b.x, b.y, 34, -Time.time*2.6f);
              SglDraw.drawCornerTri(b.x, b.y, 36, 8, -MathTransform.gradientRotateDeg(Time.time, 0, 3) + 60, true);

              for (int i = 0; i < 4; i++) {
                Draw.color(Pal.reactorPurple);
                for (int j = 0; j < 4; j++) {
                  float lerp = b.warmup()*(1 - Mathf.clamp((Time.time + 30*j)%120/85f));
                  SglDraw.drawTransform(b.x, b.y, 50 + j*20, 0, i*90 + 45, (rx, ry, r) -> {
                    Draw.rect(((TextureRegionDrawable) SglDrawConst.matrixArrow).getRegion(), rx, ry, 16*lerp, 16*lerp, r + 90);
                  });
                }

                Draw.color(b.team.color);
                for (int j = 0; j < 3; j++) {
                  float lerp = b.warmup()*(1 - Mathf.clamp((Time.time + 24*j)%72/60f));
                  SglDraw.drawTransform(b.x, b.y, 40 + j*20, 0, i*90 + 45, (rx, ry, r) -> {
                    Tmp.v1.set(18, 0).setAngle(r + 90);

                    Lines.stroke(2*lerp);
                    Lines.square(rx + Tmp.v1.x, ry + Tmp.v1.y, 6*lerp, r + 45);
                    Lines.square(rx - Tmp.v1.x, ry - Tmp.v1.y, 6*lerp, r + 45);
                  });
                }
              }
            }
          }
      );
    }};
  }

  static class EdgeFragBullet extends BulletType{
    {
      damage = 80;
      splashDamage = 40;
      splashDamageRadius = 24;
      speed = 4;
      hitSize = 3;
      lifetime = 120;
      despawnHit = true;
      hitEffect = SglFx.diamondSpark;
      hitColor = SglDrawConst.matrixNet;

      collidesTiles = false;

      homingRange = 160;
      homingPower = 0.075f;

      trailColor = SglDrawConst.matrixNet;
      trailLength = 25;
      trailWidth = 3f;
    }

    @Override
    public void draw(Bullet b) {
      super.draw(b);
      SglDraw.drawDiamond(b.x, b.y, 10, 4, b.rotation());
    }

    @Override
    public void update(Bullet b) {
      super.update(b);

      b.vel.lerpDelta(Vec2.ZERO, 0.04f);
    }
  }

  static class TrailMoveLightning implements Pool.Poolable {
    float off;
    float offDelta;

    float chance = 0.3f;
    float maxOff = 4;
    float range = 4;

    {
      flushDelta(0);
    }

    private void flushDelta(int i) {
      offDelta = Mathf.random(i <= 0? -range: 0, i >= 0? range: 0);
    }

    public void update() {
      if (Mathf.chanceDelta(chance) || off >= maxOff || off <= -maxOff) flushDelta(off >= maxOff? -1: off <= -maxOff? 1: 0);
      off += offDelta*Time.delta;
    }


    @Override
    public void reset() {
      off = 0;
      offDelta = 0;
      maxOff = 4;
      range = 4;
    }
  }
}
