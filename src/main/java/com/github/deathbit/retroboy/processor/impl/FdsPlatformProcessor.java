package com.github.deathbit.retroboy.processor.impl;

import com.github.deathbit.retroboy.domain.GameDB;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.processor.PlatformProcessor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class FdsPlatformProcessor implements PlatformProcessor {

    @Override
    public Platform platform() {
        return Platform.FDS;
    }

    @Override
    public void processGameDB(Map<String, GameDB> gameDBMapByRomName) {
        // fds_db.xml 中部分条目信息不准确，在此处手动修正
        // 示例：set(gameDBMap, "rom_name", g -> g.setRegion("Japan"));
    }
}
