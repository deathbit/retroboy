package com.github.deathbit.retroboy.domain;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FileContext {
    private String fileName;
    private String fullName;
    private String namePart;
    private String tagPart;
    private List<String> tags;
}
