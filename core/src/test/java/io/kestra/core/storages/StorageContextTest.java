package io.kestra.core.storages;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class StorageContextTest {

    @Test
    void shouldGetValidURIForTaskContext() {
        StorageContext context = StorageContext.forTask(
            "???",
            "namespace",
            "flowid",
            "executionid",
            "taskid",
            "taskrun",
            null
        );

        assertThat(context.getExecutionStorageURI(), is(URI.create("///namespace/flowid/executions/executionid")));
        assertThat(context.getContextStorageURI(), is(URI.create("///namespace/flowid/executions/executionid/tasks/taskid/taskrun")));
    }

    @Test
    void shouldGetValidURIForTriggerContext() {
        StorageContext context = StorageContext.forTrigger(
            "???",
            "namespace",
            "flowid",
            "executionid",
            "triggerid"
        );

        assertThat(context.getExecutionStorageURI(), is(URI.create("///namespace/flowid/executions/executionid")));
        assertThat(context.getContextStorageURI(), is(URI.create("///namespace/flowid/executions/executionid/trigger/triggerid")));
    }
}