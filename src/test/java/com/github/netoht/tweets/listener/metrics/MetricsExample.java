package com.github.netoht.tweets.listener.metrics;

import static com.codahale.metrics.MetricRegistry.name;

import java.util.concurrent.TimeUnit;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

public class MetricsExample {
    static final MetricRegistry metrics = new MetricRegistry();

    private static int i = 1;

    public static void main(String args[]) {
        startReport();
        Meter requests = metrics.meter(name(MetricsExample.class, "received.requests"));
        requests.mark(++i);

        metrics.counter(name(MetricsExample.class, "error.requests", "size")).inc(i);
        metrics.counter(name(MetricsExample.class, "error.requests2", "size")).inc(i);
        metrics.histogram(name(MetricsExample.class, "teste.histogram")).update(--i);
        metrics.histogram(name(MetricsExample.class, "teste.histogram2")).update(--i);

        metrics.register(name(MetricsExample.class, "teste.gauge"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return i++;
            }
        });

        Timer timer = metrics.timer(name(MetricsExample.class, "timer.teste"));
        Context context = timer.time();
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
        }
        context.stop();

        waitSomeSeconds();
    }

    static void startReport() {
        ConsoleReporter reporter = ConsoleReporter.forRegistry(metrics).convertRatesTo(TimeUnit.SECONDS).convertDurationsTo(TimeUnit.MILLISECONDS).build();
        reporter.start(1, TimeUnit.SECONDS);
    }

    static void waitSomeSeconds() {
        try {
            TimeUnit.MINUTES.sleep(1);
        } catch (InterruptedException e) {
        }
    }
}