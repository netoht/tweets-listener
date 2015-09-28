package com.github.netoht.tweets.listener.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;

@Configuration
public class SpringConfig {

    @Autowired
    public void configureMetrics(MetricRegistry metrics) {
        ConsoleReporter report = ConsoleReporter.forRegistry(metrics)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build();

        report.start(1, TimeUnit.MINUTES);
    }
}
