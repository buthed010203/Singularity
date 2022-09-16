package singularity.contents;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.content.Liquids;
import mindustry.entities.Effect;
import mindustry.gen.Sounds;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import singularity.graphic.SglDraw;
import singularity.type.SglCategory;
import singularity.world.SglFx;
import singularity.world.blocks.nuclear.EnergySource;
import singularity.world.blocks.nuclear.EnergyVoid;
import singularity.world.blocks.nuclear.NuclearPipeNode;
import singularity.world.blocks.nuclear.NuclearReactor;
import singularity.world.blocks.product.NormalCrafter;
import singularity.world.draw.DrawExpandPlasma;
import singularity.world.draw.DrawFactory;
import singularity.world.draw.SglDrawPlasma;
import singularity.world.particles.SglParticleModels;
import universecore.world.particles.Particle;
import universecore.world.particles.ParticleModel;

import static mindustry.Vars.tilesize;

public class NuclearBlocks implements ContentList{
  /**核能塔座*/
  public static Block nuclear_pipe_node,
  /**相位核能塔*/
  phase_pipe_node,
  /**衰变仓*/
  decay_bin,
  /**中子能发电机*/
  neutron_generator,
  /**核子冲击反应堆*/
  nuclear_impact_reactor,
  /**核反应堆*/
  nuclear_reactor,
  /**晶格反应堆*/
  lattice_reactor,
  /**超限裂变反应堆*/
  overrun_reactor,
  /**核能源*/
  nuclear_energy_source,
  /**核能黑洞*/
  nuclear_energy_void;
  
  @Override
  public void load(){
    nuclear_pipe_node = new NuclearPipeNode("nuclear_pipe_node"){{
      requirements(SglCategory.nuclear, ItemStack.with(SglItems.strengthening_alloy, 8, SglItems.crystal_FEX, 4));
    }};
    
    phase_pipe_node = new NuclearPipeNode("phase_pipe_node"){{
      requirements(SglCategory.nuclear, ItemStack.with(SglItems.strengthening_alloy, 24, SglItems.crystal_FEX, 16, Items.phaseFabric, 15));
      size = 2;
      maxLinks = 10;
      linkRange = 18;
    }};
    
    decay_bin = new NormalCrafter("decay_bin"){{
      requirements(SglCategory.nuclear, ItemStack.with(SglItems.strengthening_alloy, 60, SglItems.crystal_FEX, 40, Items.silicon, 50, Items.lead, 80, Items.metaglass, 40));
      size = 2;
      autoSelect = true;
      canSelect = false;
      
      newConsume();
      consume.time(600);
      consume.item(SglItems.uranium_235, 1);
      newProduce();
      produce.energy(0.25f);
      produce.item(Items.thorium, 1);
      newConsume();
      consume.time(600);
      consume.item(SglItems.plutonium_239, 1);
      newProduce();
      produce.energy(0.25f);
      newConsume();
      consume.time(900);
      consume.item(SglItems.uranium_238, 1);
      newProduce();
      produce.energy(0.12f);
      newConsume();
      consume.time(450);
      consume.item(Items.thorium, 1);
      newProduce();
      produce.energy(0.2f);
      
      updateEffect = Fx.generatespark;
      updateEffectChance = 0.01f;
      
      draw = new DrawFactory<NormalCrafterBuild>(this){
        @Override
        public TextureRegion[] icons(){
          return new TextureRegion[]{
              bottom,
              region
          };
        }
        
        {
          drawDef = e -> {
            Draw.rect(bottom, e.x, e.y);
            Draw.rect(region, e.x, e.y);
            Draw.color(SglItems.uranium_235.color);
            Draw.alpha((float)e.items.get(SglItems.uranium_235)/itemCapacity);
            Draw.rect(top, e.x, e.y);
          };
        }
      };
    }};
    
    neutron_generator = new NormalCrafter("neutron_generator"){{
      requirements(Category.power, ItemStack.with(SglItems.strengthening_alloy, 100, SglItems.crystal_FEX_power, 80, Items.phaseFabric, 70, SglItems.aerogel, 90));
      size = 3;
      
      warmupSpeed = 0.0075f;
      
      newConsume();
      consume.energy(4);
      newProduce();
      produce.power(50);
      
      draw = new SglDrawPlasma<NormalCrafterBuild>(this, 4){{
        plasma1 = Pal.reactorPurple;
        plasma2 = Pal.reactorPurple2;
  
        drawDef = e -> {
          Draw.rect(bottom, e.x, e.y);
          Draw.rect(region, e.x, e.y);
          drawPlasma(e);
          Draw.rect(top, e.x, e.y);
        };
      }};
    }};
  
    nuclear_impact_reactor = new NormalCrafter("nuclear_impact_reactor"){{
      requirements(Category.power, ItemStack.with());
      size = 5;
      itemCapacity = 30;
      liquidCapacity = 35;
      
      craftEffect = SglFx.explodeImpWaveBig;
      updateEffect = SglFx.impWave;
      effectRange = 2;
      updateEffectChance = 0.025f;
      
      ambientSoundVolume = 0.6f;
      craftedSound = Sounds.explosionbig;
      craftedSoundVolume = 1f;

      ParticleModel model = new ParticleModel().setDefault().resetCloudUpdate()
          .setTailFade(f -> Mathf.lerpDelta(f, 0, 0.04f))
          .tailGradient(Pal.lightishGray, 0.04f)
          .setColor(Pal.reactorPurple);
  
      craftTrigger = e -> {
        model.resetDeflect();
        model.setDest(e.x, e.y, 0.15f*e.consEfficiency());
        model.setDeflect(90, 0.125f);
        for(Particle particle : Particle.get(p -> p.x < e.x + 20 && p.x > e.x - 20 && p.y < e.y + 20 && p.y > e.y - 20)){
          particle.remove();
        }
        Effect.shake(4f, 18f, e.x, e.y);
        Angles.randLenVectors(System.nanoTime(), Mathf.random(8, 12), 4.75f, 6.25f,
            (x, y) -> model.create(e.x, e.y, x, y, Mathf.random(5f, 7f)));
      };
      crafting = e -> {
        if(Mathf.chanceDelta(0.02f)) Angles.randLenVectors(System.nanoTime(), 1, 2, 3.5f,
            (x, y) -> SglParticleModels.nuclearParticle.create(e.x, e.y, x, y, Mathf.random(3.25f, 4f)));
      };
      
      warmupSpeed = 0.00065f;
      
      newConsume();
      consume.item(SglItems.concentration_uranium_235, 1);
      consume.power(80);
      consume.liquid(Liquids.cryofluid, 0.6f);
      consume.time(180);
      newProduce();
      produce.power(400);
      
      newConsume();
      consume.item(SglItems.concentration_plutonium_239, 1);
      consume.power(80);
      consume.liquid(Liquids.cryofluid, 0.6f);
      consume.time(180);
      newProduce();
      produce.power(400);
      
      draw = new DrawExpandPlasma<>(this, 2);
    }};
    
    nuclear_reactor = new NuclearReactor("nuclear_reactor"){{
      requirements(SglCategory.nuclear, ItemStack.with(SglItems.strengthening_alloy, 200, SglItems.crystal_FEX, 160, SglItems.aerogel, 180, Items.lead, 180, Items.phaseFabric, 140));
      size = 4;
      itemCapacity = 35;
      liquidCapacity = 25;
      energyCapacity = 2048;
      
      ambientSoundVolume = 0.4f;
      
      newReact(SglItems.concentration_uranium_235, 450, 8, true);
      newReact(SglItems.concentration_plutonium_239, 450, 8, true);
      
      addCoolant(0.25f);
      consume.liquid(Liquids.cryofluid, 0.2f);
  
      addTransfer(new ItemStack(SglItems.plutonium_239, 1));
      consume.time(180);
      consume.item(SglItems.uranium_238, 1);
    }};
    
    lattice_reactor = new NuclearReactor("lattice_reactor"){{
      requirements(SglCategory.nuclear, ItemStack.with(SglItems.strengthening_alloy, 120, SglItems.crystal_FEX, 90, SglItems.crystal_FEX_power, 70, Items.phaseFabric, 60, Items.surgeAlloy, 80));
      size = 3;
      itemCapacity = 25;
      liquidCapacity = 20;
      energyCapacity = 1024;
      
      explosionDamageBase = 260;
      explosionRadius = 12;
      
      productHeat = 0.1f;
      
      newReact(SglItems.uranium_235, 1200, 6f, false);
      newReact(SglItems.plutonium_239, 1200, 6f, false);
      newReact(Items.thorium, 900, 5f, false);
  
      addCoolant(0.25f);
      consume.liquid(Liquids.cryofluid, 0.2f);
  
      addTransfer(new ItemStack(SglItems.plutonium_239, 1));
      consume.time(420);
      consume.item(SglItems.uranium_238, 1);
    }};
    
    overrun_reactor = new NuclearReactor("overrun_reactor"){{
      requirements(SglCategory.nuclear, ItemStack.with(SglItems.strengthening_alloy, 400, SglItems.crystal_FEX, 260, SglItems.crystal_FEX_power, 280, SglItems.degenerate_neutron_polymer, 100, Items.surgeAlloy, 375, Items.phaseFabric, 240));
      size = 6;
      itemCapacity = 50;
      liquidCapacity = 50;
      energyCapacity = 8192;
      
      explosionDamageBase = 580;
      explosionRadius = 32;
      
      productHeat = 0.35f;
      
      warmupSpeed = 0.0015f;
      
      ambientSoundVolume = 0.6f;
      
      newReact(SglItems.concentration_uranium_235, 240, 22, false);
      newReact(SglItems.concentration_plutonium_239, 240, 22, false);
      
      addCoolant(0.4f);
      consume.liquid(SglLiquids.phase_FEX_liquid, 0.4f);
      
      crafting = e -> {
        if(Mathf.chanceDelta(0.06f*e.workEfficiency())) Angles.randVectors(System.nanoTime(), 1, 15, (x, y) -> {
          float iff = Mathf.random(0.4f, Math.max(0.4f, e.workEfficiency()));
          Tmp.v1.set(x, y).scl(0.5f*iff/2);
          SglParticleModels.nuclearParticle.create(e.x + x, e.y + y, Tmp.v1.x, Tmp.v1.y, iff*6.5f*e.workEfficiency());
        });
      };
      
      draw = new SglDrawPlasma<NuclearReactorBuild>(this, 4){
        TextureRegion rotatorA, rotatorB;
  
        final Color hotColor = Color.valueOf("ff9575a3");
        final Color coolColor = new Color(1, 1, 1, 0f);
  
        @Override
        public void load(){
          super.load();
          rotatorA = Core.atlas.find(block.name + "_rotator_0");
          rotatorB = Core.atlas.find(block.name + "_rotator_1");
        }
        
        {
          plasma1 = Pal.reactorPurple;
          plasma2 = Pal.reactorPurple2;

          drawerType = e -> new SglBaseDrawer(e){
            float smooth;

            @Override
            public void draw(){
              smooth = Mathf.lerpDelta(smooth, e.liquids.currentAmount(), 0.02f);

              Draw.rect(bottom, e.x, e.y);
              drawPlasma(e);
              SglDraw.startBloom(31);
              Drawf.liquid(liquid, e.x, e.y, smooth/liquidCapacity, e.liquids.current().color.cpy().lerp(Color.white, 0.3f));
              SglDraw.endBloom();
              Draw.rect(rotatorA, e.x, e.y, e.totalProgress()*5);
              Draw.rect(rotatorB, e.x, e.y, -e.totalProgress()*5);
              Draw.rect(region, e.x, e.y);

              Draw.color(coolColor, hotColor, e.heat/e.block().maxHeat);
              Fill.rect(e.x, e.y, e.block.size * tilesize, e.block.size * tilesize);

              Draw.z(Layer.effect);
              Draw.color(Pal.reactorPurple);

              float shake = Mathf.random(-0.3f, 0.3f)*e.workEfficiency();
              Tmp.v1.set(19 + shake, 0).rotate(e.totalProgress()*2);
              Tmp.v2.set(0, 19 + shake).rotate(e.totalProgress()*2);
              Fill.poly(e.x + Tmp.v1.x, e.y + Tmp.v1.y, 3, 3f, e.totalProgress()*2);
              Fill.poly(e.x + Tmp.v2.x, e.y + Tmp.v2.y, 3, 3f, e.totalProgress()*2 + 90);
              Fill.poly(e.x - Tmp.v1.x, e.y - Tmp.v1.y, 3, 3f, e.totalProgress()*2 + 180);
              Fill.poly(e.x - Tmp.v2.x, e.y - Tmp.v2.y, 3, 3f, e.totalProgress()*2 + 270);

              Tmp.v1.set(16, 0).rotate(-e.totalProgress()*2);
              Tmp.v2.set(0, 16).rotate(-e.totalProgress()*2);
              Fill.poly(e.x + Tmp.v1.x, e.y + Tmp.v1.y, 3, 3f, -e.totalProgress()*2 - 180);
              Fill.poly(e.x + Tmp.v2.x, e.y + Tmp.v2.y, 3, 3f, -e.totalProgress()*2 - 90);
              Fill.poly(e.x - Tmp.v1.x, e.y - Tmp.v1.y, 3, 3f, -e.totalProgress()*2);
              Fill.poly(e.x - Tmp.v2.x, e.y - Tmp.v2.y, 3, 3f, -e.totalProgress()*2 + 90);

              Lines.stroke(1.8f*e.workEfficiency());
              Lines.circle(e.x, e.y, 18 + shake);
            }
          };
        }
      };
    }};
  
    nuclear_energy_source = new EnergySource("nuclear_energy_source"){{
      requirements(SglCategory.nuclear, BuildVisibility.sandboxOnly, ItemStack.empty);
    }};
    
    nuclear_energy_void = new EnergyVoid("nuclear_energy_void"){{
      requirements(SglCategory.nuclear, BuildVisibility.sandboxOnly, ItemStack.empty);
    }};
  }
}
