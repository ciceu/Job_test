package ro.hypercloud.hycoder.course.application.use_cases.teacher;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ro.hypercloud.hycoder.common.UseCase;
import ro.hypercloud.hycoder.course.application.port.TaskPort;
import ro.hypercloud.hycoder.course.application.port.TasksLibraryPort;
import ro.hypercloud.hycoder.course.application.port.TeacherPort;
import ro.hypercloud.hycoder.course.domain.Task;
import ro.hypercloud.hycoder.course.domain.TasksLibrary;
import ro.hypercloud.hycoder.course.domain.exceptions.TaskNotFoundException;
import ro.hypercloud.hycoder.course.domain.exceptions.TasksLibraryNotFoundException;
import ro.hypercloud.hycoder.course.domain.exceptions.TeacherNotFoundByIdException;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class DeleteTaskFromTaskLibraryUseCase extends UseCase<DeleteTaskFromTaskLibraryUseCase.InputValues, DeleteTaskFromTaskLibraryUseCase.OutputValues> {


    public DeleteTaskFromTaskLibraryUseCase(TasksLibraryPort tasksLibraryPort, TaskPort taskPort, TeacherPort teacherPort) {
        this.tasksLibraryPort = tasksLibraryPort;
        this.taskPort = taskPort;
        this.teacherPort = teacherPort;
    }

    @Transactional
    @Override
    public OutputValues execute(InputValues input) {
        log.debug("Task with id {} is going to be deleted by teacher with id {}", input.taskId, input.teacherId);
        try {
            teacherPort.validateTeacherById(input.teacherId);
        } catch (RuntimeException e) {
            throw new TeacherNotFoundByIdException();
        }
        List<TasksLibrary> tasksLibraries = tasksLibraryPort.findByTeacherId(input.getTeacherId());
        Task task = taskPort.findById(input.getTaskId()).orElseThrow(TaskNotFoundException::new);

        if (tasksLibraries.isEmpty())
            throw new TasksLibraryNotFoundException();

        tasksLibraries.forEach(t -> {
            if (!t.getTasks().contains(task))
                throw new TaskNotFoundException();
            taskPort.deleteById(task.getId());
        });
        log.debug("Task deleted successfully!");
        return new OutputValues("Task deleted successfully!");
    }

    @Value
    public static class InputValues implements UseCase.InputValues {
        private final UUID teacherId;
        private final UUID taskId;
    }

    @Value
    public static class OutputValues implements UseCase.OutputValues {
        private final String message;
    }
}

