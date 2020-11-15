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
import io.github.alphajiang.hyena.aop.Idempotent;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.PointUsageBuilder;
import io.github.alphajiang.hyena.biz.point.PointUsageFacade;
import io.github.alphajiang.hyena.ds.service.PointDs;
import io.github.alphajiang.hyena.ds.service.UidRegistryDs;
import io.github.alphajiang.hyena.model.base.BaseResponse;
import io.github.alphajiang.hyena.model.base.ObjectResponse;
import io.github.alphajiang.hyena.model.param.PointIncreaseParam;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.UbtAccountPo;
import io.github.alphajiang.hyena.model.po.UidRegistryPo;
import io.github.alphajiang.hyena.utils.LoggerHelper;
import io.github.alphajiang.hyena.utils.StringUtils;
import io.github.alphajiang.hyena.utils.UBTConnector;
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
import java.util.concurrent.ExecutionException;

@RestController
@Api(value = "账号相关的接口", tags = "账户")
@RequestMapping(value = "/ubt/account", produces = MediaType.APPLICATION_JSON_VALUE)
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private PointDs pointDs;

    @Autowired
    private PointUsageFacade pointUsageFacade;

    @Autowired
    private UBTConnector ubtConnector;

    @Autowired
    private UidRegistryDs uidRegistryDs;

    AccountController() {

    }


    @ApiOperation(value = "获取UBT账户信息")
    @GetMapping(value = "/getUbtAccount")
    public ObjectResponse<UbtAccountPo> getUbtAccount(
            HttpServletRequest request,
            @ApiParam(value = "注册码") @RequestParam String registerCode,
            @ApiParam(value = "用户二级ID") @RequestParam(required = false) String subUid) {
        logger.info(LoggerHelper.formatEnterLog(request));

        UbtAccountPo ret = this.ubtConnector.getUbtAccountPo(registerCode);

        ObjectResponse<UbtAccountPo> res = new ObjectResponse<UbtAccountPo>(ret);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @ApiOperation(value = "根据注册码获取已注册对象，其中包含Uid,和注册码")
    @GetMapping(value = "/getUidRegistry")
    public ObjectResponse<UidRegistryPo> getUidRegistry(
            HttpServletRequest request,
            @ApiParam(value = "注册码") @RequestParam String registerCode,
            @ApiParam(value = "用户二级ID") @RequestParam(required = false) String subUid) {
        logger.info(LoggerHelper.formatEnterLog(request));

        UidRegistryPo ret = this.uidRegistryDs.getUidRegistry(registerCode);

        ObjectResponse<UidRegistryPo> res = new ObjectResponse<UidRegistryPo>(ret);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @ApiOperation(value = "根据Uid获取已注册对象，其中包含Uid,和注册码")
    @GetMapping(value = "/getUidRegistryByUid")
    public ObjectResponse<UidRegistryPo> getUidRegistryByUid(
            HttpServletRequest request,
            @ApiParam(value = "uid") @RequestParam String uid,
            @ApiParam(value = "用户二级ID") @RequestParam(required = false) String subUid) {
        logger.info(LoggerHelper.formatEnterLog(request));

        UidRegistryPo ret = this.uidRegistryDs.getUidRegistryByUid(uid);

        ObjectResponse<UidRegistryPo> res = new ObjectResponse<UidRegistryPo>(ret);
        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @ApiOperation(value = "输入注册码和密码来注册Uid")
    @GetMapping(value = "/registerUid")
    public ObjectResponse<UidRegistryPo> registerUid(
            HttpServletRequest request,
            @ApiParam(value = "注册码") @RequestParam String registerCode,
            @ApiParam(value = "小程序UID") @RequestParam String uid,
            @ApiParam(value = "密码") @RequestParam String password,
            @ApiParam(value = "用户二级ID") @RequestParam(required = false) String subUid) {
        logger.info(LoggerHelper.formatEnterLog(request));

        UidRegistryPo ret = this.uidRegistryDs.getUidRegistry(registerCode);


        ObjectResponse<UidRegistryPo> res = new ObjectResponse<UidRegistryPo>(ret);

        //handle all the error case;
        if (ret == null) {
            res.setStatus(HyenaConstants.ERROR_REGISTER_CODE_NOT_FOUND);
            res.setError(HyenaConstants.ERROR_1220);
            return res;
        } else if (ret.getEnable()) {
            res.setStatus(HyenaConstants.ERROR_REGISTER_CODE_USED);
            res.setError(HyenaConstants.ERROR_1210);
            return res;
        } else if (!StringUtils.equals(password, ret.getPassword())) {
            res.setStatus(HyenaConstants.ERROR_PASSWORD_NOT_MATCH);
            res.setError((HyenaConstants.ERROR_1240));
            return res;
        }

        this.uidRegistryDs.updateUidRegistry(registerCode, uid, password, true);
        res.setStatus(HyenaConstants.RES_CODE_SUCCESS);

        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }

    @ApiOperation(value = "解绑Uid")
    @GetMapping(value = "/deregisterUid")
    public ObjectResponse<UidRegistryPo> DeregisterUid(
            HttpServletRequest request,
            @ApiParam(value = "注册码") @RequestParam String registerCode,
            @ApiParam(value = "小程序UID") @RequestParam String uid,
            @ApiParam(value = "密码") @RequestParam String password,
            @ApiParam(value = "用户二级ID") @RequestParam(required = false) String subUid) {
        logger.info(LoggerHelper.formatEnterLog(request));

        UidRegistryPo ret = this.uidRegistryDs.getUidRegistry(registerCode);


        ObjectResponse<UidRegistryPo> res = new ObjectResponse<UidRegistryPo>(ret);
        if (ret == null) {
            res.setStatus(HyenaConstants.ERROR_REGISTER_CODE_NOT_FOUND);
            res.setError(HyenaConstants.ERROR_1220);
            return res;
        } else if (!ret.getEnable()) {
            res.setStatus(HyenaConstants.ERROR_REGISTER_CODE_ALREADY_DISABLED);
            res.setError(HyenaConstants.ERROR_1230);
            return res;
        } else if (!StringUtils.equals(password, ret.getPassword())) {
            res.setStatus(HyenaConstants.ERROR_PASSWORD_NOT_MATCH);
            res.setError(HyenaConstants.ERROR_1240);
            return res;
        }

        //set uid as false when register code is disabled
        this.uidRegistryDs.updateUidRegistry(registerCode, null, password, false);

        res.setStatus(HyenaConstants.RES_CODE_SUCCESS);

        logger.info(LoggerHelper.formatLeaveLog(request));
        return res;
    }


    /**
     * Not curently used
     *
     * @param request
     * @param param
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    @Idempotent(name = "create-account")
    @ApiOperation(value = "创建用户帐号")
    @PostMapping(value = "/createAccount")
    public ObjectResponse<PointPo> createAccount(HttpServletRequest request,
                                                 @RequestBody @NotNull PointIncreaseParam param) throws InterruptedException, ExecutionException, IOException {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = {}", param);

        //Create common point account，which won't have a subUid
        PointUsage usage = PointUsageBuilder.fromPointIncreaseParam(param);
        PointPo ret = this.pointUsageFacade.increase(usage);

        /** Todo : Creat Blockchain Account
         Create UBT account, set  UBT account address as the subUid
         String acctAddresss = ubtConnector.createNewAccount();
         logger.info("Create new UBT Account: " + acctAddresss);
         param.setName(acctAddresss); // store UBT account address at Name
         */


        param.setType("ubt");
        param.setPoint(new BigDecimal(0));
        usage = PointUsageBuilder.fromPointIncreaseParam(param);
        ret = this.pointUsageFacade.increase(usage);

        ObjectResponse<PointPo> res = new ObjectResponse<>(ret);
        logger.info(LoggerHelper.formatLeaveLog(request));
        debugPerformance(request, startTime);
        return res;
    }

    /**
     * Not curently used
     *
     * @param request      httpRequest
     * @param registerCode 注册码
     * @param password     密码
     * @return 操作结果
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws IOException
     */
    @ApiOperation(value = "创建注册码")
    @PostMapping(value = "/createRegisterCode")
    public BaseResponse createRegisterCode(HttpServletRequest request,
                                           @ApiParam(value = "注册码", example = "223456") @RequestParam String registerCode,
                                           @ApiParam(value = "密码", defaultValue = "1234") @RequestParam String password) throws InterruptedException, ExecutionException, IOException {
        long startTime = System.nanoTime();
        logger.info(LoggerHelper.formatEnterLog(request, false) + " param = " +
                "registerCode: {registerCode}, password: {password}", registerCode, password);


        UidRegistryPo registerPo = new UidRegistryPo();
        registerPo.setEnable(false);
        registerPo.setPassword(password);
        registerPo.setRegisterCode(registerCode);

        BaseResponse res = new BaseResponse();
        if (uidRegistryDs.getUidRegistry(registerCode) != null) {//Registercode already exist
            res.setStatus(HyenaConstants.INFO_REGISTERCODE_EXIST_PASSWORD_UPDATED);
            res.setError(HyenaConstants.INFO_3200);
        } else
            res.setStatus(HyenaConstants.RES_CODE_SUCCESS);

        uidRegistryDs.insertOrUpdate(registerPo);


        logger.info(LoggerHelper.formatLeaveLog(request));
        debugPerformance(request, startTime);

        return res;
    }


    @ApiOperation(value = "禁用帐号")
    @PostMapping(value = "/disableAccount")
    public BaseResponse disableAccount(
            HttpServletRequest request,
            @ApiParam(value = "积分类型", example = "score") @RequestParam(defaultValue = "default") String type,
            @ApiParam(value = "用户注册码") @RequestParam String registerCode,
            @ApiParam(value = "用户二级ID") @RequestParam(required = false) String subUid) {
        logger.info(LoggerHelper.formatEnterLog(request));
        this.pointDs.disableAccount(type, registerCode, subUid);
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
