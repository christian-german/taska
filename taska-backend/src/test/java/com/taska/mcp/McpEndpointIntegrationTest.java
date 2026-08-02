package com.taska.mcp;

import com.taska.domain.project.ProjectMapper;
import com.taska.domain.project.ProjectService;
import com.taska.domain.task.TaskMapper;
import com.taska.domain.task.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest(
        classes = McpEndpointIntegrationTest.TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration," +
                        "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration," +
                        "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration",
                "spring.ai.mcp.server.protocol=STATELESS",
                "spring.ai.mcp.server.streamable-http.mcp-endpoint=/mcp",
                "spring.ai.mcp.server.capabilities.resource=false",
                "spring.ai.mcp.server.capabilities.prompt=false",
                "spring.ai.mcp.server.capabilities.completion=false"
        })
class McpEndpointIntegrationTest {

    private static final String INITIALIZE = """
            {"jsonrpc":"2.0","id":1,"method":"initialize","params":{
              "protocolVersion":"2025-06-18","capabilities":{},
              "clientInfo":{"name":"taska-test","version":"1.0"}}}
            """;
    private static final String LIST_TOOLS = """
            {"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}
            """;
    private static final String LIST_PROJECTS = """
            {"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"list_projects","arguments":{}}}
            """;
    private static final String LIST_TASKS = """
            {"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"list_tasks","arguments":{
              "taskListInput":{"projectId":null,"sectionId":null,"label":null,"filter":null,"showCompleted":false}}}}
            """;

    @LocalServerPort
    private int port;

    @Test
    void mcpEndpointRejectsMissingAndInvalidBearerTokens() throws Exception {
        HttpResponse<String> missingToken = post(INITIALIZE, null);
        HttpResponse<String> invalidToken = post(INITIALIZE, "invalid-token");

        assertThat(missingToken.statusCode()).isEqualTo(401);
        assertThat(invalidToken.statusCode()).isEqualTo(401);
    }

    @Test
    void authenticatedClientCanInitializeAndDiscoverTaskaTools() throws Exception {
        HttpResponse<String> initialization = post(INITIALIZE, "test-token");
        HttpResponse<String> tools = post(LIST_TOOLS, "test-token");

        assertThat(initialization.statusCode()).isEqualTo(200);
        assertThat(tools.statusCode()).isEqualTo(200);
        assertThat(tools.body()).contains("list_projects", "list_tasks", "create_task", "complete_task");
    }

    @Test
    void authenticatedClientCanInvokeAProjectTool() throws Exception {
        HttpResponse<String> response = post(LIST_PROJECTS, "test-token");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Operation completed successfully", "\"structuredContent\":{\"projects\":[]}")
                .doesNotContain("isError\":true");
    }

    @Test
    void taskListUsesAnObjectAsTheStructuredContentRoot() throws Exception {
        HttpResponse<String> response = post(LIST_TASKS, "test-token");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("\"structuredContent\":{\"tasks\":[]}");
    }

    private HttpResponse<String> post(String body, String token) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/mcp"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(body));
        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }
        return HttpClient.newHttpClient().send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(TestSecurityConfiguration.class)
    static class TestApplication {

        @Bean
        ProjectMcpTools projectMcpTools() {
            ProjectService projectService = mock(ProjectService.class);
            org.mockito.Mockito.when(projectService.findAll()).thenReturn(List.of());
            return new ProjectMcpTools(projectService, mock(ProjectMapper.class));
        }

        @Bean
        TaskMcpTools taskMcpTools() {
            TaskService taskService = mock(TaskService.class);
            org.mockito.Mockito.when(taskService.findAll(null, null, null, null, false)).thenReturn(List.of());
            return new TaskMcpTools(taskService, mock(TaskMapper.class));
        }
    }

    static class TestSecurityConfiguration {

        @Bean
        SecurityFilterChain securityFilterChain(HttpSecurity http) {
            return http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                    .build();
        }

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> {
                if ("invalid-token".equals(token)) {
                    throw new BadJwtException("Invalid token");
                }
                return Jwt.withTokenValue(token)
                        .header("alg", "none")
                        .subject("taska-user")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(300))
                        .claims(claims -> claims.put("scope", "taska"))
                        .build();
            };
        }
    }
}
