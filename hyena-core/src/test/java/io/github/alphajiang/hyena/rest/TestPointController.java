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
public class TestPointController extends HyenaTestBase {
    private final Logger logger = LoggerFactory.getLogger(TestPointController.class);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PointUsageFacade pointUsageFacade;

    @Autowired
    private PointRecDs pointRecDs;


    @BeforeEach
    public void init() {
        super.init();
    }

    @Test
    public void test_getPoint() throws Exception {

        RequestBuilder builder = MockMvcRequestBuilders.get("/ubt/point/getPoint")
                .param("type", super.getPointType())
                .param("uid", super.getUid());

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointPo> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointPo>>() {

        });
        PointPo ret = res.getData();
        Assertions.assertNotNull(ret);
    }

    @Test
    public void test_getUidRegistry() throws Exception {

        RequestBuilder builder = MockMvcRequestBuilders.get("/ubt/point/getUidRegistry")
                .param("registerCode", super.getRegisterCode());


        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<UidRegistryPo> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<UidRegistryPo>>() {

        });
        UidRegistryPo ret = res.getData();
        Assertions.assertNotNull(ret);
    }

    @Test
    public void test_registerUid() throws Exception {

        RequestBuilder builder = MockMvcRequestBuilders.get("/ubt/point/registerUid")
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

    @Test
    public void test_listPoint() throws Exception {

        ListPointParam param = new ListPointParam();
        param.setType(super.getPointType());
        param.setUidList(List.of(super.getUid()));
        param.setStart(0L).setSize(10);


        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/point/listPoint")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ListResponse<PointPo> res = JsonUtils.fromJson(resBody, new TypeReference<ListResponse<PointPo>>() {

        });
        List<PointPo> list = res.getData();
        Assertions.assertFalse(list.isEmpty());
    }

    @Test
    public void test_listPoint_fail_a() throws Exception {


        ListPointParam param = new ListPointParam();
        param.setType("invalid_type_test");
        param.setStart(0L).setSize(10);

        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/point/listPoint")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        BaseResponse res = JsonUtils.fromJson(resBody, BaseResponse.class);

        Assertions.assertFalse(res.getStatus() == HyenaConstants.RES_CODE_SUCCESS);
    }

    @Test
    public void test_listPointLog() throws Exception {
        Thread.sleep(100L);
        ListPointLogParam param = new ListPointLogParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setLogTypes(List.of(PointOpType.INCREASE.code()));
        param.setSourceTypes(List.of(super.getSourceType(), 2, 3));
        param.setOrderTypes(List.of(super.getOrderType(), 4, 5, 6));
        param.setPayTypes(List.of(super.getPayType(), 7, 8, 9));
        param.setStart(0L).setSize(10);

        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/point/listPointLog")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ListResponse<PointLogDto> res = JsonUtils.fromJson(resBody, new TypeReference<>() {

        });
        List<PointLogDto> list = res.getData();
        Assertions.assertFalse(list.isEmpty());
        Assertions.assertTrue(res.getTotal() > 0L);
    }

    @Test
    public void test_listPointLogBi() throws Exception {
        Thread.sleep(100L);
        ListPointLogParam param = new ListPointLogParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());

        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/point/listPointLogBi")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ListResponse<PointLogBi> res = JsonUtils.fromJson(resBody, new TypeReference<>() {

        });
        List<PointLogBi> list = res.getData();
        Assertions.assertFalse(list.isEmpty());
        Assertions.assertTrue(res.getTotal() > 0L);
    }

    @Test
    public void test_listPointRecord() throws Exception {
        Thread.sleep(100L);
        ListPointRecParam param = new ListPointRecParam();
        param.setType(super.getPointType());
        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/point/listPointRecord")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ListResponse<PointRecDto> res = JsonUtils.fromJson(resBody, new TypeReference<>() {

        });
        List<PointRecDto> list = res.getData();
        Assertions.assertFalse(list.isEmpty());
        Assertions.assertTrue(res.getTotal() > 0L);
    }

    @Test
    public void test_listPointRecordLog() throws Exception {
        ListPointRecLogParam param = new ListPointRecLogParam();
        param.setType(super.getPointType());
        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/point/listPointRecordLog")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ListResponse<PointRecLogDto> res = JsonUtils.fromJson(resBody, new TypeReference<ListResponse<PointRecLogDto>>() {

        });
        List<PointRecLogDto> list = res.getData();
        Assertions.assertFalse(list.isEmpty());
        Assertions.assertTrue(res.getTotal() > 0L);
    }


    @Test
    public void test_increase() throws Exception {
        PointIncreaseParam param = new PointIncreaseParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setSubUid(super.getSubUid());
        param.setPoint(BigDecimal.valueOf(9876L));
        param.setSeq("gewgewglekjwklehjoipvnbldsalkdjglajd");
        param.setSourceType(1).setOrderType(2).setPayType(3);
        Map<String, Object> extra = new HashMap<>();
        extra.put("aaa", "bbbb");
        extra.put("ccc", 123);
        param.setExtra(JsonUtils.toJsonString(extra));
        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/point/increase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        PointPo result = res.getData();
        Assertions.assertNotNull(result);
    }

    @Test
    public void test_increase_fail() throws Exception {
        StringBuilder buf = new StringBuilder();
        buf.append("{").append("\"uid\":\"").append(super.getUid()).append("\",")
                .append("\"point\":\"").append("abcd").append("\"")
                .append("}");
        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/point/increase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buf.toString());

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        BaseResponse res = JsonUtils.fromJson(resBody, BaseResponse.class);
        Assertions.assertEquals(HyenaConstants.RES_CODE_PARAMETER_ERROR, res.getStatus());
    }

    @Test
    public void test_increase_fail_b() throws Exception {
        StringBuilder buf = new StringBuilder();
        buf.append("{").append("\"uid\":\"").append(super.getUid()).append("\",")
                .append("\"point\":\"").append(123).append("\",")
                .append("\"type\":null")
                .append("}");
        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/point/increase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buf.toString());

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        BaseResponse res = JsonUtils.fromJson(resBody, BaseResponse.class);
        Assertions.assertEquals(HyenaConstants.RES_CODE_PARAMETER_ERROR, res.getStatus());
    }


    @Test
    public void test_decrease() throws Exception {
        PointOpParam param = new PointOpParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setSubUid(super.getSubUid());
        param.setPoint(BigDecimal.valueOf(1L));
        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/point/decrease")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        PointPo result = res.getData();
        Assertions.assertNotNull(result);
    }

    @Test
    public void test_freeze() throws Exception {
        PointFreezeParam param = new PointFreezeParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setSubUid(super.getSubUid());
        param.setPoint(BigDecimal.valueOf(1L));
        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/point/freeze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        PointPo result = res.getData();
        Assertions.assertNotNull(result);
    }

    @Test
    public void test_decreaseFrozen() throws Exception {
        PointUsage freeze = new PointUsage();
        freeze.setPoint(BigDecimal.valueOf(9L)).setType(super.getPointType()).setUid(super.getUid());
        this.pointUsageFacade.freeze(freeze);

        PointOpParam param = new PointOpParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setSubUid(super.getSubUid());
        param.setPoint(BigDecimal.valueOf(9L));
        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/point/decreaseFrozen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        PointPo result = res.getData();
        Assertions.assertNotNull(result);
    }

    /**
     * 消费积分, 同时解冻积分
     */
    @Test
    public void test_decreaseFrozen_unfreeze() throws Exception {
        PointUsage freeze = new PointUsage();
        freeze.setPoint(BigDecimal.valueOf(14L)).setType(super.getPointType())
                .setUid(super.getUid()).setSubUid(super.getSubUid());
        this.pointUsageFacade.freeze(freeze);

        PointDecreaseFrozenParam param = new PointDecreaseFrozenParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setSubUid(super.getSubUid());
        param.setUnfreezePoint(BigDecimal.valueOf(5L)); // 要做解冻的部分
        param.setPoint(BigDecimal.valueOf(9L)); // 要消费的部分
        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/point/decreaseFrozen")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        PointPo result = res.getData();
        Assertions.assertNotNull(result);
    }


    @Test
    public void test_unfreeze() throws Exception {
        PointUsage freeze = new PointUsage();
        freeze.setPoint(BigDecimal.valueOf(9L)).setType(super.getPointType())
                .setUid(super.getUid()).setSubUid(super.getSubUid());
        this.pointUsageFacade.freeze(freeze);

        PointUnfreezeParam param = new PointUnfreezeParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setSubUid(super.getSubUid());
        param.setPoint(BigDecimal.valueOf(9L));
        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/point/unfreeze")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        PointPo result = res.getData();
        Assertions.assertNotNull(result);
    }


    @Test
    public void test_refund() throws Exception {
        PointRefundParam param = new PointRefundParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setSubUid(super.getSubUid());
        param.setCost(BigDecimal.valueOf(5L)); // 退款部分的成本
        param.setPoint(BigDecimal.valueOf(10L)); // 退款的部分
        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/point/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        PointPo result = res.getData();
        Assertions.assertNotNull(result);
    }

    @Test
    public void test_cancel() throws Exception {


        ListPointRecParam listParam = new ListPointRecParam();
        listParam.setFrozen(false).setUid(super.getUid()).setType(super.getPointType());
        Thread.sleep(100L);
        List<PointRecDto> recList = this.pointRecDs.listPointRec(listParam);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(recList));
        PointRecDto rec = recList.iterator().next();


        PointCancelParam param = new PointCancelParam();
        param.setType(super.getPointType());
        param.setUid(super.getUid());
        param.setSubUid(super.getSubUid());
        param.setPoint(rec.getAvailable());
        param.setRecId(rec.getId());
        RequestBuilder builder = MockMvcRequestBuilders.post("/ubt/point/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtils.toJsonString(param));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<PointOpResult> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<PointOpResult>>() {

        });
        PointPo result = res.getData();
        Assertions.assertNotNull(result);
    }

    @Test
    public void test_getIncreasedPoint() throws Exception {
        Thread.sleep(100L);
        Calendar start = Calendar.getInstance();
        start.add(Calendar.DATE, -1);
        Calendar end = Calendar.getInstance();
        end.add(Calendar.DATE, 1);

        RequestBuilder builder = MockMvcRequestBuilders.get("/ubt/point/getIncreasedPoint")
                .param("type", super.getPointType())
                .param("uid", super.getUid())
                .param("start", DateUtils.toYyyyMmDdHhMmSs(start))
                .param("end", DateUtils.toYyyyMmDdHhMmSs(end));

        String resBody = mockMvc.perform(builder).andReturn().getResponse().getContentAsString();
        logger.info("response = {}", resBody);
        ObjectResponse<Long> res = JsonUtils.fromJson(resBody, new TypeReference<ObjectResponse<Long>>() {

        });
        Long result = res.getData();
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result  > 0L);
    }
}
