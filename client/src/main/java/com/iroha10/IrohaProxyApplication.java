package com.iroha10;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import iroha.protocol.BlockOuterClass;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


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
