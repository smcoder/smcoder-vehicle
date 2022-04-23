package org.smcoder.vehicle.vo;

import lombok.Data;

import java.util.List;

@Data
public class VehiclePathVO {
    private String name;
    private List<List<Double>> path;
}
