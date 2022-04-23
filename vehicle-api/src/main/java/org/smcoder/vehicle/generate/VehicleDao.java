package org.smcoder.vehicle.generate;

import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * VehicleDao继承基类
 */
@Repository
public interface VehicleDao extends MyBatisBaseDao<Vehicle, Integer> {

    List<Vehicle> dayQuery(Vehicle vehicle);

    List<Vehicle> monthQuery();

    List<Vehicle> yearQuery();
}