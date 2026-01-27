package com.github.deathbit.retroboy.domain;

import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileContext {
    private String fileName;
    private String fullName;
    private String namePart;
    private String tagPart;
    private Set<String> tags;
}
