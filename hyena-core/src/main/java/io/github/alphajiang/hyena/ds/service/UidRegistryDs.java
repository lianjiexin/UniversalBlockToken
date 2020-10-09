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

package io.github.alphajiang.hyena.ds.service;

import io.github.alphajiang.hyena.ds.mapper.UidRegistryMapper;
import io.github.alphajiang.hyena.model.po.UidRegistryPo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class UidRegistryDs {

    @Autowired
    private UidRegistryMapper uidRegistryMapper;

    public UidRegistryPo getUidRegistry(String registerCode) {
        return this.uidRegistryMapper.getUidRegistry(registerCode);
    }

    public void insertOrUpdate(UidRegistryPo uidRegistry)
    {
        this.uidRegistryMapper.insertOrUpdate(uidRegistry);
    }

    public int updateUidRegistry(@Param("registerCode") String registerCode, @Param("uid") String uid,
                                @Param("password") String password, @Param("enable") boolean enable)
    {
        return this.uidRegistryMapper.updateUidRegistry(registerCode,uid,password,enable);
    }


    public void createUidRegistryTable() {
        this.uidRegistryMapper.createUidRegistryTable();
    }
}
