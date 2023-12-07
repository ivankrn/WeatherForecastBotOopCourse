package ru.urfu.weatherforecastbot.service;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Фэйковая реализация {@link ScheduledExecutorService ScheduledExecutorService} для тестирования, позволяющая вручную
 * управлять временем
 */
class FakeScheduledExecutorService extends AbstractExecutorService implements ScheduledExecutorService {

    /**
     * Работы, запланированные для выполнения
     */
    private final Collection<Job<?>> jobs = new CopyOnWriteArrayList<>();
    /**
     * Время
     */
    private long offsetInNanos = 0;

    /**
     * Сдвигает время вперед на определенное количество
     *
     * @param time количество временных единиц
     * @param timeUnit единица измерения времени
     */
    public void elapse(long time, TimeUnit timeUnit) {
        offsetInNanos += NANOSECONDS.convert(time, timeUnit);

        for (Job<?> job : jobs) {
            if (offsetInNanos >= job.initialDelayNanos) {
                jobs.remove(job);
                job.run();
            }
        }
    }


    @Override
    public ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
        return schedule(Executors.callable(runnable, null), delay, timeUnit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit timeUnit) {
        return scheduleInternal(callable, delay, 0, timeUnit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return scheduleInternal(Executors.callable(runnable, null), delay, Math.abs(period), timeUnit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
        return scheduleInternal(Executors.callable(runnable, null), delay, -Math.abs(period), timeUnit);
    }

    @Override
    public void execute(Runnable command) {
        schedule(command, 0, NANOSECONDS);
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        return true;
    }

    /**
     * Планирует задачу с указанными параметрами
     *
     * @param callable задача, возвращающая результат
     * @param delay задержка
     * @param period период
     * @param timeUnit единица измерения времени
     * @param <V> возвращаемое значение задачи
     * @return отложенный результат выполнения задачи
     */
    private <V> ScheduledFuture<V> scheduleInternal(Callable<V> callable, long delay, long period, TimeUnit timeUnit) {
        Job<V> job = new Job<>(callable, offsetInNanos + NANOSECONDS.convert(delay, timeUnit),
                NANOSECONDS.convert(period, timeUnit));
        jobs.add(job);
        return job;
    }

    /**
     * Фэйковая реализация задачи
     *
     * @param <V> тип возвращаемого значения задачи
     */
    class Job<V> extends FutureTask<V> implements ScheduledFuture<V> {

        /**
         * Задача, возвращающая результат
         */
        final Callable<V> task;
        /**
         * Начальная задержка в наносекундах
         */
        final long initialDelayNanos;
        /**
         * Период в наносекундах
         */
        final long periodNanos;

        /**
         * Создает экземпляр Job, используя указанные параметры
         *
         * @param task задача, возвращающая результат
         * @param initialDelayInNanos начальная задержка в наносекундах
         * @param periodInNanos период в наносекундах
         */
        public Job(Callable<V> task, long initialDelayInNanos, long periodInNanos) {
            super(task);
            this.task = task;
            this.initialDelayNanos = initialDelayInNanos;
            this.periodNanos = periodInNanos;
        }

        @Override
        public long getDelay(TimeUnit timeUnit) {
            return timeUnit.convert(initialDelayNanos, NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed delayed) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void run() {
            if (periodNanos == 0) {
                super.run();
            } else {
                if (super.runAndReset()) {
                    jobs.add(reschedule(offsetInNanos));
                }
            }
        }

        /**
         * Возвращает новый экземпляр задачи с новым временем
         *
         * @param offset сдвиг в наносекундах
         * @return новая задача
         */
        private Job<V> reschedule(long offset) {
            if (periodNanos < 0) {
                return new Job<>(task, offset, periodNanos);
            }
            long newDelay = initialDelayNanos;
            while (newDelay <= offset) {
                newDelay += periodNanos;
            }
            return new Job<>(task, newDelay, periodNanos);
        }

    }

}
