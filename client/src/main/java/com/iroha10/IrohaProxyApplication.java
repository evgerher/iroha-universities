package com.iroha10;

import com.google.protobuf.CodedOutputStream;
import com.iroha10.model.Applicant;
import com.iroha10.model.Speciality;
import com.iroha10.model.University;
import com.iroha10.service.GenesisGenerator;
import com.iroha10.service.UniversityService;
import com.iroha10.utils.ChainEntitiesUtils;
import com.iroha10.utils.IrohaApiSingletone;
import io.reactivex.Observable;
import jp.co.soramitsu.iroha.java.IrohaAPI;
import jp.co.soramitsu.iroha.java.Transaction;
import jp.co.soramitsu.iroha.testcontainers.IrohaContainer;
import jp.co.soramitsu.iroha.testcontainers.PeerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import iroha.protocol.BlockOuterClass;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;

import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;

import javax.security.auth.callback.CallbackHandler;


//@SpringBootApplication
public class IrohaProxyApplication  {

	public static void main(String[] args) throws FileNotFoundException {
		Speciality speciality = new Speciality("ui","cs","","code",1);
		University university = new University("ui","ui", Arrays.asList(speciality));
//		IrohaContainer iroha = new IrohaContainer()
//				.withPeerConfig(getPeerConfig(university));
//		iroha.start();
		GenesisGenerator.getGenesisBlock(Arrays.asList(university));
		UniversityService service =  new UniversityService(ChainEntitiesUtils.universitiesKeys.get(university.getName()),
				university);
		Applicant applicant = new Applicant("id","name","surname", 300);
		KeyPair applicantKeys = service.createNewApplicantAccount(applicant);
		Observable observable = service.getWildTokensTransaction(applicant);
		observable.blockingSubscribe();
		int balance = service.getBalanceOfApplicant(applicant, ChainEntitiesUtils.Consts.WILD_ASSET_NAME);
		System.out.println("_______________________________________________");
		System.out.println(balance);
		System.out.println("_______________________________________________");


	}
	private static String bytesToHex(byte[] hashInBytes) {

		StringBuilder sb = new StringBuilder();
		for (byte b : hashInBytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();

	}


	private static void writeGenesisToFile(BlockOuterClass.Block genesis, String path) throws FileNotFoundException {
		FileOutputStream file = new FileOutputStream(path);
		try {
			file.write(genesis.toString().getBytes());
			file.flush();
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static PeerConfig getPeerConfig(University university) {

		PeerConfig config = PeerConfig.builder()
				.genesisBlock(GenesisGenerator.getGenesisBlock(Arrays.asList(university)))
				.build();

		// don't forget to add peer keypair to config
		config.withPeerKeyPair(ChainEntitiesUtils.universitiesKeys.get(university.getName()));
		return config;
	}

}
