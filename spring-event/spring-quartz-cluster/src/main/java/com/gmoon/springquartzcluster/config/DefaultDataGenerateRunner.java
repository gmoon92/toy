package com.gmoon.springquartzcluster.config;

import java.util.List;
import java.util.Optional;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.gmoon.springquartzcluster.model.WebServerSaveForm;
import com.gmoon.springquartzcluster.quartz.QuartzSchedulerHistory;
import com.gmoon.springquartzcluster.quartz.QuartzSchedulerHistoryRepository;
import com.gmoon.springquartzcluster.server.ServerService;
import com.gmoon.springquartzcluster.util.LocalIpAddressUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultDataGenerateRunner implements ApplicationRunner {
	private final ServerService serverService;
	private final QuartzSchedulerHistoryRepository schedulerHistoryRepository;
	private final Scheduler scheduler;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		saveSchedulerHistory();
		saveEnabledWebServers();
	}

	private void saveSchedulerHistory() throws SchedulerException {
		String instanceId = scheduler.getSchedulerInstanceId();
		List<String> ipAddresses = LocalIpAddressUtil.getIpAddresses();
		QuartzSchedulerHistory history = QuartzSchedulerHistory.from(instanceId, ipAddresses);
		schedulerHistoryRepository.save(history);
	}

	private void saveEnabledWebServers() {
		log.info("init Web Server Data...");
		WebServerSaveForm form = getWebServerSaveForm("gmoon");
		serverService.saveWebServer(form);
	}

	private WebServerSaveForm getWebServerSaveForm(String serverName) {
		return Optional.ofNullable(serverService.getServer(serverName))
			 .map(WebServerSaveForm::from)
			 .orElseGet(() -> {
				 WebServerSaveForm form = new WebServerSaveForm();
				 form.setName(serverName);
				 form.setPublicHost("gmoon92.github.io");
				 form.setPrivateHost("127.0.0.1");
				 form.setPort1(443);
				 form.setPort2(80);
				 return form;
			 });
	}
}
