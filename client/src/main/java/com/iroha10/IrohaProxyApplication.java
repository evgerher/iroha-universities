package com.iroha10;

import com.google.protobuf.CodedOutputStream;
import com.iroha10.model.Speciality;
import com.iroha10.model.University;
import com.iroha10.service.GenesisGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import iroha.protocol.BlockOuterClass;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;

import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;



@SpringBootApplication
public class IrohaProxyApplication  {

	public static void main(String[] args) { }

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

}
