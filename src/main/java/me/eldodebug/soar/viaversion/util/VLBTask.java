package me.eldodebug.soar.viaversion.util;

import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.scheduler.Task;
import com.viaversion.viaversion.api.scheduler.TaskStatus;

public class VLBTask implements PlatformTask<Task> {

    private final Task object;

    public VLBTask(Task object) {
        this.object = object;
    }

    @Override
    public Task getObject() {
        return object;
    }

    @Override
    public void cancel() {
        object.cancel();
    }

    public TaskStatus getStatus() {
        return this.getObject().status();
    }
}