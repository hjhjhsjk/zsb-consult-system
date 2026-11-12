package com.zsb.zsbserver.service;

import com.zsb.zsbserver.entity.Region;
import com.zsb.zsbserver.mapper.RegionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RegionServiceImpl implements RegionService {

    @Autowired
    private RegionMapper regionMapper;

    @Override
    public List<Region> getRegions(Integer parentId) {
        if (parentId == 0) {
            return regionMapper.findProvinces();
        } else {
            return regionMapper.findCitiesByProvinceId(parentId);
        }
    }
}