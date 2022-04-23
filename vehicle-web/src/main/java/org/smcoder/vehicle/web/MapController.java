package org.smcoder.vehicle.web;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smcoder.vehicle.config.HttpAPIService;
import org.smcoder.vehicle.vo.CountData;
import org.smcoder.vehicle.vo.CountVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class MapController {
    private Logger logger = LoggerFactory.getLogger(MapController.class);

    @Resource
    private HttpAPIService httpAPIService;

    @Value("${http.api.day.url}")
    private String dayUrl;
    @Value("${http.api.month.url}")
    private String monthUrl;
    @Value("${http.api.week.url}")
    private String weekUrl;

    @RequestMapping(value = "/vehicle", method = RequestMethod.GET)
    public String vehicleMap() {
        return "vehicle";
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public String count(Model model) throws Exception {
        List<CountVO> dayList = day();
        List<CountVO> weekList = week();
        List<CountVO> monthList = month();
        model.addAttribute("dayTime", dayList.stream().map(CountVO::getDt).collect(Collectors.toList()));
        model.addAttribute("dayValue", dayList.stream().map(CountVO::getDistance).collect(Collectors.toList()));
        model.addAttribute("weekTime", weekList.stream().map(CountVO::getDt).collect(Collectors.toList()));
        model.addAttribute("weekValue", weekList.stream().map(CountVO::getDistance).collect(Collectors.toList()));
        model.addAttribute("monthTime", monthList.stream().map(CountVO::getDt).collect(Collectors.toList()));
        model.addAttribute("monthValue", monthList.stream().map(CountVO::getDistance).collect(Collectors.toList()));
        return "count";
    }

    private List<CountVO> day() throws Exception {
        String dayResult = httpAPIService.doGet(dayUrl);
        List<CountVO> remoteResult = JSON.parseArray(dayResult, CountVO.class);
        return remoteResult;
    }

    private List<CountVO> month() throws Exception {
        String monthResult = httpAPIService.doGet(monthUrl);
        List<CountVO> remoteResult = JSON.parseArray(monthResult, CountVO.class);
        return remoteResult;
    }

    private List<CountVO> week() throws Exception {
        String weekResult = httpAPIService.doGet(weekUrl);
        List<CountVO> remoteResult = JSON.parseArray(weekResult, CountVO.class);
        return remoteResult;
    }
}
