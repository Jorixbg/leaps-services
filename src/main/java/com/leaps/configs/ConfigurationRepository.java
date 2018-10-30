package com.leaps.configs;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ConfigurationRepository extends CrudRepository<Configuration, Long> {

    List<Configuration> findByKey(String key);
    
}
