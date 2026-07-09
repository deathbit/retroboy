package com.github.deathbit.retroboy.handler.component.impl;

import com.github.deathbit.retroboy.domain.RuleContext;
import com.github.deathbit.retroboy.enums.Area;
import com.github.deathbit.retroboy.handler.Handler;
import com.github.deathbit.retroboy.domain.HandlerMediaTypes;
import com.github.deathbit.retroboy.domain.MediaType;
import com.github.deathbit.retroboy.handler.component.InitializeRuleStateHandlerComponent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class InitializeRuleStateHandlerComponentImpl implements InitializeRuleStateHandlerComponent {

    @Override
    public void handle(Handler handler, RuleContext ruleContext) {
        ruleContext.setRuleMap(handler.getRuleMap());
        ruleContext.setAreaRenameReportMap(createEmptyAreaReportMap(ruleContext));
        ruleContext.setAreaDuplicateNameReportMap(createEmptyAreaReportMap(ruleContext));
        ruleContext.setAreaFailureReportMap(createEmptyAreaReportMap(ruleContext));
        ruleContext.setAreaMissingMediaReportMap(createEmptyAreaMissingMediaReportMap(ruleContext));
    }

    private Map<Area, List<String>> createEmptyAreaReportMap(RuleContext ruleContext) {
        var areaReportMap = new LinkedHashMap<Area, List<String>>();
        for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            areaReportMap.put(areaConfig.getArea(), new ArrayList<>());
        }
        return areaReportMap;
    }

    private Map<Area, Map<String, List<String>>> createEmptyAreaMissingMediaReportMap(RuleContext ruleContext) {
        var areaReportMap = new LinkedHashMap<Area, Map<String, List<String>>>();
        for (var areaConfig : ruleContext.getRuleConfig().getTargetAreaConfigs()) {
            var mediaReportMap = new LinkedHashMap<String, List<String>>();
            for (MediaType mediaType : HandlerMediaTypes.MEDIA_TYPES) {
                mediaReportMap.put(mediaType.name(), new ArrayList<>());
            }
            areaReportMap.put(areaConfig.getArea(), mediaReportMap);
        }
        return areaReportMap;
    }
}

