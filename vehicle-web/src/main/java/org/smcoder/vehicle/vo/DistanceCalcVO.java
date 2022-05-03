package org.smcoder.vehicle.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class DistanceCalcVO implements Serializable {

    private Double value;

    private String name;
}
