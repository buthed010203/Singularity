package singularity.world.blocks.gas;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.struct.Seq;
import arc.util.Eachable;
import mindustry.entities.units.BuildPlan;
import mindustry.graphics.Layer;
import mindustry.input.Placement;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.Autotiler;
import mindustry.world.blocks.distribution.ItemBridge;
import singularity.Sgl;
import singularity.contents.GasBlocks;
import singularity.type.Gas;
import singularity.world.blockComp.GasBlockComp;
import singularity.world.blockComp.GasBuildComp;

import java.util.Arrays;

import static mindustry.Vars.tilesize;

public class GasConduit extends GasBlock implements Autotiler{
  public TextureRegion[] regions = new TextureRegion[5], tops = new TextureRegion[5];
  
  public boolean canLeak = true;
  
  public GasConduit(String name){
    super(name);
    outputGases = true;
    showGasFlow = true;
    rotate = true;
    solid = false;
    floating = true;
    conveyorPlacement = true;
    noUpdateDisabled = true;
    
    gasCapacity = 22.25f;
    maxGasPressure = 16;
  }
  
  @Override
  public void load(){
    super.load();
    for(int i=0; i<5; i++){
      regions[i] = Core.atlas.find(name + "_" + i);
      tops[i] = Core.atlas.find(name + "_top_" + i);
    }
  }
  
  @Override
  public void drawRequestRegion(BuildPlan req, Eachable<BuildPlan> list){
    int[] bits = getTiling(req, list);
    
    if(bits == null) return;
    
    Draw.scl(bits[1], bits[2]);
    Draw.rect(regions[bits[0]], req.drawx(), req.drawy(), req.rotation * 90);
    Draw.color();
    Draw.rect(tops[bits[0]], req.drawx(), req.drawy(), req.rotation * 90);
    Draw.scl();
  }
  
  @Override
  public Block getReplacement(BuildPlan req, Seq<BuildPlan> requests){
    Boolf<Point2> cont = p -> requests.contains(o -> o.x == req.x + p.x && o.y == req.y + p.y && o.rotation == req.rotation && (req.block instanceof GasConduit || req.block instanceof GasJunction));
    return cont.get(Geometry.d4(req.rotation)) &&
      cont.get(Geometry.d4(req.rotation - 2)) &&
      req.tile() != null &&
      req.tile().block() instanceof GasConduit &&
      Mathf.mod(req.build().rotation - req.rotation, 2) == 1 ? GasBlocks.gas_junction : this;
  }
  
  @Override
  public void handlePlacementLine(Seq<BuildPlan> plans){
    Placement.calculateBridges(plans, (ItemBridge)GasBlocks.gas_bridge_conduit);
  }
  
  @Override
  public boolean blends(Tile tile, int rotation, int otherx, int othery, int otherrot, Block otherblock){
    if(!(otherblock instanceof GasBlockComp)) return false;
    GasBlockComp blockComp = (GasBlockComp)otherblock;
    return blockComp.hasGases() && (blockComp.outputGases() || (lookingAt(tile, rotation, otherx, othery, otherblock))) && lookingAtEither(tile, rotation, otherx, othery, otherrot, otherblock);
  }
  
  @Override
  public TextureRegion[] icons(){
    return new TextureRegion[]{regions[0], tops[0]};
  }
  
  public class GasConduitBuild extends SglBuilding{
    int[] blendData;
    Color gasColor;
    Color smoothColor = Color.white.cpy();
  
    @Override
    public void onProximityUpdate(){
      super.onProximityUpdate();
      
      int[] temp = buildBlending(tile, rotation, null, true);
      blendData = Arrays.copyOf(temp, temp.length);
    }
  
    @Override
    public void handleGas(GasBuildComp source, Gas gas, float amount){
      gases.add(gas, amount);
    }
  
    @Override
    public boolean acceptGas(GasBuildComp source, Gas gas){
      return source.getBuilding().team == getBuilding().team && getGasBlock().hasGases() && pressure() < getGasBlock().maxGasPressure();
    }
  
    @Override
    public void updateTile(){
      super.updateTile();
      
      gasColor = Color.black.cpy();
      Tile next = this.tile.nearby(this.rotation);
      if(next.build instanceof GasBuildComp){
        gases.each(stack -> {
          gasColor.add(stack.gas.color.cpy().mul(stack.amount/gases.total()));
        });
        moveGas((GasBuildComp) next.build);
      }
      else if(next.build == null){
        float fract = Math.max(0, pressure() - Sgl.atmospheres.current.getCurrPressure())/getGasBlock().maxGasPressure();
        
        if(fract <= 0) return;
        gases.each(stack -> {
          gasColor.add(stack.gas.color.cpy().mul(stack.amount/gases.total()));
          float flowRate = Math.min(fract*getGasBlock().maxGasPressure()*getGasBlock().gasCapacity()*(gases().get(stack.gas)/gases().total()), gases().get(stack.gas));
          handleGas(this, stack.gas, -flowRate);
          Sgl.gasAreas.pour(next, stack.gas, flowRate);
        });
      }
      gasColor.a(pressure()/maxGasPressure*0.75f);
      
      smoothColor.lerp(gasColor, 0.015f);
    }
  
    @Override
    public void draw(){
      float rotation = rotdeg();
      int r = this.rotation;
    
      Draw.z(Layer.blockUnder);
      for(int i = 0; i < 4; i++){
        if((blendData[4] & (1 << i)) != 0){
          int dir = r - i;
          float rot = i == 0 ? rotation : (dir)*90;
          drawConduit(x + Geometry.d4x(dir) * tilesize*0.75f, y + Geometry.d4y(dir) * tilesize*0.75f, 0, rot, i != 0 ? SliceMode.bottom : SliceMode.top);
        }
      }
    
      Draw.z(Layer.block);
    
      Draw.scl(blendData[1], blendData[2]);
      drawConduit(x, y, blendData[0], rotation, SliceMode.none);
      Draw.reset();
    }
  
    protected void drawConduit(float x, float y, int bits, float rotation, SliceMode slice){
      Draw.rect(sliced(regions[bits], slice), x, y, rotation);
      
      Draw.color(smoothColor);
      Draw.rect(sliced(tops[bits], slice), x, y, rotation);
    }
  }
}
