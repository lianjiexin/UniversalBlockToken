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

import com.fasterxml.jackson.core.type.TypeReference;
import io.github.alphajiang.hyena.HyenaConstants;
import io.github.alphajiang.hyena.HyenaTestBase;
import io.github.alphajiang.hyena.biz.point.PointUsage;
import io.github.alphajiang.hyena.biz.point.PointUsageFacade;
import io.github.alphajiang.hyena.ds.service.PointRecDs;
import io.github.alphajiang.hyena.model.base.BaseResponse;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.model.base.ObjectResponse;
import io.github.alphajiang.hyena.model.dto.PointLogDto;
import io.github.alphajiang.hyena.model.dto.PointRecDto;
import io.github.alphajiang.hyena.model.dto.PointRecLogDto;
import io.github.alphajiang.hyena.model.param.*;
import io.github.alphajiang.hyena.model.po.PointPo;
import io.github.alphajiang.hyena.model.po.UidRegistryPo;
import io.github.alphajiang.hyena.model.type.PointOpType;
import io.github.alphajiang.hyena.model.vo.PointLogBi;
import io.github.alphajiang.hyena.model.vo.PointOpResult;
import io.github.alphajiang.hyena.utils.CollectionUtils;
import io.github.alphajiang.hyena.utils.DateUtils;
import io.github.alphajiang.hyena.utils.JsonUtils;
import io.github.alphajiang.hyena.wechat.WechatPayConnector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoConfigureMockMvc
public class TestAccountController extends HyenaTestBase {
    private final Logger logger = LoggerFactory.getLogger(TestAccountController.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PointUsageFacade pointUsageFacade;

    @Autowired
    private PointRecDs pointRecDs;

    @Autowired
    private WechatPayConnector wechatPayConnector;


    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    public void test_getUidRegistry() throws Exception {

        RequestBuilder builder = MockMvcRequestBuilders.get("/ubt/account/getUidRegistry")
                .param("registerCode", super.getRegisterCode());


        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<UidRegistryPo> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<UidRegistryPo>>() {

        });
        UidRegistryPo ret = res.getData();
        Assertions.assertNotNull(ret);
    }

    @Test
    public void test_getUidRegistryByUid() throws Exception {

        RequestBuilder builder = MockMvcRequestBuilders.get("/ubt/account/getUidRegistryByUid")
                .param("uid", super.getUid());


        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<UidRegistryPo> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<UidRegistryPo>>() {

        });
        UidRegistryPo ret = res.getData();
        Assertions.assertNotNull(ret);
    }

    @Test
    public void test_registerUid() throws Exception {

        RequestBuilder builder = MockMvcRequestBuilders.get("/ubt/account/registerUid")
                .param("registerCode", super.getRegisterCode())
                .param("uid",super.getUid())
                .param("password",super.getPassword());


        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<UidRegistryPo> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<UidRegistryPo>>() {

        });
        UidRegistryPo ret = res.getData();
        Assertions.assertNotNull(ret);
    }




}
