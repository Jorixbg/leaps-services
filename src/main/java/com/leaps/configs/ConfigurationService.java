package com.leaps.configs;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfigurationService {
	
	@Autowired
	ConfigurationRepository configurationRepository;
	
	public List<Configuration> loadConfigs() {
		List<Configuration> configs = new ArrayList<>();
		configurationRepository.findAll().forEach(configs::add);
		return configs;
	}
	
	public Configuration getByKey(String key) {
		return configurationRepository.findByKey(key).get(0);
	}
}
