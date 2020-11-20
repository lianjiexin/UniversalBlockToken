/*
 *  Copyright (C) 2019 Alpha Jiang. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package io.github.alphajiang.hyena.rest;

import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.biz.flow.PointFlowService;
import io.github.alphajiang.hyena.biz.flow.QueueMonitor;
import io.github.alphajiang.hyena.biz.point.PointCache;
import io.github.alphajiang.hyena.biz.point.strategy.PointMemCacheService;
import io.github.alphajiang.hyena.ds.service.ExchangeRateDs;
import io.github.alphajiang.hyena.ds.service.PointTableDs;
import io.github.alphajiang.hyena.exchange.SupportedRatePairs;
import io.github.alphajiang.hyena.model.base.BaseResponse;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.base.ObjectResponse;
import io.github.alphajiang.hyena.model.po.ExchangeRatePo;
import io.github.alphajiang.hyena.model.po.UidRegistryPo;
import io.github.alphajiang.hyena.model.vo.QueueInfo;
import io.github.alphajiang.hyena.utils.LoggerHelper;
import io.github.alphajiang.hyena.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Api(value = "系统设置接口", tags = "系统")
@RequestMapping(value = "/ubt/system", produces = MediaType.APPLICATION_JSON_VALUE)
public class SystemController {
    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

    @Autowired
    private PointTableDs pointTableDs;

    @Autowired
    private PointFlowService pointFlowService;

    @Autowired
    private PointMemCacheService pointMemCacheService;

    @Autowired
    private QueueMonitor queueMonitor;

    @Autowired
    private ExchangeRateDs exchangeRateDs;

    @ApiOperation(value = "获取积分类型列表")
    @GetMapping(value = "/listPointType")
    public ListResponse<String> listPointType(HttpServletRequest request) {
        logger.debug(LoggerHelper.formatEnterLog(request));
        var list = this.pointTableDs.listTable();
        list = list.stream()
                .map(o -> StringUtils.replaceFirst(o, HyenaConstants.PREFIX_POINT_TABLE_NAME))
                .collect(Collectors.toList());
        ListResponse<String> res = new ListResponse<>(list);
        logger.debug(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @ApiOperation(value = "新增积分类型")
    @PostMapping(value = "/addPointType")
    public BaseResponse addPointType(HttpServletRequest request,
                                     @ApiParam(value = "积分类型", example = "score") @RequestParam(name = "name", required = true) String name) {
        logger.info(LoggerHelper.formatEnterLog(request));
        this.pointTableDs.getOrCreateTable(name);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return BaseResponse.success();
    }

    @ApiOperation(value = "设置兑换率")
    @PostMapping(value = "/updateExchangeRate")
    public BaseResponse updateExchangeRate(HttpServletRequest request,
                                     @ApiParam(value = "source", example = "UBT") @RequestParam(name = "source", required = true) String source,
                                           @ApiParam(value = "dest", example = "USD") @RequestParam(name = "dest", required = true) String dest,
                                           @ApiParam(value = "rate", example = "6.53") @RequestParam(name = "rate", required = true) BigDecimal rate) {
        logger.info(LoggerHelper.formatEnterLog(request));

        ExchangeRatePo exchangeRatePo = new ExchangeRatePo();
        String symbol = SupportedRatePairs.getSymbol(source,dest).toString();
        if(StringUtils.isBlank(symbol)){
            BaseResponse res = new BaseResponse();
            res.setStatus(HyenaConstants.ERROR_SUPPORTED_EXCHANGE_RATE_PAIR);
            res.setError(HyenaConstants.ERROR_4001);
            return res;
        }
        if(rate.doubleValue() <= 0)
        {
            BaseResponse res = new BaseResponse();
            res.setStatus(HyenaConstants.ERROR_NEGATIVE_RATE_NOT_SUPPORTED);
            res.setError(HyenaConstants.ERROR_4002);
            return res;
        }
        logger.info("Updating exchange rate for: " + symbol);
        exchangeRatePo.setSymbol(symbol);
        exchangeRatePo.setSource(source);
        exchangeRatePo.setDest(dest);
        exchangeRatePo.setRate(rate);
        exchangeRatePo.setEnable(true);

        this.exchangeRateDs.insertOrUpdate(exchangeRatePo);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return BaseResponse.success();
    }

    @ApiOperation(value = "获取当前兑换汇率")
    @GetMapping(value = "/getExchangeRate")
    public ObjectResponse<ExchangeRatePo> getExchangeRate(
            HttpServletRequest request,
            @ApiParam(value = "source") @RequestParam String source,
            @ApiParam(value = "dest") @RequestParam(required = false) String dest) {
        logger.info(LoggerHelper.formatEnterLog(request));

        //@TO-do: Update how responses are sent back
        String symbol = SupportedRatePairs.getSymbol(source,dest).toString();
        if(StringUtils.isBlank(symbol)){
            return null;
        }

        ExchangeRatePo ret = this.exchangeRateDs.getExchangeRate(symbol);

        ObjectResponse<ExchangeRatePo> res = new ObjectResponse<ExchangeRatePo>(ret);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @ApiOperation(value = "获取缓存信息")
    @GetMapping(value = "/dumpMemCache")
    public ListResponse<PointCache> dumpMemCache(HttpServletRequest request) {
        logger.info(LoggerHelper.formatEnterLog(request));
        List<PointCache> list = new ArrayList<>();
        list.addAll(pointMemCacheService.dump());
        ListResponse<PointCache> ret = new ListResponse<>(list, list.size());
        logger.info(LoggerHelper.formatLeaveLog(request));
        return ret;
    }

    @ApiOperation(value = "获取队列信息")
    @GetMapping(value = "/dumpQueue")
    public ListResponse<QueueInfo> dumpQueue(HttpServletRequest request) {
        logger.info(LoggerHelper.formatEnterLog(request));
        List<QueueInfo> list = queueMonitor.dump();
        ListResponse<QueueInfo> ret = new ListResponse<>(list);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return ret;
    }
}
