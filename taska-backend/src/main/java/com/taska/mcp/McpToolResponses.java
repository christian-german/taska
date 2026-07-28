package com.taska.mcp;

import com.taska.exception.ResourceNotFoundException;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/** Creates safe MCP tool results without exposing backend implementation details. */
@Slf4j
final class McpToolResponses {

    private McpToolResponses() {
    }

    static McpSchema.CallToolResult success(Object result) {
        return McpSchema.CallToolResult.builder()
                .structuredContent(result)
                .addTextContent("Operation completed successfully.")
                .build();
    }

    static McpSchema.CallToolResult execute(Supplier<Object> operation) {
        try {
            return success(operation.get());
        } catch (ResourceNotFoundException exception) {
            return error(exception.getMessage());
        } catch (IllegalArgumentException exception) {
            return error(exception.getMessage());
        } catch (Exception exception) {
            log.error("Unexpected MCP tool failure", exception);
            return error("The operation could not be completed. Please try again.");
        }
    }

    static McpSchema.CallToolResult error(String message) {
        return McpSchema.CallToolResult.builder()
                .isError(true)
                .addTextContent(message)
                .build();
    }
}
