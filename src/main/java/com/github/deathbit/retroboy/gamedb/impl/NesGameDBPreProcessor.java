package com.github.deathbit.retroboy.gamedb.impl;

import com.github.deathbit.retroboy.domain.GameDB;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.gamedb.GameDBPreProcessor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NesGameDBPreProcessor implements GameDBPreProcessor {

    @Override
    public Platform platform() {
        return Platform.NES;
    }

    @Override
    public void process(Map<String, GameDB> gameDBMap) {
        // nes_db.xml 中部分条目信息不准确，在此处手动修正
        // 示例：set(gameDBMap, "rom_name", g -> g.setRegion("Japan"));
    }
}
