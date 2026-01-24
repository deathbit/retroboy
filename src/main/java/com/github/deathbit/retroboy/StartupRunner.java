package com.github.deathbit.retroboy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartupRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== 执行一次性启动代码 ===");
        log.info("应用程序启动完成，开始执行初始化逻辑...");
        
        // 在这里添加需要在启动时执行的一次性代码
        // 例如：初始化数据、加载配置、预热缓存等
        
        log.info("初始化完成！");
    }
}
