package org.smcoder.vehicle.dto;

import lombok.Data;

@Data
public class LocationDTO {
    private Double latitude;
    private Double longitude;
    private Long timestamp;
    private String userName;
    private Long userId;
    private Long trajectoryId;
}
