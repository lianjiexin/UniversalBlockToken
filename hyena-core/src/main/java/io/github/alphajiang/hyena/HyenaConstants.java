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

public interface HyenaConstants {

    int SQL_VERSION = 3;

    int RES_CODE_SUCCESS = 0;
    int RES_CODE_SERVICE_ERROR = 1000;
    int RES_CODE_NO_ENOUGH_POINT = 1010;
    int RES_CODE_PARAMETER_ERROR = 1100;

    int RES_CODE_STATUS_ERROR = 1200;
    int ERROR_REGISTER_CODE_USED = -1210; // error code should be less than 0;
    int ERROR_REGISTER_CODE_NOT_FOUND = -1220; // error code should be less than 0;
    int ERROR_REGISTER_CODE_ALREADY_DISABLED = -1230; // error code should be less than 0;
    int ERROR_PASSWORD_NOT_MATCH = -1240; // error code should be less than 0;
    int ERROR_DUPLICATE_UID_REGISTRATION_ATTEMPT = -2200; // UID has to be unique for each register code


    int RES_CODE_SERVICE_BUSY = 1300;
    int RES_CODE_DUPLICATE = 2000;
    int RES_CODE_DUPLICATE_IDEMPOTENT = 2001;
    int RES_UNSUPPORTED_POINT_TYPE = 3000;
    int RES_CODE_SERVER_ERROR = 9000;
    int RES_CODE_UNKNOW_ERROR = 9999;

    String PREFIX_POINT_TABLE_NAME = "t_point_";

    String REQ_IDEMPOTENT_SEQ_KEY = "seq";


    String CONST_TEST_DB_DRIVER = "org.h2.Driver";

    String ERROR_1210 = "注册码已绑定，请先从其他小程序上解绑再注册";
    String ERROR_1220 = "注册码或密码输入错误";
    String ERROR_1230 = "注册码已经解绑，无法再次解绑";
    String ERROR_1240 = "密码不一致";

    String ERROR_2200 = "同一UID不能绑定多个注册码，请更换UID.";
}
