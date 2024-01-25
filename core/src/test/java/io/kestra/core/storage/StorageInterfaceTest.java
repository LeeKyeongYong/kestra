package io.kestra.core.storage;

import io.kestra.core.models.executions.Execution;
import io.kestra.core.models.executions.TaskRun;
import io.kestra.core.models.flows.Flow;
import io.kestra.core.storages.StorageInterface;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@MicronautTest
public class StorageInterfaceTest {

    @Inject
    StorageInterface storageInterface;

    @Test
    void executionPrefix() {
        var flow = Flow.builder().id("flow").namespace("namespace").build();
        var execution = Execution.builder().id("execution").namespace("namespace").flowId("flow").build();
        var taskRun = TaskRun.builder().id("taskrun").namespace("namespace").flowId("flow").executionId("execution").build();

        var prefix = storageInterface.executionPrefix(flow, execution);
        assertThat(prefix, notNullValue());
        assertThat(prefix, is("/namespace/flow/executions/execution"));

        prefix = storageInterface.executionPrefix(execution);
        assertThat(prefix, notNullValue());
        assertThat(prefix, is("/namespace/flow/executions/execution"));

        prefix = storageInterface.executionPrefix(taskRun);
        assertThat(prefix, notNullValue());
        assertThat(prefix, is("/namespace/flow/executions/execution"));
    }

    @Test
    void cachePrefix() {
        var prefix = storageInterface.cachePrefix("namespace", "flow", "task", null);
        assertThat(prefix, notNullValue());
        assertThat(prefix, is("namespace/flow/task/cache"));

        prefix = storageInterface.cachePrefix("namespace", "flow", "task", "value");
        assertThat(prefix, notNullValue());
        assertThat(prefix, startsWith("namespace/flow/task/cache/"));
    }

    @Test
    void statePrefix() {
        var prefix = storageInterface.statePrefix("namespace", "flow", "name", null);
        assertThat(prefix, notNullValue());
        assertThat(prefix, is("namespace/flow/states/name"));

        prefix = storageInterface.statePrefix("namespace", "flow", "name", "value");
        assertThat(prefix, notNullValue());
        assertThat(prefix, startsWith("namespace/flow/states/name/"));
    }

    @Test
    void outputPrefix() {
        var flow = Flow.builder().id("flow").namespace("namespace").build();
        var prefix = storageInterface.outputPrefix(flow);
        assertThat(prefix, notNullValue());
        assertThat(prefix.toString(), is("///namespace/flow"));
    }

    @Test
    void namespaceFilePrefix() {
        var prefix = storageInterface.namespaceFilePrefix("io.namespace");
        assertThat(prefix, notNullValue());
        assertThat(prefix, is("/io/namespace/_files"));
    }
}
