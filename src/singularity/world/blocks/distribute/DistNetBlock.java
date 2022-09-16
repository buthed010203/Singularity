package singularity.world.blocks.distribute;

import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.world.Block;
import singularity.world.components.distnet.DistElementBlockComp;
import singularity.world.components.distnet.DistElementBuildComp;
import singularity.world.blocks.SglBlock;
import singularity.world.modules.DistributeModule;
import universecore.annotations.Annotations;

public class DistNetBlock extends SglBlock implements DistElementBlockComp{
  public int frequencyUse = 1;
  
  public DistNetBlock(String name){
    super(name);
    update = true;
    saveConfig = false;
  }
  
  @Override
  public int frequencyUse(){
    return frequencyUse;
  }

  @Annotations.ImplEntries
  public class DistNetBuild extends SglBuilding implements DistElementBuildComp{
    public DistributeModule distributor;
    public Seq<DistElementBuildComp> netLinked = new Seq<>();
    public int priority;
  
    @Override
    public Building create(Block block, Team team){
      super.create(block, team);
      distributor = new DistributeModule(this);
      distributor.setNet();
      return this;
    }

    @Override
    public void updateTile(){
      distributor.network.update();
    }

    @Override
    public DistributeModule distributor(){
      return distributor;
    }
  
    @Override
    public int priority(){
      return priority;
    }
  
    @Override
    public void priority(int priority){
      this.priority = priority;
      distributor.network.priorityModified(this);
    }
  }
}
