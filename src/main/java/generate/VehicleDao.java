package generate;

import org.springframework.stereotype.Repository;

/**
 * VehicleDao继承基类
 */
@Repository
public interface VehicleDao extends MyBatisBaseDao<Vehicle, Integer> {
}