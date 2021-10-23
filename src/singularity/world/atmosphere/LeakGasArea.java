package singularity.world.atmosphere;

import arc.graphics.Color;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Position;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pool;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.core.World;
import mindustry.entities.EntityGroup;
import mindustry.gen.Drawc;
import mindustry.gen.Entityc;
import mindustry.gen.Groups;
import mindustry.gen.Unitc;
import mindustry.io.TypeIO;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.blocks.environment.Floor;
import singularity.Sgl;
import singularity.type.Gas;
import singularity.type.Reaction;
import singularity.type.SglContents;
import singularity.world.SglFx;

@SuppressWarnings("unchecked")
public class LeakGasArea implements Pool.Poolable, Entityc, Drawc{
  public static final float maxGasCapacity = 100;
  
  public Tile tile;
  
  public float radius;
  public float gasAmount;
  public Color color;
  public Gas gas;
  
  public transient int id = EntityGroup.nextId();
  public boolean added = false;
  public float x, y;
  
  public int timing;
  
  public static LeakGasArea create(){
    return new LeakGasArea();
  }
  
  public void set(Gas gas, float flow, Tile tile){
    this.tile = tile;
    this.gasAmount += flow;
    this.gas = gas;
    this.color = gas.color;
    set(tile.drawx(), tile.drawy());
  }
  
  @Override
  public boolean isAdded(){
    return false;
  }
  
  @Override
  public void update(){
    float leak = Mathf.maxZero(gasAmount - maxGasCapacity);
    
    float leakRate = Sgl.atmospheres.current.getCurrPressure()*gasAmount/20;
    gasAmount -= leakRate;
    if(Vars.state.isCampaign()) Sgl.atmospheres.current.currAtmoSector.add(gas, gasAmount + leak);
    
    float amount = gasAmount/maxGasCapacity;
    radius = 5*amount;
    float rate = Math.min(0.7f, gasAmount/10)*Time.delta;
    
    double random = Math.random();
    if(random<rate){
      SglFx.gasLeak.at(x, y, 0, color, amount);
    }
  
    Geometry.circle(tile.x, tile.y, 5, (x, y) -> {
      Tile otherT = Vars.world.tile(x, y);
      if(otherT == tile || otherT == null) return;
      Seq<LeakGasArea> others = Sgl.gasAreas.get(otherT);
      if(others == null) return;
      for(LeakGasArea other: others){
        if(other.gas == gas) continue;
        if(Tmp.cr1.set(tile.x, tile.y, radius/Vars.tilesize).overlaps(Tmp.cr2.set(other.tile.x, other.tile.y, other.radius/Vars.tilesize))){
          Reaction<?, ?, ?> reaction = Sgl.reactions.match(gas, other.gas);
          Tile t = Vars.world.tile((tile.x + other.tile.x)/2, (tile.y + other.tile.y)/2);
          
          if(reaction != null) Sgl.reactionPoints.transfer(t, gas, 0.4f);
        }
      }
    });
    
    if(timing>0){
      timing--;
    }
    else{
      if(gasAmount <= 1) remove();
    }
  }
  
  public void flow(float flow){
    gasAmount += flow;
    timing = 3;
  }
  
  @Override
  public void draw(){
  
  }
  
  @Override
  public void read(Reads read){
    short REV = read.s();
    if (REV == 0) {
      gas = SglContents.gas(read.i());
      read.i();
    } else {
      if (REV != 1) throw new IllegalArgumentException("Unknown revision '" + REV + "' for entity type 'PuddleComp'");
      gas = SglContents.gas(read.i());
    }
    color = gas.color;
    gasAmount = read.f();
    tile = TypeIO.readTile(read);
    x = read.f();
    y = read.f();
    
    this.afterRead();
  }
  
  @Override
  public void afterRead(){
    Sgl.gasAreas.add(this);
  }
  
  @Override
  public void write(Writes writes){
    writes.s(1);
    writes.i(gas.id);
    writes.f(gasAmount);
    TypeIO.writeTile(writes, tile);
    writes.f(x);
    writes.f(y);
  }
  
  @Override
  public void reset(){
    gasAmount = 0;
    color = null;
    gas = null;
  
    id = EntityGroup.nextId();
    x = 0;
    y = 0;
  
    added = false;
  }
  
  @Override
  public void remove() {
    if (this.added) {
      Groups.all.remove(this);
      Groups.draw.remove(this);
      this.added = false;
      Sgl.gasAreas.remove(this);
      Groups.queueFree(this);
    }
  }
  
  @Override
  public void add(){
    if (!added) {
      Groups.all.add(this);
      Groups.draw.add(this);
      added = true;
    }
  }
  
  @Override
  public boolean isLocal(){
    if(this instanceof Unitc){
      Unitc u = (Unitc) this;
      return u.controller() != Vars.player;
    }
    
    return true;
  }
  
  @Override
  public boolean isRemote(){
    if (this instanceof Unitc) {
      Unitc u = (Unitc)this;
      return u.isPlayer() && !this.isLocal();
    }
    return false;
  }
  
  @Override
  public boolean isNull(){
    return false;
  }
  
  @Override
  public <T extends Entityc> T self(){
    return (T)this;
  }
  
  @Override
  public <T> T as(){
    return (T)this;
  }
  
  @Override
  public int classId(){
    return 100;
  }
  
  @Override
  public boolean serialize(){
    return true;
  }
  
  @Override
  public int id(){
    return id;
  }
  
  @Override
  public void id(int id){
    this.id = id;
  }
  
  @Override
  public float clipSize(){
    return 20f;
  }
  
  @Override
  public void set(float x, float y){
    this.x = x;
    this.y = y;
  }
  
  @Override
  public void set(Position position){
    set(position.getX(), position.getY());
  }
  
  @Override
  public void trns(float x, float y) {
    set(this.x + x, this.y + y);
  }
  
  @Override
  public void trns(Position position){
    trns(position.getX(), position.getY());
  }
  
  @Override
  public int tileX() {
    return World.toTile(x);
  }
  
  @Override
  public int tileY() {
    return World.toTile(y);
  }
  
  @Override
  public Floor floorOn(){
    Tile tile = this.tileOn();
    return tile != null && tile.block() == Blocks.air ? tile.floor() : (Floor)Blocks.air;
  }
  
  @Override
  public Block blockOn(){
    Tile tile = this.tileOn();
    return tile == null ? Blocks.air : tile.block();
  }
  
  @Override
  public boolean onSolid(){
    return false;
  }
  
  @Override
  public Tile tileOn(){
    return Vars.world.tileWorld(this.x, this.y);
  }
  
  @Override
  public float getX(){
    return x;
  }
  
  @Override
  public float getY(){
    return y;
  }
  
  @Override
  public float x(){
    return x;
  }
  
  @Override
  public void x(float x){
    this.x = x;
  }
  
  @Override
  public float y(){
    return y;
  }
  
  @Override
  public void y(float y){
    this.y = y;
  }
}
