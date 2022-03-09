package singularity.world.modules;

import arc.struct.IntSeq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.world.modules.BlockModule;
import singularity.world.blockComp.distributeNetwork.DistElementBuildComp;
import singularity.world.blockComp.distributeNetwork.DistNetworkCoreComp;
import singularity.world.distribution.DistributeNetwork;
import singularity.world.distribution.request.DistRequestBase;

public class DistributeModule extends BlockModule{
  public final DistElementBuildComp entity;
  public final IntSeq distNetLinks = new IntSeq();
  
  public DistRequestBase<?> lastAssign;
  public DistributeNetwork network;
  
  public DistributeModule(DistElementBuildComp entity){
    this.entity = entity;
  }
  
  public void setNet(){
    setNet(null);
  }
  
  public void setNet(DistributeNetwork net){
    if(net == null){
      new DistributeNetwork().add(entity);
    }
    else network = net;
    if(network.netValid()) entity.networkValided();
  }
  
  public void assign(DistRequestBase<?> request){
    if(network.netValid()){
      DistCoreModule core = core().distCore();
      core.receive(request);
      lastAssign = request;
    }
  }
  
  public DistNetworkCoreComp core(){
    return network.getCore();
  }
  
  @Override
  public void read(Reads read){
    int len = read.i();
    for(int i = 0; i < len; i++){
      distNetLinks.add(read.i());
    }
  }
  
  @Override
  public void write(Writes write){
    write.i(distNetLinks.size);
    for(int i = 0; i < distNetLinks.size; i++){
      write.i(distNetLinks.get(i));
    }
  }
}
