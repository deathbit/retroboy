package com.github.deathbit.retroboy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileContext {
    private String fileName;
    private String fullName;
    private String namePart;
    private String tagPart;
    private Set<String> tags;
}
