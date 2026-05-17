package com.taska.domain.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.taska.domain.task.Task;
import com.taska.domain.task.TaskRepository;
import com.taska.domain.task.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "taska.notification.enabled", havingValue = "true", matchIfMissing = true)
public class TaskNotificationScheduler {

    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    @Scheduled(fixedDelayString = "${taska.notification.scheduler-delay}")
    public void checkUpcomingTasks() {
        log.debug("Checking for upcoming tasks to notify");
        Instant in15min = Instant.now().plus(15, ChronoUnit.MINUTES);
        log.debug("Checking tasks due around {}", in15min);
        List<Task> tasks = taskService.findTasksDueAround(in15min);
        log.debug("Found {} tasks due around {}", tasks.size(), in15min);
        List<String> tokens = deviceTokenRepository.findAll()
                .stream().map(DeviceToken::getToken).toList();
        log.debug("Found {} device tokens", tokens.size());

        if (tokens.isEmpty()) return;

        for (Task task : tasks) {
            for (String token : tokens) {
                sendNotification(token, task.getContent() + " dans 15 min", task.getDescription(), task);
            }
            task.setIsNotified(true);
            taskRepository.save(task);
        }
    }

    private void sendNotification(String token, String title, String body, Task task) {
        log.debug("Sending notification for task {} to device token {}", task.getId(), token);
        Message message = Message.builder()
                .setToken(token)
                .putData("task_id", task.getId().toString())
                .putData("title", title != null ? title : "")
                .putData("body", body != null ? body : "")
                .build();
        FirebaseMessaging.getInstance().sendAsync(message);
    }
}
