package br.com.sistemanf.web.security;

import org.springframework.security.crypto.password.PasswordEncoder;

public class SenhaNFPasswordEncoder implements PasswordEncoder {

	@Override
	public String encode(CharSequence senha) {
		try {
			String salt = SenhaSegura.gerarSalt();
			String hash = SenhaSegura.gerarHash(senha.toString(), salt);

			return salt + ":" + hash;
		} catch (Exception e) {
			throw new IllegalStateException("Erro ao gerar hash da senha.", e);
		}
	}

	@Override
	public boolean matches(CharSequence senha, String senhaArmazenada) {
		if (senha == null || senhaArmazenada == null) {
			return false;
		}

		int separador = senhaArmazenada.indexOf(':');

		if (separador <= 0 || separador == senhaArmazenada.length() - 1) {
			return false;
		}

		String salt = senhaArmazenada.substring(0, separador);
		String hash = senhaArmazenada.substring(separador + 1);

		try {
			return SenhaSegura.verificarSenha(senha.toString(), salt, hash);
		} catch (Exception e) {
			return false;
		}
	}
}