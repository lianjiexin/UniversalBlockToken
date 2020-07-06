package io.github.alphajiang.hyena.utils;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class UBTUtils {

    /**
     * erc20代币转账
     *
     * @param from            转账地址
     * @param to              收款地址
     * @param value           转账金额
     * @param privateKey      转账这私钥
     * @param contractAddress 代币合约地址
     * @return 交易哈希
     * @throws ExecutionException 执行异常
     * @throws InterruptedException 中断异常
     * @throws IOException IO 异常
     */
    public static String transferERC20Token(String from, String to, BigInteger value, String privateKey, String networkAddress, String contractAddress,int decimal) throws ExecutionException, InterruptedException, IOException {
        Web3j web3j = Web3j.build(new HttpService(networkAddress + contractAddress));
        //加载转账所需的凭证，用私钥
        Credentials credentials = Credentials.create(privateKey);
        //获取nonce，交易笔数
        BigInteger nonce;
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING).send();
        if (ethGetTransactionCount == null) {
            return null;
        }
        nonce = ethGetTransactionCount.getTransactionCount();
        //gasPrice和gasLimit 都可以手动设置
        BigInteger gasPrice;
        EthGasPrice ethGasPrice = web3j.ethGasPrice().sendAsync().get();
        if (ethGasPrice == null) {
            return null;
        }
        gasPrice = ethGasPrice.getGasPrice();
        //BigInteger.valueOf(4300000L) 如果交易失败 很可能是手续费的设置问题
        BigInteger gasLimit = BigInteger.valueOf(60000L);
        //ERC20代币合约方法
        BigInteger val = new BigDecimal(value).multiply(new BigDecimal("10").pow(decimal)).toBigInteger();// 单位换算

        Function function = new Function(
                "transfer",
                Arrays.asList(new Address(to), new Uint256(val)),
                Collections.singletonList(new TypeReference<Type>() {
                }));
        //创建RawTransaction交易对象
        String encodedFunction = FunctionEncoder.encode(function);
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit,
                contractAddress, encodedFunction);

        //签名Transaction
        byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signMessage);
        //发送交易
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).sendAsync().get();
        String hash = ethSendTransaction.getTransactionHash();
        if (hash != null) {
            return hash;
        }
        return null;
    }

    private static final String DATA_PREFIX = "0x70a08231000000000000000000000000";

    /**
     *
     * @param address Token 地址
     * @param contractAddress Contract 地址
     * @param networkAddress Network 地址， 可以是local, main net, Rinkeby 等
     * @return balance of Token Address
     * @throws IOException IO 异常
     */
    public static BigDecimal getBalance(String address, String contractAddress,String networkAddress) throws IOException {
        String value = Admin.build(new HttpService(networkAddress))
                .ethCall(org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(address,
                        contractAddress, DATA_PREFIX + address.substring(2)), DefaultBlockParameterName.PENDING).send().getValue();
        String s = new BigInteger(value.substring(2), 16).toString();
        BigDecimal balance = new BigDecimal(s).divide(toDecimal(18), 6, RoundingMode.HALF_DOWN);

        return balance;
    }

    /**
     * 转换成符合 decimal 的数值
     * @param decimal 转换成符合 decimal 的数值
     * @return 转化完的数值
     */
    public static BigDecimal toDecimal(int decimal){
        StringBuffer sbf = new StringBuffer("1");
        for (int i = 0; i < decimal; i++) {
            sbf.append("0");
        }
        return new BigDecimal(sbf.toString());
    }




}
