import com.iroha.model.Applicant;
import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;
import com.iroha.service.GenesisGenerator;
import com.iroha.service.QueryToChainService;
import com.iroha.service.UniversityService;
import com.iroha.utils.ChainEntitiesUtils;
import iroha.protocol.BlockOuterClass;
import iroha.protocol.QryResponses;
import jp.co.soramitsu.iroha.java.detail.InlineTransactionStatusObserver;
import lombok.val;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.testcontainers.shaded.org.apache.commons.io.input.ObservableInputStream;

import jp.co.soramitsu.iroha.java.TransactionStatusObserver;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.iroha.service.GenesisGenerator.saveKey;
import static com.iroha.service.GenesisGenerator.writeGenesisToFiles;
import static com.iroha.utils.ChainEntitiesUtils.ChainLogicConstants.UNIVERSITIES_DOMAIN;
import static com.iroha.utils.ChainEntitiesUtils.ChainLogicConstants.WILD_ASSET_NAME;
import static com.iroha.utils.ChainEntitiesUtils.ChainLogicConstants.WILD_SPECIALITY_ASSET_NAME;
import static com.iroha.utils.ChainEntitiesUtils.getAssetId;
import static com.iroha.utils.ChainEntitiesUtils.getUniversityDomain;
import static java.lang.Thread.sleep;

public class IntegrationWIthIrohaTest {

    private static List<Speciality> specialities;
    private static List<University> universities;
    private static UniversityService service;
    private static University university;
    private static Map<String, KeyPair> uniKeys;
    private static InlineTransactionStatusObserver observer;
    private static QueryToChainService queryToChainService;
    private static Applicant applicant;
    private static Speciality speciality;
    private static KeyPair applicantKeys;

    @Before
    void start_iroha() throws IOException, InterruptedException {
        speciality = new Speciality("ui", "cs", "", "code", 1);
        specialities = Arrays.asList(speciality);

        university = new University("ui", "ui", "192.168.0.3:10002");
        University kai = new University("kai", "kai", "192.168.0.2:10001");
        University kfu = new University("kfu", "kfu", "192.168.0.4:10003");

        universities = Arrays.asList(university, kai, kfu);
        uniKeys = ChainEntitiesUtils.generateKeys(
                universities.stream()
                        .map(t -> t.getName())
                        .collect(Collectors.toList())
        );

        university.setSpecialities(specialities);
        kai.setSpecialities(specialities);
        kfu.setSpecialities(specialities);

        kai.setPeerKey(uniKeys.get("kai"));
        kfu.setPeerKey(uniKeys.get("kfu"));
        university.setPeerKey(uniKeys.get("ui"));

        BlockOuterClass.Block genesis = GenesisGenerator.getGenesisBlock(universities, uniKeys);
        writeGenesisToFiles(genesis, new String[]{
                "./docker/genesis-kai/genesis.block",
                "./docker/genesis-ui/genesis.block",
                "./docker/genesis-kfu/genesis.block"
        });

        saveKey(kai.getPeerKey(), "./docker/genesis-kai");
        saveKey(university.getPeerKey(), "./docker/genesis-ui");
        saveKey(kfu.getPeerKey(), "./docker/genesis-kfu");


        File dir = new File("./docker");
        Process p = Runtime.getRuntime().exec(new String[]{"docker-compose", "up", "-d"}, null, dir);
        sleep(30000);

        observer = TransactionStatusObserver.builder()

                // executed when stateless or stateful validation is failed
                .onComplete(() -> System.out.println("Complete"))
                .build();
        queryToChainService = new QueryToChainService(uniKeys.get(university.getName()), university);
    }

    @Test
    void create_account_test() throws InterruptedException {
        service = new UniversityService(
                uniKeys.get(university.getName()),
                university);
        String pubkey = ChainEntitiesUtils.bytesToHex(uniKeys.get(university.getName()).getPublic().getEncoded());


        applicant = new Applicant("name", "surname");

        applicantKeys = ChainEntitiesUtils.generateKey();
        applicant.setPkey(applicantKeys.getPrivate().toString());
        applicant.setPubkey(ChainEntitiesUtils.bytesToHex(applicantKeys.getPublic().getEncoded()));


        service.createNewApplicantAccount(applicant, applicantKeys, observer);


        sleep(15000);

        service.getWildTokensTransaction(applicant, observer);

        sleep(10000);
        List<QryResponses.AccountAsset> assets = queryToChainService.getAllAssertsOfApplicant(applicant);
        for (QryResponses.AccountAsset asset : assets) {
            Assert.assertEquals(asset.getBalance(), "5");
        }


    }

    @Test
    void choose_university_test() throws InterruptedException {
        service.chooseUniversity(applicant,applicantKeys,observer, university, uniKeys.get(university.getName()));
        sleep(10000);
        val assets =queryToChainService.getAllAssertsOfApplicant(applicant);
        for(QryResponses.AccountAsset asset: assets){
            Assert.assertTrue((
                    asset.getBalance().equals("4") && asset.getAssetId().equals(getAssetId(WILD_ASSET_NAME,UNIVERSITIES_DOMAIN))
            ||
                    (asset.getBalance().equals("3")) && asset.getAssetId().equals(getAssetId(WILD_SPECIALITY_ASSET_NAME,getUniversityDomain(university)))));
        }
    }

    @Test
    void choose_speciality_test() throws InterruptedException {
        service.chooseSpeciality(applicant,speciality,observer,applicantKeys, university, uniKeys.get(university.getName()));
        sleep(10000);
        val assets =queryToChainService.getAllAssertsOfApplicant(applicant);
        for(QryResponses.AccountAsset asset: assets){
            Assert.assertTrue((
                    asset.getBalance().equals("4") && asset.getAssetId().equals(getAssetId(WILD_ASSET_NAME,UNIVERSITIES_DOMAIN)))
                    ||
                    (asset.getBalance().equals("2")) && asset.getAssetId().equals(getAssetId(WILD_SPECIALITY_ASSET_NAME,getUniversityDomain(university)))
                    ||
                    ((asset.getBalance().equals("1")) && asset.getAssetId().equals(getAssetId(speciality.getName(),getUniversityDomain(university))))
            );
        }
    }

    @Test
    void swap_university_test() throws InterruptedException {
        University kai = new University("kai", "kai", "192.168.0.2:10001");
        service.swapUniversity(applicant,university,speciality, kai,applicantKeys, uniKeys.get(kai.getName()),observer);
        sleep(10000);
        val assets =queryToChainService.getAllAssertsOfApplicant(applicant);
        for(QryResponses.AccountAsset asset: assets){
            Assert.assertTrue((
                    asset.getBalance().equals("4") && asset.getAssetId().equals(getAssetId(WILD_ASSET_NAME,UNIVERSITIES_DOMAIN)))
                    ||
                    (asset.getBalance().equals("2")) && asset.getAssetId().equals(getAssetId(WILD_SPECIALITY_ASSET_NAME,getUniversityDomain(university)))
                    ||
                    ((asset.getBalance().equals("1")) && asset.getAssetId().equals(getAssetId(speciality.getName(),getUniversityDomain(kai)))
            ));
        }
    }

}
