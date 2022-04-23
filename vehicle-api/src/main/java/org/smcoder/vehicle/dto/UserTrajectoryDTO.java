package org.smcoder.vehicle.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserTrajectoryDTO {
    private Long userId;
    private String userName;
    private List<TrajectorDTO> trajectories;
}
