package com.taska;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.taska.comment.adapter.http.CommentController;
import com.taska.label.adapter.http.LabelController;
import com.taska.planningcalendar.adapter.http.PlanningCalendarController;
import com.taska.priority.adapter.http.TaskPriorityEvaluationController;
import com.taska.project.adapter.http.ProjectController;
import com.taska.task.adapter.http.ProjectTaskController;
import com.taska.task.adapter.http.TaskController;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

class ApiNamingContractTest {

    @Test
    void taskQueriesUseCamelCaseContractNames() throws NoSuchMethodException {
        Method getAll = TaskController.class
                .getDeclaredMethod("getAll", UUID.class, String.class, boolean.class, LocalDate.class, LocalDate.class, LocalDate.class);

        assertEquals(List.of("projectId", "label", "showCompleted", "date", "from", "to"), requestParameterNames(getAll));
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
        // A project's tasks are served by the task module, which owns the projection
        // they return.
        assertEquals("/projects/{projectId}/tasks", resourcePath(ProjectTaskController.class));

        assertEquals("/{taskId}", getPath(TaskController.class, "getById"));
        // A task's computed priority is served by the priority module, which owns the
        // evaluation.
        assertEquals("/tasks/{taskId}/priority-evaluation", resourcePath(TaskPriorityEvaluationController.class));
        assertEquals("/{taskId}", putPath(TaskController.class, "update"));
        assertTrue(Arrays.stream(TaskController.class.getDeclaredMethods()).noneMatch(method -> method.getName().equals("replaceFollowing")));
        assertEquals("/{taskId}/occurrences/{occurrenceScheduledAt}", putPath(TaskController.class, "replaceOccurrence"));

        assertEquals("/{labelId}", getPath(LabelController.class, "getById"));
        assertEquals("/{labelId}", putPath(LabelController.class, "update"));
        assertEquals("/{labelId}", deletePath(LabelController.class, "delete"));
        assertEquals("/{commentId}", putPath(CommentController.class, "update"));
        assertEquals("/{commentId}", deletePath(CommentController.class, "delete"));
        assertEquals("/{planningCalendarId}", getPath(PlanningCalendarController.class, "getById"));
        assertEquals("/{planningCalendarId}", putPath(PlanningCalendarController.class, "update"));
    }

    private List<String> requestParameterNames(Method method) {
        return Arrays.stream(method.getParameters()).map(parameter -> parameter.getAnnotation(RequestParam.class).name()).toList();
    }

    private String resourcePath(Class<?> controller) {
        return controller.getAnnotation(RequestMapping.class).value()[0];
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
        return Arrays.stream(controller.getDeclaredMethods()).filter(method -> method.getName().equals(methodName)).findFirst().orElseThrow();
    }
}
