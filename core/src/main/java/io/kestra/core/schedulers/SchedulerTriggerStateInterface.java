package io.kestra.core.schedulers;

import io.kestra.core.models.triggers.Trigger;
import io.kestra.core.models.triggers.TriggerContext;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Optional;

public interface SchedulerTriggerStateInterface {
    Optional<Trigger> findLast(TriggerContext trigger);

    List<Trigger> findAllForAllTenants();

    Trigger save(Trigger trigger, ScheduleContextInterface scheduleContextInterface) throws ConstraintViolationException;

    Trigger save(Trigger trigger) throws ConstraintViolationException;
}
