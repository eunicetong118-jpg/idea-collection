package com.ideacollection.repository;

import com.ideacollection.model.Theme;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ThemeRepository extends MongoRepository<Theme, String> {
}