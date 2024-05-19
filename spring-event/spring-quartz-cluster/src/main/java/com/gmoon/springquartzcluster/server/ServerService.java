package com.gmoon.springquartzcluster.server;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gmoon.springquartzcluster.model.WebServerSaveForm;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServerService {
	private final ServerRepository serverRepository;

	@Transactional(readOnly = true)
	@Cacheable(value = ServerCacheNames.SERVER_ALL)
	public List<Server> getEnabledServers() {
		return serverRepository.getEnabledServers();
	}

	@Transactional(readOnly = true)
	@Cacheable(value = ServerCacheNames.SERVER_FIND_BY_NAME, key = "#serverName")
	public Server getServer(String serverName) {
		return serverRepository.findServerByName(serverName);
	}

	@Transactional
	@CacheEvict(value = ServerCacheNames.SERVER_FIND_BY_NAME, key = "#saveForm.name")
	public WebServerSaveForm saveWebServer(WebServerSaveForm saveForm) {
		String name = saveForm.getName();
		Server webServer = Optional.ofNullable(serverRepository.findServerByName(name))
			 .map(saveForm::toEntity)
			 .orElseGet(saveForm::createEnabledWebServer);

		Server savedServer = serverRepository.save(webServer);
		return WebServerSaveForm.from(savedServer);
	}
}
