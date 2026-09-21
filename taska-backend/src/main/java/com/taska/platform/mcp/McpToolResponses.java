package com.taska.platform.mcp;

import com.taska.platform.exception.ResourceNotFoundException;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates safe MCP tool results without exposing backend implementation details.
 *
 * <p>
 * Shared by every feature's MCP adapter, the way the global exception handler is shared by every HTTP controller.
 */
public final class McpToolResponses {

    private static final Logger log = LoggerFactory.getLogger(McpToolResponses.class);

    private McpToolResponses() {
    }

    public static McpSchema.CallToolResult success(Object operationResult) {
        return McpSchema.CallToolResult.builder().structuredContent(operationResult).addTextContent("Operation completed successfully.").build();
    }

    public static McpSchema.CallToolResult execute(Supplier<Object> operation) {
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

    public static McpSchema.CallToolResult error(String message) {
        return McpSchema.CallToolResult.builder().isError(true).addTextContent(message).build();
    }
}
