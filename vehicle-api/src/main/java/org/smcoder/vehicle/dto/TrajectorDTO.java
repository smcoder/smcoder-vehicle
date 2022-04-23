package org.smcoder.vehicle.dto;

import lombok.Data;

import java.util.List;

@Data
public class TrajectorDTO {
    private Long userId;
    private String userName;
    private Long trajectoryId;
    private List<LocationDTO> locations;
}
