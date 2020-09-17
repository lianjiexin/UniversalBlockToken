package io.github.alphajiang.hyena.utils;

import io.github.alphajiang.hyena.ds.service.UbtAccountDs;
import io.github.alphajiang.hyena.model.po.UbtAccountPo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.NewAccountIdentifier;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

@Service
public class UBTConnector {

    private static final Logger logger = LoggerFactory.getLogger(UBTConnector.class);

    private Admin web3j;

    @Autowired
    private UbtAccountDs ubtAccountDs;

    private static String defaultUserPassword = "123456";

    @Value("${spring.ubt.contractAddress}")
    private String contractAddress;

    @Value("${spring.ubt.primaryAcctAddress}")
    private String primaryAcctAddress;

    @Value("${spring.ubt.primaryAcctpriKey}")
    private String primaryAcctpriKey;

    @Value("${spring.ubt.networkPrefix}")
    private String networkPrefix;


    public UBTConnector(){


    }

    @PostConstruct
    public void init()
    {
        String url = networkPrefix + contractAddress;
        logger.info("web3j connecting to : " + url);
        this.web3j = Admin.build(new HttpService(url));

    }


    /**
     * Deposit UBT from primary account address to clientAddress
     * @param clientId clientId to retrieve client blockChain Address
     * @param value value of UBT
     * @param decimal decimal to support
     * @return transaction Hash
     * @throws InterruptedException if the Execution is interrupted for some reason
     * @throws ExecutionException general execution exception
     * @throws IOException all IO exception
     */
    public String depositUBT(String clientId, BigDecimal value, int decimal) throws InterruptedException, ExecutionException, IOException {
        logger.info("primaryAcctAddress:" + primaryAcctAddress);
        logger.info("clientId:" + clientId);
        logger.info("value:" + value);
        logger.info("priKey_primaryAcctAddress: " + primaryAcctpriKey);

        UbtAccountPo ubtAccount = ubtAccountDs.getUbtAccount(clientId);
        String toAddress = ubtAccount.getBlockchainAccount();
        logger.info("ToAddress: " + toAddress);

        String hash = transferERC20Token(web3j, primaryAcctAddress,toAddress,value, primaryAcctpriKey,18);
        logger.info("transaction hash: " + hash);
        return hash;
    }

    public UbtAccountPo getUbtAccountPo(String uid)
    {
        return ubtAccountDs.getUbtAccount(uid);
    }

    /**
     * return UBT from client Account to primary account address
     * @param clientId Id for the client
     * @param value value of UBT
     * @param decimal decimal to support
     * @return transaction Hash
     * @throws InterruptedException if the Execution is interrupted for some reason
     * @throws ExecutionException general execution exception
     * @throws IOException all IO exception
     */
    public String returnUBT(String clientId, BigDecimal value, int decimal) throws InterruptedException, ExecutionException, IOException {

        logger.info("clientId:" + clientId);
        logger.info("value:" + value);

        UbtAccountPo ubtAccount = ubtAccountDs.getUbtAccount(clientId);

        String hash = transferERC20Token(web3j, ubtAccount.getBlockchainAccount(),primaryAcctAddress,value,ubtAccount.getPriKey(),18);
        logger.info("transaction hash: " + hash);
        return hash;
    }

    public String createNewAccount() throws IOException {

            NewAccountIdentifier newAccountIdentifier = web3j.personalNewAccount(defaultUserPassword).send();
            String address = newAccountIdentifier.getAccountId();
            System.out.println("new account address " + address);
            return address;

    }

    /**
     * erc20代币转账
     * @param web3j           Web3j
     * @param from            转账地址
     * @param to              收款地址
     * @param value           转账金额
     * @param privateKey      转账地址私钥
     * @param decimal 余额位数
     * @return 交易哈希
     * @throws ExecutionException 执行异常
     * @throws InterruptedException 中断异常
     * @throws IOException IO 异常
     */
    public String transferERC20Token(Admin web3j,String from, String to, BigDecimal value, String privateKey,int decimal) throws ExecutionException, InterruptedException, IOException {
        logger.info("fromAddress: " + from + ",\t toAddress: "
                + to + ",\t value:" + value + ",\t privateKey:" + privateKey + ",\t decimal:" + decimal);
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
        BigInteger val = value.multiply(new BigDecimal("10").pow(decimal)).toBigInteger();// 单位换算

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
