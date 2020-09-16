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

import io.github.alphajiang.hyena.ds.mapper.UbtAccountMapper;
import io.github.alphajiang.hyena.model.po.UbtAccountPo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
public class UbtAccountDs {

    @Autowired
    private UbtAccountMapper ubtAccountMapper;

    public UbtAccountPo getUbtAccount(String uid) {
        return this.ubtAccountMapper.getUbtAccount(uid);
    }

    public void insertOrUpdate(UbtAccountPo ubtAccount)
    {
        this.ubtAccountMapper.insertOrUpdate(ubtAccount);
    }

    public int updateUbtAccount(@Param("uid") String uid, @Param("blockchainAccount") String blockchainAccount, @Param("priKey") String priKey)
    {
        return this.ubtAccountMapper.updateUbtAccount(uid,blockchainAccount,priKey);
    }


    public void createUbtAccountTable() {
        this.ubtAccountMapper.createUbtAccountTable();
    }
}
