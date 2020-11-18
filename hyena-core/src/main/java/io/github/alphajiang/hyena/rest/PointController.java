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
import io.github.alphajiang.hyena.OrderTypeConstant;
import io.github.alphajiang.hyena.aop.Idempotent;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.PointUsageBuilder;
import io.github.alphajiang.hyena.biz.point.PointUsageFacade;
import io.github.alphajiang.hyena.biz.point.strategy.PointMemCacheService;
import io.github.alphajiang.hyena.ds.service.*;
import io.github.alphajiang.hyena.exchange.StaticExchangeRate;
import io.github.alphajiang.hyena.model.base.BaseResponse;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.base.ObjectResponse;
import io.github.alphajiang.hyena.model.dto.PointLogDto;
import io.github.alphajiang.hyena.model.dto.PointRecDto;
import io.github.alphajiang.hyena.model.dto.PointRecLogDto;
import io.github.alphajiang.hyena.model.exception.HyenaParameterException;
import io.github.alphajiang.hyena.model.param.*;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.UbtAccountPo;
import io.github.alphajiang.hyena.model.po.UidRegistryPo;
import io.github.alphajiang.hyena.model.type.SortOrder;
import io.github.alphajiang.hyena.model.vo.PointLogBi;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.utils.*;
import io.github.alphajiang.hyena.wechat.WechatPayConnector;
import io.github.alphajiang.hyena.wechat.XmlToMapUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@Api(value = "积分相关的接口", tags = "积分")
@RequestMapping(value = "/ubt/point", produces = MediaType.APPLICATION_JSON_VALUE)
public class PointController {

    private static final Logger logger = LoggerFactory.getLogger(PointController.class);

    @Autowired
    private PointDs pointDs;

    @Autowired
    private PointUsageFacade pointUsageFacade;

    @Autowired
    private PointLogDs pointLogDs;

    @Autowired
    private PointRecDs pointRecDs;

    @Autowired
    private PointRecLogDs pointRecLogDs;

    @Autowired
    private PointMemCacheService pointMemCacheService;

    @Autowired
    private PointTableDs pointTableDs;

    @Autowired
    private UBTConnector ubtConnector;

    @Autowired
    private WechatPayConnector wechatPayConnector;

    PointController()
    {

    }

    @ApiOperation(value = "获取积分信息")
    @GetMapping(value = "/getPoint")
    public ObjectResponse<PointPo> getPoint(
            HttpServletRequest request,
            @ApiParam(value = "积分类型", example = "score") @RequestParam(defaultValue = "default") String type,
            @ApiParam(value = "用户ID") @RequestParam String uid,
            @ApiParam(value = "用户二级ID") @RequestParam(required = false) String subUid) {
        logger.info(LoggerHelper.formatEnterLog(request));

        var ret = this.pointMemCacheService.getPoint(type, uid, subUid, false);
        ObjectResponse<PointPo> res = new ObjectResponse<>(ret.getPointCache().getPoint());
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }


    @ApiOperation(value = "获取积分列表")
    @PostMapping(value = "/listPoint")
    public ListResponse<PointPo> listPoint(HttpServletRequest request,
                                           @RequestBody ListPointParam param) {
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);

        param.setSorts(List.of(SortParam.as("pt.id", SortOrder.desc)));
        var res = this.pointDs.listPoint4Page(param);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }


    @ApiOperation(value = "获取变更明细列表")
    @PostMapping(value = "/listPointLog")
    public ListResponse<PointLogDto> listPointLog(
            HttpServletRequest request,
            @RequestBody ListPointLogParam param) {
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);
        if (CollectionUtils.isEmpty(param.getSorts())) {
            param.setSorts(List.of(SortParam.as("log.id", SortOrder.desc)));
        }
        var res = this.pointLogDs.listPointLog4Page(param);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @ApiOperation(value = "获取变更明细统计")
    @PostMapping(value = "/listPointLogBi")
    public ListResponse<PointLogBi> listPointLogBi(
            HttpServletRequest request,
            @RequestBody ListPointLogParam param) {
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);
        var list = this.pointLogDs.listPointLogBi(param);
        var res = new ListResponse<>(list, list.size());
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @ApiOperation(value = "获取记录列表")
    @PostMapping(value = "/listPointRecord")
    public ListResponse<PointRecDto> listPointRecord(HttpServletRequest request,
                                                     @RequestBody ListPointRecParam param) {
        logger.info(LoggerHelper.formatEnterLog(request, false) + "param = {}", param);
        if (CollectionUtils.isEmpty(param.getSorts())) {
            param.setSorts(List.of(SortParam.as("rec.id", SortOrder.desc)));
        }
        var res = this.pointRecDs.listPointRec4Page(param);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @ApiOperation(value = "获取记录历史明细列表")
    @PostMapping(value = "/listPointRecordLog")
    public ListResponse<PointRecLogDto> listPointRecordLog(
            HttpServletRequest request,
            @RequestBody ListPointRecLogParam param) {
        logger.info(LoggerHelper.formatEnterLog(request, false) + "param = {}", param);

        //ListPointRecLogParam param = new ListPointRecLogParam();
        //param.setUid(uid).setRecId(recId).setTag(tag);
//        if (seqNum != null) {
//            param.setSeqNum(seqNum);
//        }
        if (param.getSorts() == null) {
            param.setSorts(List.of(SortParam.as("log.id", SortOrder.desc)));
        }
        var res = this.pointRecLogDs.listPointRecLog4Page(param);


        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @Idempotent(name = "increase-point")
    @ApiOperation(value = "增加用户积分")
    @PostMapping(value = "/increase")
    public ObjectResponse<PointPo> increasePoint(HttpServletRequest request,
                                                 @RequestBody @NotNull PointIncreaseParam param) throws InterruptedException, ExecutionException,IOException {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);

        PointUsage usage = PointUsageBuilder.fromPointIncreaseParam(param);
        PointPo ret = this.pointUsageFacade.increase(usage);

        /** Disable UBT sync for now
        if(StringUtils.equals(param.getType(),"ubt")){
            logger.info("Type is UBT, sync offline point to online");
            String uid = param.getUid();
            BigDecimal value = param.getPoint();
            logger.info("UID :" + uid + "\t value: " + value);

            ubtConnector.depositUBT(uid,value,18);
        }
         **/

        ObjectResponse<PointPo> res = new ObjectResponse<>(ret);
        logger.info(LoggerHelper.formatLeaveLog(request));
        debugPerformance(request, startTime);
        return res;
    }

    @Idempotent(name = "create-account")
    @ApiOperation(value = "创建用户帐号")
    @PostMapping(value = "/create")
    public ObjectResponse<PointPo> createAccount(HttpServletRequest request,
                                                 @RequestBody @NotNull PointIncreaseParam param) throws InterruptedException, ExecutionException,IOException {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);

       //Create common point account，which won't have a subUid
        PointUsage usage = PointUsageBuilder.fromPointIncreaseParam(param);
        PointPo ret = this.pointUsageFacade.increase(usage);

        //Create UBT account, set  UBT account address as the subUid
        String acctAddresss = ubtConnector.createNewAccount();
        logger.info("Create new UBT Account: " + acctAddresss);
        param.setName(acctAddresss); // store UBT account address at Name
        param.setType("ubt");
        param.setPoint(new BigDecimal(0));
        usage = PointUsageBuilder.fromPointIncreaseParam(param);
        ret = this.pointUsageFacade.increase(usage);

        ObjectResponse<PointPo> res = new ObjectResponse<>(ret);
        logger.info(LoggerHelper.formatLeaveLog(request));
        debugPerformance(request, startTime);
        return res;
    }

    @Idempotent(name = "decrease-point")
    @ApiOperation(value = "消费用户积分")
    @PostMapping(value = "/decrease")
    public ObjectResponse<PointOpResult> decreasePoint(HttpServletRequest request,
                                                       @RequestBody PointDecreaseParam param) throws InterruptedException, ExecutionException,IOException {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);
        PointUsage usage = PointUsageBuilder.fromPointOpParam(param);
        usage.setRecId(param.getRecId());
        PointOpResult ret = this.pointUsageFacade.decrease(usage);

        /** hold off UBT sync for now
        if(StringUtils.equals(param.getType(),"ubt")){
            logger.info("Type is UBT, sync offline point to online");

            ubtConnector.returnUBT(param.getUid(),param.getPoint(),18);
        }
         **/

        ObjectResponse<PointOpResult> res = new ObjectResponse<>(ret);
        logger.info(LoggerHelper.formatLeaveLog(request));
        debugPerformance(request, startTime);
        return res;
    }

    @Idempotent(name = "withdraw-rmb")
    @ApiOperation(value = "人民币提现")
    @PostMapping(value = "/withDrawRmb")
    public BaseResponse withDrawRmb(HttpServletRequest request,
                                    @RequestBody CashWithdrawParam param) throws Exception {

        String type = param.getType();
        if(!StringUtils.isBlank(type) && !StringUtils.equals("ubt",type)){
            BaseResponse ret = new BaseResponse();
            ret.setStatus(HyenaConstants.ERROR_ILLEGAL_CASH_WITHDRAW_ATTEMPT);
            ret.setError("Point Type " + type +" cannot be withdrawn.");
            return ret;
        }

        logger.info(LoggerHelper.formatEnterLog(request));
        logger.info(param.toString());
        String openId = param.getOpenId();
        BigDecimal point = param.getPoint();
        String uid = param.getUid();
        double exchangeRate = StaticExchangeRate.getExchangeRate("UBT","RMB");
        BigDecimal cashAmount = point.multiply(new BigDecimal(exchangeRate));

        logger.info("Withdraw RMB cash amount " + cashAmount + " to openId \t" + openId);
        String jsonStr = wechatPayConnector.withdrawCash(openId,cashAmount.toString());

        //reduce the point
        param.setOrderType(OrderTypeConstant.RMB_WITH_DRAW);
        ObjectResponse<PointOpResult> res = decreasePoint(request,param);

        Map<String, String> responseMap = XmlToMapUtil.xmlToMap(jsonStr);

        return new ObjectResponse<Map<String,String>>(responseMap);
    }

    @Idempotent(name = "decreaseFrozen-point")
    @ApiOperation(value = "消费已冻结的用户积分")
    @PostMapping(value = "/decreaseFrozen")
    public ObjectResponse<PointOpResult> decreaseFrozenPoint(HttpServletRequest request,
                                                             @RequestBody PointDecreaseFrozenParam param) {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);
        PointUsage usage = PointUsageBuilder.fromPointDecreaseParam(param);

        PointOpResult ret = this.pointUsageFacade.decreaseFrozen(usage);
        ObjectResponse<PointOpResult> res = new ObjectResponse<>(ret);
        logger.info(LoggerHelper.formatLeaveLog(request));
        debugPerformance(request, startTime);
        return res;
    }


    @Idempotent(name = "freeze-point")
    @ApiOperation(value = "冻结用户积分")
    @PostMapping(value = "/freeze")
    public ObjectResponse<PointOpResult> freezePoint(HttpServletRequest request,
                                                     @RequestBody PointOpParam param) {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);

        PointUsage usage = PointUsageBuilder.fromPointOpParam(param);
        PointOpResult cusPoint = this.pointUsageFacade.freeze(usage);
        ObjectResponse<PointOpResult> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        debugPerformance(request, startTime);
        return res;
    }

    @Idempotent(name = "unfreeze-point")
    @ApiOperation(value = "解冻用户积分")
    @PostMapping(value = "/unfreeze")
    public ObjectResponse<PointOpResult> unfreezePoint(HttpServletRequest request,
                                                       @RequestBody PointUnfreezeParam param) {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);

        PointUsage usage = PointUsageBuilder.fromPointUnfreezeParam(param);
        PointOpResult cusPoint = this.pointUsageFacade.unfreeze(usage);

        ObjectResponse<PointOpResult> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        debugPerformance(request, startTime);
        return res;
    }

    @Idempotent(name = "cancel-point")
    @ApiOperation(value = "撤销用户积分")
    @PostMapping(value = "/cancel")
    public ObjectResponse<PointOpResult> cancelPoint(HttpServletRequest request,
                                                     @RequestBody PointCancelParam param) {
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);

        PointUsage usage = PointUsageBuilder.fromPointCancelParam(param);
        PointOpResult cusPoint = this.pointUsageFacade.cancel(usage);

        ObjectResponse<PointOpResult> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @Idempotent(name = "freeze-by-rec-id")
    @ApiOperation(value = "按积分块冻结")
    @PostMapping(value = "/freezeByRecId")
    public ObjectResponse<PointOpResult> freezeByRecId(HttpServletRequest request,
                                                    @RequestBody PointFreezeByRecIdParam param) {
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);
//        if (param.getUnfreezePoint() != null && param.getUnfreezePoint() < 0L) {
//            throw new HyenaParameterException("invalid parameter: unfreezePoint");
//        }
        PointUsage usage = PointUsageBuilder.fromPointFreezeByRecIdParam(param);
        PointOpResult cusPoint = this.pointUsageFacade.freezeByRecId(usage);
        ObjectResponse<PointOpResult> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @Idempotent(name = "freeze-cost")
    @ApiOperation(value = "按成本冻结")
    @PostMapping(value = "/freezeCost")
    public ObjectResponse<PointOpResult> freezeCost(HttpServletRequest request,
                                                    @RequestBody PointFreezeParam param) {
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);
//        if (param.getUnfreezePoint() != null && param.getUnfreezePoint() < 0L) {
//            throw new HyenaParameterException("invalid parameter: unfreezePoint");
//        }
        PointUsage usage = PointUsageBuilder.fromPointFreezeParam(param);
        PointOpResult cusPoint = this.pointUsageFacade.freezeCost(usage);
        ObjectResponse<PointOpResult> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @Idempotent(name = "unfreeze-cost")
    @ApiOperation(value = "按成本解冻")
    @PostMapping(value = "/unfreezeCost")
    public ObjectResponse<PointOpResult> unfreezeCost(HttpServletRequest request,
                                                      @RequestBody PointUnfreezeParam param) {
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);
        PointUsage usage = PointUsageBuilder.fromPointUnfreezeParam(param);
        PointOpResult cusPoint = this.pointUsageFacade.unfreezeCost(usage);
        ObjectResponse<PointOpResult> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

//    @Idempotent(name = "refund-frozen")
//    @ApiOperation(value = "已冻结积分做退款")
//    @PostMapping(value = "/refundFrozen")
//    public ObjectResponse<PointOpResult> refundFrozen(HttpServletRequest request,
//                                                @RequestBody PointRefundFrozenParam param) {
//        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);
//        PointUsage usage = PointUsageBuilder.fromPointRefundFrozenParam(param);
//        //usage.setUnfreezePoint(param.getUnfreezePoint());
//        PointOpResult cusPoint = this.pointUsageFacade.refundFrozen(usage);
//        ObjectResponse<PointOpResult> res = new ObjectResponse<>(cusPoint);
//        logger.info(LoggerHelper.formatLeaveLog(request));
//        return res;
//    }

    @Idempotent(name = "refund")
    @ApiOperation(value = "退款")
    @PostMapping(value = "/refund")
    public ObjectResponse<PointOpResult> refund(HttpServletRequest request,
                                                @RequestBody PointRefundParam param) {
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);
        PointUsage usage = PointUsageBuilder.fromPointRefundParam(param);
        usage.setUnfreezePoint(param.getUnfreezePoint());
        PointOpResult cusPoint = this.pointUsageFacade.refund(usage);
        ObjectResponse<PointOpResult> res = new ObjectResponse<>(cusPoint);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @ApiOperation(value = "获取时间段内总共增加的积分数量")
    @GetMapping(value = "/getIncreasedPoint")
    public ObjectResponse<BigDecimal> getIncreasedPoint(
            HttpServletRequest request,
            @ApiParam(value = "积分类型", example = "score") @RequestParam(defaultValue = "default") String type,
            @ApiParam(value = "用户ID") @RequestParam(required = false) String uid,
            @ApiParam(value = "开始时间", example = "2019-03-25 18:35:21") @RequestParam(required = false, value = "start") String strStart,
            @ApiParam(value = "结束时间", example = "2019-04-26 20:15:31") @RequestParam(required = false, value = "end") String strEnd) {
        logger.info(LoggerHelper.formatEnterLog(request));
        try {
            Calendar calStart = DateUtils.fromYyyyMmDdHhMmSs(strStart);
            Calendar calEnd = DateUtils.fromYyyyMmDdHhMmSs(strEnd);
            var ret = this.pointRecDs.getIncreasedPoint(type, uid, calStart.getTime(), calEnd.getTime());

            ObjectResponse<BigDecimal> res = new ObjectResponse<>(ret);
            logger.info(LoggerHelper.formatLeaveLog(request) + " ret = {}", ret);
            return res;
        } catch (ParseException e) {
            logger.warn(e.getMessage(), e);
            throw new HyenaParameterException("参数错误, 时间格式无法解析");
        }
    }

    @ApiOperation(value = "禁用帐号")
    @PostMapping(value = "/disableAccount")
    public BaseResponse disableAccount(
            HttpServletRequest request,
            @ApiParam(value = "积分类型", example = "score") @RequestParam(defaultValue = "default") String type,
            @ApiParam(value = "用户ID") @RequestParam String uid,
            @ApiParam(value = "用户二级ID") @RequestParam(required = false) String subUid) {
        logger.info(LoggerHelper.formatEnterLog(request));
        this.pointDs.disableAccount(type, uid, subUid);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return BaseResponse.success();
    }

    private void debugPerformance(HttpServletRequest request, long startTime) {
        long curTime = System.nanoTime();
        if (curTime - startTime > 2000L * 1000000) {
            logger.warn("延迟过大...{}. url = {}",
                    (curTime - startTime) / 1000000,
                    request.getRequestURI());
        }
    }
}
