package org.camunda.bpm.scenario.test.timers;

import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.scenario.Scenario;
import org.camunda.bpm.scenario.action.DeferredAction;
import org.camunda.bpm.scenario.action.UserTaskAction;
import org.camunda.bpm.scenario.delegate.TaskDelegate;
import org.camunda.bpm.scenario.test.AbstractTest;
import org.junit.Test;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Martin Schimak <martin.schimak@plexiti.com>
 */
public class EventSubprocessInterruptingTimerTest extends AbstractTest {

  @Test
  @Deployment(resources = {"org/camunda/bpm/scenario/test/timers/EventSubprocessInterruptingTimerTest.bpmn"})
  public void testCompleteTask() {

    when(scenario.actsOnUserTask("UserTask")).thenReturn(new UserTaskAction() {
      @Override
      public void execute(TaskDelegate task) {
        task.complete();
      }
    });

    Scenario.run(scenario).startByKey("EventSubprocessInterruptingTimerTest").execute();

    verify(scenario, times(1)).actsOnUserTask("UserTask");
    verify(scenario, times(1)).hasStarted("UserTask");
    verify(scenario, times(1)).hasFinished("UserTask");
    verify(scenario, times(1)).hasFinished("EndEventCompleted");
    verify(scenario, never()).hasFinished("EndEventCanceled");

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/scenario/test/timers/EventSubprocessInterruptingTimerTest.bpmn"})
  public void testExactlyReachingMaximalTimeForTask() {

    when(scenario.actsOnUserTask("UserTask")).thenReturn(new UserTaskAction() {
      @Override
      public void execute(TaskDelegate task) {
        task.defer("PT5M", new DeferredAction() {
          @Override
          public void execute() {
            // do nothing
          }
        });
      }
    });

    Scenario.run(scenario).startByKey("EventSubprocessInterruptingTimerTest").execute();

    verify(scenario, times(1)).actsOnUserTask("UserTask");
    verify(scenario, times(1)).hasStarted("UserTask");
    verify(scenario, times(1)).hasFinished("UserTask");
    verify(scenario, never()).hasFinished("EndEventCompleted");
    verify(scenario, times(1)).hasFinished("EndEventCanceled");

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/scenario/test/timers/EventSubprocessInterruptingTimerTest.bpmn"})
  public void testTakeMuchTooLongForTask() {

    when(scenario.actsOnUserTask("UserTask")).thenReturn(new UserTaskAction() {
      @Override
      public void execute(final TaskDelegate task) {
        task.defer("PT6M", new DeferredAction() {
          @Override
          public void execute() {
            task.complete();
          }
        });
      }
    });

    Scenario.run(scenario).startByKey("EventSubprocessInterruptingTimerTest").execute();

    verify(scenario, times(1)).hasStarted("UserTask");
    verify(scenario, times(1)).hasFinished("UserTask");
    verify(scenario, never()).hasFinished("EndEventCompleted");
    verify(scenario, times(1)).hasFinished("EndEventCanceled");

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/scenario/test/timers/EventSubprocessInterruptingTimerTest.bpmn"})
  public void testTakeABitTimeForTask() {

    when(scenario.actsOnUserTask("UserTask")).thenReturn(new UserTaskAction() {
      @Override
      public void execute(final TaskDelegate task) {
        task.defer("PT4M", new DeferredAction() {
          @Override
          public void execute() {
            task.complete();
          }
        });
      }
    });

    Scenario.run(scenario).startByKey("EventSubprocessInterruptingTimerTest").execute();

    verify(scenario, times(1)).actsOnUserTask("UserTask");
    verify(scenario, times(1)).hasStarted("UserTask");
    verify(scenario, times(1)).hasFinished("UserTask");
    verify(scenario, times(1)).hasFinished("EndEventCompleted");
    verify(scenario, never()).hasFinished("EndEventCanceled");

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/scenario/test/timers/EventSubprocessInterruptingTimerTest.bpmn"})
  public void testDoNothing() {

    when(scenario.actsOnUserTask("UserTask")).thenReturn(new UserTaskAction() {
      @Override
      public void execute(TaskDelegate task) {
        // Deal with task but do nothing here
      }
    });

    Scenario.run(scenario).startByKey("EventSubprocessInterruptingTimerTest").execute();

    verify(scenario, times(1)).actsOnUserTask("UserTask");
    verify(scenario, times(1)).hasStarted("UserTask");
    verify(scenario, times(1)).hasFinished("UserTask");
    verify(scenario, never()).hasFinished("EndEventCompleted");
    verify(scenario, times(1)).hasFinished("EndEventCanceled");

  }

  @Test(expected=AssertionError.class)
  @Deployment(resources = {"org/camunda/bpm/scenario/test/timers/EventSubprocessInterruptingTimerTest.bpmn"})
  public void testDoNotDealWithTask() {

    Scenario.run(scenario).startByKey("EventSubprocessInterruptingTimerTest").execute();

  }

  @Test
  @Deployment(resources = {"org/camunda/bpm/scenario/test/timers/EventSubprocessInterruptingTimerTest.bpmn"})
  public void testWhileOtherProcessInstanceIsRunning() {

    when(scenario.actsOnUserTask("UserTask")).thenReturn(new UserTaskAction() {
      @Override
      public void execute(TaskDelegate task) {
        task.complete();
      }
    });

    when(otherScenario.actsOnUserTask("UserTask")).thenReturn(new UserTaskAction() {
      @Override
      public void execute(TaskDelegate task) {
      }
    });

    Scenario.run(otherScenario).startByKey("EventSubprocessInterruptingTimerTest").execute();
    Scenario.run(scenario).startByKey("EventSubprocessInterruptingTimerTest").execute();

    verify(scenario, times(1)).actsOnUserTask("UserTask");
    verify(scenario, times(1)).hasStarted("UserTask");
    verify(scenario, times(1)).hasFinished("UserTask");
    verify(scenario, times(1)).hasFinished("EndEventCompleted");
    verify(scenario, never()).hasFinished("EndEventCanceled");

  }

}
