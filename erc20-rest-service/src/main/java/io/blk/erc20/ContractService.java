package io.blk.erc20;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.interfaces.ECPrivateKey;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import io.blk.erc20.generated.HumanStandardToken;
import io.reactivex.Flowable;
import io.reactivex.annotations.Nullable;
import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.*;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.tx.ClientTransactionManager;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import static org.web3j.tx.Contract.GAS_LIMIT;
import static org.web3j.tx.ManagedTransaction.GAS_PRICE;

/**
 * Our smart contract service.
 */
@Service
public class ContractService {

    private final Quorum quorum;

    private final NodeConfiguration nodeConfiguration;

    @Autowired
    public ContractService(Quorum quorum, NodeConfiguration nodeConfiguration) {
        this.quorum = quorum;
        this.nodeConfiguration = nodeConfiguration;
    }


    public String name(String contractAddress) throws Exception {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {


            return humanStandardToken.name().send();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    public String totalSupply(String contractAddress) throws Exception {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return humanStandardToken.totalSupply().send().toString();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    public JSONObject genAddress() throws Exception {
        JSONObject processJson = new JSONObject();
        try {
            String seed = UUID.randomUUID().toString();
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            BigInteger privateKeyInDec = ecKeyPair.getPrivateKey();

            String sPrivatekeyInHex = privateKeyInDec.toString(16);

            WalletFile aWallet = Wallet.createLight(seed, ecKeyPair);
            String sAddress = aWallet.getAddress();


            processJson.put("address", "0x" + sAddress);
            processJson.put("privatekey", sPrivatekeyInHex);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return processJson;
    }

    /**
     *
     *
     * http://api-cn.etherscan.com/api?module=transaction&action=getstatus&txhash=0x15f8e5ea1079d9a0bb04a4c58ae5fe7654b5b2b4463375ff7ffb490aa0032f3a&apikey=YourApiKeyToken
     *
     * @return
     * @throws Exception
     */
    public JSONObject CheckContractExecutionStatus(String transactionHash) throws Exception {
        JSONObject processJson = new JSONObject();
        try {

            String url = "http://api-cn.etherscan.com/api?module=transaction&action=getstatus&txhash="+transactionHash+"&apikey=67BWK4YUYY22GEP6XZ618ESPHXAHGD3WZS";
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();

                System.out.println(response.body().string());
            } catch (IOException e) {
                throw new IOException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return processJson;
    }


    /**
     * http://api-cn.etherscan.com/api?module=transaction&action=gettxreceiptstatus&txhash=0x513c1ba0bebf66436b5fed86ab668452b7805593c05073eb2d51d3a52f480a76&apikey=YourApiKeyToken
     * @return
     * @throws Exception
     */
    public String checkTransactionReceiptStatus(String transactionHash) throws Exception {
        //JSONObject processJson = new JSONObject();
        String str ="";
        try {
            String url = "http://api-cn.etherscan.com/api?module=transaction&action=gettxreceiptstatus&txhash="+transactionHash+"&apikey=67BWK4YUYY22GEP6XZ618ESPHXAHGD3WZS";
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();
                str = response.body().string();
            } catch (IOException e) {
                throw new IOException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return str;
    }


    public String decimals(String contractAddress) throws Exception {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return humanStandardToken.decimals().send().toString();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    public EthBlockNumber  getLastestBlock() throws Exception {
        EthBlockNumber block ;
        try {
            block =quorum.ethBlockNumber().send();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return block;
    }

    public String balanceOf(String contractAddress, String ownerAddress) throws Exception {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return humanStandardToken.balanceOf(ownerAddress).send().toString();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public BigInteger ethBalanceOf( String ownerAddress) throws Exception {
        BigInteger balance = BigInteger.ZERO;
        try {
            balance  = quorum.ethGetBalance(ownerAddress, DefaultBlockParameterName.LATEST).send().getBalance();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return balance;
    }

    public String symbol(String contractAddress) throws Exception {
        HumanStandardToken humanStandardToken = load(contractAddress);
        try {
            return humanStandardToken.symbol().send();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    public EthBlock getBlockByNum(BigInteger num) throws Exception {
        EthBlock block =null;
        try {
             block = quorum.ethGetBlockByNumber(DefaultBlockParameter.valueOf(num),true).send();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return block;
    }


    public JSONArray transferEvent(BigInteger from,  BigInteger to ,String contractAddress ){
        JSONArray array = new JSONArray();
        try {
            EthFilter filter = new EthFilter(DefaultBlockParameter.valueOf(from), DefaultBlockParameter.valueOf(to), contractAddress);
            filter.addSingleTopic(EventEncoder.encode(HumanStandardToken.TRANSFER_EVENT));

            List<EthLog.LogResult>  logs =  quorum.ethGetLogs(filter).send().getLogs();
            if(logs!=null && logs.size()>0){
                JSONObject jsonObject = new JSONObject();
                for(int i=0;i<logs.size() ;i++){
                    Log logResult=(Log)logs.get(i).get();

                    List<String> topics = logResult.getTopics();
                    jsonObject.put("BlockNumber",logResult.getBlockNumber());
                    jsonObject.put("transactionHash",logResult.getTransactionHash());


                    jsonObject.put("fromAddress",topics.get(1).substring(26));
                    jsonObject.put("toAddress",topics.get(2).substring(26));
                    jsonObject.put("value",new BigInteger(logResult.getData().substring(2), 16));
                    array.add(jsonObject);

                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return array;
    }

    public EthSendTransaction transterCoin(String toAddress,String contractAddress , String amount ,List<String>  privateKey){

        EthSendTransaction ethSendTransaction = null;
        Credentials credentials;
        try {
            credentials = Credentials.create(privateKey.get(0));

            EthGetTransactionCount ethGetTransactionCount = quorum.ethGetTransactionCount(
                    credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();

            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            EthGasPrice price = quorum.ethGasPrice().send();

            BigInteger value = Convert.toWei(amount, Convert.Unit.MWEI).toBigInteger();

            org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                    "transfer",
                    Arrays.asList(new Address(toAddress), new Uint256(value)),
                    Arrays.asList(new TypeReference<Type>() {
                    }));

            String encodedFunction = FunctionEncoder.encode(function);

            RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, price.getGasPrice(),
                   Convert.toWei("200000", Convert.Unit.WEI).toBigInteger(), contractAddress, encodedFunction);
                    //block.getBlock().getGasLimit(), contractAddress, encodedFunction);

            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            ethSendTransaction = quorum.ethSendRawTransaction(hexValue).send();


        }catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ethSendTransaction;


    }

    public EthSendTransaction transferEth(String toAddress, String amount ,List<String>  privateKey){

        EthSendTransaction ethSendTransaction = null;
        Credentials credentials;
        try {
            credentials = Credentials.create(privateKey.get(0));

            EthGetTransactionCount ethGetTransactionCount = quorum.ethGetTransactionCount(
                    credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();

            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            EthGasPrice price = quorum.ethGasPrice().send();

            BigInteger value = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();

            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                                     nonce, price.getGasPrice(), Convert.toWei("200000", Convert.Unit.WEI).toBigInteger(), toAddress, value);

            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);

            ethSendTransaction = quorum.ethSendRawTransaction(hexValue).send();

        }catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ethSendTransaction;

    }

    public EthGetTransactionReceipt getethGetTransactionReceipt( String transactionHash ){
        EthGetTransactionReceipt ethGetTransactionReceipt = null;
        try{
            ethGetTransactionReceipt =  quorum.ethGetTransactionReceipt(transactionHash).send();
            }catch (Exception e) {
                throw new RuntimeException(e);
            }

        return ethGetTransactionReceipt;
    }


    private HumanStandardToken load(String contractAddress) {
        TransactionManager transactionManager = new ClientTransactionManager(
                quorum, nodeConfiguration.getFromAddress(), Collections.emptyList());
        return HumanStandardToken.load(
                contractAddress, quorum, transactionManager, GAS_PRICE, GAS_LIMIT);
    }

}
