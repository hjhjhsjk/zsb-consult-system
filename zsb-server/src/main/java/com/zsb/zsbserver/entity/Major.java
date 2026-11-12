package com.zsb.zsbserver.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Major {
    private Integer id;
    @JsonProperty("c_name")
    private String cName;
}