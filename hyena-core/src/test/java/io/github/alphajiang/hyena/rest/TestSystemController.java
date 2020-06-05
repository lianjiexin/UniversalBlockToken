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
import io.github.alphajiang.hyena.model.base.BaseResponse;
import io.github.alphajiang.hyena.model.base.ListResponse;
import io.github.alphajiang.hyena.utils.JsonUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.UUID;

@AutoConfigureMockMvc
public class TestSystemController extends HyenaTestBase {
    private final Logger logger = LoggerFactory.getLogger(TestSystemController.class);

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    public void test_listPointType() throws Exception {

        RequestBuilder builder = MockMvcRequestBuilders.get("/ubt/system/listPointType");

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ListResponse<String> res = JsonUtils.fromJson(resBody, new TypeReference<ListResponse<String>>() {

        });
        List<String> list = res.getData();
        Assertions.assertFalse(list.isEmpty());
    }

    @Test
    public void test_addPointType() throws Exception {
        String pointType = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/system/addPointType").param("name", pointType);

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        BaseResponse res = JsonUtils.fromJson(resBody, BaseResponse.class);
        Assertions.assertEquals(HyenaConstants.RES_CODE_SUCCESS, res.getStatus());

        // add twice
        resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        res = JsonUtils.fromJson(resBody, BaseResponse.class);
        Assertions.assertEquals(HyenaConstants.RES_CODE_SUCCESS, res.getStatus());
    }

    @Test
    public void test_dumpMemCache() throws Exception {
        String pointType = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        RequestBuilder builder = MockMvcRequestBuilders.get("/ubt/system/dumpMemCache").param("name", pointType);

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        BaseResponse res = JsonUtils.fromJson(resBody, BaseResponse.class);
        Assertions.assertEquals(HyenaConstants.RES_CODE_SUCCESS, res.getStatus());

        // add twice
        resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        res = JsonUtils.fromJson(resBody, BaseResponse.class);
        Assertions.assertEquals(HyenaConstants.RES_CODE_SUCCESS, res.getStatus());
    }

    @Test
    public void test_dumpQueue() throws Exception {
        String pointType = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        RequestBuilder builder = MockMvcRequestBuilders.get("/ubt/system/dumpQueue").param("name", pointType);

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        BaseResponse res = JsonUtils.fromJson(resBody, BaseResponse.class);
        Assertions.assertEquals(HyenaConstants.RES_CODE_SUCCESS, res.getStatus());

        // add twice
        resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        res = JsonUtils.fromJson(resBody, BaseResponse.class);
        Assertions.assertEquals(HyenaConstants.RES_CODE_SUCCESS, res.getStatus());
    }
}
