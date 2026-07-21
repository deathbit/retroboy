//package com.github.deathbit.retroboy.handler;
//
//import com.github.deathbit.retroboy.enums.Area;
//import com.github.deathbit.retroboy.enums.Platform;
//import com.github.deathbit.retroboy.handler.component.CoreHandler;
//import com.github.deathbit.retroboy.handler.component.MediaHandler;
//import com.github.deathbit.retroboy.handler.component.MoveGameHandler;
//import com.github.deathbit.retroboy.handler.component.RenameGameHandler;
//import com.github.deathbit.retroboy.handler.component.ReleaseHandler;
//import com.github.deathbit.retroboy.handler.component.RuleContextInitializer;
//import com.github.deathbit.retroboy.handler.component.RuleEngine;
//import com.github.deathbit.retroboy.handler.component.ReportHandler;
//import com.github.deathbit.retroboy.rule.Rule;
//import com.github.deathbit.retroboy.rule.Rules;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import java.util.Map;
//
//public abstract class AbstractHandler implements Handler {
//
//    @Autowired
//    private RuleContextInitializer ruleContextInitializer;
//    @Autowired
//    private RuleEngine ruleEngine;
//    @Autowired
//    private MoveGameHandler moveGameHandler;
//    @Autowired
//    private RenameGameHandler renameGameHandler;
//    @Autowired
//    private MediaHandler mediaHandler;
//    @Autowired
//    private ReportHandler reportHandler;
//    @Autowired
//    private CoreHandler coreHandler;
//    @Autowired
//    private ReleaseHandler releaseHandler;
//
//    @Override
//    public void handle() throws Exception {
//        renameGameHandler.handle(ruleContext);
//        mediaHandler.handle(ruleContext);
//        reportHandler.handle(ruleContext);
//        coreHandler.handle(ruleContext);
//        releaseHandler.handle(ruleContext);
//    }
//}
