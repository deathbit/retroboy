package com.github.deathbit.retroboy.handler;

import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.enums.Platform;
import com.github.deathbit.retroboy.handler.component.CheckMissingMediaFilesHandlerComponent;
import com.github.deathbit.retroboy.handler.component.CleanTargetDirectoryHandlerComponent;
import com.github.deathbit.retroboy.handler.component.CopyAreaFilesHandlerComponent;
import com.github.deathbit.retroboy.handler.component.CreateAreaDirectoriesHandlerComponent;
import com.github.deathbit.retroboy.handler.component.InitializeRuleStateHandlerComponent;
import com.github.deathbit.retroboy.handler.component.PrepareRuleContextHandlerComponent;
import com.github.deathbit.retroboy.handler.component.RenameAreaFilesHandlerComponent;
import com.github.deathbit.retroboy.handler.component.SelectAreaFilesHandlerComponent;
import com.github.deathbit.retroboy.handler.component.WriteProcessingReportsHandlerComponent;
import com.github.deathbit.retroboy.rule.Rule;
import com.github.deathbit.retroboy.rule.Rules;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public abstract class AbstractHandler implements Handler {

    @Autowired
    private PrepareRuleContextHandlerComponent prepareRuleContextHandlerComponent;
    @Autowired
    private InitializeRuleStateHandlerComponent initializeRuleStateHandlerComponent;
    @Autowired
    private SelectAreaFilesHandlerComponent selectAreaFilesHandlerComponent;
    @Autowired
    private CleanTargetDirectoryHandlerComponent cleanTargetDirectoryHandlerComponent;
    @Autowired
    private CreateAreaDirectoriesHandlerComponent createAreaDirectoriesHandlerComponent;
    @Autowired
    private CopyAreaFilesHandlerComponent copyAreaFilesHandlerComponent;
    @Autowired
    private RenameAreaFilesHandlerComponent renameAreaFilesHandlerComponent;
    @Autowired
    private CheckMissingMediaFilesHandlerComponent checkMissingMediaFilesHandlerComponent;
    @Autowired
    private WriteProcessingReportsHandlerComponent writeProcessingReportsHandlerComponent;

    @Override
    public void handle() throws Exception {
        var ruleContext = prepareRuleContextHandlerComponent.handle(this);
        initializeRuleStateHandlerComponent.handle(this, ruleContext);
        selectAreaFilesHandlerComponent.handle(ruleContext);
        cleanTargetDirectoryHandlerComponent.handle(ruleContext);
        createAreaDirectoriesHandlerComponent.handle(ruleContext);
        copyAreaFilesHandlerComponent.handle(ruleContext);
        renameAreaFilesHandlerComponent.handle(ruleContext);
        checkMissingMediaFilesHandlerComponent.handle(ruleContext);
        writeProcessingReportsHandlerComponent.handle(ruleContext);
    }

    @Override
    public Map<Area, Rule> getRuleMap() {
        return Map.of(
            Area.JPN, Rules.IS_JAPAN_BASE,
            Area.USA, Rules.IS_USA_BASE,
            Area.EUR, Rules.IS_EUROPE_BASE
        );
    }

    public abstract Platform getPlatform();
}
