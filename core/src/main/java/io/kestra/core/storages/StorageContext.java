package io.kestra.core.storages;

import io.kestra.core.models.executions.TaskRun;
import io.kestra.core.utils.Slugify;
import lombok.Getter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Context used for storing and retrieving data from Kestra's storage.
 */
@Getter
public abstract class StorageContext {

    private final String tenantId;

    private final String namespace;

    private final String flowId;

    private final String executionId;

    public StorageContext(String tenantId,
                          String namespace,
                          String flowId,
                          String executionId) {
        this.tenantId = tenantId;
        this.namespace = namespace;
        this.flowId = flowId;
        this.executionId = executionId;
    }

    /**
     * Gets the base storage URI for this context execution.
     *
     * @return the {@link URI}.
     */
    public URI getExecutionStorageURI() {
        try {
            return new URI("//" + fromParts(
                namespace.replace(".", "/"),
                Slugify.of(flowId),
                "executions",
                executionId
            ));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Gets the base storage URI for this context.
     *
     * @return the {@link URI}.
     */
    public abstract URI getContextStorageURI();

    /**
     * Factory method for constructing a new {@link StorageContext} scoped to a given Task.
     */
    public static StorageContext forTask(TaskRun taskRun) {
        return new StorageContext.Task(
            taskRun.getTenantId(),
            taskRun.getNamespace(),
            taskRun.getFlowId(),
            taskRun.getExecutionId(),
            taskRun.getTaskId(),
            taskRun.getId(),
            taskRun.getValue()
        );
    }

    /**
     * Factory method for constructing a new {@link StorageContext} scoped to a given Task.
     */
    public static StorageContext forTask(String tenantId,
                                         String namespace,
                                         String flowId,
                                         String executionId,
                                         String taskId,
                                         String taskRunId,
                                         String taskRunValue) {
        return new StorageContext.Task(tenantId, namespace, flowId, executionId, taskId, taskRunId, taskRunValue);
    }

    /**
     * Factory method for constructing a new {@link StorageContext} scoped to a given Trigger.
     */
    public static StorageContext forTrigger(String tenantId,
                                            String namespace,
                                            String flowId,
                                            String executionId,
                                            String triggerId) {
        return new StorageContext.Trigger(tenantId, namespace, flowId, executionId, triggerId);
    }

    protected static String fromParts(String... parts) {
        return "/" + Arrays.stream(parts).filter(Objects::nonNull).collect(Collectors.joining("/"));
    }


    /**
     * A storage context scoped to a Task.
     */
    @Getter
    public static class Task extends StorageContext {

        private final String taskId;
        private final String taskRunId;
        private final String taskRunValue;

        private Task(String tenantId,
                     String namespace,
                     String flowId,
                     String executionId,
                     String taskId,
                     String taskRunId,
                     String taskRunValue) {
            super(tenantId, namespace, flowId, executionId);
            this.taskId = taskId;
            this.taskRunId = taskRunId;
            this.taskRunValue = taskRunValue;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public URI getContextStorageURI() {
            try {
                return new URI("//" + fromParts(
                    getNamespace().replace(".", "/"),
                    Slugify.of(getFlowId()),
                    "executions",
                    getExecutionId(),
                    "tasks",
                    Slugify.of(getTaskId()),
                    getTaskRunId()
                ));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    /**
     * A storage context scoped to a Trigger.
     */
    @Getter
    public static class Trigger extends StorageContext {

        private final String triggerId;

        private Trigger(String tenantId,
                        String namespace,
                        String flowId,
                        String executionId,
                        String triggerId) {
            super(tenantId, namespace, flowId, executionId);
            this.triggerId = triggerId;
        }

        /**
         * {@inheritDoc}
         **/
        @Override
        public URI getContextStorageURI() {
            try {
                return new URI("//" + fromParts(
                    getNamespace().replace(".", "/"),
                    Slugify.of(getFlowId()),
                    "executions",
                    getExecutionId(),
                    "trigger",
                    Slugify.of(getTriggerId())
                ));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }
}
