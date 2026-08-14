package miju.com.robodelivery.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties
@Configuration
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemProperties {
    private int minimumLoadingBatteryCapacity;
    private double maximumBoxWeightLimit;
    private double minimumBoxWeightLimit;
    private String successfulReturnWebhookKey;
}
