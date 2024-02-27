package net.stalpo.stalpomaparthelper.interfaces;

import net.minecraft.item.map.MapState;

import java.util.List;

public interface FakeMapRenderer {
    public void stalpomaparthelper$renderMaps(List<Integer> ids, List<MapState> states);
    public void stalpomaparthelper$renderMap(int id, MapState state);
}
