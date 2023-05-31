package singularity.world.consumers;

import arc.Core;
import arc.math.Mathf;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Image;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectFloatMap;
import arc.struct.ObjectIntMap;
import arc.struct.Seq;
import arc.util.Scaling;
import arc.util.Strings;
import arc.util.Tmp;
import mindustry.ctype.Content;
import mindustry.gen.Building;
import mindustry.gen.Icon;
import mindustry.gen.Tex;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.ui.Styles;
import mindustry.world.blocks.environment.Floor;
import mindustry.world.meta.Stat;
import mindustry.world.meta.StatUnit;
import mindustry.world.meta.Stats;
import singularity.world.components.FloorCrafterBuildComp;
import universecore.components.blockcomp.ConsumerBuildComp;
import universecore.world.consumers.BaseConsume;
import universecore.world.consumers.ConsumeType;

public class SglConsumeFloor<T extends Building & ConsumerBuildComp & FloorCrafterBuildComp> extends BaseConsume<T> {
  final ObjectFloatMap<Floor> floorEff = new ObjectFloatMap<>();

  public float baseEfficiency = 0;

  public SglConsumeFloor(Object... floors){
    for (int i = 0; i < floors.length; i+=2) {
      Floor floor = (Floor) floors[i];
      Float effInc = (Float) floors[i + 1];

      floorEff.put(floor, effInc);
    }
  }

  public float getEff(ObjectIntMap<Floor> floorCount){
    float res = baseEfficiency;

    for (ObjectIntMap.Entry<Floor> entry : floorCount) {
      res += floorEff.get(entry.key, 0)*entry.value;
    }

    return res;
  }

  @Override
  public ConsumeType<?> type() {
    return SglConsumeType.floor;
  }

  @Override
  public void buildIcons(Table table) {
    table.image(Icon.terrain);
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public void merge(BaseConsume<T> baseConsume) {
    if (baseConsume instanceof SglConsumeFloor cons){
      for (Object o : cons.floorEff) {
        if (o instanceof ObjectFloatMap map){
          for (ObjectFloatMap.Entry<Floor> entry : ((ObjectFloatMap<Floor>) map)) {
            floorEff.put(entry.key, floorEff.get(entry.key, 1)*entry.value);
          }
        }
      }

      return;
    }
    throw new IllegalArgumentException("only merge consume with same type");
  }

  @Override
  public void consume(T t) {
    //no action
  }

  @Override
  public void update(T t) {
    //no action
  }

  @Override
  public void display(Stats stats) {
    stats.add(Stat.tiles, st -> {
      st.row().table(((TextureRegionDrawable)Tex.whiteui).tint(Tmp.c1.set(Pal.darkestGray).a(0.7f)), t -> {
        t.clearChildren();
        t.defaults().pad(5).left();

        int c = 0;
        for (ObjectFloatMap.Entry<Floor> entry : floorEff) {
          t.stack(
              new Image(entry.key.uiIcon).setScaling(Scaling.fit),
              new Table(table -> {
                table.top().right().add((entry.value < 0 ? "[scarlet]" : baseEfficiency == 0 ? "[accent]" : "[accent]+") + (int)(entry.value*100) + "%").style(Styles.outlineLabel);
                table.top().left().add("/" + StatUnit.blocks.localized()).color(Pal.gray);
              })
          ).fill().padRight(4);
          t.add(entry.key.localizedName).left().padLeft(0);
          c++;

          if (c != 0 && c % 3 == 0){
            t.row();
          }
        }
      }).fill();
    });
  }

  @Override
  public void build(T t, Table table) {
    table.add(new Bar(
        () -> Core.bundle.get("infos.floorEfficiency") + ": " + Strings.autoFixed(Mathf.round(efficiency(t)*100), 0) + "%",
        () -> Pal.accent,
        () -> Mathf.clamp(efficiency(t))
    )).growX().height(18f).pad(4);
    table.row();
  }

  @Override
  public float efficiency(T t) {
    return getEff(t.floorCount());
  }

  @Override
  public Seq<Content> filter() {
    return null;
  }
}
