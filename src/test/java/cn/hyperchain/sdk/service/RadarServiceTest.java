package cn.hyperchain.sdk.service;

import cn.hyperchain.sdk.account.Account;
import cn.hyperchain.sdk.account.Algo;
import cn.hyperchain.sdk.common.solidity.Abi;
import cn.hyperchain.sdk.common.utils.FileUtil;
import cn.hyperchain.sdk.exception.RequestException;
import cn.hyperchain.sdk.provider.DefaultHttpProvider;
import cn.hyperchain.sdk.provider.HttpProvider;
import cn.hyperchain.sdk.provider.ProviderManager;
import cn.hyperchain.sdk.request.Request;
import cn.hyperchain.sdk.response.RadarResponse;
import cn.hyperchain.sdk.response.ReceiptResponse;
import cn.hyperchain.sdk.transaction.Transaction;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class RadarServiceTest {
    private static String url = "localhost:8081";
    private static HttpProvider httpProvider = new DefaultHttpProvider.Builder().setUrl(url).build();
    private static ProviderManager providerManager = new ProviderManager.Builder().providers(httpProvider).build();
    private static RadarService radarService = ServiceManager.getRadarService(providerManager);
    private static String contractAddress = null;

    @BeforeClass
    public static void deploy() throws Exception {
        AccountService accountService = ServiceManager.getAccountService(providerManager);
        ContractService contractService = ServiceManager.getContractService(providerManager);

        Account account = accountService.genAccount(Algo.ECRAW);

        String bin = FileUtil.readFile(FileUtil.readFileAsStream("solidity/TypeTestContract_sol_TypeTestContract.bin"));
        String abiString = FileUtil.readFile(Thread.currentThread().getContextClassLoader().getResourceAsStream("solidity/TypeTestContract_sol_TypeTestContract.abi"));
        Abi abi = Abi.fromJson(abiString);

        Transaction transaction = new Transaction.EVMBuilder(account.getAddress()).deploy(bin, abi, "contract01").build();
        transaction.sign(account);
        ReceiptResponse receiptResponse = contractService.deploy(transaction).send().polling();
        contractAddress = receiptResponse.getContractAddress();
    }

    @Test
    @Ignore
    public void testListenContract() throws IOException, RequestException {
        String source = FileUtil.readFile(FileUtil.readFileAsStream("solidity/TypeTestContract.sol"));
        Request<RadarResponse> listenContract = radarService.listenContract(source, contractAddress);
        RadarResponse radarResponse = listenContract.send();
        System.out.println(radarResponse);
    }
}
