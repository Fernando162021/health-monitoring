package com.raccoon.healthmonitoring.graphql;

import com.raccoon.healthmonitoring.alerts.Alert;
import com.raccoon.healthmonitoring.devices.Device;
import com.raccoon.healthmonitoring.devices.DeviceRepository;
import com.raccoon.healthmonitoring.vitals.Vital;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class DeviceGraphQLHandler {
    private final DeviceRepository deviceRepository;

    private static final Sinks.Many<Vital> vitalSink = Sinks.many().multicast().onBackpressureBuffer();
    private static final Sinks.Many<Alert> alertSink = Sinks.many().multicast().onBackpressureBuffer();

    @QueryMapping
    public List<Device> devices() {
        return deviceRepository.findAllByIsActiveTrue();
    }

    @SubscriptionMapping
    public Flux<Vital> liveVitals() {
        log.debug("Client subscribed to liveVitals");
        return vitalSink.asFlux();
    }

    @SubscriptionMapping
    public Flux<Alert> liveAlerts() {
        log.debug("Client subscribed to liveAlerts");
        return alertSink.asFlux();
    }

    @SchemaMapping(typeName = "Device", field = "lastVital")
    public Vital lastVital(Device device) {
        return device.getLastVital();
    }

    public static void publishVital(Vital vital) {
        vitalSink.tryEmitNext(vital);
    }

    public static void publishAlert(Alert alert) {
        log.info("Publishing alert via GraphQL subscription: {} for device {}", alert.getMetric(), alert.getDeviceId());
        alertSink.tryEmitNext(alert);
    }
}
