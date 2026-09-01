package com.github.deathbit.retroboy.domain.game;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoIntroGame implements Game {
    private String id;
    private String packageId;
    private List<String> areas;
    private String title;
    private String additional;
    private String adult;
    private String aftermarket;
    private String alt;
    private String bios;
    private String categories;
    private String clone;
    private String complete;
    private String dat;
    private String datter_note;
    private String description;
    private String devstatus;
    private String langchecked;
    private String languages;
    private String licensed;
    private String listed;
    private String name;
    private String name_alt;
    private String physical;
    private String region;
    private String regparent;
    private String showlang;
    private String special1;
    private String special2;
    private String sticky_note;
    private String version1;
    private String version2;
}
