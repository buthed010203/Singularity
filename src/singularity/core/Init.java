package singularity.core;

import arc.Core;
import arc.graphics.g2d.Draw;
import arc.math.Interp;
import arc.math.Mat;
import arc.scene.Element;
import arc.scene.Group;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.scene.utils.Elem;
import arc.util.Align;
import arc.util.Tmp;
import dynamilize.DynamicClass;
import dynamilize.JavaHandleHelper;
import dynamilize.annotations.DynamilizeClass;
import dynamilize.runtimeannos.AspectInterface;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.gen.Building;
import mindustry.ui.Styles;
import mindustry.ui.fragments.HintsFragment;
import mindustry.world.Block;
import mindustry.world.blocks.defense.OverdriveProjector;
import mindustry.world.blocks.liquid.Conduit;
import singularity.Sgl;
import singularity.game.SglHint;
import singularity.world.meta.SglAttribute;
import universecore.util.handler.FieldHandler;

/**改动游戏原内容重初始化，用于对游戏已定义的实例进行操作*/
public class Init{
  public static final DynamicClass HintDyClass = HintAspect.INSTANCE;

  public static void init(){
    if(Sgl.config.modReciprocal){
      //添加Hint的draw入口
      Vars.ui.hudGroup.removeChild(FieldHandler.getValueDefault(Vars.ui.hints, "group"));
      Vars.ui.hints = Sgl.classes.getDynamicMaker().newInstance(Vars.ui.hints.getClass(), new Class[]{HintsAspect.class}, HintDyClass).objSelf();
      Vars.ui.hints.build(Vars.ui.hudGroup);
    }
  }
  
  /**内容重载，对已加载的内容做出变更(或者覆盖)*/
  public static void reloadContent(){
    //设置方块及地板属性
    Blocks.stone.attributes.set(SglAttribute.bitumen, 0.12f);

    for(Block target: Vars.content.blocks()){
      //为液体装卸器保证不从(常规)导管中提取液体
      if(target instanceof Conduit) target.unloadable = false;

      //禁用所有超速器
      if(target instanceof OverdriveProjector over){
        over.placeablePlayer = false;
        over.buildType = () -> new Building(){
          @Override
          public void update() {
            kill();
          }
        };
      }
    }
  }

  @AspectInterface
  public interface HintsAspect{
    void display(HintsFragment.Hint hint);
    void complete();
  }

  @DynamilizeClass
  public static abstract class HintAspect implements DynamilizeClass.DynamilizedClass<HintsFragment>{
    private static final JavaHandleHelper $helper$ = Sgl.classes.getDynamicMaker().getHelper();

    public static int page;
    public static Runnable build;

    private static final Mat mat = new Mat();

    public void display(HintsFragment.Hint hint){
      if(getVar("current") != null) return;

      page = 0;
      this.<Group>getVar("group").fill(t -> {
        setVar("last", t);
        t.left();
        Table ta = t.table().fill().get();

        if (hint instanceof SglHint sglHint){
          Element elem = new Element(){
            @Override
            public void draw() {
              mat.set(Draw.proj());
              Draw.proj(Core.camera.mat);
              sglHint.draw(page, t.color);
              Draw.proj(mat);
            }
          };
          elem.touchable = Touchable.disabled;
          t.addChild(elem);
        }

        build = () -> {
          ta.clearChildren();

          ta.table(Styles.black5, cont -> {
            cont.actions(Actions.alpha(0f), Actions.alpha(1f, 1f, Interp.smooth));
            cont.margin(6f).add(hint instanceof SglHint sglHint? sglHint.text(page): hint.text()).width(Vars.mobile ? 270f : 400f).left().labelAlign(Align.left).wrap();
          });
          ta.row();

          if (hint instanceof SglHint sglHint){
            ta.left().table(bu -> {
              if (page > 0){
                bu.left().button("@hint.last", Styles.nonet, () -> {
                  page--;
                  build.run();
                }).size(112f, 40f).left();
              }
              if (page < sglHint.pages() - 1){
                bu.left().button("@hint.next", Styles.nonet, () -> {
                  page++;
                  build.run();
                }).size(112f, 40f).left();
              }
              else {
                bu.left().button("@hint.skip", Styles.nonet, () -> {
                  if(getVar("current") != null){
                    invokeFunc("complete");
                  }
                }).size(112f, 40f).left();
              }
            }).grow();
          }
          else {
            ta.button("@hint.skip", Styles.nonet, () -> {
              if(getVar("current") != null){
                invokeFunc("complete");
              }
            }).size(112f, 40f).left();
          }
        };
        build.run();
      });

      setVar("current", hint);
    }
  }
}
