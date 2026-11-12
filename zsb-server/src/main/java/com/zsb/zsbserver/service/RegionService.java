package com.zsb.zsbserver.service;

import com.zsb.zsbserver.entity.Region;
import java.util.List;

public interface RegionService {
    List<Region> getRegions(Integer parentId);
}