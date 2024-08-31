package com.thegreatchicken.TGCPlugin.utils;

import com.thegreatchicken.TGCPlugin.PluginLoader;

public class Scheduler {
    public static class Task implements Runnable
    {
        private int      id       = -1;
        private Runnable runnable = null;

        private boolean closed = false;

        public void cancelAndRun () {
            if (closed) return ;

            PluginLoader.BUKKIT_SERVER
                .getScheduler()
                .cancelTask(id);
            
            this.run();
            this.close();
        }
        private void close () {
            this.closed = true;
        }

        @Override
        public void run() {
            this.close();
            this.runnable.run();
        }
    }

    public static Task schedule (Runnable runnable, int duration) {
        Task task = new Task();
        task.runnable = runnable;
        
        task.id = PluginLoader.BUKKIT_SERVER
            .getScheduler()
            .scheduleSyncDelayedTask(PluginLoader.PLUGIN, () -> {
                task.run();
            }, duration);

        return task;
    }
}
