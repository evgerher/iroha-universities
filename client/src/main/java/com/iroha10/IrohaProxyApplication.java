package com.iroha10;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Arrays;

import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;



@SpringBootApplication
public class IrohaProxyApplication {

	public static void main(String[] args) {
//		SpringApplication.run(IrohaProxyApplication.class, args);

		for(int i = 0;i<10;i++) {
			Ed25519Sha3 crypto = new Ed25519Sha3();
			KeyPair peerKeypair = crypto.generateKeypair();

			System.out.println(i);
			System.out.println("public: ");
			System.out.println(bytesToHex(peerKeypair.getPublic().getEncoded()));
			System.out.println("private: ");
			System.out.println(bytesToHex(peerKeypair.getPrivate().getEncoded()));
		}

	}

	private static String bytesToHex(byte[] hashInBytes) {

		StringBuilder sb = new StringBuilder();
		for (byte b : hashInBytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();

	}

}
