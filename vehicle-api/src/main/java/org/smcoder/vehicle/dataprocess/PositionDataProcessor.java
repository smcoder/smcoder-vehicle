package org.smcoder.vehicle.dataprocess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.smcoder.vehicle.config.HttpAPIService;
import org.smcoder.vehicle.generate.Vehicle;
import org.smcoder.vehicle.generate.VehicleDao;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;

@Controller
public class PositionDataProcessor {
    @Resource
    private HttpAPIService httpAPIService;

    @Resource
    private VehicleDao vehicleDao;


    @RequestMapping("data")
    @ResponseBody
    public void data() throws Exception {
        String result = httpAPIService.doGet("http://a.amap.com/amap-ui/static/data/big-routes.json");
        JSONArray jsonArray = JSON.parseArray(result);
        for (int index = 0; index < jsonArray.size(); index++) {
            JSONObject item = jsonArray.getJSONObject(index);
            String name = item.getString("name");
            JSONArray valueArray = item.getJSONArray("path");
            for (int value = 0; value < valueArray.size(); value++) {
                BigDecimal lang = valueArray.getJSONArray(value).getBigDecimal(0);
                BigDecimal lon = valueArray.getJSONArray(value).getBigDecimal(1);
                Vehicle vehicle = new Vehicle();
                vehicle.setPlat(name);
                vehicle.setLang(lang.toString());
                vehicle.setLon(lon.toString());
                vehicle.setReportTime(new Date());
                vehicleDao.insertSelective(vehicle);
            }
        }
    }
}
