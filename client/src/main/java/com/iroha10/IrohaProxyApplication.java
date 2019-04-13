package com.iroha10;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Arrays;

import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;



@SpringBootApplication
public class IrohaProxyApplication  {

	public static void main(String[] args) {
		SpringApplication.run(IrohaProxyApplication.class, args);
	}

	private static String bytesToHex(byte[] hashInBytes) {

		StringBuilder sb = new StringBuilder();
		for (byte b : hashInBytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();

	}

}
