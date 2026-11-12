package com.zsb.zsbserver.service;

import com.zsb.zsbserver.entity.Major;
import java.util.List;

public interface MajorService {
    List<Major> getAllMajors();
    void addMajor(Major major);
    void deleteMajor(Integer id);
}