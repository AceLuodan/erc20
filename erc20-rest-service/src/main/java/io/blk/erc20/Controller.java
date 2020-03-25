package io.blk.erc20;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

import io.reactivex.annotations.Nullable;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;

/**
 * Controller for our ERC-20 contract API.
 */
@Api("ERC-20 token standard API")
@RestController
public class Controller {

    private final ContractService ContractService;

    @Autowired
    public Controller(ContractService ContractService) {
        this.ContractService = ContractService;
    }


    @ApiOperation("获取usdt名称  0xdAC17F958D2ee523a2206206994597C13D831ec7")
    @RequestMapping(value = "/{contractAddress}/name", method = RequestMethod.GET)
    String name(@PathVariable String contractAddress) throws Exception {
        return ContractService.name(contractAddress);
    }

    @ApiOperation("获取usdt总供应量  0xdAC17F958D2ee523a2206206994597C13D831ec7")
    @RequestMapping(value = "/{contractAddress}/totalSupply", method = RequestMethod.GET)
    String totalSupply(@PathVariable String contractAddress) throws Exception {
        return ContractService.totalSupply(contractAddress);
    }

    @ApiOperation("Get decimal precision of tokens")
    @RequestMapping(value = "/{contractAddress}/decimals", method = RequestMethod.GET)
    String decimals(@PathVariable String contractAddress) throws Exception {
        return ContractService.decimals(contractAddress);
    }

    @ApiOperation("获取USDT余额，")
    @RequestMapping(
            value = "/{contractAddress}/balanceOf/{ownerAddress}", method = RequestMethod.GET)
    String balanceOf(
            @PathVariable String contractAddress,
            @PathVariable String ownerAddress) throws Exception {
        return ContractService.balanceOf(contractAddress, ownerAddress);
    }


    @ApiOperation("获取ETH余额 ,ETH  小数位 18  0xdAC17F958D2ee523a2206206994597C13D831ec7")
    @RequestMapping(
            value = "/ethBalanceOf/{ownerAddress}", method = RequestMethod.GET)
    BigInteger ethBalanceOf(

            @PathVariable String ownerAddress) throws Exception {
        return ContractService.ethBalanceOf(ownerAddress);
    }


    @ApiOperation("获取usdt标识  0xdAC17F958D2ee523a2206206994597C13D831ec7")
    @RequestMapping(value = "/{contractAddress}/symbol", method = RequestMethod.GET)
    String symbol(@PathVariable String contractAddress) throws Exception {
        return ContractService.symbol(contractAddress);
    }


    @ApiOperation("通过块数获取区块数据")
    @RequestMapping(value = "/{num}/block", method = RequestMethod.GET)
    EthBlock symbol(@PathVariable BigInteger num) throws Exception {
        return ContractService.getBlockByNum(num);
    }

    @ApiOperation(
            value = "Transfer tokens you own to another address",
            notes = "USDT decimal 6")
    @ApiImplicitParam(name = "privateFor",
            value = "Comma separated list of public keys of enclave nodes that transaction is "
                    + "private for",
            paramType = "header",
            dataType = "string")
    @RequestMapping(value = "/{contractAddress}/transfer", method = RequestMethod.POST)
    EthSendTransaction transfer(
            HttpServletRequest request,
            @PathVariable String contractAddress,
            @RequestBody TransferRequest transferRequest) throws Exception {

        return ContractService.transterCoin(transferRequest.getTo(), contractAddress , transferRequest.getValue() , extractPrivateFor(request));
    }

    @ApiOperation(
            value = " ETH 转账",
            notes = "eth 小数位 18")
    @ApiImplicitParam(name = "privateFor",
            value = "Comma separated list of public keys of enclave nodes that transaction is "
                    + "private for",
            paramType = "header",
            dataType = "string")
    @RequestMapping(value = "/ethTransfer", method = RequestMethod.POST)
    EthSendTransaction transferEth(
            HttpServletRequest request,
            @RequestBody TransferRequest transferRequest) throws Exception {

        return ContractService.transferEth(transferRequest.getTo() , transferRequest.getValue() , extractPrivateFor(request));
    }


    @ApiOperation("根据区块数获取USDT 转账交易   USDT 小数位 6   0xdAC17F958D2ee523a2206206994597C13D831ec7")
    @RequestMapping(value = "/{contractAddress}/transferEvent", method = RequestMethod.POST)
    JSONArray getTopicEvent(
            HttpServletRequest request,
            @PathVariable String contractAddress,
            @RequestBody TransferEvent transferEvent) throws Exception {

        return  ContractService.transferEvent(transferEvent.getFrom(),  transferEvent.getTo() ,contractAddress );
    }

    @ApiOperation("获取最新块数")
    @RequestMapping(value = "/getLastestBlock", method = RequestMethod.GET)
    EthBlockNumber getLastestBlock() throws Exception {

        return  ContractService.getLastestBlock();
    }

    /**
     * Note: isError":"0" = Pass , isError":"1" = Error during Contract Execution
     *
     * @param transactionHash
     * @return
     * @throws Exception
     */
    JSONObject CheckContractExecutionStatus(@PathVariable String transactionHash) throws Exception {
        return  ContractService.CheckContractExecutionStatus(transactionHash);
    }


    /**
     * Note: status: 0 = Fail, 1 = Pass. Will return null/empty value for pre-byzantium fork
     * @param transactionHash
     * @return
     * @throws Exception
     */
    @ApiOperation("查询交易状态 0 = Fail, 1 = Pass.")
    @RequestMapping(value = "/{transactionHash}/checkTransactionReceiptStatus/", method = RequestMethod.GET)
    String checkTransactionReceiptStatus(@PathVariable String transactionHash) throws Exception {
        return  ContractService.checkTransactionReceiptStatus(transactionHash);
    }

    @ApiOperation("生成地址")
    @RequestMapping(value = "/genAddress", method = RequestMethod.GET)
    JSONObject getAdress() throws Exception {
        return  ContractService.genAddress();
    }

    private static @Nullable List<String> extractPrivateFor(HttpServletRequest request) {
        String privateFor = request.getHeader("privateFor");
        if (privateFor == null) {
            return null;
        } else {
            return Arrays.asList(privateFor.split(","));
        }
    }

    @Data
    static class ContractSpecification {
        private  BigInteger initialAmount;
        private  String tokenName;
        private  BigInteger decimalUnits;
        private  String tokenSymbol;

        ContractSpecification() {
        }

        ContractSpecification(BigInteger initialAmount, String tokenName, BigInteger decimalUnits, String tokenSymbol) {
            this.initialAmount = initialAmount;
            this.tokenName = tokenName;
            this.decimalUnits = decimalUnits;
            this.tokenSymbol = tokenSymbol;
        }

        public BigInteger getDecimalUnits() {
            return decimalUnits;
        }

        public BigInteger getInitialAmount() {
            return initialAmount;
        }

        public String getTokenName() {
            return tokenName;
        }

        public String getTokenSymbol() {
            return tokenSymbol;
        }
    }

    @Data
    static class ApproveRequest {
        private String spender;
        private BigInteger value;

        ApproveRequest() {}

        ApproveRequest(String spender, BigInteger value) {
            this.spender = spender;
            this.value = value;
        }

        public String getSpender() {
            return spender;
        }

        public BigInteger getValue() {
            return value;
        }
    }

    @Data
    static class TransferFromRequest {
        private String from;
        private String to;
        private BigInteger value;

        TransferFromRequest() {}

        TransferFromRequest(String from, String to, BigInteger value) {
            this.from = from;
            this.to = to;
            this.value = value;
        }


        public String getFrom() {
            return from;
        }

        BigInteger getValue() {
            return value;
        }

        public String getTo() {
            return to;
        }
    }

    @Data
    static class TransferRequest {
        private String to;
        private String value;

        TransferRequest(String to, String value) {
            this.to = to;
            this.value = value;
        }

        TransferRequest() {}

        public String getTo() {
            return to;
        }

        public String getValue() {
            return value;
        }
    }

    @Data
    static class TransferEvent {
        private BigInteger from;
        private BigInteger to;

        TransferEvent(BigInteger from, BigInteger to) {
            this.to = to;
            this.from = from;
        }

        TransferEvent() {}

        public BigInteger getFrom() {
            return from;
        }

        public BigInteger getTo() {
            return to;
        }
    }

    @Data
    static class ApproveAndCallRequest {
        private String spender;
        private BigInteger value;
        private String extraData;

        ApproveAndCallRequest() {}

        ApproveAndCallRequest(String spender, BigInteger value, String extraData) {
            this.spender = spender;
            this.value = value;
            this.extraData = extraData;
        }

        String getSpender() {
            return spender;
        }

        BigInteger getValue() {
            return value;
        }

        String getExtraData() {
            return extraData;
        }
    }

    @Data
    static class AllowanceRequest {
        private String ownerAddress;
        private String spenderAddress;

        AllowanceRequest() {}

        AllowanceRequest(String ownerAddress, String spenderAddress) {
            this.ownerAddress = ownerAddress;
            this.spenderAddress = spenderAddress;
        }

        public String getOwnerAddress() {
            return ownerAddress;
        }

        public void setOwnerAddress(String ownerAddress) {
            this.ownerAddress = ownerAddress;
        }

        public String getSpenderAddress() {
            return spenderAddress;
        }

        public void setSpenderAddress(String spenderAddress) {
            this.spenderAddress = spenderAddress;
        }
    }
}
