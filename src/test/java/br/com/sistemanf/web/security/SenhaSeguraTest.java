package br.com.sistemanf.web.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SenhaSeguraTest {

	@Test
	void deveReconhecerSenhaCorreta() throws Exception {
		String senha = "SenhaTeste123";
		String salt = SenhaSegura.gerarSalt();
		String hash = SenhaSegura.gerarHash(senha, salt);

		assertTrue(SenhaSegura.verificarSenha(senha, salt, hash));
	}

	@Test
	void deveRejeitarSenhaIncorreta() throws Exception {
		String senha = "SenhaTeste123";
		String salt = SenhaSegura.gerarSalt();
		String hash = SenhaSegura.gerarHash(senha, salt);

		assertFalse(SenhaSegura.verificarSenha("SenhaErrada123", salt, hash));
	}

	@Test
	void deveGerarSaltsDiferentes() {
		String primeiroSalt = SenhaSegura.gerarSalt();
		String segundoSalt = SenhaSegura.gerarSalt();

		assertNotEquals(primeiroSalt, segundoSalt);
	}
}