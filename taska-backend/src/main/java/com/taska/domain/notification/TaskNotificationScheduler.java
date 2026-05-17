package com.taska.domain.notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.taska.domain.task.Task;
import com.taska.domain.task.TaskRepository;
import com.taska.domain.task.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskNotificationScheduler {

    private final TaskService taskService;
    private final TaskRepository taskRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    @Scheduled(fixedDelay = 60_000)
    public void checkUpcomingTasks() {
        LocalDateTime in15min = LocalDateTime.now().plusMinutes(15);
        List<Task> tasks = taskService.findTasksDueAround(in15min);
        List<String> tokens = deviceTokenRepository.findAll()
                .stream().map(DeviceToken::getToken).toList();

        if (tokens.isEmpty()) return;

        for (Task task : tasks) {
            for (String token : tokens) {
                sendNotification(token, "Tâche imminente", task.getContent() + " dans 15 min");
            }
            task.setIsNotified(true);
            taskRepository.save(task);
        }
    }

    private void sendNotification(String token, String title, String body) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .build();
        FirebaseMessaging.getInstance().sendAsync(message);
    }
}
