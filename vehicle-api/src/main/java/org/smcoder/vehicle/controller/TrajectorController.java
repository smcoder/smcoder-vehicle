package org.smcoder.vehicle.controller;

import com.alicloud.openservices.tablestore.model.*;
import org.smcoder.vehicle.algorithms.GeometryAlgorithms;
import org.smcoder.vehicle.algorithms.basic.Path;
import org.smcoder.vehicle.algorithms.basic.Rectangle;
import org.smcoder.vehicle.constants.Constants;
import org.smcoder.vehicle.dto.LocationDTO;
import org.smcoder.vehicle.dto.TrajectorDTO;
import org.smcoder.vehicle.dto.UserTrajectoryDTO;
import org.smcoder.vehicle.service.RedisService;
import org.smcoder.vehicle.utils.VOConverter;
import org.smcoder.vehicle.vo.VehiclePathVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;

@RestController
@CrossOrigin
public class TrajectorController {
    @Resource
    RedisService redisService;

    @RequestMapping(value = "/example_data",
            method = RequestMethod.GET,
            produces = "application/json;charset=UTF-8")
    @CrossOrigin
    public String exampleData() {
        File file = new File("/home/smcoder/Desktop/data.txt");
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return buffer.toString();
    }

    @RequestMapping(value = "/user_and_time_scope", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @CrossOrigin
    public List<VehiclePathVO> pathByUserAndTimeScope(@RequestParam("user_id") Long userId,
                                                      @RequestParam("start_time") Long startTime,
                                                      @RequestParam("end_time") Long endTime) {
        RangeRowQueryCriteria rangeRowQueryCriteria = new RangeRowQueryCriteria(Constants.TABLE_NAME_USER_TRAJECTOR);
        PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_USER_ID, PrimaryKeyValue.fromLong(userId));
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_TRAJECTORY_ID, PrimaryKeyValue.fromLong(startTime));
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_TIMESTAMP, PrimaryKeyValue.fromLong(startTime));
        rangeRowQueryCriteria.setInclusiveStartPrimaryKey(primaryKeyBuilder.build());
        primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_USER_ID, PrimaryKeyValue.fromLong(userId));
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_TRAJECTORY_ID, PrimaryKeyValue.fromLong(endTime));
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_TIMESTAMP, PrimaryKeyValue.fromLong(endTime));
        rangeRowQueryCriteria.setExclusiveEndPrimaryKey(primaryKeyBuilder.build());
        rangeRowQueryCriteria.setMaxVersions(1);
        GetRangeResponse response = Constants.client.getRange(new GetRangeRequest(rangeRowQueryCriteria));

        UserTrajectoryDTO result = new UserTrajectoryDTO();
        result.setUserId(userId);
        result.setTrajectories(new ArrayList());
        Map<Long, TrajectorDTO> trajectorMap = new HashMap();
        List<Row> rows = response.getRows();
        for (Row row : rows) {
            Long trajectorId = row.getPrimaryKey().getPrimaryKeyColumn(Constants.COLUMN_NAME_TRAJECTORY_ID).getValue().asLong();
            TrajectorDTO tmpTraj = null;
            if (trajectorMap.containsKey(trajectorId)) {
                tmpTraj = trajectorMap.get(trajectorId);
            } else {
                tmpTraj = new TrajectorDTO();
                tmpTraj.setTrajectoryId(trajectorId);
                tmpTraj.setLocations(new ArrayList());
                trajectorMap.put(trajectorId, tmpTraj);
            }
            LocationDTO locationDTO = new LocationDTO();
            locationDTO.setTimestamp(row.getPrimaryKey().getPrimaryKeyColumn(Constants.COLUMN_NAME_TIMESTAMP).getValue().asLong());
            for (Column column : row.getColumns()) {
                if (column.getName().equals(Constants.COLUMN_NAME_LATITUDE)) {
                    locationDTO.setLatitude(column.getValue().asDouble());
                } else if (column.getName().equals(Constants.COLUMN_NAME_LONGITUDE)) {
                    locationDTO.setLongitude(column.getValue().asDouble());
                }
            }
            tmpTraj.getLocations().add(locationDTO);
        }
        Iterator<Map.Entry<Long, TrajectorDTO>> iterator = trajectorMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, TrajectorDTO> entry = iterator.next();
            result.getTrajectories().add(entry.getValue());
        }
        return VOConverter.convertUserTrajectorDTO2TrajectorPathVO(result);
    }


    @RequestMapping(value = "/time_and_region_scope", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @CrossOrigin
    public List<VehiclePathVO> pathByTimeScope(@RequestParam("start_time") Long startTime,
                                               @RequestParam("end_time") Long endTime,
                                               @RequestParam("top_latitude") Double topLatitude,
                                               @RequestParam("bottom_latitude") Double bottomLatitude,
                                               @RequestParam("left_longitude") Double leftLongitude,
                                               @RequestParam("right_longitude") Double rightLongitude) {
        List<VehiclePathVO> redisTimeRegionValue = redisService.getRedisTimeRegionValue(startTime, endTime, topLatitude, bottomLatitude, leftLongitude, rightLongitude);
        if (redisTimeRegionValue != null) {
            return redisTimeRegionValue;
        }
        RangeRowQueryCriteria rangeRowQueryCriteria = new RangeRowQueryCriteria(Constants.TABLE_NAME_USER_TRAJECTOR);
        PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_USER_ID, PrimaryKeyValue.INF_MIN);
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_TRAJECTORY_ID, PrimaryKeyValue.fromLong(startTime));
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_TIMESTAMP, PrimaryKeyValue.fromLong(startTime));
        rangeRowQueryCriteria.setInclusiveStartPrimaryKey(primaryKeyBuilder.build());
        primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_USER_ID, PrimaryKeyValue.INF_MAX);
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_TRAJECTORY_ID, PrimaryKeyValue.fromLong(endTime));
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_TIMESTAMP, PrimaryKeyValue.fromLong(endTime));
        rangeRowQueryCriteria.setExclusiveEndPrimaryKey(primaryKeyBuilder.build());
        rangeRowQueryCriteria.setMaxVersions(1);
        GetRangeResponse response = Constants.client.getRange(new GetRangeRequest(rangeRowQueryCriteria));
        Map<Long, Map<Long, TrajectorDTO>> userIdTrajectoriesDtoMap = new HashMap();
        List<Row> rows = response.getRows();
        for (Row row : rows) {
            Long userId = row.getPrimaryKey().getPrimaryKeyColumn(Constants.COLUMN_NAME_USER_ID).getValue().asLong();
            Long trajectorId = row.getPrimaryKey().getPrimaryKeyColumn(Constants.COLUMN_NAME_TRAJECTORY_ID).getValue().asLong();
            Long timestamp = row.getPrimaryKey().getPrimaryKeyColumn(Constants.COLUMN_NAME_TIMESTAMP).getValue().asLong();
            Double latitude = null;
            Double longitude = null;
            for (Column column : row.getColumns()) {
                if (column.getName().equals(Constants.COLUMN_NAME_LATITUDE)) {
                    latitude = column.getValue().asDouble();
                } else if (column.getName().equals(Constants.COLUMN_NAME_LONGITUDE)) {
                    longitude = column.getValue().asDouble();
                }
            }
            Map<Long, TrajectorDTO> trajectorIdTrajectorDtoMap;
            if (userIdTrajectoriesDtoMap.containsKey(userId)) {
                trajectorIdTrajectorDtoMap = userIdTrajectoriesDtoMap.get(userId);
            } else {
                trajectorIdTrajectorDtoMap = new HashMap();
                userIdTrajectoriesDtoMap.put(userId, trajectorIdTrajectorDtoMap);
            }

            TrajectorDTO tempTrajectDto;
            if (trajectorIdTrajectorDtoMap.containsKey(trajectorId)) {
                tempTrajectDto = trajectorIdTrajectorDtoMap.get(trajectorId);
            } else {
                tempTrajectDto = new TrajectorDTO();
                tempTrajectDto.setTrajectoryId(trajectorId);
                tempTrajectDto.setUserId(userId);
                tempTrajectDto.setLocations(new LinkedList());
                trajectorIdTrajectorDtoMap.put(trajectorId, tempTrajectDto);
            }
            LocationDTO locationDTO = new LocationDTO();
            locationDTO.setTimestamp(timestamp);
            locationDTO.setLatitude(latitude);
            locationDTO.setLongitude(longitude);
            locationDTO.setUserId(userId);
            locationDTO.setTrajectoryId(trajectorId);
            tempTrajectDto.getLocations().add(locationDTO);
        }
        List<UserTrajectoryDTO> userTrajectoryDTOList = new ArrayList();
        Iterator<Map.Entry<Long, Map<Long, TrajectorDTO>>> iterator = userIdTrajectoriesDtoMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Map<Long, TrajectorDTO>> entry = iterator.next();
            Long userId = entry.getKey();
            Map<Long, TrajectorDTO> trajectoryIdTrajectorDtoMap = entry.getValue();
            Iterator<Map.Entry<Long, TrajectorDTO>> iteratorTrajector = trajectoryIdTrajectorDtoMap.entrySet().iterator();
            List<TrajectorDTO> trajectorDTOList = new ArrayList<>();
            while (iteratorTrajector.hasNext()) {
                Map.Entry<Long, TrajectorDTO> trajectorDTOEntry = iteratorTrajector.next();
                TrajectorDTO trajectorDTO = trajectorDTOEntry.getValue();
                trajectorDTOList.add(trajectorDTO);
            }
            UserTrajectoryDTO userTrajectoryDTO = new UserTrajectoryDTO();
            userTrajectoryDTO.setUserId(userId);
            userTrajectoryDTO.setTrajectories(trajectorDTOList);
            userTrajectoryDTOList.add(userTrajectoryDTO);
        }
        Rectangle rectangle = new Rectangle();
        rectangle.setLeftX(leftLongitude);
        rectangle.setRightX(rightLongitude);
        rectangle.setTopY(topLatitude);
        rectangle.setBottomY(bottomLatitude);
        for (UserTrajectoryDTO userTrajectoryDTO : userTrajectoryDTOList) {
            List<TrajectorDTO> trajectorDTOList = new ArrayList();
            for (TrajectorDTO trajectorDTO : userTrajectoryDTO.getTrajectories()) {
                Path path = VOConverter.convertTrajectorDTO2Path(trajectorDTO);
                List<Path> cutPaths = GeometryAlgorithms.cutPathByRect(path, rectangle);
                for (Path cutPath : cutPaths) {
                    TrajectorDTO cutTrajectorDTO = new TrajectorDTO();
                    cutTrajectorDTO.setTrajectoryId(trajectorDTO.getTrajectoryId());
                    cutTrajectorDTO.setLocations(VOConverter.convertPointsToLocations(cutPath.getPoints()));
                    trajectorDTOList.add(cutTrajectorDTO);
                }
            }
            userTrajectoryDTO.setTrajectories(trajectorDTOList);
        }

        List<VehiclePathVO> result = new ArrayList();
        for (UserTrajectoryDTO userTrajectoryDTO : userTrajectoryDTOList) {
            result.addAll(VOConverter.convertUserTrajectorDTO2TrajectorPathVO(userTrajectoryDTO));
        }
        redisService.putRedisTimeRegionValue(startTime, endTime, topLatitude, bottomLatitude, leftLongitude, rightLongitude, result);
        return result;
    }

    @RequestMapping(value = "/time_and_region_scope_without_redis", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @CrossOrigin
    public List<VehiclePathVO> pathByTimeScopeWithoutRedis(@RequestParam("start_time") Long startTime,
                                                           @RequestParam("end_time") Long endTime,
                                                           @RequestParam("top_latitude") Double topLatitude,
                                                           @RequestParam("bottom_latitude") Double bottomLatitude,
                                                           @RequestParam("left_longitude") Double leftLongitude,
                                                           @RequestParam("right_longitude") Double rightLongitude) {
        // 先在 redis　中查询是否有没有
//        List<TrajectorPathVO> redisTimeRegionValue = redisService.getRedisTimeRegionValue(startTime, endTime, topLatitude, bottomLatitude, leftLongitude, rightLongitude);
//        if (redisTimeRegionValue != null) {
//            return redisTimeRegionValue;
//        }
        RangeRowQueryCriteria rangeRowQueryCriteria = new RangeRowQueryCriteria(Constants.TABLE_NAME_USER_TRAJECTOR);
        // 设置起始主键
        PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_USER_ID, PrimaryKeyValue.INF_MIN);
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_TRAJECTORY_ID, PrimaryKeyValue.fromLong(startTime));
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_TIMESTAMP, PrimaryKeyValue.fromLong(startTime));
        rangeRowQueryCriteria.setInclusiveStartPrimaryKey(primaryKeyBuilder.build());
//         设置结束主键
        primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_USER_ID, PrimaryKeyValue.INF_MAX);
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_TRAJECTORY_ID, PrimaryKeyValue.fromLong(endTime));
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_TIMESTAMP, PrimaryKeyValue.fromLong(endTime));
        rangeRowQueryCriteria.setExclusiveEndPrimaryKey(primaryKeyBuilder.build());
        rangeRowQueryCriteria.setMaxVersions(1);
        GetRangeResponse response = Constants.client.getRange(new GetRangeRequest(rangeRowQueryCriteria));
        Map<Long, Map<Long, TrajectorDTO>> userIdTrajectoriesDtoMap = new HashMap();
        List<Row> rows = response.getRows();
        for (Row row : rows) {
            Long userId = row.getPrimaryKey().getPrimaryKeyColumn(Constants.COLUMN_NAME_USER_ID).getValue().asLong();
            Long trajectorId = row.getPrimaryKey().getPrimaryKeyColumn(Constants.COLUMN_NAME_TRAJECTORY_ID).getValue().asLong();
            Long timestamp = row.getPrimaryKey().getPrimaryKeyColumn(Constants.COLUMN_NAME_TIMESTAMP).getValue().asLong();
            Double latitude = null;
            Double longitude = null;
            for (Column column : row.getColumns()) {
                if (column.getName().equals(Constants.COLUMN_NAME_LATITUDE)) {
                    latitude = column.getValue().asDouble();
                } else if (column.getName().equals(Constants.COLUMN_NAME_LONGITUDE)) {
                    longitude = column.getValue().asDouble();
                }
            }
            Map<Long, TrajectorDTO> trajectorIdTrajectorDtoMap;
            if (userIdTrajectoriesDtoMap.containsKey(userId)) {
                trajectorIdTrajectorDtoMap = userIdTrajectoriesDtoMap.get(userId);
            } else {
                trajectorIdTrajectorDtoMap = new HashMap();
                userIdTrajectoriesDtoMap.put(userId, trajectorIdTrajectorDtoMap);
            }

            TrajectorDTO tempTrajectDto;
            if (trajectorIdTrajectorDtoMap.containsKey(trajectorId)) {
                tempTrajectDto = trajectorIdTrajectorDtoMap.get(trajectorId);
            } else {
                tempTrajectDto = new TrajectorDTO();
                tempTrajectDto.setTrajectoryId(trajectorId);
                tempTrajectDto.setUserId(userId);
                tempTrajectDto.setLocations(new LinkedList());
                trajectorIdTrajectorDtoMap.put(trajectorId, tempTrajectDto);
            }
            LocationDTO locationDTO = new LocationDTO();
            locationDTO.setTimestamp(timestamp);
            locationDTO.setLatitude(latitude);
            locationDTO.setLongitude(longitude);
            locationDTO.setUserId(userId);
            locationDTO.setTrajectoryId(trajectorId);
            tempTrajectDto.getLocations().add(locationDTO);
        }
        List<UserTrajectoryDTO> userTrajectoryDTOList = new ArrayList();
        Iterator<Map.Entry<Long, Map<Long, TrajectorDTO>>> iterator = userIdTrajectoriesDtoMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Map<Long, TrajectorDTO>> entry = iterator.next();
            Long userId = entry.getKey();
            Map<Long, TrajectorDTO> trajectoryIdTrajectorDtoMap = entry.getValue();
            Iterator<Map.Entry<Long, TrajectorDTO>> iteratorTrajector = trajectoryIdTrajectorDtoMap.entrySet().iterator();
            List<TrajectorDTO> trajectorDTOList = new ArrayList<>();
            while (iteratorTrajector.hasNext()) {
                Map.Entry<Long, TrajectorDTO> trajectorDTOEntry = iteratorTrajector.next();
                TrajectorDTO trajectorDTO = trajectorDTOEntry.getValue();
                trajectorDTOList.add(trajectorDTO);
            }
            UserTrajectoryDTO userTrajectoryDTO = new UserTrajectoryDTO();
            userTrajectoryDTO.setUserId(userId);
            userTrajectoryDTO.setTrajectories(trajectorDTOList);
            userTrajectoryDTOList.add(userTrajectoryDTO);
        }
        // 将 userTrajectoryDtoList　转化成 Path 切分
        Rectangle rectangle = new Rectangle();
        rectangle.setLeftX(leftLongitude);
        rectangle.setRightX(rightLongitude);
        rectangle.setTopY(topLatitude);
        rectangle.setBottomY(bottomLatitude);

        for (UserTrajectoryDTO userTrajectoryDTO : userTrajectoryDTOList) {
            List<TrajectorDTO> trajectorDTOList = new ArrayList();
            for (TrajectorDTO trajectorDTO : userTrajectoryDTO.getTrajectories()) {
                Path path = VOConverter.convertTrajectorDTO2Path(trajectorDTO);
                List<Path> cutPaths = GeometryAlgorithms.cutPathByRect(path, rectangle);
                for (Path cutPath : cutPaths) {
                    TrajectorDTO cutTrajectorDTO = new TrajectorDTO();
                    cutTrajectorDTO.setTrajectoryId(trajectorDTO.getTrajectoryId());
                    cutTrajectorDTO.setLocations(VOConverter.convertPointsToLocations(cutPath.getPoints()));
                    trajectorDTOList.add(cutTrajectorDTO);
                }
            }
            userTrajectoryDTO.setTrajectories(trajectorDTOList);
        }

        List<VehiclePathVO> result = new ArrayList();
        for (UserTrajectoryDTO userTrajectoryDTO : userTrajectoryDTOList) {
            result.addAll(VOConverter.convertUserTrajectorDTO2TrajectorPathVO(userTrajectoryDTO));
        }
        return result;
    }

    @RequestMapping(value = "/submit_gps_record")
    @CrossOrigin
    public String submitGpsRecord(@RequestParam("user_id") Long userId,
                                  @RequestParam("trajectory_id") Long trajectoryId,
                                  @RequestParam("timestamp") Long timestamp,
                                  @RequestParam("latitude") Double latitude,
                                  @RequestParam("longitude") Double longitude) {
        PrimaryKeyBuilder primaryKeyBuilder = PrimaryKeyBuilder.createPrimaryKeyBuilder();
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_USER_ID, PrimaryKeyValue.fromLong(userId));
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_TRAJECTORY_ID, PrimaryKeyValue.fromLong(trajectoryId));
        primaryKeyBuilder.addPrimaryKeyColumn(Constants.COLUMN_NAME_TIMESTAMP, PrimaryKeyValue.fromLong(timestamp));
        PrimaryKey primaryKey = primaryKeyBuilder.build();
        RowPutChange rowPutChange = new RowPutChange(Constants.TABLE_NAME_USER_TRAJECTOR, primaryKey);
        rowPutChange.addColumn(new Column(Constants.COLUMN_NAME_LATITUDE, ColumnValue.fromDouble(latitude)));
        rowPutChange.addColumn(new Column(Constants.COLUMN_NAME_LONGITUDE, ColumnValue.fromDouble(longitude)));

        try {
            Constants.client.putRow(new PutRowRequest(rowPutChange));
        } catch (Exception e) {
            return "failed";
        }
        return "sucess";
    }

    @RequestMapping(value = "/test")
    public List<VehiclePathVO> get(@RequestParam("start_time") Long startTime,
                                   @RequestParam("end_time") Long endTime,
                                   @RequestParam("top_latitude") Double topLatitude,
                                   @RequestParam("bottom_latitude") Double bottomLatitude,
                                   @RequestParam("left_longitude") Double leftLongitude,
                                   @RequestParam("right_longitude") Double rightLongitude) {
        return redisService.getRedisTimeRegionValue(startTime, endTime, topLatitude, bottomLatitude, leftLongitude, rightLongitude);
    }
}
