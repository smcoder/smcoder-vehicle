package org.smcoder.vehicle.generate;

import org.apache.ibatis.annotations.Param;
import org.smcoder.vehicle.vo.VehicleVO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * VehicleDao继承基类
 */
@Repository
public interface VehicleDao extends MyBatisBaseDao<Vehicle, Integer> {

    List<VehicleVO> dayQuery(Vehicle vehicle);

    List<VehicleVO> monthQuery();

    String weekQuery(@Param("dtStart") String dtStart, @Param("dtEnd") String dtEnd);
}