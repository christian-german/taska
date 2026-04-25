package com.taska.config;

import com.taska.model.Project;
import com.taska.model.ViewStyle;
import com.taska.repository.ProjectRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements ApplicationRunner {

    private final ProjectRepository projectRepository;

    public DataInitializer(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (projectRepository.findByIsInboxProjectTrue().isEmpty()) {
            Project inbox = new Project();
            inbox.setName("Inbox");
            inbox.setIsInboxProject(true);
            inbox.setColor("charcoal");
            inbox.setPosition(0);
            inbox.setIsFavorite(false);
            inbox.setViewStyle(ViewStyle.LIST);
            projectRepository.save(inbox);
        }
    }
}
