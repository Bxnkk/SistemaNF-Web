package br.com.sistemanf.web.security;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class SenhaSegura {

	private static final int ITERACOES = 600000;

	public static String gerarSalt() {

		byte[] salt = new byte[16];

		SecureRandom random = new SecureRandom();
		random.nextBytes(salt);

		return Base64.getEncoder().encodeToString(salt);
	}

	public static String gerarHash(String senha, String saltBase64) throws Exception {

		byte[] salt = Base64.getDecoder().decode(saltBase64);

		PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), salt, ITERACOES, 256);

		try {
			SecretKeyFactory fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

			byte[] hash = fabrica.generateSecret(spec).getEncoded();

			return Base64.getEncoder().encodeToString(hash);

		} finally {
			spec.clearPassword();
		}
	}

	public static boolean verificarSenha(String senha, String salt, String hashSalvo) throws Exception {

		String hashDigitado = gerarHash(senha, salt);

		byte[] esperado = Base64.getDecoder().decode(hashSalvo);
		byte[] recebido = Base64.getDecoder().decode(hashDigitado);

		return MessageDigest.isEqual(esperado, recebido);
	}
}