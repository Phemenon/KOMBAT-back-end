package com.kombat.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class MinionSelectionUpdateRequest {
    private String userId;
    private Set<String> selectedTypes;
}