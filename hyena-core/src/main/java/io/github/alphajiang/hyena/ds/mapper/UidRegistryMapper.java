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

package io.github.alphajiang.hyena.ds.mapper;

import io.github.alphajiang.hyena.model.po.UbtAccountPo;
import io.github.alphajiang.hyena.model.po.UidRegistryPo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UidRegistryMapper {

    void createUidRegistryTable();

    void insertOrUpdate(UidRegistryPo uidRegistry);

    int updateUidRegistry(@Param("registerCode") String registerCode, @Param("uid") String uid, @Param("password") String password, @Param("enable") boolean enable);

    UidRegistryPo getUidRegistry(@Param("registerCode") String registerCode);
}
