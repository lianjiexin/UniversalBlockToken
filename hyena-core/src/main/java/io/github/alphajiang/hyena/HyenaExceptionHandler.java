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

package io.github.alphajiang.hyena;

import io.github.alphajiang.hyena.model.base.BaseResponse;
import io.github.alphajiang.hyena.model.exception.BaseException;
import io.github.alphajiang.hyena.model.exception.DuplicateUidRegistrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;

@RestControllerAdvice
public class HyenaExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(HyenaExceptionHandler.class);


    @ExceptionHandler
    @ResponseBody
    public BaseResponse errorHandler(NativeWebRequest request, Throwable exception) {
        logger.info("request {} exception {}", request, exception);

        String errorMsg;
        int status;
        String seq = this.getSeq(request);

        if (exception instanceof HttpMessageNotReadableException) {
            HttpMessageNotReadableException exp = (HttpMessageNotReadableException) exception;
            status = HyenaConstants.RES_CODE_PARAMETER_ERROR;
            errorMsg = "参数不能为空";
            logger.warn("message = {}", errorMsg, exp);
        } else if (exception instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException exp = (MethodArgumentNotValidException) exception;
            status = HyenaConstants.RES_CODE_PARAMETER_ERROR;
            FieldError fieldError = exp.getBindingResult().getFieldError();
            if (fieldError != null) {
                errorMsg = fieldError.getDefaultMessage();
            } else {
                errorMsg = "参数错误..";
            }
            logger.warn("param = {}, message = {}", exp.getParameter(), errorMsg, exp);

        } else if (exception instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException rtE = (MissingServletRequestParameterException) exception;
            status = HyenaConstants.RES_CODE_PARAMETER_ERROR;
            errorMsg = rtE.getMessage();
            logger.warn(exception.getMessage(), rtE);
            logger.info("请求参数错误: " + rtE.getMessage());
        } else if (exception instanceof DuplicateUidRegistrationException) {
            BaseException exp = (BaseException) exception;
            status = HyenaConstants.ERROR_DUPLICATE_UID_REGISTRATION_ATTEMPT;
            errorMsg = HyenaConstants.ERROR_2200 + exp.getMessage();
            logger.info("UID 重复: ");
            this.logException(exp);

        } else if (exception instanceof IllegalArgumentException) {
            IllegalArgumentException rtE = (IllegalArgumentException) exception;
            status = HyenaConstants.RES_CODE_PARAMETER_ERROR;
            errorMsg = rtE.getMessage();
            logger.warn(exception.getMessage(), rtE);
            logger.info("参数异常: " + exception.getMessage(), rtE);
        } else if (exception instanceof BaseException) {
            BaseException exp = (BaseException) exception;
            status = exp.getCode();
            errorMsg = exp.getMessage();
            this.logException(exp);


        } else {
            logger.error("未定义异常: " + exception.getMessage(), exception);
            errorMsg = "系统异常, 请联系系统管理员";
            status = HyenaConstants.RES_CODE_SERVER_ERROR;
        }

        logger.warn("status = {}, message = {}", status, errorMsg);

        BaseResponse res = new BaseResponse();
        res.setStatus(status);
        res.setError(errorMsg);
        res.setSeq(seq);
        return res;

    }

    private String getSeq(NativeWebRequest request) {
        try {
            return (String) request.getAttribute(HyenaConstants.REQ_IDEMPOTENT_SEQ_KEY, 0);
        } catch (Exception e) {
            return "";
        }
    }

    private void logException(BaseException exp) {
        if (exp.getLogLevel() == Level.ERROR) {
            logger.error(exp.getMessage(), exp);
        } else if (exp.getLogLevel() == Level.WARN) {
            logger.warn(exp.getMessage(), exp);
        } else if (exp.getLogLevel() == Level.INFO) {
            logger.info(exp.getMessage(), exp);
        } else if (exp.getLogLevel() == Level.DEBUG) {
            logger.debug(exp.getMessage(), exp);
        } else if (exp.getLogLevel() == Level.TRACE) {
            logger.trace(exp.getMessage(), exp);
        } else {
            logger.error(exp.getMessage(), exp);
        }
    }

}
