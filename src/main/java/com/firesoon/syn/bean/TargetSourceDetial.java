package com.firesoon.syn.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class TargetSourceDetial {
    private String targetSourceType;
    private int sourceId;

}
