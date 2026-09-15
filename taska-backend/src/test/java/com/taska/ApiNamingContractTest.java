package com.taska;

import com.taska.domain.comment.controller.CommentController;
import com.taska.domain.label.controller.LabelController;
import com.taska.domain.planningcalendar.controller.PlanningCalendarController;
import com.taska.domain.project.controller.ProjectController;
import com.taska.domain.task.controller.TaskController;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiNamingContractTest {

    @Test
    void taskQueriesUseCamelCaseContractNames() throws NoSuchMethodException {
        Method getAll = TaskController.class.getDeclaredMethod(
                "getAll",
                UUID.class,
                String.class,
                boolean.class,
                LocalDate.class,
                LocalDate.class,
                LocalDate.class);

        assertEquals(
                List.of("projectId", "label", "showCompleted", "date", "from", "to"),
                requestParameterNames(getAll));
    }

    @Test
    void commentQueriesUseCamelCaseContractNames() throws NoSuchMethodException {
        Method getAll = CommentController.class.getDeclaredMethod("getAll", UUID.class, UUID.class);

        assertEquals(List.of("taskId", "projectId"), requestParameterNames(getAll));
    }

    @Test
    void resourcePathsUseDescriptiveVariableNames() {
        assertEquals("/{projectId}", getPath(ProjectController.class, "getById"));
        assertEquals("/{projectId}", putPath(ProjectController.class, "update"));
        assertEquals("/{projectId}", deletePath(ProjectController.class, "delete"));
        assertEquals("/{projectId}/tasks", getPath(ProjectController.class, "getTasks"));

        assertEquals("/{taskId}", getPath(TaskController.class, "getById"));
        assertEquals("/{taskId}/priority-evaluation", getPath(TaskController.class, "getPriorityEvaluation"));
        assertEquals("/{taskId}", putPath(TaskController.class, "update"));
        assertEquals(
                "/{taskId}/occurrences/{occurrenceScheduledAt}/following",
                putPath(TaskController.class, "replaceFollowing"));
        assertEquals(
                "/{taskId}/occurrences/{occurrenceScheduledAt}",
                putPath(TaskController.class, "replaceOccurrence"));

        assertEquals("/{labelId}", getPath(LabelController.class, "getById"));
        assertEquals("/{labelId}", putPath(LabelController.class, "update"));
        assertEquals("/{labelId}", deletePath(LabelController.class, "delete"));
        assertEquals("/{commentId}", putPath(CommentController.class, "update"));
        assertEquals("/{commentId}", deletePath(CommentController.class, "delete"));
        assertEquals("/{planningCalendarId}", getPath(PlanningCalendarController.class, "getById"));
        assertEquals("/{planningCalendarId}", putPath(PlanningCalendarController.class, "update"));
    }

    private List<String> requestParameterNames(Method method) {
        return Arrays.stream(method.getParameters())
                .map(parameter -> parameter.getAnnotation(RequestParam.class).name())
                .toList();
    }

    private String getPath(Class<?> controller, String methodName) {
        return methodNamed(controller, methodName).getAnnotation(GetMapping.class).value()[0];
    }

    private String putPath(Class<?> controller, String methodName) {
        return methodNamed(controller, methodName).getAnnotation(PutMapping.class).value()[0];
    }

    private String deletePath(Class<?> controller, String methodName) {
        return methodNamed(controller, methodName).getAnnotation(DeleteMapping.class).value()[0];
    }

    private Method methodNamed(Class<?> controller, String methodName) {
        return Arrays.stream(controller.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
    }
}
