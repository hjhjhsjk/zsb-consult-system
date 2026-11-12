package com.zsb.zsbserver.service;

import com.zsb.zsbserver.entity.Major;
import com.zsb.zsbserver.mapper.MajorMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MajorServiceImpl implements MajorService {

    @Autowired
    private MajorMapper majorMapper;

    @Override
    public List<Major> getAllMajors() {
        return majorMapper.findAllMajors();
    }

    @Override
    public void addMajor(Major major) {
        majorMapper.insertMajor(major);
    }

    @Override
    public void deleteMajor(Integer id) {
        majorMapper.deleteMajorById(id);
    }
}