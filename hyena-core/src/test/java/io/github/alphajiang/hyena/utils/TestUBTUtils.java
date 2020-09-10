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

package io.github.alphajiang.hyena.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/*
For temporary testing purpose, not the real unit tests.
 */
public class TestUBTUtils {

    //@Test
    public void testGetBalance() throws IOException {
        String networkAddress = UBTConstants.localNetworkPrefix;
        System.out.println("Check Balance on network:" + networkAddress);
        BigDecimal ret = UBTUtils.getBalance(UBTTestConstants.localTestAddress1, UBTConstants.localContractAddress,
                networkAddress);
        String strReturnValue = ret.toPlainString();
        System.out.println("Current Balance is : " + strReturnValue);
        Assertions.assertNotNull(ret.toPlainString());
    }

   //@Test
    public void testTransferERC20Token() throws InterruptedException, ExecutionException, IOException {

        String hash = UBTUtils.transferERC20Token(UBTTestConstants.localTestAddress0, UBTTestConstants.localTestAddress1,
                BigInteger.valueOf(100),
                UBTTestConstants.priKey_localTestAddress0,UBTConstants.localNetworkPrefix,UBTConstants.localContractAddress,18);

        System.out.println("Return Hash Value is : " + hash);
        Assertions.assertNotNull(hash);


    }


}
