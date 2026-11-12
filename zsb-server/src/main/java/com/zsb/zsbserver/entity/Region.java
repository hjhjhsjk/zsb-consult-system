package com.zsb.zsbserver.entity;

import lombok.Data;

@Data
public class Region {
    private Integer id;
    private Integer parentId;
    private String name;
    private Integer level;
    private Integer sort;
}